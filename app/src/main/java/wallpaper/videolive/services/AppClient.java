package wallpaper.videolive.services;

public class AppClient {
    private AppClient() {}

    public static APIService getAPIService() {
        return RetrofitClient.getClient().create(APIService.class);
    }
}
