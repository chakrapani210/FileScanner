package com.chakra.filescanner.data;

import com.chakra.filescanner.Utils;

import java.io.File;

/**
 * Created by f68dpim on 9/30/16.
 */
public class FileData implements Comparable<FileData> {
    public String path;
    public long size;

    public FileData(String path, long length) {
        this.path = path;
        this.size = length;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExtData) {
            return path.equals(((FileData) obj).path);
        } else if(obj instanceof String){
            return path.equals((String) obj);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return Utils.getFileName(path) + ":" + Utils.toKbMb(size);
    }

    @Override
    public int compareTo(FileData another) {
        long result = another.size - size;
        // No need to type cast to int. Here we just need find diff. is negative or Positive or zero
        return result == 0 ? 0 : (result > 0 ? 1 : -1);
    }
}
