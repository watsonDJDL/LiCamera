package com.linfeng.licamera.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

import static com.linfeng.licamera.util.CommonUtil.context;

public class FileUtil {
    public static final String FOLDER_NAME = "liEdit";
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
}
