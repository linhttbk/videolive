package wallpaper.videolive.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class PrefUtils {
    public static final String KEY_PREF_URI = "uri";
    public static final String KEY_PREF_MUTE = "mute";
    public static final String KEY_PREF_REN_MODE = "rendererMode";
    public static final String KEY_MIN_VALUE = "min";
    public static final String KEY_MAX_VALUE = "max";


    public class RendererMode {
        public static final String CLASSIC = "Classic";
        public static final String LETTER_BOXED = "Letter Boxed";
        public static final String STRETCHED = "Stretched";
    }

    public static SharedPreferences getPreferences(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void savePreferences(Context context, String key, Set<String> stringSet) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putStringSet(key, stringSet);
        editor.apply();
    }

    public static void savePreferences(Context context, String key, boolean content) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(key, content);
        editor.apply();
    }

    public static void savePreferences(Context context, String key, String content) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(key, content);
        editor.apply();
    }

    public static void savePreferences(Context context, String key, int content) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(key, content);
        editor.apply();
    }

    public static boolean getPreferences(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getBoolean(key, false);
    }

    public static String getPreferences(Context context, String key, String defVal) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(key, defVal);
    }

    public static int getPreferences(Context context, String key, int defVal) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt(key, defVal);
    }

    public static Set<String> getPreferences(Context context, String key, Set<String> stringSet) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getStringSet(key, stringSet);
    }
}
