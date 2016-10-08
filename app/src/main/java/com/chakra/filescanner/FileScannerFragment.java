package com.chakra.filescanner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chakra.filescanner.data.ExtData;
import com.chakra.filescanner.data.FileData;
import com.chakra.filescanner.data.ScannedData;
import com.chakra.filescanner.service.FileScannerService;
import com.chakra.filescanner.service.IFileScannerService;
import com.chakra.filescanner.service.IScanListener;

/**
 * Created by f68dpim on 10/2/16.
 */
public class FileScannerFragment extends Fragment implements View.OnClickListener, IScanListener, IBackAware {
    TextView mAverageFileZise;
    LinearLayout mFileListLayout;
    LinearLayout mExtListLayout;
    IFileScannerService mScanner;
    Button mStartScanButton;
    Button mStopScanButton;
    ProgressBar mProgressBar;
    private ScannedData.FileStatistics mData;

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScanner = (IFileScannerService) service;
            mScanner.registerScanListener(FileScannerFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mScanner = null;
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        mStartScanButton = (Button)view.findViewById(R.id.start);
        mStopScanButton = (Button)view.findViewById(R.id.stop);
        mStartScanButton.setOnClickListener(this);
        mStopScanButton.setOnClickListener(this);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);
        mFileListLayout = (LinearLayout)view.findViewById(R.id.filesList);
        mExtListLayout = (LinearLayout)view.findViewById(R.id.extList);
        mAverageFileZise = (TextView)view.findViewById(R.id.averageSize);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if(mScanner != null) {
                    mScanner.startScan();
                }
                break;
            case R.id.stop:
                if(mScanner != null) {
                    mScanner.stopScan();
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_option, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.share).setVisible(mData != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mData.toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
        }
        return false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getContext(), FileScannerService.class);
        getContext().bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStartScanning() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScanComplete(ScannedData.FileStatistics data) {
        updateData(data);
    }

    @Override
    public void onProgressUpdate(int progress) {
       mProgressBar.setProgress(progress);
    }

    @Override
    public void onStop(ScannedData.FileStatistics data) {
        updateData(data);
    }

    private void updateData(ScannedData.FileStatistics data) {
        mProgressBar.setVisibility(View.GONE);
        mData = data;
        getActivity().invalidateOptionsMenu();

        mAverageFileZise.setText(Utils.toKbMb((long) data.getAverageSize()));
        mFileListLayout.removeAllViews();
        mExtListLayout.removeAllViews();
        for(FileData fd: data.getLargeFiles()) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, mFileListLayout, false);
            ((TextView)view.findViewById(R.id.file_name)).setText(Utils.getFileName(fd.path));
            ((TextView)view.findViewById(R.id.file_size)).setText(Utils.toKbMb(fd.size));
            mFileListLayout.addView(view);
        }

        for(ExtData ed: data.getFrequentFiles()) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, mExtListLayout, false);
            ((TextView)view.findViewById(R.id.file_name)).setText(Utils.getFileName(ed.ext));
            ((TextView)view.findViewById(R.id.file_size)).setText(ed.count + "");
            mExtListLayout.addView(view);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScanner.unRegisterScanListener(FileScannerFragment.this);
        getContext().unbindService(mConn);
    }

    @Override
    public boolean onHandleBackPressed() {
        mScanner.stopScan();
        return false;
    }
}

