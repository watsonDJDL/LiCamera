package com.linfeng.licamera.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.linfeng.licamera.util.CommonUtil.context;

public class FileUtil {
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
}
