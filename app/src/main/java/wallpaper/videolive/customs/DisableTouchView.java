package wallpaper.videolive.customs;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class DisableTouchView extends RelativeLayout {
    public DisableTouchView(Context context) {
        super(context);
    }

    public DisableTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisableTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DisableTouchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e( "onTouchEvent: ","hello" );
        return true;
    }
}
