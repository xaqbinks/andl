package com.balatromod;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;

public class ModApplication extends Application {

    private static final String TAG = "BalatroModApp";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "Attaching base context and starting VirtualCore...");
        try {
            VASettings.ENABLE_IO_REDIRECT = true;
            VirtualCore.get().startup(base);
            Log.d(TAG, "VirtualCore started successfully.");
        } catch (Throwable e) {
            Log.e(TAG, "Failed to start VirtualCore", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {
            @Override
            public void onMainProcess() {
                ModManager.get().init(ModApplication.this);
            }

            @Override
            public void onVirtualProcess() {
                // Nothing to do here for now
            }
        });
    }
}
