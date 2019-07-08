package wallpaper.videolive.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wallpaper.videolive.R;
import wallpaper.videolive.listener.OnItemClick;
import wallpaper.videolive.models.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    List<Category> mItems = new ArrayList<>();

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    private OnItemClick onItemClick;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(mItems.get(position).getTitle());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.placeholder(R.drawable.placeholder).transforms(new CenterCrop(), new RoundedCorners(16));
        Glide.with(holder.itemView.getContext()).load(mItems.get(position).getUrl()).apply(requestOptions).thumbnail(0.5f).into(holder.thumbnail);

    }

    public void replace(List<Category> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        this.mItems.clear();
        this.mItems.addAll(items);
        notifyDataSetChanged();
    }

    public Category getItem(int position) {
        if (position < 0 || position > mItems.size())
            return null;
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.title)
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) thumbnail.getLayoutParams();
            layoutParams.height = layoutParams.width;
            DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            layoutParams.height = width / 2 - 2 * 10;
            thumbnail.setLayoutParams(layoutParams);
        }

        @OnClick(R.id.thumbnail)
        public void onThumbnailClick() {
            if (onItemClick != null) onItemClick.onItemClick(getAdapterPosition());
        }
    }
}
