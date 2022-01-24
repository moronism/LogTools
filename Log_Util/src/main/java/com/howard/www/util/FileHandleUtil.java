package com.howard.www.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHandleUtil {

    public static List<String> obtainRootPathFile(String path){
        if(path!=null){
            List<String> tempOriginalFiles= new ArrayList<String>(400);
            obtainRootPathFile(path,tempOriginalFiles);
            return tempOriginalFiles;
        }
        return null;
    }

    public static void obtainRootPathFile(String path,List<String> originalFiles) {
        File pathFile = new File(path);
        if (pathFile.exists()) {
            File[] fileItems = pathFile.listFiles();
            if (fileItems.length > 0) {
                for (File fileItem : fileItems) {
                    if (fileItem.isDirectory()) {
                        obtainRootPathFile(fileItem.getAbsolutePath(),originalFiles);
                    } else {
                        originalFiles.add(fileItem.getAbsolutePath());
                    }
                }
            }
        }
    }
}
