package wallpaper.videolive.models;


import java.util.Date;

public class Video {
    private String title;
    private String path;
    private String size;
    private String date;

    public Video(String title, String path, String size, String date) {
        this.title = title;
        this.path = path;
        this.size = size;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
