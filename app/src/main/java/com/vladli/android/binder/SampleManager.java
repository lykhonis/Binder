package com.vladli.android.binder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vladlichonos on 6/4/15.
 */
public class SampleManager {

    Context mContext;
    ISampleService mService;
    Connection mConnection;
    Handler mHandler;
    ExecutorService mExecutor;
    volatile boolean mBinding;
    OnSetupListener mBindListener;

    public SampleManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public boolean isBound() {
        return mBinding && mService != null;
    }

    public void release() {
        if (mBinding) {
            execute(new Runnable() {
                @Override
                public void run() {
                    if (mBinding) {
                        mBinding = false;
                        mContext.unbindService(mConnection);
                        mBindListener = null;
                        mConnection = null;
                    }
                    if (mExecutor != null) {
                        mExecutor.shutdownNow();
                        mExecutor = null;
                    }
                }
            });
        }
    }

    public void setup(final OnSetupListener listener) {
        Intent service = new Intent();
        // use class name for services in different processes
        service.setClassName("com.vladli.android.binder",
                             "com.vladli.android.binder.SampleService");
        // passing callback binder
        service.putExtra("callback", new ParcelableBinder(mCallback.asBinder()));
        if (mBinding = mContext.bindService(service,
                                            mConnection = new Connection(),
                                            // create if service is not running
                                            Context.BIND_AUTO_CREATE)) {
            mBindListener = listener;
            mExecutor = Executors.newSingleThreadExecutor();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSetupFailed(SampleManager.this);
                }
            });
        }
    }

    public void requestData() {
        if (!isBound()) {
            throw new IllegalStateException("Manager setup() has not been called.");
        }
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mService.requestData();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected void onResultData(String data) {
    }

    void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    ISampleCallback mCallback = new ISampleCallback.Stub() {

        @Override
        public void notifyResultData(final String data) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onResultData(data);
                }
            });
        }
    };

    public interface OnSetupListener {

        void onSetupCompleted(SampleManager manager);

        void onSetupFailed(SampleManager manager);
    }

    class Connection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ISampleService.Stub.asInterface(service);
            mBindListener.onSetupCompleted(SampleManager.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    }
}
