package wallpaper.videolive.async;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import wallpaper.videolive.utils.BitmapUtils;
import wallpaper.videolive.views.fragments.PreviewImageDialog;

public class WallPaperSettingTask extends AsyncTask<String, Void, Void> {
    WeakReference<PreviewImageDialog> context;
    WeakReference<WallpaperManager> wallpaperManager;
    WeakReference<Bitmap> bitmap;

    public WallPaperSettingTask(PreviewImageDialog context, Bitmap bitmap) {
        this.context = new WeakReference<>(context);
        wallpaperManager = new WeakReference<>(WallpaperManager.getInstance(context.getContext()));
        this.bitmap = new WeakReference<>(bitmap);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context == null || context.get() == null || context.get().isHidden())
            return;
        context.get().disableTouchView.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            WallpaperManager wallpaperManager = getInstance(this.wallpaperManager);
            if (wallpaperManager != null && bitmap != null && bitmap.get() != null && !bitmap.get().isRecycled()) {
                Bitmap result = BitmapUtils.getScaledDownBitmap(bitmap.get(), Math.max(wallpaperManager.getDesiredMinimumWidth(), wallpaperManager.getDesiredMinimumHeight()), false);
                wallpaperManager.setBitmap(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        super.onPostExecute(voids);
        if (getInstance(context) != null && !getInstance(context).isHidden()) {
            context.get().dismiss();
            context.get().disableTouchView.setVisibility(View.GONE);
        }

    }

    public <T> T getInstance(WeakReference<T> weakReference) {
        if (weakReference == null || weakReference.get() == null)
            return null;
        return weakReference.get();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        cancelRequest(context);
        cancelRequest(wallpaperManager);
        if (getInstance(bitmap) != null && !getInstance(bitmap).isRecycled()) {
            bitmap.get().recycle();
        }
        cancelRequest(bitmap);


    }

    private <T> void cancelRequest(WeakReference<T> weakReference) {
        if (weakReference != null && weakReference.get() != null) {
            weakReference.clear();
        }
    }
}
