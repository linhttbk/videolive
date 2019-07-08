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
import wallpaper.videolive.models.Image;
import wallpaper.videolive.utils.AppUtils;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<Image> mItems = new ArrayList<>();
    private OnItemClick onItemClick;

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvView.setText(mItems.get(position).getViewCount());
        holder.tvDownLoad.setText(mItems.get(position).getDownload());
        RequestOptions requestOptions = new RequestOptions();
        int size =  AppUtils.getWidthScreen(holder.itemView.getContext())/2 - 2*2;
        requestOptions = requestOptions.placeholder(R.drawable.placeholder).override(size,size);
        Glide.with(holder.itemView.getContext()).load(mItems.get(position).getUrl()).apply(requestOptions).thumbnail(0.5f).into(holder.imgThumb);

    }
    public void insert(List<Image> items){
        if(items==null||items.isEmpty()) return;
        this.mItems.addAll(items);
        notifyItemRangeInserted(Math.max(0, this.mItems.size() - items.size()), this.mItems.size());
    }

    public void replace(List<Image> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        this.mItems.clear();
        this.mItems.addAll(items);
        notifyDataSetChanged();
    }

    public Image getItem(int position){
        if(position<0||position >= mItems.size())
            return null;
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        ImageView imgThumb;
        @BindView(R.id.tvView)
        TextView tvView;
        @BindView(R.id.tvDownLoad)
        TextView tvDownLoad;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imgThumb.getLayoutParams();
            int width = AppUtils.getWidthScreen(itemView.getContext());
            layoutParams.height = width / 2 - 2 * 2;
            imgThumb.setLayoutParams(layoutParams);
        }
        @OnClick(R.id.imgThumb)
        public void onImageThumbClick(){
            if(onItemClick!=null)onItemClick.onItemClick(getAdapterPosition());
        }
    }
}
