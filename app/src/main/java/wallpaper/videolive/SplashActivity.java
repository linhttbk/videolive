package wallpaper.videolive;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallpaper.videolive.models.ResultUpdate;
import wallpaper.videolive.services.AppClient;
import wallpaper.videolive.views.activities.HomeActivity;

public class SplashActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_splash);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ads_id_inter1));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            int verCode = pInfo.versionCode;
            Log.e("onCreate: ", verCode + " ");
            AppClient.getAPIService().checkUpdateApp(verCode).enqueue(new Callback<ResultUpdate>() {
                @Override
                public void onResponse(Call<ResultUpdate> call, Response<ResultUpdate> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getResult().equalsIgnoreCase("1")) {
                            showDialog("a new version was available, download it from the store!");
                        } else {
                            splash(1500);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResultUpdate> call, Throwable t) {
                    splash(3000);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void splash(int timeOut) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSplash();
            }
        }, timeOut);
    }

    private void stopSplash() {
        final Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdClosed() {
                    if (intent != null) {
                        startActivity(intent);
                        finish();
                    }
                    ;
                }
            });

        } else {
            if (intent != null) {
                startActivity(intent);
                finish();
            }
            ;
        }
    }

    private void showDialog(String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle(R.string.title_dialog_update);
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        goToInstallApp("vlar.videolive");
                        finish();

                    }
                });
        builder1.setNegativeButton(
                android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        stopSplash();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void goToInstallApp(String appId) {
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
        startActivity(viewIntent);
    }
}
