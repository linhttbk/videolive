package wallpaper.videolive.customs;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalSpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private boolean isHorizontalOffset = false;

    public VerticalSpacesItemDecoration(int space) {
        this.space = space;
    }

    public VerticalSpacesItemDecoration(int space, boolean isHorizontalOffset) {
        this.space = space;
        this.isHorizontalOffset = isHorizontalOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        if (isHorizontalOffset) {
            outRect.left = space;
            outRect.right = space;
        }
        outRect.bottom = space;

        int currentPos = parent.getChildLayoutPosition(view);
        // Add top margin only for the first item to avoid double space between items
        if (currentPos == 0&&!isHorizontalOffset) {
            outRect.top = space;
        } else {
            outRect.top = 0;
        }

    }
}