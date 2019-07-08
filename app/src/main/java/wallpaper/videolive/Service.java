package wallpaper.videolive;

import rajawali.wallpaper.Wallpaper;

public class Service extends Wallpaper {
	private Renderer mRenderer;

	@Override
	public Engine onCreateEngine() {
		mRenderer = new Renderer(this);
		return new WallpaperEngine( getBaseContext(), mRenderer, false);
	}

	@Override
	public void onDestroy() {
		mRenderer.onSurfaceDestroyed();
		super.onDestroy();
	}
}