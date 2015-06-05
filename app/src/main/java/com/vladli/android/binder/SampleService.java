package com.vladli.android.binder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by vladlichonos on 6/4/15.
 */
public class SampleService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        ParcelableBinder binder = intent.getParcelableExtra("callback");
        if (binder == null) return null;
        ISampleCallback callback = ISampleCallback.Stub.asInterface(binder.getBinder());
        return new SampleBinder(callback);
    }

    class SampleBinder extends ISampleService.Stub {

        ISampleCallback mCallback;

        public SampleBinder(ISampleCallback callback) {
            mCallback = callback;
        }

        @Override
        public void requestData() throws RemoteException {
            try {
                Thread.sleep(1000l); // doing something
            } catch (InterruptedException ignore) {
            }
            // sending synchronous notification to the caller
            mCallback.notifyResultData("Hello Binder!");
        }
    }
}
