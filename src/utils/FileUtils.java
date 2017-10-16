package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import utils.RowDataReader.ReaderCallback;

public class FileUtils {

    private static final int READ_BUFFER_SIZE = 1444;

    public static String readFileToString(File file) {
        if (file == null) {
            System.err.println("Invalid file");
            return null;
        }

        final StringBuffer stringBuffer = new StringBuffer();
        ReaderCallback callback = new ReaderCallback() {
            @Override
            public boolean onReadRow(int rowIndex, String rowData) {
                stringBuffer.append(rowData).append("\n");
                return true;
            }
        };
        RowDataReader.read(file, callback);

        int length = stringBuffer.length();
        if (length > 0) {
            stringBuffer.delete(length - 1, length);
        }
        return stringBuffer.toString();
    }

    public static String readFileToString(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            System.err.println("Invalid path");
            return null;
        }
        return readFileToString(new File(filePath));
    }

    public static String getExtension(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            int index = filePath.lastIndexOf('.');
            return index > -1 ? filePath.substring(index + 1) : null;
        }
        return null;
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            int extensionIndex = filePath.lastIndexOf('.');
            extensionIndex = extensionIndex > -1 ? extensionIndex : filePath.length();
            int pathSeparatorIndex = filePath.lastIndexOf('/');
            pathSeparatorIndex = pathSeparatorIndex > -1 ? pathSeparatorIndex : -1;
            return filePath.substring(pathSeparatorIndex + 1, extensionIndex);
        }
        return null;
    }

    public static File getFile(String path) throws Exception {
        return getFile(path, false);
    }

    public static File getFile(String path, boolean checkExistance) throws Exception {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Invalid input paths");
        }
        File file = new File(path);
        if (checkExistance && !file.exists()) {
            throw new FileNotFoundException("File " + path + " do not exist");
        }
        return file;
    }

    public static File getFolder(String path) throws Exception {
        return getFolder(path, false);
    }

    public static File getFolder(String path, boolean checkExistence) throws Exception {
        File folder = getFile(path, checkExistence);
        return folder.isDirectory() ? folder : null;
    }

    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            System.err.println("Invalid path");
            return false;
        }
        return deleteFile(new File(filePath));
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            System.err.println("Invalid file");
            return false;
        }
        try {
            return file.delete();
        } catch (Exception e) {
            System.err.println("Failed when deleting file ");
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static boolean moveFile(String srcPath, String desPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(desPath)) {
            System.err.println("Invalid paths");
            return false;
        }
        return moveFile(new File(srcPath), new File(desPath));
    }

    public static boolean moveFile(File srcFile, File desFile) {
        if (srcFile == null || desFile == null) {
            System.err.println("Invalid file");
            return false;
        }
        try {
            return srcFile.renameTo(desFile);
        } catch (Exception e) {
            System.err.println("Failed when moving file ");
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static boolean copyFile(String srcPath, String desPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(desPath)) {
            System.err.println("Invalid paths");
            return false;
        }
        return copyFile(new File(srcPath), new File(desPath));
    }

    public static boolean copyFile(File srcFile, File desFile) {
        if (srcFile == null || desFile == null) {
            System.err.println("Invalid file");
            return false;
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            int byteread = 0;
            if (!srcFile.exists()) {
                System.err.println("Source file don't exist!");
                return false;
            }
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(desFile);
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            while ((byteread = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteread);
            }
            System.out.println(String.format("Done copying file from [%s] to [%s]", srcFile.getAbsolutePath(), desFile.getAbsolutePath()));
            return true;
        } catch (Exception e) {
            System.err.println("Failed when copying file ");
            e.printStackTrace(System.err);
            deleteFile(desFile);
            return false;
        } finally {
            MiscUtils.closeSilently(inputStream);
            MiscUtils.closeSilently(outputStream);
        }
    }

    public static boolean createFolder(String destFolder) {
        File destFolderFile = new File(destFolder);
        return createFolder(destFolderFile);
    }

    public static boolean createFolder(File destFolderFile) {
        if (destFolderFile.exists() && destFolderFile.isFile()) {
            boolean deleted = deleteFile(destFolderFile);
            System.out.println("CreateFolder, there is a file that has the same name as the dest folder, " +
                    "delete it first. result:" + deleted);
            if (!deleted) {
                return false;
            }
        }

        // ensure the destination folder exist
        mkdirsCustomize(destFolderFile);
        if (!destFolderFile.exists()) {
            System.out.println("folder: " + destFolderFile + " Can not be created.");
            return false;
        }
        return true;
    }

    private static boolean mkdirsCustomize(File file) {
        // If the terminal directory already exists, return is it a directory
        if (file.exists()) {
            return file.isDirectory();
        }

        // Try to create a parent directory and then this directory
        if (!mkdirsCustomize(file.getParentFile())) {
            return false;
        }

        // If the receiver can be created, answer true
        return file.mkdir();
    }
}
