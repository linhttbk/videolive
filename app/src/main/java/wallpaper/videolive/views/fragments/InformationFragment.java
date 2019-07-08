package wallpaper.videolive.views.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallpaper.videolive.R;
import wallpaper.videolive.views.activities.HomeActivity;

public class InformationFragment extends Fragment {
    @BindView(R.id.tvDesc)
    TextView tvDesc;
    @BindView(R.id.tvDescWeb)
    TextView tvDescWeb;
    @BindView(R.id.tvDescPolicy)
    TextView tvDescPolicy;


    public static InformationFragment newInstance() {
        InformationFragment fragment = new InformationFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((HomeActivity) getActivity()).setVisibleActionSetWallPaper(View.GONE);
            ((HomeActivity) getActivity()).setTitleBar(getString(R.string.title_infor_wp));
            ((HomeActivity) getActivity()).enablePullToRefresh(false);
            ((HomeActivity) getActivity()).showProgressLoading(false);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_infor, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDesc.setText(Html.fromHtml(getString(R.string.desc_app)));
        //  tvDescWeb.setText(Html.fromHtml(getString(R.string.desc_web)));
        setTextViewHTML(tvDescWeb, getString(R.string.desc_web));
        setTextViewHTML(tvDescPolicy, getString(R.string.desc_policy));

    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(span.getURL()));
                startActivity(viewIntent);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    protected void setTextViewHTML(TextView text, String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @OnClick(R.id.rl1)
    public void onAppFCClick() {
        goToInstallApp("com.flashcard.linh.flashcard");
    }

    @OnClick(R.id.rl2)
    public void onAppYTClick() {
        goToInstallApp("com.lttube.application");
    }

    ;

    @OnClick(R.id.rl3)
    public void onAppSimClick() {
        goToInstallApp("com.linh.sim10so");
    }

    private void goToInstallApp(String appId) {
        Intent viewIntent =
                new Intent("android.intent.action.VIEW",
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
        startActivity(viewIntent);
    }
}
