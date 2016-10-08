package com.chakra.filescanner.service;

/**
 * Created by f68dpim on 9/29/16.
 */
public interface IFileScannerService {
    void startScan();
    void stopScan();
    boolean isScanning();
    void registerScanListener(IScanListener listener);
    void unRegisterScanListener(IScanListener listener);
}
