package wallpaper.videolive.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallpaper.videolive.R;
import wallpaper.videolive.models.Video;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private List<Video> mItems = new ArrayList();
    private Context context;
    private OnItemClickListener onItemClickListener;

    public VideoAdapter(Context context) {
        this.context = context;
    }

    public void replace(List<Video> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        this.mItems.clear();
        this.mItems.addAll(items);
        notifyDataSetChanged();
    }

    public Video getItem(int position) {
        if (mItems != null && position >= 0 && position < mItems.size())
            return mItems.get(position);
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = mItems.get(position);
        Glide
                .with(context)
                .asBitmap()
                .load(Uri.fromFile(new File(video.getPath())))
                .into(holder.imgThumb);
        holder.tvTitle.setText(video.getTitle());
        holder.tvSize.setText(video.getSize() + " | " + video.getDate());

    }

    @Override
    public int getItemCount() {
        if (mItems != null && !mItems.isEmpty()) return mItems.size();
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        ImageView imgThumb;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvSize)
        TextView tvSize;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.root)
        public void OnVideoClick() {
            if (onItemClickListener != null) onItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
