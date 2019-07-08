package wallpaper.videolive.models;

/**
 * Created by linh on 9/19/2017.
 */

public class Song {
    public Song() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String name;
    private int id;

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    private boolean isPlay;


    public Song(String name, int uri, boolean isPlay) {
        this.name = name;
        this.id = uri;
        this.isPlay = isPlay;
    }
}
