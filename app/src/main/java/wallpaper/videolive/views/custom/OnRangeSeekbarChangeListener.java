package wallpaper.videolive.views.custom;

public interface OnRangeSeekbarChangeListener {
    void valueChanged(Number minValue, Number maxValue);
    void valueMinChanged(Number minValue, Number maxValue);
    void valueMaxChanged(Number minValue, Number maxValue);
}
