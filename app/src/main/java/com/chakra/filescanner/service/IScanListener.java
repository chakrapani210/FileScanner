package com.chakra.filescanner.service;

import com.chakra.filescanner.data.ScannedData;

/**
 * Created by f68dpim on 10/2/16.
 */
public interface IScanListener {
    void onStartScanning();
    void onScanComplete(ScannedData.FileStatistics data);
    void onProgressUpdate(int progress);
    void onStop(ScannedData.FileStatistics data);
}
