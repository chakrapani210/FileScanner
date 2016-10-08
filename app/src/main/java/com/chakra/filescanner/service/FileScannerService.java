package com.chakra.filescanner.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.chakra.filescanner.data.ScannedData;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by f68dpim on 9/29/16.
 */
public class FileScannerService extends Service {
    private static final long HALF_SECOND = 500;
    private FileScanner mFileScanner;
    private AtomicBoolean mIsDirectoryScanComplete = new AtomicBoolean(false);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mFileScanner;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        File sdCardDir = Environment.getExternalStorageDirectory();
        mFileScanner = new FileScanner(sdCardDir);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFileScanner.stopScan(false);
        mFileScanner = null;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    class FileScanner extends Binder implements IFileScannerService {
        BlockingQueue<File> mFileQueue;
        Queue<File> mDirQueue;
        File mRootDir;
        ScannedData mScannedData;
        private Scanner mScanner;
        private FileReader mReader;
        List<IScanListener> mListeners;
        private boolean mIsScanning = false;
        private final static int START_MSG = 1;
        private final static int UPDATE_MSG = 2;
        private final static int STOP_MSG = 3;
        private final static int COMPLETE_MSG = 4;
        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case START_MSG:
                        for (IScanListener listener: mListeners) {
                            listener.onStartScanning();
                        }
                        break;
                    case UPDATE_MSG:
                        for (IScanListener listener: mListeners) {
                            int progress = 0;
                            if(mScannedData.getTotalFileCount() != 0) {
                                //Calculating % value
                                 progress = 100 * mScannedData.getTotalFileCount()
                                        / (mScannedData.getTotalFileCount() + mFileQueue.size());
                            }
                            listener.onProgressUpdate(progress);
                        }
                        mHandler.sendEmptyMessageDelayed(UPDATE_MSG, HALF_SECOND);
                        break;
                    case STOP_MSG:
                        mIsScanning = false;
                        for (IScanListener listener: mListeners) {
                            listener.onStop(mScannedData.getFileStatistics());
                        }
                        break;
                    case COMPLETE_MSG:
                        mIsScanning = false;
                        for (IScanListener listener: mListeners) {
                            listener.onScanComplete(mScannedData.getFileStatistics());
                        }
                        break;
                }
            }
        };
        public FileScanner(File rootDir) {
            mFileQueue = new LinkedBlockingDeque<File>();
            mDirQueue = new LinkedList<>();
            mRootDir = rootDir;
            mScannedData = new ScannedData();
            mDirQueue.offer(mRootDir);
            mListeners = new ArrayList<IScanListener>();
        }

        @Override
        public void startScan() {
            if(!mIsScanning) {
                mIsScanning = true;
                mScanner = new Scanner();
                mReader = new FileReader();
                new Thread(mScanner).start();
                new Thread(mReader).start();
            }
            mHandler.sendEmptyMessage(START_MSG);
            mHandler.removeMessages(UPDATE_MSG);
            mHandler.sendEmptyMessageDelayed(UPDATE_MSG, HALF_SECOND);
        }

        @Override
        public void stopScan() {
            stopScan(true);

        }

        @Override
        public boolean isScanning() {
            return mIsScanning;
        }

        private void stopScan(boolean updateUi) {
            mScanner.pause();
            mReader.pause();
            if(updateUi) {
                mHandler.sendEmptyMessage(STOP_MSG);
            }
        }
        @Override
        public void registerScanListener(IScanListener listener) {
            mListeners.add(listener);
        }

        @Override
        public void unRegisterScanListener(IScanListener listener) {
            mListeners.remove(listener);
        }

        class Scanner implements Runnable {
            private AtomicBoolean isPaused = new AtomicBoolean(false);

            @Override
            public void run() {
                try {
                    while (!isPaused.get()) {
                        File dir = mDirQueue.poll();
                        if(dir != null) {
                            File[] files = dir.listFiles();
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    mDirQueue.offer(file);
                                } else {
                                    mFileQueue.put(file);
                                }
                            }
                        } else {
                            mIsDirectoryScanComplete.set(true);
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            void pause() {
                isPaused.set(true);
                Thread.currentThread().interrupt();
            }
        }
        class FileReader implements Runnable {
            private AtomicBoolean isPaused = new AtomicBoolean(false);

            @Override
            public void run() {
                try {
                    while (!isPaused.get() && !mIsDirectoryScanComplete.get()) {
                        File file = mFileQueue.poll(1, TimeUnit.SECONDS);
                        if(file != null) {
                            mScannedData.addFile(file);
                        }
                    }
                    if(mIsDirectoryScanComplete.get()) {
                        mHandler.sendEmptyMessage(COMPLETE_MSG);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            void pause() {
                isPaused.set(true);
                Thread.currentThread().interrupt();
            }
        }
    }
}