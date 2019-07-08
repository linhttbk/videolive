package wallpaper.videolive.views.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import wallpaper.videolive.R;
import wallpaper.videolive.adapters.RingtoneAdapter;
import wallpaper.videolive.models.Song;
import wallpaper.videolive.utils.RingtoneUtils;
import wallpaper.videolive.views.activities.HomeActivity;

import static wallpaper.videolive.utils.PermissionUtils.REQUEST_PERMISSION_CONTACTS;
import static wallpaper.videolive.utils.PermissionUtils.REQUEST_PICK_CONTACT;
import static wallpaper.videolive.utils.PermissionUtils.REQUEST_WRITE_SETTINGS;
import static wallpaper.videolive.utils.PermissionUtils.REQUEST_WRITE_STORAGE;

public class RingtoneHomeFragment extends Fragment implements RingtoneAdapter.OnItemClick {
    @BindView(R.id.rcvList)
    RecyclerView mRecycleView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private Thread thread;
    private static final String POPUP_CONSTANT = "mPopup";
    private static final String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
    RingtoneAdapter mAdapter = new RingtoneAdapter();
    Snackbar snackbar;
    public static RingtoneHomeFragment newInstance() {
        Bundle args = new Bundle();
        RingtoneHomeFragment fragment = new RingtoneHomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setTitleBar(getContext().getString(R.string.title_tab_ringtone));
            ((HomeActivity) getActivity()).enablePullToRefresh(false);
            ((HomeActivity) getActivity()).showProgressLoading(false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ringtone_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClick(this);
        mAdapter.replace(getAllListSong());
    }

    private ArrayList<Song> getAllListSong() {
        int resourceID = 0;
        String[] realName = getContext().getResources().getStringArray(R.array.list_nameRel);
        String[] realNameFile = getContext().getResources().getStringArray(R.array.list_nameRelFile);
        ArrayList<Song> listPlay = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();
        int index = 0;
        for (int count = 0; count < fields.length; count++) {
            try {
                if (fields[count].getName().equals(realNameFile[index])) {
                    resourceID = fields[count].getInt(fields[count]);
                    Song song = new Song(realName[index++], resourceID, false);
                    listPlay.add(song);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return listPlay;
    }


    @Override
    public void onItemClick(int position) {
        playMusic(position);
    }

    @Override
    public void onMoreClick(int position, View view) {

        if (!hasPermissionWriteSetting()) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            showDialogGrantPermissionWriteSetting(getString(R.string.msg_note_permission_write_settings));

        } else {
            if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                showDialogSetRingTone(position, view);
            }
        }
    }

    private void playMusic(final int position) {

        final Song song = mAdapter.getItem(position);

        if (song == null) return;
        if (!song.isPlay()) {
            releaseMusic();
            mediaPlayer = MediaPlayer.create(getContext(), song.getId());
            mediaPlayer.start();
            progressBar.setProgress(0);
            progressBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mediaPlayer = null;
                    progressBar.setProgress(0);
                    song.setPlay(false);
                    if (thread != null) thread.interrupt();
                    mAdapter.resetPlayer();
                    mAdapter.notifyDataSetChanged();
                }
            });
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int currentPosition = 0;
                    int total = mediaPlayer.getDuration();
                    while (mediaPlayer != null && currentPosition < total) {
                        try {
                            Thread.sleep(1000);
                            currentPosition = mediaPlayer.getCurrentPosition();
                        } catch (InterruptedException e) {
                            return;
                        } catch (Exception e) {
                            return;
                        }
                        progressBar.setProgress(currentPosition);
                    }

                }

            });
            thread.start();
        } else {
            if (mediaPlayer != null) mediaPlayer.reset();
        }

    }

    private void releaseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            if (thread != null) thread.interrupt();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseMusic();
    }

    private void showDialogSetRingTone(int position, View view) {
        final Song song = mAdapter.getItem(position);
        if (song == null) return;
        final PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.dialog_menu, popup.getMenu());

        try {
            // Reflection apis to enforce show icon
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(POPUP_CONSTANT)) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(POPUP_FORCE_SHOW_ICON, boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                String name = getContext().getResources().getResourceEntryName(song.getId());
                boolean success;
                switch (item.getItemId()) {
                    case R.id.default_ring:
                        success = RingtoneUtils.setRingTone(getContext(), name, song.getName(),
                                RingtoneManager.TYPE_RINGTONE, Settings.System.RINGTONE);
                        if (success)
                            Toast.makeText(getContext(), R.string.toast_msg_success, Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getContext(), R.string.toast_msg_fail, Toast.LENGTH_LONG).show();

                        return true;
                    case R.id.contact_ring:
                        if (!hasPermission(Manifest.permission.READ_CONTACTS) || !hasPermission(Manifest.permission.WRITE_CONTACTS)) {
                            requestPermission(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, REQUEST_PERMISSION_CONTACTS);
                        } else {
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                            openListContact();
                        }

                        return true;
                    case R.id.notification_ring:
                        success = RingtoneUtils.setRingTone(getContext(), name, song.getName(),
                                RingtoneManager.TYPE_NOTIFICATION, Settings.System.NOTIFICATION_SOUND);

                        if (success)
                            Toast.makeText(getContext(), R.string.toast_msg_success, Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getContext(), R.string.toast_msg_fail, Toast.LENGTH_LONG).show();

                        return true;
                    case R.id.alarm_ring:
                        success = RingtoneUtils.setRingTone(getContext(), name, song.getName(),
                                RingtoneManager.TYPE_ALARM, Settings.System.ALARM_ALERT);
                        if (success)
                            Toast.makeText(getContext(), R.string.toast_msg_success, Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getContext(), R.string.toast_msg_fail, Toast.LENGTH_LONG).show();

                        return true;
                    case R.id.share_ring:
                        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + song.getId());
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("audio/*");
                        share.putExtra(Intent.EXTRA_STREAM, uri);
                        getActivity().startActivity(Intent.createChooser(share, "Share Sound File"));

                        return true;

                    default:
                        return false;
                }
            }
        });
        popup.show();
    }


    private boolean hasPermissionWriteSetting() {
        if (Build.VERSION.SDK_INT < 23) return true;
        return Settings.System.canWrite(getContext());
    }

    private boolean hasPermission(String permission) {
        PackageManager pm = getContext().getPackageManager();
        int hasPerm = pm.checkPermission(
                permission,
                getContext().getPackageName());
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }

    private void openAndroidPermissionsMenu() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
    }


    private void requestPermission(String[] permission, int requestCode) {
        requestPermissions(permission, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("onRequestPermissions: ", "write" + requestCode);
        if (requestCode == REQUEST_WRITE_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showDialogGrantPermission(getString(R.string.granted_permission_msg));
            else {
                snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                        R.string.msg_snackbar_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.button_snackbar_permission,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= 23)
                                    requestPermissions(
                                            new String[]{Manifest.permission
                                                    .WRITE_EXTERNAL_STORAGE},
                                            REQUEST_WRITE_STORAGE);
                            }
                        }).show();
            }

        } else if (requestCode == REQUEST_PERMISSION_CONTACTS) {
            if (!hasPermission(Manifest.permission.READ_CONTACTS) || !hasPermission(Manifest.permission.WRITE_CONTACTS)) {
                snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                        R.string.msg_snackbar_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.button_snackbar_permission,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= 23) {
                                    requestPermissions(
                                            new String[]{Manifest.permission
                                                    .READ_CONTACTS, Manifest.permission
                                                    .WRITE_CONTACTS},
                                            REQUEST_PERMISSION_CONTACTS);
                                }

                            }
                        });


                snackbar.show();
            } else {
                openListContact();
            }
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult: ", "fragment");
        if (requestCode == REQUEST_PICK_CONTACT) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying())
                mediaPlayer.start();
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = getContext().getContentResolver().query(contactData, null, null, null, null);
                if (cursor.moveToFirst()) {
                    String contactNumberName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactNumber = null;

                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            break;
                        }
                        pCur.close();
                    }
                    // Todo something when contact number selected
                    Log.e("onActivityResult: ", contactNumberName + " " + contactNumber != null ? contactNumber : "null");
                    Song song = mAdapter.getItem(mAdapter.getSelectSetIndex());
                    if (song != null && contactNumber != null) {
                        String name = getContext().getResources().getResourceEntryName(song.getId());
                        boolean success = RingtoneUtils.setRingtoneContactNumber(getContext(), name, contactNumber, contactNumberName);
                        if (success) {
                            Toast.makeText(getContext(), R.string.toast_msg_success, Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getContext(), R.string.toast_msg_fail, Toast.LENGTH_LONG).show();
                    }

                }


            }
        } else if (requestCode == REQUEST_WRITE_SETTINGS) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying())
                mediaPlayer.start();
            if (!hasPermissionWriteSetting()) {
                showDialogGrantPermission(getString(R.string.not_granted_write_settings));
            } else {
                showDialogGrantPermission(getString(R.string.granted_permission_msg));
            }
        }
    }

    private void showDialogGrantPermission(String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle(R.string.title_dialog_granted);
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void showDialogGrantPermissionWriteSetting(String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle(R.string.title_dialog_granted);
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        openAndroidPermissionsMenu();

                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void openListContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_CONTACT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
    }
}
