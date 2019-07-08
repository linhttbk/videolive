package wallpaper.videolive.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import wallpaper.videolive.models.Category;
import wallpaper.videolive.models.Image;
import wallpaper.videolive.models.ResultUpdate;

public interface APIService {
    @GET("getCategory.php")
    Call<List<Category>> getAllCategory();

    @GET("getimage.php")
    Call<List<Image>> getAllImage(@Query("cat") String catId, @Query("page") String page);

    @POST("updateview.php")
    @FormUrlEncoded
    Call<ResultUpdate> updateView(@Field("id") String imgId);

    @POST("updatedownload.php")
    @FormUrlEncoded
    Call<ResultUpdate> updateDownload(@Field("id") String imgId);

    @GET("koreancheck.php")
    Call<ResultUpdate> checkUpdateApp(@Query("code") int verCode);
}
