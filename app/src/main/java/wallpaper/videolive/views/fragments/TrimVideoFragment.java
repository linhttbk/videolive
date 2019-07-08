package wallpaper.videolive.views.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import wallpaper.videolive.Preview;
import wallpaper.videolive.R;
import wallpaper.videolive.listener.OnSetWallPaperCallback;
import wallpaper.videolive.listener.OnTrimVideoCallBack;
import wallpaper.videolive.utils.PrefUtils;
import wallpaper.videolive.views.activities.HomeActivity;
import wallpaper.videolive.views.custom.OnRangeSeekbarChangeListener;
import wallpaper.videolive.views.custom.OnRangeSeekbarFinalValueListener;
import wallpaper.videolive.views.custom.RangeSeekBarView;

public class TrimVideoFragment extends Fragment implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, OnRangeSeekbarChangeListener, OnRangeSeekbarFinalValueListener, OnTrimVideoCallBack {
    private String path;
    private static final String KEY_PATH = "path";
    @BindView(R.id.rsbView)
    RangeSeekBarView rangeSeekBar;
    @BindView(R.id.svVideo)
    SurfaceView svVideo;
    private SurfaceHolder mSurfaceHolder;
    MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private Runnable runnable;
    private OnSetWallPaperCallback callback;

    public static TrimVideoFragment newInstance(String path, OnSetWallPaperCallback callback) {
        Bundle args = new Bundle();
        args.putString(KEY_PATH, path);
        TrimVideoFragment fragment = new TrimVideoFragment();
        fragment.callback = callback;
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trim_video, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (path != null) {
            //    initFramesVideo();
            mSurfaceHolder = svVideo.getHolder();
            mSurfaceHolder.addCallback(this);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(KEY_PATH);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.VISIBLE);
            ((HomeActivity) getActivity()).setShowBackIconToolbar();
            ((HomeActivity) getActivity()).setTitleBar(getContext().getString(R.string.title_tab_trim_video));
            ((HomeActivity) getActivity()).enablePullToRefresh(false);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(mSurfaceHolder);
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        } catch (IOException e) {
            e.printStackTrace();
            rangeSeekBar.setVisibility(View.INVISIBLE);
            showErrorPlayVideo();

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {

        //calculator video player size
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        ViewGroup.LayoutParams lp = svVideo.getLayoutParams();
        lp.width = screenWidth;
        lp.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);
        svVideo.setLayoutParams(lp);

        mMediaPlayer.start();
        rangeSeekBar.setDataType(RangeSeekBarView.DataType.INTEGER);
        rangeSeekBar.setMinValue(0);
        rangeSeekBar.setMaxValue(mediaPlayer.getDuration() > 0 ? mediaPlayer.getDuration() : 100);
        rangeSeekBar.setBarColor(getResources().getColor(R.color.yellow));
        rangeSeekBar.setLeftThumbColor(getResources().getColor(R.color.yellow));
        rangeSeekBar.setLeftThumbHighlightColor(getResources().getColor(R.color.yellow));
        rangeSeekBar.setRightThumbColor(getResources().getColor(R.color.yellow));
        rangeSeekBar.setRightThumbHighlightColor(getResources().getColor(R.color.yellow));
        rangeSeekBar.setMaxValue(mediaPlayer.getDuration()).setMaxStartValue(mediaPlayer.getDuration());
        rangeSeekBar.setOnRangeSeekbarChangeListener(this);
        rangeSeekBar.setOnRangeSeekbarFinalValueListener(this);

//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (mMediaPlayer != null) {
//                    int mCurrentPosition = mMediaPlayer.getCurrentPosition();
//                    if (mCurrentPosition >= (int) rangeSeekBar.getSelectedMaxValue()) {
//                        try {
//                            mediaPlayer.seekTo((int) rangeSeekBar.getSelectedMinValue() >= 0 ? (int) rangeSeekBar.getSelectedMinValue() : 0);
//                        } catch (IllegalStateException e) {
//                        }
//                    }
//                }
//            }
//        };
//        mHandler.postDelayed(runnable, 300);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                    if (mCurrentPosition >= (int) rangeSeekBar.getSelectedMaxValue()) {
                        try {
                            mediaPlayer.seekTo((int) rangeSeekBar.getSelectedMinValue() >= 0 ? (int) rangeSeekBar.getSelectedMinValue() : 0);
                        } catch (IllegalStateException e) {
                        }
                    }
                }
                mHandler.postDelayed(runnable, 300);
            }
        };
        getActivity().runOnUiThread(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setShowIndicatorIconToolbar();
        }
        if (mHandler != null && runnable != null) {
            mHandler.removeCallbacks(runnable);
            mHandler = null;
            runnable = null;
        }

    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    @Override
    public void valueChanged(Number minValue, Number maxValue) {

    }

    @Override
    public void valueMinChanged(Number minValue, Number maxValue) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
            mMediaPlayer.seekTo((Integer) minValue);
        }

    }

    @Override
    public void valueMaxChanged(Number minValue, Number maxValue) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
            mMediaPlayer.seekTo((Integer) maxValue);
        }
    }

    @Override
    public void finalValue(Number minValue, Number maxValue) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo((Integer) minValue);
            mMediaPlayer.start();
        }
    }

    @Override
    public void onStartTrim() {
        Log.e("onStartTrim: ", "Hello");
        previewVideo();

    }

    private void previewVideo() {
        PrefUtils.savePreferences(getContext(), PrefUtils.KEY_PREF_URI, path);
        PrefUtils.savePreferences(getContext(), PrefUtils.KEY_MIN_VALUE, (int) rangeSeekBar.getSelectedMinValue());
        PrefUtils.savePreferences(getContext(), PrefUtils.KEY_MAX_VALUE, (int) rangeSeekBar.getSelectedMaxValue());
        releaseMediaPlayer();
        getActivity().onBackPressed();
        if(callback!=null)callback.callBack();
//       .
    }


    private void showErrorPlayVideo() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(getContext().getString(R.string.title_dialog_play_video))
                .setMessage(getContext().getString(R.string.msg_dialog_play_video))
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete

                        getActivity().onBackPressed();
                        dialog.dismiss();

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
