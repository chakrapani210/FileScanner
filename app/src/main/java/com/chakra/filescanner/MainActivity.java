package com.chakra.filescanner;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fmgr = getSupportFragmentManager();
        Fragment fragment = fmgr.findFragmentById(R.id.containerfragment);
        if(fragment == null) {
            fragment = new FileScannerFragment();
            fmgr.beginTransaction().add(R.id.containerfragment, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment();
        if(fragment != null && fragment instanceof IBackAware) {
            if(((IBackAware)fragment).onHandleBackPressed()) {
                // Do nothing
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    public Fragment getCurrentFragment() {
        Fragment mnFragment;
        mnFragment = getSupportFragmentManager().findFragmentById(R.id.containerfragment);
        return mnFragment;
    }
}
