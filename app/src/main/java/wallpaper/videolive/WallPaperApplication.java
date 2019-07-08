package wallpaper.videolive;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class WallPaperApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.ads_id));
    }
}
