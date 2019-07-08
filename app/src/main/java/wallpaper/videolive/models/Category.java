package wallpaper.videolive.models;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("id")
    private String id;
    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;


    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
}
