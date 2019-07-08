package wallpaper.videolive;

import android.os.Bundle;
import android.preference.PreferenceActivity;

// Deprecated PreferenceActivity methods are used for API Level 10 (and lower) compatibility
// https://developer.android.com/guide/topics/ui/settings.html#Overview
@SuppressWarnings("deprecation")
public class Settings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		finish();
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


}