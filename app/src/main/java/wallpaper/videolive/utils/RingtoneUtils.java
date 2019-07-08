package wallpaper.videolive.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import wallpaper.videolive.R;

public class RingtoneUtils {
    public static final String keyPref = "resIdRing";

    public static boolean setRingTone(Context context, String name, String relName, int type, String mode) {
        File file = new File(Environment.getExternalStorageDirectory(),
                "/IRingtones/My Ringtones/");
        if (!file.exists()) {
            file.mkdirs();
        }

        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/IRingtones/My Ringtones/";

        File f = new File(path + "/", relName + ".mp3");

        Uri mUri = Uri.parse("android.resource://"
                + context.getPackageName() + "/raw/" + name);
        ContentResolver mCr = context.getContentResolver();
        AssetFileDescriptor soundFile;
        try {
            soundFile = mCr.openAssetFileDescriptor(mUri, "r");
        } catch (FileNotFoundException e) {
            soundFile = null;
        }

        try {
            byte[] readData = new byte[1024];
            FileInputStream fis = soundFile.createInputStream();
            FileOutputStream fos = new FileOutputStream(f);
            int i = fis.read(readData);

            while (i != -1) {
                fos.write(readData, 0, i);
                i = fis.read(readData);
            }

            fos.close();
        } catch (IOException io) {
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, f.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, relName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.MediaColumns.SIZE, f.length());
        values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
        if (RingtoneManager.TYPE_RINGTONE == type) {
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        } else if (RingtoneManager.TYPE_NOTIFICATION == type) {
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        } else if (RingtoneManager.TYPE_ALARM == type) {
            values.put(MediaStore.Audio.Media.IS_ALARM, true);
        }

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(f
                .getAbsolutePath());
        mCr.delete(uri, MediaStore.MediaColumns.DATA + "=\"" + f.getAbsolutePath() + "\"", null);
        Uri newUri = mCr.insert(uri, values);


        try {
            RingtoneManager.setActualDefaultRingtoneUri(context,
                    type, newUri);
            Settings.System.putString(mCr, mode,
                    newUri.toString());

        } catch (Throwable t) {
            return false;
        }
        return true;

    }

    public static boolean setRingtoneContactNumber(Context context, String soundName, String phoneNumber, String contactName) {
        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
// The columns used for `Contacts.getLookupUri`
        final String[] projection = new String[]{
                ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY
        };
// Build your Cursor
        final Cursor data = context.getContentResolver().query(lookupUri, projection, null, null, null);
        data.moveToFirst();
        try {
            // Get the contact lookup Uri
            final long contactId = data.getLong(0);
            final String lookupKey = data.getString(1);
            final Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
            if (contactUri == null) {
                // Invalid arguments
                return false;
            }
            File file = new File(Environment.getExternalStorageDirectory(),
                    "/IRingtones/My Ringtones/");
            if (!file.exists()) {
                file.mkdirs();
            }

            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/IRingtones/My Ringtones/";

            File f = new File(path + "/", contactName + ".mp3");

            Uri mUri = Uri.parse("android.resource://"
                    + context.getPackageName() + "/raw/" + soundName);
            ContentResolver mCr = context.getContentResolver();
            AssetFileDescriptor soundFile;
            try {
                soundFile = mCr.openAssetFileDescriptor(mUri, "r");
            } catch (FileNotFoundException e) {
                soundFile = null;
            }

            try {
                byte[] readData = new byte[1024];
                FileInputStream fis = soundFile.createInputStream();
                FileOutputStream fos = new FileOutputStream(f);
                int i = fis.read(readData);

                while (i != -1) {
                    fos.write(readData, 0, i);
                    i = fis.read(readData);
                }

                fos.close();
            } catch (IOException io) {
                return false;
            }


            // Apply the custom ringtone

            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, f.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, soundName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.SIZE, f.length());
            values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(f
                    .getAbsolutePath());
            mCr.delete(uri, MediaStore.MediaColumns.DATA + "=\"" + f.getAbsolutePath() + "\"", null);
            Uri newUri = mCr.insert(uri, values);
            values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, newUri.toString());

            context.getContentResolver().update(contactUri, values, null, null);

        } finally {
            data.close();
        }
        return true;
    }

}
