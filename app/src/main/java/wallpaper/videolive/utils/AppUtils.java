package wallpaper.videolive.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;

public class AppUtils {
    public static int getWidthScreen(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }
    public static void sendFeedBack(Context context){
        final Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setData(Uri.parse("mailto:"));
        // emailIntent.setType("text/email");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "livewallpaperandringtone@gmail.com" });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear," + "");



        try {
            context.startActivity(emailIntent);
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}
