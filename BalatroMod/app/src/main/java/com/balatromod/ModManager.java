package com.balatromod;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ModManager {

    private static final String TAG = "BalatroModManager";
    private static final ModManager sInstance = new ModManager();
    private Context mContext;

    private final List<String> mEnabledMods = new ArrayList<>();
    private static final String ORIGINAL_GAME_SCRIPT_NAME = "boot.lua";
    private static final String ORIGINAL_BOOT_SCRIPT_FILENAME = "original_boot.lua";
    private static final String FINAL_LOADER_SCRIPT_FILENAME = "loader.lua";

    public static ModManager get() {
        return sInstance;
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
        regenerateLoaderScript();
    }

    public void onAppCloned(String packageName) {
        Log.d(TAG, "App cloned: " + packageName + ". Extracting original boot script.");
        try {
            InstalledAppInfo appInfo = VirtualCore.get().getInstalledAppInfo(packageName, 0);
            if (appInfo == null) {
                Log.e(TAG, "Could not get app info for " + packageName);
                return;
            }
            Context guestContext = mContext.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            AssetManager guestAssets = guestContext.getAssets();

            File originalBootFile = new File(mContext.getFilesDir(), ORIGINAL_BOOT_SCRIPT_FILENAME);
            copyAssetFromGuest(guestAssets, ORIGINAL_GAME_SCRIPT_NAME, originalBootFile.getAbsolutePath());
            Log.d(TAG, "Copied original boot.lua successfully.");

            File loaderFile = new File(mContext.getFilesDir(), FINAL_LOADER_SCRIPT_FILENAME);
            VirtualCore.get().addRedirect(ORIGINAL_GAME_SCRIPT_NAME, loaderFile.getAbsolutePath());
            Log.d(TAG, "Permanently redirecting " + ORIGINAL_GAME_SCRIPT_NAME + " to " + loaderFile.getAbsolutePath());

            regenerateLoaderScript();
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract original boot script.", e);
        }
    }

    public void setModEnabled(String modName, boolean enabled) {
        if (enabled) {
            if (!mEnabledMods.contains(modName)) {
                Log.d(TAG, "Enabling mod: " + modName);
                mEnabledMods.add(modName);
            }
        } else {
            Log.d(TAG, "Disabling mod: " + modName);
            mEnabledMods.remove(modName);
        }
        regenerateLoaderScript();
        android.widget.Toast.makeText(mContext, modName + " " + (enabled ? "enabled" : "disabled"), android.widget.Toast.LENGTH_SHORT).show();
    }

    public void moveModUp(String modName) {
        int index = mEnabledMods.indexOf(modName);
        if (index > 0) {
            String removed = mEnabledMods.remove(index);
            mEnabledMods.add(index - 1, removed);
            regenerateLoaderScript();
            Log.d(TAG, "Moved mod " + modName + " up.");
        }
    }

    public void moveModDown(String modName) {
        int index = mEnabledMods.indexOf(modName);
        if (index != -1 && index < mEnabledMods.size() - 1) {
            String removed = mEnabledMods.remove(index);
            mEnabledMods.add(index + 1, removed);
            regenerateLoaderScript();
            Log.d(TAG, "Moved mod " + modName + " down.");
        }
    }

    private void regenerateLoaderScript() {
        Log.d(TAG, "Regenerating monolithic loader script...");
        File loaderFile = new File(mContext.getFilesDir(), FINAL_LOADER_SCRIPT_FILENAME);

        try (FileOutputStream out = new FileOutputStream(loaderFile)) {
            File originalBootFile = new File(mContext.getFilesDir(), ORIGINAL_BOOT_SCRIPT_FILENAME);
            if (originalBootFile.exists()) {
                out.write(readBytes(originalBootFile));
                out.write("\n\n".getBytes());
            }

            for (String modName : mEnabledMods) {
                out.write(("-- Loading mod: " + modName + "\n").getBytes());
                if ("speed_mod".equals(modName)) {
                    SharedPreferences prefs = mContext.getSharedPreferences("BalatroModPrefs", Context.MODE_PRIVATE);
                    float speed = prefs.getFloat("speed_mod_value", 1.0f);
                    String speedScript = String.format(java.util.Locale.US, "G.GAME.SPEEDFACTOR = %.1f\n", speed);
                    out.write(speedScript.getBytes());
                    Log.d(TAG, "Injected speed mod with value: " + speed);
                } else {
                    out.write(readAsset(modName + ".lua"));
                }
                out.write("\n\n".getBytes());
            }
            Log.d(TAG, "Monolithic loader script regenerated successfully.");

        } catch (IOException e) {
            Log.e(TAG, "Failed to regenerate loader script", e);
        }
    }

    private byte[] readBytes(File file) throws IOException {
        return readBytes(new java.io.FileInputStream(file));
    }

    private byte[] readAsset(String assetName) throws IOException {
        return readBytes(mContext.getAssets().open(assetName));
    }

    private byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = in.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        in.close();
        return byteBuffer.toByteArray();
    }

    private void copyAssetFromGuest(AssetManager guestAssets, String assetName, String destPath) throws IOException {
        InputStream in = guestAssets.open(assetName);
        OutputStream out = new FileOutputStream(destPath);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
    }
}
