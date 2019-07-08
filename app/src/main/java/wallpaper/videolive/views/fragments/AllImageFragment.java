package wallpaper.videolive.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallpaper.videolive.R;
import wallpaper.videolive.adapters.ImageAdapter;
import wallpaper.videolive.customs.VerticalSpacesItemDecoration;
import wallpaper.videolive.listener.CallBack;
import wallpaper.videolive.listener.EndlessRecyclerViewScrollListener;
import wallpaper.videolive.listener.OnItemClick;
import wallpaper.videolive.models.Image;
import wallpaper.videolive.models.ResultUpdate;
import wallpaper.videolive.services.AppClient;
import wallpaper.videolive.views.activities.HomeActivity;

public class AllImageFragment extends Fragment implements OnItemClick, CallBack {
    private String cat = "";
    private String catTitle = "";
    @BindView(R.id.rcvList)
    RecyclerView mRecycleView;
    @BindView(R.id.adView)
    AdView adView;
    ImageAdapter adapter = new ImageAdapter();
    EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    WeakReference<Call<List<Image>>> service;

    public static AllImageFragment newInstance(String cat, String catTitle) {
        Bundle bundle = new Bundle();
        bundle.putString("cat", cat);
        bundle.putString("catTitle", catTitle);
        AllImageFragment allImageFragment = new AllImageFragment();
        allImageFragment.setArguments(bundle);
        return allImageFragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setTitleBar(catTitle);
            ((HomeActivity) getActivity()).enablePullToRefresh(true);
            ((HomeActivity) getActivity()).setShowBackIconToolbar();

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cat = getArguments().getString("cat", "");
            catTitle = getArguments().getString("catTitle", "");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_all, container, false);
        ButterKnife.bind(this, view);
        setUpRecycleView();
        setUpRefresh();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAds();
        getAllImageByCategory();

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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                loadMoreImage(String.valueOf(page));
            }
        };
        mRecycleView.setLayoutManager(gridLayoutManager);
        mRecycleView.addItemDecoration(new VerticalSpacesItemDecoration(2, true));
        adapter.setOnItemClick(this);
        mRecycleView.setAdapter(adapter);
        mRecycleView.addOnScrollListener(endlessRecyclerViewScrollListener);
    }

    private void setUpRefresh() {
        ((HomeActivity) getActivity()).setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((HomeActivity) getActivity()).endRefresh();
                endlessRecyclerViewScrollListener.resetState();
                cancelRequest();
                getAllImageByCategory();
                if (adView.getVisibility() == View.GONE) {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.loadAd(adRequest);
                }
            }
        });
    }

    private void getAllImageByCategory() {
        if (getActivity() != null) ((HomeActivity) getActivity()).showProgressLoading(true);
        if (service == null || service.get() == null || service.get().isExecuted() || service.get().isCanceled()) {
            service = new WeakReference<>(AppClient.getAPIService().getAllImage(cat, "0"));
            service.get().enqueue(new Callback<List<Image>>() {
                @Override
                public void onResponse(Call<List<Image>> call, Response<List<Image>> response) {
                    if (getActivity() != null) {
                        ((HomeActivity) getActivity()).showProgressLoading(false);
                        ((HomeActivity) getActivity()).endRefresh();

                    }
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.replace(response.body());
                    } else {
                        cancelRequest();
                    }
                }

                @Override
                public void onFailure(Call<List<Image>> call, Throwable t) {
                    if (getActivity() != null) {
                        ((HomeActivity) getActivity()).showProgressLoading(false);
                        cancelRequest();
                    }
                }
            });
        }
    }

    private void loadMoreImage(String page) {
        if (service == null || service.get() == null || service.get().isExecuted() || service.get().isCanceled()) {
            service = new WeakReference<>(AppClient.getAPIService().getAllImage(cat, page));
            service.get().enqueue(new Callback<List<Image>>() {
                @Override
                public void onResponse(Call<List<Image>> call, Response<List<Image>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.insert(response.body());
                    } else {
                        cancelRequest();
                    }
                }

                @Override
                public void onFailure(Call<List<Image>> call, Throwable t) {
                    cancelRequest();
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelRequest();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setShowIndicatorIconToolbar();
            ((HomeActivity) getActivity()).showProgressLoading(false);
        }
    }

    @Override
    public void onItemClick(final int position) {

        final Image image = adapter.getItem(position);
        if (image != null) {
            int viewCount = Integer.valueOf(image.getViewCount());
            image.setViewCount((viewCount + 1) + "");
            adapter.notifyItemChanged(position);
            AppClient.getAPIService().updateView(image.getId()).enqueue(new Callback<ResultUpdate>() {
                @Override
                public void onResponse(Call<ResultUpdate> call, Response<ResultUpdate> response) {

                }

                @Override
                public void onFailure(Call<ResultUpdate> call, Throwable t) {

                }
            });
            FragmentManager fragmentManager = getFragmentManager();
            PreviewImageDialog.newInstance(image, position, this).show(fragmentManager, PreviewImageDialog.class.getSimpleName());
        }
    }

    private void cancelRequest() {
        if (service != null && service.get() != null) {
            service.get().cancel();
            service.clear();
            service = null;
        }
    }

    @Override
    public void onUpdateDownloadSuccess(int position) {
        if (adapter != null) adapter.notifyItemChanged(position);
    }
}
