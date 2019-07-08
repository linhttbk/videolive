package wallpaper.videolive.models;

import com.google.gson.annotations.SerializedName;

public class Image  {
    @SerializedName("id")
    private String id;
    @SerializedName("url")
    private String url;
    @SerializedName("cat_id")
    private String catId;
    @SerializedName("view")
    private String viewCount;
    @SerializedName("download")
    private String download;

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getCatId() {
        return catId;
    }

    public String getViewCount() {
        return viewCount;
    }

    public String getDownload() {
        return download;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public void setDownload(String download) {
        this.download = download;
    }
}
