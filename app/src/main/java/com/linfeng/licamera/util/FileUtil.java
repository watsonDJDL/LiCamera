package com.linfeng.licamera.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.linfeng.licamera.LiApplication;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static com.linfeng.licamera.util.CommonUtil.context;

public class FileUtil {
    private static final String TAG = "FileUtil";
    public static final String FOLDER_NAME = "liEdit";
    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static String storagePath = "";

    public static String getBasePath() {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + File.separator + "LiCamera";
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdir();
            }
        }
        return storagePath;
    }
    public static boolean checkFileExist(final String path) {
        if (TextUtils.isEmpty(path))
            return false;

        File file = new File(path);
        return file.exists();
    }

    // 获取文件扩展名
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * 将图片文件加入到相册
     *
     * @param context
     * @param dstPath
     */
    public static void ablumUpdate(final Context context, final String dstPath) {
        if (TextUtils.isEmpty(dstPath) || context == null)
            return;

        File file = new File(dstPath);
        //System.out.println(" file.length() = "+file.length());
        if (!file.exists() || file.length() == 0) {//文件若不存在  则不操作
            return;
        }

        ContentValues values = new ContentValues(2);
        String extensionName = getExtensionName(dstPath);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + (TextUtils.isEmpty(extensionName) ? "jpeg" : extensionName));
        values.put(MediaStore.Images.Media.DATA, dstPath);
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static File genEditFile(){
        return getEmptyFile(getFileName());
    }

    public static String getFileName() {
        return "liedit" + System.currentTimeMillis() + ".jpg";
    }

    public static File getEmptyFile(String name) {
        File folder = createFolders();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (folder.exists()) {
            File file = new File(folder, name);
            return file;
        }
        return null;
    }

    public static File createFolders() {
        File baseDir;
        baseDir = context().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File aviaryFolder = new File(baseDir, FOLDER_NAME);
        if (aviaryFolder.exists())
            return aviaryFolder;
        if (aviaryFolder.isFile())
            aviaryFolder.delete();
        if (aviaryFolder.mkdirs())
            return aviaryFolder;
        return Environment.getExternalStorageDirectory();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            BufferedInputStream in = null;

            try {
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                byte[] var7 = bos.toByteArray();
                return var7;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }

                bos.close();
            }
        }
    }

    /*
     * 获取手机外部存储路径
     * */
    private static String getOutputFile() {
        File mediaFile = null;
        boolean OutputExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (OutputExist) {
            mediaFile = LiApplication.getContext().getExternalCacheDir();
            return mediaFile.toString();
        }
        return null;
    }

    /*
     * 获取录制视频的日期 作为存储文件路径一部分
     * */
    private static String getDate() {
        Log.d(TAG, "获取录制视频的日期 ");
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        String date = "" + year + "_" + (month + 1) + "_" + day;
        return date;
    }

    /*
     *创建视频存储文件夹 录制好的视频存储在手机外部存储中 以录像时间+mp4格式命名
     * */
    public static String getOutputMediaFile() {
        Log.d(TAG, "获取视频存储的位置 ");
        String mediaPath = getOutputFile();
        if (mediaPath != null) {
            File mediaFile = new File(mediaPath + "/recordVideo");
            if (!mediaFile.exists()) {
                mediaFile.mkdirs();
            }
            return mediaFile.getAbsolutePath() + File.separator + getDate() + ".mp4";
        }
        return null;
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

}
