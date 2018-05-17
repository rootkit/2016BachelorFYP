package com.weimanteam.weiman.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/*
 *@Author Huang
 * version 1.0 相册使用的是一次性Intent，其中在Intent中就包含了裁剪的属性。
 * 而相机使用的是先获取完整图片，而后再裁剪。
 * version 1.1 为了兼容Android5.0，相册提取图片修改。
 */
public class TakePhotoUtil {
	/**
	 * Constructs an intent for image cropping. 调用图片剪辑程序 头像用，拍照裁剪
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP", null)
				.setDataAndType(photoUri, "image/*").putExtra("crop", "true")
				.putExtra("aspectX", 1).putExtra("aspectY", 1)
				.putExtra("outputX", 200).putExtra("outputY", 200)
				.putExtra("outputFormat", "JPEG")
				.putExtra("noFaceDetection", true)
				.putExtra("return-data", true);
		return intent;
	}

	/**
	 * 和上面的尺寸不一样,身份证用,拍照裁剪,貌似可以通用，
	 */
	public static Intent getCropImageIntent2(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP", null)
				.setDataAndType(photoUri, "image/*")
				.putExtra("crop", "true")
				.putExtra("aspectX", 7)
				.putExtra("aspectY", 10)
				.putExtra("outputX", 1400)
				.putExtra("outputY", 2000)
				.putExtra("scale", true)
				.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
				.putExtra("noFaceDetection", true)
				.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
				.putExtra("return-data", false);
		return intent;
	}

	// used for cropping picture from android 5.0 gallery
	public static Intent getCropImageIntent2(Uri orgUri, Uri desUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(orgUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 7);
		intent.putExtra("aspectY", 10);
		intent.putExtra("outputX", 1400);
		intent.putExtra("outputY", 2000);
		intent.putExtra("scale", true);
		// 将剪切的图片保存到desUri中
		intent.putExtra(MediaStore.EXTRA_OUTPUT, desUri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		return intent;
	}

	// 相册小图
	@Deprecated
	public static Intent getSmallIntent() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		return intent;
	}

	// 兼容Android5.0，提取图片，大图和小图
	public static Intent getIntentForAndroid5(Uri imageUri) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		return intent;
	}

	// 相册大图
	@Deprecated
	public static Intent getBigIntent(Uri imageUri) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		intent.putExtra("crop", "false");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 2000);
		intent.putExtra("outputY", 2000);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", false); // no face detection
		return intent;
	}

	// 裁剪属性
	public static BitmapFactory.Options getOption(int width) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		int scale = (int) (options.outWidth / (float) width);
		if (scale <= 0)
			scale = 1;
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false;
		return options;
	}

	// 将Bitmap转换成InputStream
	public static InputStream Bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	public static Bitmap decodeUriAsBitmap(Uri uri, Context context) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	// Used for getting picture from android 5.0 gallery
	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	private static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}
}