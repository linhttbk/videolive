package wallpaper.videolive.views.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallpaper.videolive.R;
import wallpaper.videolive.async.WallPaperSettingTask;
import wallpaper.videolive.customs.DisableTouchView;
import wallpaper.videolive.listener.CallBack;
import wallpaper.videolive.models.Image;
import wallpaper.videolive.models.ResultUpdate;
import wallpaper.videolive.services.AppClient;

import static wallpaper.videolive.utils.PermissionUtils.REQUEST_WRITE_STORAGE;


public class PreviewImageDialog extends DialogFragment {
    @BindView(R.id.imgPreview)
    PhotoView imgPreview;
    @BindView(R.id.progressLoading)
    ProgressBar progressLoading;
    @BindView(R.id.loadingSetWallpaper)
    ProgressBar loadingSetWallpaper;

    @BindView(R.id.disableTouchView)
    public DisableTouchView disableTouchView;
    @BindView(R.id.setWallpaper)
    Button setWallpaper;
    @BindView(R.id.imgBack)
    ImageView imgBack;
    @BindView(R.id.imgDownload)
    ImageView imgDownload;
    private Image image;
    private int position;
    WallpaperManager wallpaperManager;
    private Bitmap bitmap;
    private CallBack callBack;

    public static PreviewImageDialog newInstance(Image image, int position, CallBack callBack) {
        PreviewImageDialog previewImageDialog = new PreviewImageDialog();
        previewImageDialog.image = image;
        previewImageDialog.position = position;
        previewImageDialog.callBack = callBack;
        return previewImageDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallpaperManager = WallpaperManager.getInstance(getContext());
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Preview_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setWindowAnimations(R.style.Preview_Dialog);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.preview_image_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressLoading.setVisibility(View.GONE);
                loadingSetWallpaper.setVisibility(View.VISIBLE);
                new WallPaperSettingTask(PreviewImageDialog.this, bitmap).execute();

            }
        });
        disableTouchView.setVisibility(View.VISIBLE);
        progressLoading.setVisibility(View.VISIBLE);
        loadingSetWallpaper.setVisibility(View.GONE);
        progressLoading.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FC3434"), PorterDuff.Mode.SRC_IN);
        if (image.getUrl() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            Glide.with(getContext()).asBitmap().load(image.getUrl()).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    disableTouchView.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    disableTouchView.setVisibility(View.GONE);
                    bitmap = resource;
                    return false;
                }
            }).into(imgPreview);
        }
    }


    @OnClick(R.id.imgBack)
    public void onBackClick() {
        dismiss();
    }

    @OnClick(R.id.imgDownload)
    public void onDownloadClick() {
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        } else {
            if (bitmap == null) {
                showDialogResult(getString(R.string.download_fail_msg));
            } else {
                downloadImage(bitmap);
            }
        }

    }

    private void downloadImage(Bitmap finalBitmap) {
        updateDownload();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/LiveWallPaperApp");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fileName = "wallpaper" + n + ".jpg";
        File file = new File(myDir, fileName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            showDialogResult(getString(R.string.download_success, file.getAbsolutePath()));

        } catch (Exception e) {
            e.printStackTrace();
            showDialogResult(getString(R.string.download_fail_msg));
        }
    }

    private void updateDownload() {
        if (image != null) {
            int downCount = Integer.valueOf(image.getDownload());
            image.setDownload((downCount + 1) + "");
            if (callBack != null) callBack.onUpdateDownloadSuccess(position);
            AppClient.getAPIService().updateDownload(image.getId()).enqueue(new Callback<ResultUpdate>() {
                @Override
                public void onResponse(Call<ResultUpdate> call, Response<ResultUpdate> response) {

                }

                @Override
                public void onFailure(Call<ResultUpdate> call, Throwable t) {

                }
            });
        }
    }

    private boolean hasPermission(String permission) {
        PackageManager pm = getContext().getPackageManager();
        int hasPerm = pm.checkPermission(
                permission,
                getContext().getPackageName());
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String[] permission, int requestCode) {
        requestPermissions(permission, requestCode);
    }

    private void showDialogResult(String msg) {
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showDialogResult(getString(R.string.granted_permission_msg));
            else {
                showDialogResult(getString(R.string.not_granted_write_storage));
            }

        }

    }

}
