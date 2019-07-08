package wallpaper.videolive.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallpaper.videolive.R;
import wallpaper.videolive.models.Song;
import wallpaper.videolive.utils.RingtoneUtils;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.ViewHolder> {
    private static final String POPUP_CONSTANT = "mPopup";
    private static final String POPUP_FORCE_SHOW_ICON = "setForceShowIcon";
    private List<Song> mItems = new ArrayList();
    private OnItemClick onItemClick;
    private int selectedIndex = -1;

    public int getSelectSetIndex() {
        return selectSetIndex;
    }

    private int selectSetIndex = -1;


    public void replace(List<Song> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        this.mItems.clear();
        this.mItems.addAll(items);
        notifyDataSetChanged();
    }

    public Song getItem(int position) {
        Log.e("getItem: ", position + " " + mItems.size());
        if (mItems != null && position >= 0 && position < mItems.size())
            return mItems.get(position);
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_ringtone, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mItems.get(position).setPlay(position == selectedIndex);
        holder.tvTitle.setText(mItems.get(position).getName());
        holder.imvPlay.setImageResource(mItems.get(position).isPlay() ? R.drawable.ic_pause : R.drawable.ic_play_game);
        holder.root.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(getColorByPosition(position)));
    }

    @Override
    public int getItemCount() {
        if (mItems != null && !mItems.isEmpty()) return mItems.size();
        return 0;
    }

    private int getColorByPosition(int position) {
        switch (position % 5) {
            case 0:
                return R.color.index_1;
            case 1:
                return R.color.index_2;
            case 2:
                return R.color.index_3;
            case 3:
                return R.color.index_4;
            case 4:
                return R.color.index_5;
        }
        return R.color.index_1;
    }


    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root_view)
        CardView root;

        @BindView(R.id.imvPlay)
        ImageView imvPlay;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.imvMore)
        ImageView imvMore;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        @OnClick(R.id.imvPlay)
        public void onImagePlayClick() {
            if (getItem(getAdapterPosition()).isPlay()) selectedIndex = -1;
            else
                selectedIndex = getAdapterPosition();
            if (onItemClick != null) onItemClick.onItemClick(getAdapterPosition());
            notifyDataSetChanged();
        }

        @OnClick(R.id.imvMore)
        public void onImageMoreClick() {
            selectSetIndex = getAdapterPosition();
            onItemClick.onMoreClick(getAdapterPosition(), imvMore);
        }
    }

    public void resetPlayer() {
        selectedIndex = -1;
    }


    public interface OnItemClick {
        void onItemClick(int position);

        void onMoreClick(int position, View view);
    }
}
