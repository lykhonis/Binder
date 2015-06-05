package com.vladli.android.binder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by vladlichonos on 6/4/15.
 */
public class SampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for sake of simplicity and providing sample
        // service will be created, execute task (~1s) and return result
        // we show toast message with our result

        SampleManager manager = new SampleManager(this) {

            @Override
            protected void onResultData(String data) {
                // getting callback from requestData() on UI thread (here)
                // of course this can be listener pattern or anything else that suits your needs
                Toast.makeText(SampleActivity.this, data, Toast.LENGTH_LONG).show();
            }
        };

        manager.setup(new SampleManager.OnSetupListener() {
            @Override
            public void onSetupCompleted(SampleManager manager) {
                // making request for some long operation from UI thread (here)
                manager.requestData();

                // it is a safe call to make sure to unbind service to avoid memory leaks
                // because manager uses queue to execute requests in background,
                // all posted requests will be executed in order they been called including release
                manager.release();
            }

            @Override
            public void onSetupFailed(SampleManager manager) {
                // nothing here
            }
        });
    }
}
