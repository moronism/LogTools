package com.howard.www.util;

public class DecompressionUtil {

    public static void unZipLogFile(String rootPath) throws Exception {


    }
    public static String unZipLogFilePath(String pathName) throws Exception {
        UncompressFileGZIP uncompressFileGZIP = new UncompressFileGZIP();
        String outFileName = uncompressFileGZIP.doUncompressFile(pathName);
        return outFileName;
    }
}
