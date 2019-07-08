package wallpaper.videolive.views.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallpaper.videolive.Preview;
import wallpaper.videolive.R;
import wallpaper.videolive.customs.DisableTouchView;
import wallpaper.videolive.listener.OnTrimVideoCallBack;
import wallpaper.videolive.utils.AppUtils;
import wallpaper.videolive.utils.PrefUtils;
import wallpaper.videolive.views.fragments.InformationFragment;
import wallpaper.videolive.views.fragments.PictureHomeFragment;
import wallpaper.videolive.views.fragments.RingtoneHomeFragment;
import wallpaper.videolive.views.fragments.SettingFragment;
import wallpaper.videolive.views.fragments.TrimVideoFragment;
import wallpaper.videolive.views.fragments.VideoHomeFragment;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.imgSetWallPaper)
    ImageView imgSetWallPaper;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.disableTouchView)
    DisableTouchView disableTouchView;
    @BindView(R.id.loadingView)
    ProgressBar loadingView;
    @BindView(R.id.swrBase)
    SwipeRefreshLayout mRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener;
    ActionBarDrawerToggle toggle;

    public void setCallBack(OnTrimVideoCallBack callBack) {
        this.callBack = callBack;
    }

    OnTrimVideoCallBack callBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        initView();


    }

    private void initView() {
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fm = getSupportFragmentManager();
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                    return;
                } else if (fm.getBackStackEntryCount() == 0 && !drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.openDrawer(GravityCompat.START);
                } else {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.setDrawerIndicatorEnabled(true);
                    onBackPressed();
                }
            }
        });
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, VideoHomeFragment.newInstance());
        ft.commit();
        setupRefreshLayout();

    }

    @OnClick(R.id.imgSetWallPaper)
    protected void setWallPaperClick() {
        Log.e("onStartTrim1: ", "Hello");
        if (callBack != null) callBack.onStartTrim();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.e("onActivityResult: ", "oke");
            if (requestCode == 11) {
                Uri selectedImageUri = data.getData();
                PrefUtils.savePreferences(this, PrefUtils.KEY_PREF_URI, selectedImageUri.toString());

                Intent intent = new Intent(HomeActivity.this,
                        Preview.class);
                startActivity(intent);
                finish();

            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //  getMenuInflater().inflate(R.menu.home, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.isChecked()) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        int id = item.getItemId();
        if (id == R.id.nav_videowp) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ft.replace(R.id.container, VideoHomeFragment.newInstance());
                    ft.commit();
                    drawer.closeDrawer(GravityCompat.START);
                }
            });
        } else if (id == R.id.nav_picturewp) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ft.replace(R.id.container, PictureHomeFragment.newInstance());
                    ft.commit();
                    drawer.closeDrawer(GravityCompat.START);
                }
            });

        } else if (id == R.id.nav_ringtone) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ft.replace(R.id.container, RingtoneHomeFragment.newInstance());
                    ft.commit();
                    drawer.closeDrawer(GravityCompat.START);
                }
            });

        } else if (id == R.id.nav_setting) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ft.replace(R.id.container, SettingFragment.newInstance());
                    ft.commit();
                    drawer.closeDrawer(GravityCompat.START);
                }
            });


        } else if (id == R.id.nav_send) {
            drawer.closeDrawer(GravityCompat.START);
            AppUtils.sendFeedBack(this);
        }else if(id == R.id.nav_infor){
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ft.replace(R.id.container, InformationFragment.newInstance());
                    ft.commit();
                    drawer.closeDrawer(GravityCompat.START);
                }
            });
        }


        return true;
    }

    public void switchFragment(Fragment target, String title) {
        toolbar.setTitle(title);
        if (target instanceof TrimVideoFragment) this.setCallBack((TrimVideoFragment) target);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.right_in, R.anim.left_out, R.anim.left_in, R.anim.right_out);
        ft.replace(R.id.container, target, target.getClass().getSimpleName());
        ft.addToBackStack(target.getClass().getSimpleName());
        ft.commit();

    }

    public void setVisibleActionSetWallPaper(int state) {
        imgSetWallPaper.setVisibility(state);
    }

    public void setTitleBar(String title) {
        tvTitle.setText(title);
    }

    public void setShowBackIconToolbar() {
        toggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    }

    public void setShowIndicatorIconToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

    }

    public void showProgressLoading(boolean isShow) {
        try {
            disableTouchView.setVisibility(isShow ? View.VISIBLE : View.GONE);
            loadingView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        } catch (Exception ignored) {
        }

    }

    private void setupRefreshLayout() {
        mRefreshLayout.setEnabled(false);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRefreshListener != null) {
                    mRefreshListener.onRefresh();
                }
            }
        });

    }

    public void enablePullToRefresh(boolean isEnable) {
        mRefreshLayout.setEnabled(isEnable);
    }

    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener mRefreshListener) {
        this.mRefreshListener = mRefreshListener;
    }

    public void endRefresh() {
        mRefreshLayout.setRefreshing(false);
    }
}
