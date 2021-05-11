package com.linfeng.licamera.videoEditor.utils;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.linfeng.licamera.util.CommonUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RemixAudioUtil {
    private static final String TAG = "RemixAudioUtils";

    public static void mix(
            String videoPath,
            String bgAudioPath,
            String outPath,
            int startTimeUs,
            int endTimeUs,
            int videoVolume,
            int bgAudioVolume) {
        File parent = CommonUtil.context().getExternalCacheDir();
        String videoPcm = new File(parent, "video.pcm").getAbsolutePath();
        String bgPcm = new File(parent, "bgPcm.pcm").getAbsolutePath();

        decodeToPcm(videoPath, videoPcm, startTimeUs, endTimeUs);
        decodeToPcm(bgAudioPath, bgPcm, startTimeUs, endTimeUs);

        String mixPcm = new File(parent, "mix.pcm").getAbsolutePath();
        mixPcm(videoPcm, bgPcm, mixPcm, videoVolume, bgAudioVolume);

        //  demo的MP3和MP4：采样率是44100hz，声道数是 双声道 2，16位的
        // pcm -> WAV
        String wavFile = new File(parent, "mix.wav").getAbsolutePath();
        new PcmToWavHelper(44100, 2, 16)
                .pcmToWav(mixPcm, wavFile);
        Log.d(TAG, "pcm -> WAV done:$wavFile");
        mixVideoAndBG(videoPath, outPath, startTimeUs, endTimeUs, wavFile);
    }


    private static void mixVideoAndBG(
            String videoInput,
            String output,
            int startTimeUs,
            int endTimeUs,
            String wavFile ) {

        try {
            MediaMuxer mediaMuxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(videoInput);

            // 视频轨道取自视频文件，跟视频所有信息一样
            int videoIndex = selectTrack(mediaExtractor, false);
            MediaFormat videoFormat = mediaExtractor.getTrackFormat(videoIndex);
            int muxerVideoTrackIndex = mediaMuxer.addTrack(videoFormat);

            // 音频轨道取自视频文件，跟视频所有信息一样
            int audioIndex = selectTrack(mediaExtractor, true);
            MediaFormat audioFormat = mediaExtractor.getTrackFormat(audioIndex);

            // 将音频轨道设置为aac
            int audioBitrate = audioFormat.getInteger(MediaFormat.KEY_BIT_RATE);
            audioFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);

            // 返回新的音频轨道
            int muxerAudioIndex = mediaMuxer.addTrack(audioFormat);

            // 混合开始
            mediaMuxer.start();

            //音频的wav
            MediaExtractor pcmExtractor = new MediaExtractor();
            pcmExtractor.setDataSource(wavFile);
            int audioTrackIndex = selectTrack(pcmExtractor, true);
            pcmExtractor.selectTrack(audioTrackIndex);
            MediaFormat pcmTrackFormat = pcmExtractor.getTrackFormat(audioTrackIndex);

            int maxBufferSize = 0;
            maxBufferSize = audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE) ?
                    pcmTrackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) : 100 * 1000;

            Log.d(TAG, "pcmTrackFormat maxBufferSize=$maxBufferSize");

            MediaFormat encodeFormat = MediaFormat.createAudioFormat(
                    MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2);
            //比特率
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitrate);
            //音质等级
            encodeFormat.setInteger(
                    MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC
            );
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);
            // 编码器aac
            MediaCodec encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            encoder.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            // 编码 wav -> aac
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean encodeDone = false;
            ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
            while (!encodeDone) {
                int inputBufferIndex = encoder.dequeueInputBuffer(10_000);
                if (inputBufferIndex >= 0) {
                    long sampleTime = pcmExtractor.getSampleTime();
                    //  pts小于0  来到了文件末尾 通知编码器  不用编码了
                    if (sampleTime < 0) {
                        Log.d(TAG, "sampleTime<0");
                        encoder.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        int flags = pcmExtractor.getSampleFlags();
                        int size = pcmExtractor.readSampleData(buffer, 0);
                        ByteBuffer inputBuffer = encoder.getInputBuffer(inputBufferIndex);
                        //inputBuffer判空
                        inputBuffer.clear();
                        inputBuffer.put(buffer);
                        encoder.queueInputBuffer(inputBufferIndex, 0, size, sampleTime, flags);
                        // 读取下一帧数据
                        pcmExtractor.advance();
                    }
                }
                int outputBufferIndex = encoder.dequeueOutputBuffer(info, 10_000);
                while (outputBufferIndex >= 0) {
                    if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        encodeDone = true;
                        Log.d(TAG, "BUFFER_FLAG_END_OF_STREAM break");
                        break;
                    }
                    ByteBuffer encodeOutputBuffer = encoder.getOutputBuffer(outputBufferIndex);
                    //encodeOutputBuffer判空
                    mediaMuxer.writeSampleData(muxerAudioIndex, encodeOutputBuffer, info);

                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = encoder.dequeueOutputBuffer(info, 10_000);
                }
            }
            Log.d(TAG, "pcm -> aac audio done");

            // 选择视频轨道，开始写入视频
            mediaExtractor.selectTrack(videoIndex);
            mediaExtractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            maxBufferSize = videoFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE) ?
                videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) : 100 * 1000;

            Log.d(TAG, "videoFormat=$videoFormat");
            Log.d(TAG, "start video mixVideoAndMusic maxBufferSize=$maxBufferSize");
            buffer = ByteBuffer.allocateDirect(maxBufferSize);
            while (true) {
                long sampleTimeUs = mediaExtractor.getSampleTime();
                if (sampleTimeUs == -1L) {
                    Log.d(TAG, "sampleTimeUs == -1");
                    break;
                }
                if (sampleTimeUs < startTimeUs) {
                    mediaExtractor.advance();
                    continue;
                }
                if (sampleTimeUs > endTimeUs) {
                    Log.d(TAG, "sampleTimeUs > endTimeUs");
                    break;
                }
                info.presentationTimeUs = sampleTimeUs - startTimeUs;
                info.flags = mediaExtractor.getSampleFlags();
                info.size = mediaExtractor.readSampleData(buffer, 0);
                if (info.size < 0) {
                    Log.d(TAG, "info.size<0 break");
                    break;
                }
                mediaMuxer.writeSampleData(muxerVideoTrackIndex, buffer, info);
                mediaExtractor.advance();
            }
            Log.d(TAG, "video done");

            pcmExtractor.release();
            mediaExtractor.release();
            encoder.stop();
            encoder.release();
            mediaMuxer.stop();
            mediaMuxer.release();
            Log.d(TAG, "all done");
        } catch(IOException e) {
            e.printStackTrace();
        }

    }


    private static void mixPcm(
            String pcm1,
            String pcm2,
            String mixPcm,
            int videoVolume,
            int bgAudioVolume) {
        float volume1 = videoVolume * 1.0f / 100;
        float volume2 = bgAudioVolume * 1.0f / 100;
        int buffSize = 2048;
        byte[] buffer1 = new byte[buffSize];
        byte[] buffer2 = new byte[buffSize];
        byte[] buffer3 = new byte[buffSize];
        try {
            FileInputStream fis1 = new FileInputStream(pcm1);
            FileInputStream fis2 = new FileInputStream(pcm2);
            FileOutputStream fosMix = new FileOutputStream(mixPcm);
            boolean isEnd1 = false;
            boolean isEnd2 = false;
            short temp1;
            short temp2;
            int temp;
            int ret1 = -1;
            int ret2 = -1;
            while (!isEnd1 || !isEnd2) {
                if (!isEnd1) {
                    ret1 = fis1.read(buffer1);
                    isEnd1 = ret1 == -1;
                }
                if (!isEnd2) {
                    ret2 = fis2.read(buffer2);
                    isEnd2 = ret2 == -1;
                    for (int i = 0 ; i<buffer2.length ; i +=2) {
                        temp1 = (short) ((buffer1[i] & 0xff) | (buffer1[i + 1] & 0xff) << 8);
                        temp2 = (short) ((buffer2[i] & 0xff) | (buffer2[i + 1] & 0xff) << 8);

                        // 两个short变量相加 会大于short
                        temp = (int)(temp1 * volume1 + temp2 * volume2);
                        // short类型的取值范围[-32768 ~ 32767]
                        if (temp > 32767) {
                            temp = 32767;
                        } else if (temp < -32768) {
                            temp = -32768;
                        }

                        // 低八位 高八位 低八位 高八位 。。。
                        buffer3[i] = (byte) (temp & 0x00ff);
                        buffer3[i + 1] = (byte) ((temp & 0xFF00) >> 8 );
                    }
                    fosMix.write(buffer3);
                }
            }
            fis1.close();
            fis2.close();
            fosMix.flush();
            fosMix.close();
            Log.d(TAG, "mixPcm:$mixPcm");
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void decodeToPcm(String srcPath, String  outPcmPath,int startTimeUs,int endTimeUs) {
        if (endTimeUs < startTimeUs) {
            return;
        }
        try {
            File outPcmFile = new File(outPcmPath);
            FileChannel writePcmChannel = new FileOutputStream(outPcmFile).getChannel();
            MediaExtractor mediaExtractor = new MediaExtractor();
            File srcVideo = new File(srcPath);
            FileInputStream fis = new FileInputStream(srcVideo);
            FileDescriptor dp = fis.getFD();
            mediaExtractor.setDataSource(dp);
            int audioTrack = selectTrack(mediaExtractor, true);
            if (audioTrack == -1) {
                return;
            }
            mediaExtractor.selectTrack(audioTrack);
            mediaExtractor.seekTo((long)startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            MediaFormat format = mediaExtractor.getTrackFormat(audioTrack);
            Log.d(TAG, "format=$format");
            int maxBufferSize;
            if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                Log.d(TAG, "KEY_MAX_INPUT_SIZE");
            } else {
                maxBufferSize = 100 * 1000;
            }
            Log.d(TAG, "maxBufferSize=$maxBufferSize");
            ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            mediaCodec.configure(format, null, null, 0);
            mediaCodec.start();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            while (true) {
                int inputIndex = mediaCodec.dequeueInputBuffer(10_000);
                if (inputIndex >= 0) {
                    long sampleTimeUs = mediaExtractor.getSampleTime();
                    if (sampleTimeUs == -1L) {
                        break;
                    } else if (sampleTimeUs > endTimeUs) {
                        break;
                    } else if (sampleTimeUs < sampleTimeUs) {
                        mediaExtractor.advance();
                    }
                    info.presentationTimeUs = sampleTimeUs;
                    info.flags = mediaExtractor.getSampleFlags();
                    info.size = mediaExtractor.readSampleData(buffer, 0);

                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);

                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputIndex);
                    inputBuffer.clear();
                    inputBuffer.put(data);
                    mediaCodec.queueInputBuffer(
                            inputIndex,
                            0,
                            info.size,
                            info.presentationTimeUs,
                            info.flags
                    );
                    mediaExtractor.advance();
                }
                int outputIndex = mediaCodec.dequeueOutputBuffer(info, 10_000);
                while (outputIndex >= 0) {
                    ByteBuffer outByteBuffer = mediaCodec.getOutputBuffer(outputIndex);
                    // pcm
                    writePcmChannel.write(outByteBuffer);

                    mediaCodec.releaseOutputBuffer(outputIndex, false);
                    outputIndex = mediaCodec.dequeueOutputBuffer(info, 10_000);
                }
            }
            Log.d(TAG, "decode pcm done:$outPcmPath");
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static int selectTrack(MediaExtractor mediaExtractor,Boolean  audio){
        int count = mediaExtractor.getTrackCount();
        for (int i = 0; i < count ; i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (audio) {
                if (!TextUtils.isEmpty(mime) && mime.startsWith("audio/")) {
                    return i;
                }
            } else {
                if (!TextUtils.isEmpty(mime) && mime.startsWith("video/")) {
                    return i;
                }
            }
        }
        return -1;
    }
}
