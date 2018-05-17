package com.weimanteam.weiman.activity;

import java.util.ArrayList;
import java.util.Random;

import com.weimanteam.weiman.util.Const;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AdjustASMPointActivity extends Activity {
	private final String TAG = getClass().getName();;
	private ArrayList<Integer> ASMPoints;
	private DrawingWithBezier surfaceView;
	private int headPointX = 0;
	private int headPointY = 0;
	double hairRatioOfWholeFace = 0.25;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		ASMPoints = intent
				.getIntegerArrayListExtra(Const.ADJUST_ASM_POINT_FROM);
		surfaceView = new DrawingWithBezier(this);
		setContentView(surfaceView);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed");
		reverseScalePonits(surfaceView.scale);
		Intent mIntent = new Intent();
		mIntent.putIntegerArrayListExtra(Const.ADJUST_ASM_POINT_BACK, ASMPoints);

		setResult(Activity.RESULT_OK, mIntent);
		super.onBackPressed();
	}

	/*
	 * 将传入的坐标点乘以缩放值，然后显示在SuifaceView中
	 */
	private void scalePonitsToShow(float scale) {
		for (int i = 0; i < ASMPoints.size(); i++) {
			ASMPoints.set(i, (int) (ASMPoints.get(i) * scale));
		}
		
		//预设头顶的坐标
		headPointY = (int) (ASMPoints.get(14 * 2 + 1) - (ASMPoints.get(6 * 2 + 1) - ASMPoints.get(14 * 2 + 1)) * hairRatioOfWholeFace);
		headPointX = ASMPoints.get(14 * 2);
		ASMPoints.add(headPointX);
		ASMPoints.add(headPointY);
	}
	
	/*
	 * 将经过缩放的坐标点除以缩放值，用于放回上一个界面
	 */
	private void reverseScalePonits(float scale) {
		for (int i = 0; i < ASMPoints.size(); i++) {
			ASMPoints.set(i, (int) (ASMPoints.get(i) / scale));
		}
		
	}
	
	
	public class DrawingWithBezier extends SurfaceView implements
			SurfaceHolder.Callback {
		private Context mContex;
		private int mX;
		private int mY;
		private SurfaceHolder sfh;
		private Canvas canvas;
		private Matrix matrix;
		private float scale = 1;

		private final Paint mGesturePaint = new Paint();
		private int nearest = 0;
		private final float threshold = 2000;

		private boolean isDrawing;

		public DrawingWithBezier(Context context) {
			super(context);
			mContex = context;
			sfh = this.getHolder();
			sfh.addCallback(this);
			mGesturePaint.setAntiAlias(true);
			mGesturePaint.setStyle(Style.FILL);
			mGesturePaint.setStrokeWidth(5);
			mGesturePaint.setColor(Color.WHITE);

		}

		private void drawCanvas() {
			try {
				canvas = sfh.lockCanvas();
				canvas.drawColor(Color.RED);
				canvas.drawBitmap(CameraUIActivity.faceBitmap, matrix, null);
				for (int i = 0; i < ASMPoints.size(); i = i + 2) {
					canvas.drawCircle(ASMPoints.get(i), ASMPoints.get(i + 1),
							20, mGesturePaint);
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (canvas != null)
					sfh.unlockCanvasAndPost(canvas);
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				float x = event.getX();
				float y = event.getY();
				isDrawing = findNearest(x, y);
				mX = (int) x;
				mY = (int) y;

				return true;
			case MotionEvent.ACTION_MOVE:
				if (isDrawing) {
					x = event.getX();
					y = event.getY();

					mX = (int) x;
					mY = (int) y;
					ASMPoints.set(nearest, mX);
					ASMPoints.set(nearest + 1, mY);

					drawCanvas();
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isDrawing) {
					touchUp(event);
					invalidate();
					return true;
				}
				break;
			}
			return super.onTouchEvent(event);
		}

		private void touchUp(MotionEvent event) {
			isDrawing = false;
		}

		private boolean findNearest(float x, float y) {
			float minLength = Integer.MAX_VALUE;
			for (int i = 0; i < ASMPoints.size(); i = i + 2) {
				float temp = (x - ASMPoints.get(i)) * (x - ASMPoints.get(i))
						+ (y - ASMPoints.get(i + 1))
						* (y - ASMPoints.get(i + 1));
				if (temp < minLength) {
					minLength = temp;
					nearest = i;
				}
			}

			if (minLength <= threshold) {
				return true;
			} else
				return false;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			calBitmapMatrix();
			drawCanvas();
		}

		private void calBitmapMatrix() {
			// 获取屏幕宽度
			float screenW = getWidth();
			// 获取屏幕高度
			float screenH = getHeight();

			float width = CameraUIActivity.faceBitmap.getWidth();
			float height = CameraUIActivity.faceBitmap.getHeight();
			scale = screenW / width;

			matrix = new Matrix();
			matrix.setScale(scale, scale);
			
			scalePonitsToShow(scale);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

		}
	}

}
