package wallpaper.videolive.models;

import com.google.gson.annotations.SerializedName;

public class ResultUpdate {
    @SerializedName("result")
    private String result;

    public String getResult() {
        return result;
    }
}
