package com.weimanteam.weiman.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.view.View;
import android.webkit.WebView;

public class SnapUtil {
    /**
     * 截取webView可视区域的截图
     * @param webView;
     * @return
     */
	public static Bitmap captureWebViewVisibleSize(WebView webview){
		Bitmap returnedBitmap = null;
	    webview.setDrawingCacheEnabled(true);
	    returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
	    webview.setDrawingCacheEnabled(false);
	    return returnedBitmap;
	}
	/**
	 * 截取webView快照(webView加载的整个内容的大小)
	 * @param webView
	 * @return
	 */
	public static Bitmap captureWebView(WebView webView){
		Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		webView.draw(canvas);
		
//		Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(),snapShot.getHeight(), Bitmap.Config.ARGB_8888);
//		Canvas canvas = new Canvas(bmp);
//		snapShot.draw(canvas);
		return bitmap;
	}
	
	/**
	 * 截屏
	 * @param context
	 * @return
	 */
    public static Bitmap captureScreen(Activity context){
      View cv = context.getWindow().getDecorView();
      Bitmap bmp = Bitmap.createBitmap(cv.getWidth(), cv.getHeight(),Config.ARGB_8888);
      Canvas canvas = new Canvas(bmp);
      cv.draw(canvas);
      return bmp;
    }
}