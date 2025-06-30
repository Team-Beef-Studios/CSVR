package com.lvonasek.csvr;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class XashActivity extends SDLActivity {
    private static Activity activity;
    private boolean mUseVolumeKeys;
    private String mPackageName;
    private static final String TAG = "CSVR";

    private static final int PERMISSION_CODE = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //getWindow().addFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
        };
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, PERMISSION_CODE);
                return;
            }
        }

        // Prepare root directory
        File root = new File("/sdcard/xash");
        root.mkdir();
        try {
            new File(root, ".nomedia").createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Copy custom weapon models
        File models = new File(root, "cstrike/models");
        if (!new File(models, "nocopy").exists()) {
            copyAssets("models", models);
            copyAssets("models/shield", new File(models, "shield"));
        }
        copyAsset("vr_weapons.cfg", new File(models, "vr_weapons.cfg"), false);
        nativeSetenv("xr_manufacturer", Build.MANUFACTURER.toUpperCase());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            System.exit(0);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Now that we don't exit from native code, we need to exit here, resetting
        // application state (actually global variables that we don't cleanup on exit)
        //
        // When the issue with global variables will be resolved, remove that exit() call
        System.exit(0);
    }

    @Override
    protected String[] getLibraries() {
        return new String[]{"SDL2", "xash"};
    }

    @SuppressLint("HardwareIds")
    private String getAndroidID() {
        return Secure.getString(getContentResolver(), Secure.ANDROID_ID);
    }

    @SuppressLint("ApplySharedPref")
    private void saveAndroidID(String id) {
        getSharedPreferences("xash_preferences", MODE_PRIVATE).edit().putString("xash_id", id).commit();
    }

    private String loadAndroidID() {
        return getSharedPreferences("xash_preferences", MODE_PRIVATE).getString("xash_id", "");
    }

    @Override
    public String getCallingPackage() {
        if (mPackageName != null) {
            return mPackageName;
        }

        return super.getCallingPackage();
    }

    private AssetManager getAssets(boolean isEngine) {
        return getAssets();
    }

    private String[] getAssetsList(boolean isEngine, String path) {
        AssetManager am = getAssets(isEngine);

        try {
            return am.list(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[]{};
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (SDLActivity.mBrokenLibraries) {
            return false;
        }

        int keyCode = event.getKeyCode();
        if (!mUseVolumeKeys) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                    keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                    keyCode == KeyEvent.KEYCODE_CAMERA ||
                    keyCode == KeyEvent.KEYCODE_ZOOM_IN ||
                    keyCode == KeyEvent.KEYCODE_ZOOM_OUT) {
                return false;
            }
        }

        return getWindow().superDispatchKeyEvent(event);
    }

    @Override
    protected String[] getArguments() {
        String gamedir = getIntent().getStringExtra("gamedir");
        if (gamedir == null) gamedir = "cstrike";
        nativeSetenv("XASH3D_GAME", gamedir);

        String gamelibdir = getIntent().getStringExtra("gamelibdir");
        if (gamelibdir != null) nativeSetenv("XASH3D_GAMELIBDIR", gamelibdir);

        String pakfile = getIntent().getStringExtra("pakfile");
        if (pakfile != null) nativeSetenv("XASH3D_EXTRAS_PAK2", pakfile);

        mUseVolumeKeys = getIntent().getBooleanExtra("usevolume", false);
        mPackageName = getIntent().getStringExtra("package");

        String[] env = getIntent().getStringArrayExtra("env");
        if (env != null) {
            for (int i = 0; i < env.length; i += 2)
                nativeSetenv(env[i], env[i + 1]);
        }

        String argv = getIntent().getStringExtra("argv");
        if (argv == null) argv = "-console -log";
        return argv.split(" ");
    }

    public static int openURL(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private boolean copyAsset(String from, File to, boolean overwrite) {
        if (overwrite || !to.exists()) {
            try {
                InputStream in = getAssets(false).open(from);
                FileOutputStream out = new FileOutputStream(to);
                byte[] buf = new byte[1024];
                while (true) {
                    int count = in.read(buf);
                    if (count <= 0) {
                        break;
                    }
                    out.write(buf, 0, count);
                }
                out.close();
                in.close();
                Log.d(TAG, "File " + from + " unpacked");
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private void copyAssets(String from, File to) {
        boolean ok = true;
        for (String model : getAssetsList(false, from)) {
            if (!copyAsset(from + "/" + model, new File(to, model), true)) {
                ok = false;
            }
        }
        if (ok) {
            Log.d(TAG, "Folder " + from + " unpacked");
        }
    }
}
