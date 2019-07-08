package wallpaper.videolive.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import wallpaper.videolive.R;
import wallpaper.videolive.utils.PrefUtils;
import wallpaper.videolive.views.activities.HomeActivity;

import static wallpaper.videolive.utils.PrefUtils.KEY_PREF_MUTE;
import static wallpaper.videolive.utils.PrefUtils.KEY_PREF_REN_MODE;
import static wallpaper.videolive.utils.PrefUtils.RendererMode.CLASSIC;

public class SettingFragment extends Fragment {

    @BindView(R.id.spinerMode)
    Spinner spinnerMode;

    @BindView(R.id.scSound)
    SwitchCompat scSound;

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setTitleBar(getString(R.string.title_wp_settings));
            ((HomeActivity) getActivity()).enablePullToRefresh(false);
            ((HomeActivity) getActivity()).showProgressLoading(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String mode = PrefUtils.getPreferences(getContext(), KEY_PREF_REN_MODE, CLASSIC);
        setUpSpinner(mode);
        boolean sound = PrefUtils.getPreferences(getContext(), KEY_PREF_MUTE);
        scSound.setChecked(sound);
        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String mode = (String) spinnerMode.getItemAtPosition(i);
                PrefUtils.savePreferences(getContext(), KEY_PREF_REN_MODE, mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        scSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PrefUtils.savePreferences(getContext(), KEY_PREF_MUTE, b);
            }
        });
    }

    private void setUpSpinner(String mode) {
        spinnerMode.setSelection(0);
        for (int i = 0; i < spinnerMode.getCount(); i++) {
            String modeSelected = (String) spinnerMode.getItemAtPosition(i);
            if (mode.equalsIgnoreCase(modeSelected)) {
                spinnerMode.setSelection(i);
                break;
            }
        }
    }
}
