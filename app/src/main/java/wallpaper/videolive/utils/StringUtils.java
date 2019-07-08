package wallpaper.videolive.utils;

import android.text.format.DateFormat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
    private static final String EMPTY = "";

    public static String formatDate(long date) {
        String result = DateFormat.format("dd/MM/yyyy HH:mm:ss", new Date(date)).toString();
        return result == null ? EMPTY : result;
    }

    public static String formatSize(long size) {
        if (size <= 0)
            return "0B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        String result = new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        return result == null ? EMPTY : result;
    }
}
