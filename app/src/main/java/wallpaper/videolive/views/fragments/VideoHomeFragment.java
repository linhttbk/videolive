package wallpaper.videolive.views.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import wallpaper.videolive.Preview;
import wallpaper.videolive.R;
import wallpaper.videolive.adapters.VideoAdapter;
import wallpaper.videolive.customs.VerticalSpacesItemDecoration;
import wallpaper.videolive.listener.OnSetWallPaperCallback;
import wallpaper.videolive.models.Video;
import wallpaper.videolive.utils.StringUtils;
import wallpaper.videolive.views.activities.HomeActivity;

import static wallpaper.videolive.utils.PermissionUtils.REQUEST_READ_STORAGE;

public class VideoHomeFragment extends Fragment implements VideoAdapter.OnItemClickListener, OnSetWallPaperCallback {
    @BindView(R.id.rcvList)
    RecyclerView mRecycleView;
    @BindView(R.id.adView)
    AdView adView;
    VideoAdapter mAdapter;
    Snackbar snackbar;

    public static VideoHomeFragment newInstance() {
        Bundle args = new Bundle();
        VideoHomeFragment fragment = new VideoHomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setTitleBar(getContext().getString(R.string.title_tab_video_wp));
            ((HomeActivity) getActivity()).enablePullToRefresh(false);
            ((HomeActivity) getActivity()).showProgressLoading(false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAds();
        setUpRecycleView();
        if (!hasPermissionReadStorage()) {
            requestPermission();
        } else
            printNamesToLogCat();
    }

    private void loadAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                adView.setVisibility(View.GONE);
            }
        });
    }

    private void setUpRecycleView() {
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new VideoAdapter(getContext());
        mRecycleView.addItemDecoration(new VerticalSpacesItemDecoration(10));
        mAdapter.setOnItemClickListener(this);
        mRecycleView.setAdapter(mAdapter);
    }

    public void printNamesToLogCat() {
        ArrayList<Video> videos = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA};
        Cursor c = getContext().getContentResolver().query(uri, projection, null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                String path = c.getString(0);
                File file = new File(path);
                if (file.isFile()) {

                    String name = path.substring(path.lastIndexOf("/") + 1);
                    String date = StringUtils.formatDate(file.lastModified());
                    String size = StringUtils.formatSize(file.length());
                    videos.add(new Video(name, path, size, date));
                }
            }
            c.close();
            mAdapter.replace(videos);
        }
    }

    private boolean hasPermissionReadStorage() {
        PackageManager pm = getContext().getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                getContext().getPackageName());
        return hasPerm == PackageManager.PERMISSION_GRANTED;
        // do stuff
    }

    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) printNamesToLogCat();
            else {
                snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                        "Please allowed  this permission to can be used",
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Open",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= 23)
                                    requestPermissions(
                                            new String[]{Manifest.permission
                                                    .READ_EXTERNAL_STORAGE},
                                            REQUEST_READ_STORAGE);
                            }
                        }).show();
            }

        }

    }

    @Override
    public void onItemClick(int position) {
        Video video = mAdapter.getItem(position);
        if (video != null && getActivity() != null) {
            ((HomeActivity) getActivity()).switchFragment(TrimVideoFragment.newInstance(video.getPath(), this), getContext().getString(R.string.title_tab_video_wp));

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (snackbar != null && snackbar.isShown()) snackbar.dismiss();
        if (getActivity() != null)
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
    }

    @Override
    public void callBack() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getActivity(), Preview.class));
            }
        });

    }
}
