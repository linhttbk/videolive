package wallpaper.videolive.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wallpaper.videolive.R;
import wallpaper.videolive.adapters.CategoryAdapter;
import wallpaper.videolive.customs.VerticalSpacesItemDecoration;
import wallpaper.videolive.listener.OnItemClick;
import wallpaper.videolive.models.Category;
import wallpaper.videolive.services.AppClient;
import wallpaper.videolive.views.activities.HomeActivity;

public class PictureHomeFragment extends Fragment {
    @BindView(R.id.rcvList)
    RecyclerView mRecycleView;
    CategoryAdapter categoryAdapter = new CategoryAdapter();

    public static PictureHomeFragment newInstance() {
        Bundle args = new Bundle();
        PictureHomeFragment fragment = new PictureHomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setTitleBar(getContext().getString(R.string.title_tab_pic_wp));
            ((HomeActivity) getActivity()).enablePullToRefresh(true);

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picture_home, container, false);
        ButterKnife.bind(this, view);
        setUpRecycleView();
        setUpRefresh();
        Log.e("onCreateView: ","fc" );
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCategory();
    }

    private void setUpRecycleView() {
        mRecycleView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecycleView.addItemDecoration(new VerticalSpacesItemDecoration(10, true));
        mRecycleView.setAdapter(categoryAdapter);
        categoryAdapter.setOnItemClick(new OnItemClick() {
            @Override
            public void onItemClick(int position) {
                Category category = categoryAdapter.getItem(position);
                if(category==null) return;
                String catId = category.getId();
                String catTitle = category.getTitle();
                ((HomeActivity) getActivity()).switchFragment(AllImageFragment.newInstance(catId,catTitle),catTitle);
            }
        });
    }

    private void setUpRefresh() {
        ((HomeActivity) getActivity()).setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((HomeActivity) getActivity()).endRefresh();
                getCategory();
            }
        });
    }

    private void getCategory() {
        if (getActivity() != null) ((HomeActivity) getActivity()).showProgressLoading(true);
        AppClient.getAPIService().getAllCategory().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (getActivity() != null) {
                    ((HomeActivity) getActivity()).showProgressLoading(false);
                    ((HomeActivity) getActivity()).endRefresh();

                }
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.replace(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                if (getActivity() != null) {
                    ((HomeActivity) getActivity()).showProgressLoading(false);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
