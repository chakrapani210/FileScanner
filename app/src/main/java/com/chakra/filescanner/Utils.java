package com.chakra.filescanner;

import com.chakra.filescanner.data.FileData;

import java.io.File;

/**
 * Created by f68dpim on 10/7/16.
 */
public class Utils {
    public static String toKbMb(long size) {
        if(size >= 1024 * 1024 * 1024) {
            return ((float)size/(1024 * 1024 * 1024)) + " GB";
        }
        if(size >= 1024 * 1024) {
            return ((float)size/(1024 * 1024)) + " MB";
        }
        if(size >= 1024) {
            return ((float)size/1024) + " KB";
        }
        return size + " B";
    }

    public static String arrayToString(Object[] objs) {
        StringBuilder sb = new StringBuilder();
        for (Object obj: objs) {
            sb.append(obj + "\n");
        }
        return sb.toString();
    }

    public static String getFileName(String path) {
        if(path != null && path.contains("/")) {
            return new File(path).getName();
        }
        return path;
    }
}
