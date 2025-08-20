package com.balatromod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstallResult;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "BalatroModPrefs";
    private static final String KEY_PACKAGE_NAME = "clonedPackageName";
    private static final String BALATRO_PACKAGE_NAME = "com.playstack.balatro";

    private Button mLaunchButton;
    private Button mPrepareButton;
    private Switch mSeedSwitch;
    private Button mConfigSpeedButton;
    private TextView mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLaunchButton = findViewById(R.id.btn_launch_app);
        mPrepareButton = findViewById(R.id.btn_prepare_app);
        mSeedSwitch = findViewById(R.id.switch_seed_mod);
        mConfigSpeedButton = findViewById(R.id.btn_config_speed);
        mStatusText = findViewById(R.id.text_status);

        mPrepareButton.setOnClickListener(v -> prepareBalatro());
        mLaunchButton.setOnClickListener(v -> launchBalatro());
        mConfigSpeedButton.setOnClickListener(v -> showSpeedSelectionDialog());

        setupModListeners();
        updateUIState();
    }

    private void setupModListeners() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Seeded Unlocks Mod Listener
        mSeedSwitch.setChecked(prefs.getBoolean("seeded_unlocks_enabled", false));
        mSeedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean("seeded_unlocks_enabled", isChecked).apply();
            ModManager.get().setModEnabled("seeded_unlocks", isChecked);
        });

        // Load Order Listeners
        findViewById(R.id.btn_speed_up).setOnClickListener(v -> ModManager.get().moveModUp("speed_mod"));
        findViewById(R.id.btn_speed_down).setOnClickListener(v -> ModManager.get().moveModDown("speed_mod"));
        findViewById(R.id.btn_seed_up).setOnClickListener(v -> ModManager.get().moveModUp("seeded_unlocks"));
        findViewById(R.id.btn_seed_down).setOnClickListener(v -> ModManager.get().moveModDown("seeded_unlocks"));
    }

    private void updateUIState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean appIsCloned = prefs.getString(KEY_PACKAGE_NAME, null) != null;

        mLaunchButton.setEnabled(appIsCloned);
        mConfigSpeedButton.setEnabled(appIsCloned);
        mSeedSwitch.setEnabled(appIsCloned);
        findViewById(R.id.btn_speed_up).setEnabled(appIsCloned);
        findViewById(R.id.btn_speed_down).setEnabled(appIsCloned);
        findViewById(R.id.btn_seed_up).setEnabled(appIsCloned);
        findViewById(R.id.btn_seed_down).setEnabled(appIsCloned);

        if (appIsCloned) {
            mStatusText.setText("Balatro is ready for modding!");
            mPrepareButton.setVisibility(View.GONE);
        } else {
            mStatusText.setText("Welcome! Please prepare Balatro for modding.");
            mPrepareButton.setVisibility(View.VISIBLE);
        }
    }

    private void prepareBalatro() {
        mPrepareButton.setEnabled(false);
        mStatusText.setText("Preparing Balatro... please wait.");
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    PackageInfo info = getPackageManager().getPackageInfo(BALATRO_PACKAGE_NAME, 0);
                    Log.d(TAG, "Balatro found, attempting to clone...");
                    InstallResult result = VirtualCore.get().installPackage(info.applicationInfo.sourceDir, 0);
                    if (result.isSuccess) {
                        Log.d(TAG, "Cloning successful.");
                        ModManager.get().onAppCloned(BALATRO_PACKAGE_NAME);
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString(KEY_PACKAGE_NAME, BALATRO_PACKAGE_NAME);
                        editor.apply();
                        return true;
                    } else {
                        Log.e(TAG, "Cloning failed: " + result.error);
                        return false;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Balatro not installed on device.");
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, "Balatro prepared successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to prepare Balatro. Is it installed?", Toast.LENGTH_LONG).show();
                }
                updateUIState();
                mPrepareButton.setEnabled(true);
            }
        }.execute();
    }

    private void launchBalatro() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String balatroPackageName = prefs.getString(KEY_PACKAGE_NAME, null);
        if (balatroPackageName == null) {
            Toast.makeText(MainActivity.this, "Error: App not prepared.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Attempting to launch " + balatroPackageName);
        Intent intent = VirtualCore.get().getLaunchIntent(balatroPackageName, 0);
        if (intent != null) {
            Log.d(TAG, "Launch intent created, starting activity...");
            startActivity(intent);
        } else {
            Log.w(TAG, "Launch intent not found for " + balatroPackageName);
            Toast.makeText(MainActivity.this, "Could not launch app.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSpeedSelectionDialog() {
        final CharSequence[] items = {"1x (Normal)", "2x", "4x", "8x", "Disabled"};
        final float[] speedValues = {1.0f, 2.0f, 4.0f, 8.0f, 0.0f}; // 0.0f to disable

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Game Speed");
        builder.setItems(items, (dialog, which) -> {
            float selectedSpeed = speedValues[which];
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            if (selectedSpeed == 0.0f) {
                editor.putBoolean("speed_mod_enabled", false);
                ModManager.get().setModEnabled("speed_mod", false);
            } else {
                editor.putBoolean("speed_mod_enabled", true);
                editor.putFloat("speed_mod_value", selectedSpeed);
                ModManager.get().setModEnabled("speed_mod", true);
            }
            editor.apply();
        });
        builder.show();
    }
}
