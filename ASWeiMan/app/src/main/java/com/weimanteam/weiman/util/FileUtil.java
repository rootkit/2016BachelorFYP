package com.weimanteam.weiman.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.Point;

import com.weimanteam.weiman.R;
import com.weimanteam.weiman.bean.Photo;
import com.weimanteam.weiman.config.Global;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class FileUtil {
	private static final String TAG = "util.com.weimanteam.weiman.util.FileUtil";
	private final static int BUFFER_SIZE = 4096;

	/** Create a File for saving an image */
	public static File getOutputMediaFile(Context context) {
		File file = null;
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		try {
			file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					Global.APP_NAME);
			Log.i(TAG, "Successfully created mediaStorageDir: " + file);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error in Creating mediaStorageDir: " + file);
			// 当出现异常时，将存储位置移动到内存的目录下/data/data/packagename/cache
			file = new File(context.getCacheDir(), Global.APP_NAME);
		}

		// Create the storage directory if it does not exist
		if (!file.exists()) {
			if (!file.mkdirs()) {
				// <uses-permission
				// android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
				Log.d(TAG,
						"failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
				return null;
			}
		}

		File mediafile = new File(file.getPath() + File.separator + "IMG_"
				+ "temp.jpg");

		return mediafile;
	}

	/** Create a File for saving an image */
	public static File getOutputMediaFileRandom(Context context) {
		File file = null;
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		try {
			file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					Global.APP_NAME);
			Log.i(TAG, "Successfully created mediaStorageDir: " + file);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error in Creating mediaStorageDir: " + file);
			// 当出现异常时，将存储位置移动到内存的目录下/data/data/packagename/cache
			file = new File(context.getCacheDir(), Global.APP_NAME);
		}

		// Create the storage directory if it does not exist
		if (!file.exists()) {
			if (!file.mkdirs()) {
				// <uses-permission
				// android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
				Log.d(TAG,
						"failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
				return null;
			}
		}
		new DateFormat();
		String name = DateFormat.format("yyyyMMddHHmmss",
				Calendar.getInstance(Locale.CHINA))
				+ ".jpg";

		File mediafile = new File(file.getPath() + File.separator + name);

		return mediafile;
	}
	
	/** Create a File for saving an image */
	public static File getOutputMediaDirectory(Context context) {
		File file = null;
		try {
			file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					Global.APP_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			// 当出现异常时，将存储位置移动到内存的目录下/data/data/packagename/cache
			file = new File(context.getCacheDir(), Global.APP_NAME);
		}
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.d(TAG,	"failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
				return null;
			}
		}
		return file;
	}

	/* write bitmap to storage */
	public static boolean writeBitmap(File f, Bitmap bm) {
		FileOutputStream out = null;
		boolean target = true;
		try {
			out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();

		} catch (FileNotFoundException e) {
			target = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			target = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return target;
	}

	public static ArrayList<Photo> getFileBitmaps(File directory) {
		ArrayList<Photo> bitmaps = new ArrayList<Photo>();
		
		if (directory != null && directory.exists() && directory.isDirectory())
			for (File bitmap : directory.listFiles()) {
				if (bitmap.getName().startsWith("2")) {//取20151223.jpg这类图片
					Photo temp = new Photo();
					temp.setBitmap(getLoacalBitmap(bitmap));
					temp.setName(bitmap.getName());
					temp.setPath(bitmap.getPath());
					
					bitmaps.add(temp);
				}
			}
		
		Collections.sort(bitmaps);
		return bitmaps;
	}
	
	/**
	 * 加载本地图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			return BitmapFactory.decodeStream(fis); // /把流转化为Bitmap图片

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	// 计算2的指数倍压缩比例
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	// 按比例2的指数倍压缩图片
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options); // Bitmap
																	// bitmap =
																	// BitmapFactory.decodeFile(uri.getPath(),
																	// options);
	}

	public static void copyDataFile2LocalDir(Context context) {
		try {
			File dataDir = context.getDir("data", Context.MODE_PRIVATE);

			File f_frontalface = new File(dataDir,
					"haarcascade_frontalface_alt2.xml");
			File f_lefteye = new File(dataDir, "haarcascade_mcs_lefteye.xml");
			File f_righteye = new File(dataDir, "haarcascade_mcs_righteye.xml");

			if (!isDataFileInLocalDir(f_frontalface, f_lefteye, f_righteye)) {
				boolean f1, f2, f3;

				f1 = putDataFileInLocalDir(context,
						R.raw.haarcascade_frontalface_alt2, f_frontalface);
				f2 = putDataFileInLocalDir(context,
						R.raw.haarcascade_mcs_lefteye, f_lefteye);
				f3 = putDataFileInLocalDir(context,
						R.raw.haarcascade_mcs_righteye, f_righteye);

				// if (f1 && f2 && f3) {
				// // tv_info.setText("load cascade file successed");
				// } else {
				// tv_info.setText("load cascade file failed");
				// }
			}
		} catch (IOError e) {
			e.printStackTrace();
		}
	}

	private static boolean isDataFileInLocalDir(File f_frontalface,
			File f_lefteye, File f_righteye) {
		boolean ret = false;
		try {
			ret = f_frontalface.exists() && f_lefteye.exists()
					&& f_righteye.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * put raw data into local DIR /data/data/com.example.asm/app_data/
	 */
	private static boolean putDataFileInLocalDir(Context context, int id, File f) {
		Log.d(TAG, "putDataFileInLocalDir: " + f.toString());
		try {
			InputStream is = context.getResources().openRawResource(id);
			FileOutputStream os = new FileOutputStream(f);
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		Log.d(TAG, "putDataFileInLocalDir: done!");
		return true;
	}

	public static String getAssetsFile(Context context, String filePath,
			String fileName) {
		BufferedReader reader = null;
		StringBuffer stringBuffer = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(context
					.getAssets().open(filePath + File.separator + fileName)));

			// do reading, usually loop until end of file reading
			String mLine;
			while ((mLine = reader.readLine()) != null) {
				stringBuffer.append(mLine);
			}
		} catch (IOException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return stringBuffer.toString();
	}

	public static Bitmap getAssetsImage(Context context, String filePath,
			String fileName) {
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(filePath + File.separator + fileName);
			bitmap = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return bitmap;
	}

	public static ArrayList<ArrayList<String>> getAssetsFileAndParseString(
			Context context, String filePath, String fileName) throws Exception {
		// 一个文件的数据
		ArrayList<ArrayList<String>> pointsList = new ArrayList<ArrayList<String>>();
		// 一个五官的数据(多行)
		ArrayList<String> holdPointList = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(context
					.getAssets().open(filePath + File.separator + fileName)));

			// do reading, usually loop until end of file reading
			String mLine;
			while ((mLine = reader.readLine()) != null) {
				if (mLine.contains("frontSide")) {
					if (holdPointList != null)
						pointsList.add(holdPointList);
					holdPointList = new ArrayList<String>();
				}
				if (mLine.contains("\"attr\":\"d\"")
						|| mLine.contains("\"path\":\"") || mLine.contains("\"style\":[{\"attr\"")) {
					int start = mLine.indexOf('M');
					if (start == -1) continue;//临时处理下
					int end = mLine.lastIndexOf('"');
					String subLine = mLine.substring(start, end);
					holdPointList.add(subLine);
				}
			}
			// 将最后一个，就是第eye29的SVG图像，加入到列中
			if (holdPointList != null)
				pointsList.add(holdPointList);
		} catch (IOException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		return pointsList;
	}
}