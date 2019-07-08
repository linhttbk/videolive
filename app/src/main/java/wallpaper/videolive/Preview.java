package wallpaper.videolive;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import wallpaper.videolive.utils.PrefUtils;

public class Preview extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 16) {
            /*
             * Open live wallpaper preview (API Level 16 or greater).
             */
            Log.e("onActivityResult: ", "selectedImageUri" + PrefUtils.getPreferences(this, PrefUtils.KEY_PREF_URI, ""));
            intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            String pkg = getPackageName();
            String cls = Service.class.getCanonicalName();
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(pkg, cls));

        } else {

            /*
             * Open live wallpaper picker (API Level 15 or lower).
             *
             * Display a quick little message (toast) with instructions.
             */
            intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
            Resources res = getResources();
//			String hint = res.getString(R.string.picker_toast_prefix) + res.getString(R.string.lwp_name)
//					+ res.getString(R.string.picker_toast_suffix);
            String hint = "Choose video live wallpaper from the list";

            Toast toast = Toast.makeText(this, hint, Toast.LENGTH_LONG);
            toast.show();
        }
        startActivity(intent);
        finish();
    }




}
