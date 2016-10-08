package com.chakra.filescanner.data;

import com.chakra.filescanner.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

public class ScannedData {

    private static final int MAX_FILES_SIZE = 10;
    private static final int MAX_EXTS_SIZE = 5;

    public static class FileStatistics {
        FileData[] largeFiles;
        ExtData[] frequentFiles;
        float averageSize;

        public FileData[] getLargeFiles() {
            return largeFiles;
        }

        public ExtData[] getFrequentFiles() {
            return frequentFiles;
        }

        public float getAverageSize() {
            return averageSize;
        }

        @Override
        public String toString() {
            return "Large File Names: " + Utils.arrayToString(largeFiles)
                    + "\n Most Frequent Extensions: " + Utils.arrayToString(frequentFiles)
                    + "\n" + "Average File Size: " + averageSize;
        }
    }
    private TreeSet<FileData> mScannedFiles;
    private HashMap<String, ExtData> mScannedExt;
    private long mTotalSize;
    private int mNoOfFiles;
    private boolean mIsDataProcessed;
    private Object mLock = new Object();

    public ScannedData() {
        this.mScannedFiles = new TreeSet<FileData>();
        this.mScannedExt = new HashMap<String, ExtData>();
    }

    public void addFile(File file) {
        synchronized (mLock) {
            mTotalSize += file.length();
            mNoOfFiles++;
            String path = file.getAbsolutePath();
            if(mScannedFiles.size() == MAX_FILES_SIZE) {
                if(mScannedFiles.last().size < file.length()) {
                    FileData fileData = new FileData(path, file.length());
                    mScannedFiles.add(fileData);
                    mScannedFiles.remove(mScannedFiles.last());
                }
            } else {
                FileData fileData = new FileData(path, file.length());
                mScannedFiles.add(fileData);
            }

            if (path.contains(".")) {
                int i = path.lastIndexOf(".");
                String ext = path.substring(i);

                if (mScannedExt.get(ext) != null) {
                    mScannedExt.get(ext).count ++;
                } else {
                    mScannedExt.put(ext, new ExtData(ext, 1));
                }
            }
        }
    }

    public FileStatistics getFileStatistics() {
        synchronized (mLock) {
            FileStatistics data = new FileStatistics();
            data.averageSize = mTotalSize / mNoOfFiles;
            data.largeFiles = mScannedFiles.toArray(new FileData[]{});
            ArrayList<ExtData> extLit = new ArrayList<ExtData>();
            extLit.addAll(mScannedExt.values());
            Collections.sort(extLit);
            data.frequentFiles = extLit.subList(0, MAX_EXTS_SIZE).toArray(new ExtData[]{});
            return data;
        }
    }


    public int getTotalFileCount() {
        return mNoOfFiles;
    }
}
