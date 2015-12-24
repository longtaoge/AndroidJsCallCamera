package com.common.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

/** 图片处理工具类 */
public class PictureUtils {
	private static final String LOGTAG = "PictureUtils";
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 3;
	public static final int BOTTOM = 4;
	public static final int CENTER = 5;
	public static final Config BITMAP_CONFIG = Bitmap.Config.ARGB_4444;

	/**
	 * 按比例缩放图片
	 * 
	 * @param path
	 *            图片路径
	 * @param width
	 *            宽度大于此值进行缩放
	 * @param height
	 *            高度度大于此值进行缩放
	 * @return
	 */
	public static Bitmap reducePic(String path, int width, int height) {
		if (!FileUtils.isFileExist(path))
			return null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回bm为空
		int widthRatio = (int) Math.ceil(options.outWidth / width);
		int heightRatio = (int) Math.ceil(options.outHeight / height);
		if (widthRatio > 1 || heightRatio > 1) {
			if (widthRatio > heightRatio) {
				options.inSampleSize = widthRatio;
			} else {
				options.inSampleSize = heightRatio;
			}
		}
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}

	/**
	 * 图片缩放成固定大小
	 * 
	 * @param path
	 *            图片路径
	 * @param newWidth
	 *            图片缩放宽度
	 * @param newHeight
	 *            图片缩放高度
	 * @return
	 */
	public static Bitmap reducePicSize(String path, int newWidth, int newHeight) {
		if (!FileUtils.isFileExist(path))
			return null;
		return reducePicSize(BitmapFactory.decodeFile(path), newWidth,
				newHeight);
	}

	/**
	 * 图片缩放成固定大小
	 * 
	 * @param path
	 *            图片路径
	 * @param newWidth
	 *            图片缩放宽度
	 * @param newHeight
	 *            图片缩放高度
	 * @return
	 */
	public static Bitmap reducePicSize(Bitmap bitmap, int newWidth,
			int newHeight) {
		if (bitmap == null)
			return null;
		// 获取这个图片的宽和高
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		// 计算缩放率，新尺寸除原始尺寸
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		return toScale(bitmap, scaleWidth, scaleHeight);
	}

	/** 把图片存到SD卡中 */
	public static void savePictrueToSDCard(String path, Bitmap bitmap) {
		saveReduceQuality(path, bitmap, 100);
	}

	/**
	 * 把图片降低质量并存到SD卡中
	 * 
	 * @param path
	 *            保存路径
	 * @param bitmap
	 *            原图片
	 * @param quality
	 *            降低质量 0-100，0最低，100最高
	 */
	public static void saveReduceQuality(String path, Bitmap bitmap, int quality) {
		if (bitmap == null || StringUtils.isEmpty(path)) {
			return;
		}
		FileUtils.creatDirs(path);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(path));
			// 采用压缩转档方法
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
			// 调用flush()方法，更新BufferStream
			bos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 结束OutputStream
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/** 图片改为圆型 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int radius = width >= height ? height : width;
		return getRoundedCornerBitmap(bitmap, radius);
	}

	/**
	 * 图片改为圆型
	 * 
	 * @param bitmap
	 * @param radius
	 *            直径
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {
		if (bitmap == null)
			return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int x = 0;
		int y = 0;
		// 截取中间部分
		if (width > radius)
			x = (width - radius) / 2;
		if (height > radius)
			y = (height - radius) / 2;

		Bitmap output = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(x, y, radius + x, radius + y);
		final RectF rectF = new RectF(rect);
		final float roundPx = radius / 2;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 图片旋转
	 * 
	 * @param path
	 * @param angle
	 *            旋转角度
	 * @return
	 */
	public static Bitmap toRotate(String path, int angle) {
		return toRotate(BitmapFactory.decodeFile(path), angle);
	}

	/**
	 * 图片旋转
	 * 
	 * @param path
	 * @param angle
	 *            旋转角度
	 * @return
	 */
	public static Bitmap toRotate(Bitmap bitmapOrg, int angle) {
		if (bitmapOrg == null)
			return null;
		// 获取这个图片的宽和高
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 旋转图片 动作
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,
				height, matrix, true);
		return resizedBitmap;
	}

	/**
	 * @param path
	 *            传入的图片路径
	 * @return
	 */
	public static int readPictureDegree(String path) {
		int ndegree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int norientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_FLIP_HORIZONTAL);
			switch (norientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				ndegree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				ndegree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				ndegree = 270;
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ndegree;
	}

	/**
	 * 图片至灰
	 * 
	 * @param path
	 *            传入的图片路径
	 * @return 去色后的图片
	 */
	public static Bitmap toGrayscale(String path) {
		return toGrayscale(BitmapFactory.decodeFile(path));
	}

	/**
	 * 图片至灰
	 * 
	 * @param bmpOriginal
	 *            传入的图片
	 * @return 去色后的图片
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		if (bmpOriginal == null)
			return null;
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/**
	 * 去色同时加圆角
	 * 
	 * @param bmpOriginal
	 *            原图
	 * @param pixels
	 *            圆角弧度
	 * @return 修改后的图片
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal, int pixels) {
		return toRoundCorner(toGrayscale(bmpOriginal), pixels);
	}

	/**
	 * 把图片变成圆角
	 * 
	 * @param bitmap
	 *            需要修改的图片
	 * @param pixels
	 *            圆角的弧度
	 * @return 圆角图片
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		if (bitmap == null)
			return null;
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), BITMAP_CONFIG);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 使圆角功能支持BitampDrawable
	 * 
	 * @param bitmapDrawable
	 * @param pixels
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static BitmapDrawable toRoundCorner(BitmapDrawable bitmapDrawable,
			int pixels) {
		Bitmap bitmap = bitmapDrawable.getBitmap();
		bitmapDrawable = new BitmapDrawable(toRoundCorner(bitmap, pixels));
		return bitmapDrawable;
	}

	/**
	 * 水印
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createBitmapForWatermark(Bitmap src, Bitmap watermark) {
		if (src == null) {
			return null;
		}
		int w = src.getWidth();
		int h = src.getHeight();
		int ww = watermark.getWidth();
		int wh = watermark.getHeight();
		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(w, h, BITMAP_CONFIG);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);
		// draw src into
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
		// draw watermark into
		cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newb;
	}

	/**
	 * 图片合成
	 * 
	 * @return
	 */
	public static Bitmap potoMix(int direction, Bitmap... bitmaps) {
		if (bitmaps.length <= 0) {
			return null;
		}
		if (bitmaps.length == 1) {
			return bitmaps[0];
		}
		Bitmap newBitmap = bitmaps[0];
		// newBitmap = createBitmapForFotoMix(bitmaps[0],bitmaps[1],direction);
		for (int i = 1; i < bitmaps.length; i++) {
			newBitmap = createBitmapForFotoMix(newBitmap, bitmaps[i], direction);
		}
		return newBitmap;
	}

	private static Bitmap createBitmapForFotoMix(Bitmap first, Bitmap second,
			int direction) {
		if (first == null) {
			return null;
		}
		if (second == null) {
			return first;
		}
		int fw = first.getWidth();
		int fh = first.getHeight();
		int sw = second.getWidth();
		int sh = second.getHeight();
		Bitmap newBitmap = null;
		if (direction == LEFT) {
			newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
					BITMAP_CONFIG);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, sw, 0, null);
			canvas.drawBitmap(second, 0, 0, null);
		} else if (direction == RIGHT) {
			newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh,
					BITMAP_CONFIG);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, 0, 0, null);
			canvas.drawBitmap(second, fw, 0, null);
		} else if (direction == TOP) {
			newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
					BITMAP_CONFIG);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, 0, sh, null);
			canvas.drawBitmap(second, 0, 0, null);
		} else if (direction == BOTTOM) {
			newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh,
					BITMAP_CONFIG);
			Canvas canvas = new Canvas(newBitmap);
			canvas.drawBitmap(first, 0, 0, null);
			canvas.drawBitmap(second, 0, fh, null);
		}
		return newBitmap;
	}

	/**
	 * Drawable 转 Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmapByBD(Drawable drawable) {
		if (drawable == null)
			return null;
		BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		return bitmapDrawable.getBitmap();
	}

	/**
	 * Bitmap 转 Drawable
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Drawable bitmapToDrawableByBD(Bitmap bitmap) {
		if (bitmap == null)
			return null;
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}

	/**
	 * byte[] 转 bitmap
	 * 
	 * @param b
	 * @return
	 */
	public static Bitmap bytesToBimap(byte[] b) {
		if (b != null && b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * bitmap 转 byte[]
	 * 
	 * @param bm
	 * @return
	 */
	public static byte[] bitmapToBytes(Bitmap bm) {
		if (bm == null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/** 获取网络图片名 */
	public static String getNameFromUrl(String url) {
		if (StringUtils.isEmpty(url))
			return null;
		return url.substring(url.lastIndexOf(File.separator), url.length());
	}

	/**
	 * 获取纯色圆角背景图
	 * 
	 * @param color
	 *            颜色
	 * @param pixels
	 *            圆角半径
	 * @param width
	 *            宽度
	 * @param hight
	 *            高度
	 * @return
	 */
	public synchronized static Bitmap getRoundedCornerBitmap(int color,
			int pixels, int width, int hight) {
		try {
			final Bitmap output = Bitmap.createBitmap(width, hight,
					BITMAP_CONFIG);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, width, hight);
			final RectF rectF = new RectF(rect);
			final float roundPx = pixels;
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			return output;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "getRoundedCornerBitmap:OutOfMemoryError");
			return null;
		}
	}

	/**
	 * 获取播放器专辑封面图片
	 * 
	 * @param bitmap
	 *            专辑原始图片
	 * @return
	 */
	public synchronized static Bitmap getAblumBitmap(final Bitmap bitmap) {
		if (bitmap == null)
			return null;
		return curBitmap(bitmap, 148, 148);
	}

	/**
	 * 获取通知栏专辑封面图片
	 * 
	 * @param bitmap
	 *            专辑原始图片
	 * @return
	 */
	public synchronized static Bitmap getNotifyAblumBitmap(final Bitmap bitmap) {
		if (bitmap == null)
			return null;
		return curBitmap(bitmap, 96, 98);
	}

	/**
	 * 按比例拉伸压缩图片
	 * 
	 * @param sourceBitmap
	 *            原始图片
	 * @param toWidth
	 *            生成图片的宽度
	 * @param toHeigh
	 *            生成图片的高度
	 * @return
	 */
	public synchronized static Bitmap curBitmap(final Bitmap sourceBitmap,
			int toWidth, int toHeigh) {
		if (sourceBitmap == null)
			return null;
		int targetWidth = sourceBitmap.getWidth();
		int targetHeight = sourceBitmap.getHeight();
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
				BITMAP_CONFIG);

		Canvas canvas = new Canvas(targetBitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.ANTI_ALIAS_FLAG,
				Paint.FILTER_BITMAP_FLAG));
		Path path = new Path();

		path.addCircle(targetWidth, targetHeight / 2, 34 * toWidth
				/ (2 * targetWidth), Path.Direction.CCW);
		canvas.clipPath(path, Region.Op.DIFFERENCE);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// 用来对位图进行滤波处理
		paint.setFilterBitmap(true);
		Rect rec = new Rect(0, 0, targetWidth, targetHeight);
		canvas.drawBitmap(sourceBitmap, rec, rec, paint);
		return targetBitmap;
	}

	/**
	 * 根据指定的dp数据处理图片
	 * 
	 * @param bm
	 * @param toWidth
	 * @param toHeight
	 * @param context
	 * @return
	 */
	public synchronized static Bitmap getBitmapByPix(final Bitmap bm,
			int toWidth, int toHeight, Context context) {
		if (bm == null)
			return null;
		// 获得图片的宽高
		try {
			int width = bm.getWidth();
			int height = bm.getHeight();
			Log.e("bm.getWidth()", bm.getWidth() + "");
			// 设置想要的大小
			int newWidth = dip2px(context, toWidth);
			int newHeight = dip2px(context, toHeight);// 66dp是自己想要的大小，大家随意
			// 计算缩放比例
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			// 取得想要缩放的matrix参数
			final Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			// 得到新的图片
			return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "getBitmapByPix:OutOfMemoryError");
			return null;
		}

	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 柔化效果(高斯模糊)
	 * 
	 * @param bmp
	 * @return
	 */
	public synchronized static Bitmap blurImageAmeliorate(final Bitmap bmp,
			int delta) {
		if (bmp == null)
			return null;
		try {
			long start = System.currentTimeMillis();
			// 高斯矩阵
			int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };

			int width = bmp.getWidth();
			int height = bmp.getHeight();
			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.RGB_565);

			int pixR = 0;
			int pixG = 0;
			int pixB = 0;

			int pixColor = 0;

			int newR = 0;
			int newG = 0;
			int newB = 0;
			int idx = 0;
			int[] pixels = new int[width * height];
			bmp.getPixels(pixels, 0, width, 0, 0, width, height);
			for (int i = 1, length = height - 1; i < length; i++) {
				for (int k = 1, len = width - 1; k < len; k++) {
					idx = 0;
					for (int m = -1; m <= 1; m++) {
						for (int n = -1; n <= 1; n++) {
							pixColor = pixels[(i + m) * width + k + n];
							pixR = Color.red(pixColor);
							pixG = Color.green(pixColor);
							pixB = Color.blue(pixColor);

							newR = newR + (int) (pixR * gauss[idx]);
							newG = newG + (int) (pixG * gauss[idx]);
							newB = newB + (int) (pixB * gauss[idx]);
							idx++;
						}
					}

					newR /= delta;
					newG /= delta;
					newB /= delta;

					newR = Math.min(255, Math.max(0, newR));
					newG = Math.min(255, Math.max(0, newG));
					newB = Math.min(255, Math.max(0, newB));

					pixels[i * width + k] = Color.argb(255, newR, newG, newB);

					newR = 0;
					newG = 0;
					newB = 0;
				}
			}

			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "blurImageAmeliorate:OutOfMemoryError");
			return null;
		}
	}

	/**
	 * 图片放大
	 * 
	 * @param bmp
	 * @param scaleX
	 *            放大的宽度比例
	 * @param scaleY
	 *            放大的高度比例
	 * @return
	 */
	public synchronized static Bitmap toScale(final Bitmap bmp, float scaleX,
			float scaleY) {
		if (bmp == null)
			return null;
		try {
			int bmpWidth = bmp.getWidth();
			int bmpHeight = bmp.getHeight();
			/* 设置图片放大的比例 */
			/* 计算这次要放大的比例 */
			/* 产生reSize后的Bitmap对象 */
			Matrix matrix = new Matrix();
			matrix.postScale(scaleX, scaleY);
			Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth,
					bmpHeight, matrix, true);
			return resizeBmp;

		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "toBig:OutOfMemoryError");
			return null;
		}
		// bmp.recycle();
	}

	/**
	 * 图片剪切
	 * 
	 * @param mBitmap
	 * @param r
	 *            剪切的区域
	 * @param config
	 * @return
	 */
	public synchronized static Bitmap cutBitmap(final Bitmap mBitmap, Rect r,
			Config config) {
		if (mBitmap == null)
			return null;
		try {
			int width = r.width();
			int height = r.height();
			final Bitmap croppedImage = Bitmap.createBitmap(width, height,
					config);
			Canvas cvs = new Canvas(croppedImage);
			Rect dr = new Rect(0, 0, width, height);
			cvs.drawBitmap(mBitmap, r, dr, null);
			return croppedImage;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "cutBitmap:OutOfMemoryError");
			return null;
		}
	}

	/**
	 * 获取专辑详情页面背景图
	 * 
	 * @param mBitmap
	 * @param toWidth
	 *            图片显示宽度
	 * @param toHigth
	 *            图片高度
	 * @return
	 */
	public synchronized static Bitmap getDetailBg(Bitmap mBitmap, int toWidth,
			int toHigth) {
		if (mBitmap == null)
			return null;
		try {
			int h = mBitmap.getHeight();
			int w = mBitmap.getWidth();
			float scale = ((float) toWidth) / (float) w;
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap bmp = Bitmap.createBitmap(mBitmap, 0, 0, w, h, matrix, true);
			int scaleH = bmp.getHeight();
			int scaleW = bmp.getWidth();
			Bitmap cutBmp = cutBitmap(bmp, new Rect((scaleW - toWidth) / 2,
					(scaleH - toHigth) / 2, (scaleW + toWidth) / 2,
					(scaleH + toHigth) / 2), BITMAP_CONFIG);
			Bitmap result = BoxBlurFilter(cutBmp, 12);
			recycleBitmap(bmp);
			recycleBitmap(cutBmp);
			return result;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "BoxBlurFilter:OutOfMemoryError");
			return null;
		}

	}

	/**
	 * 获取播放器音乐背景
	 * 
	 * @param mBitmap
	 * @param toWidth
	 *            图片宽度
	 * @param toHigth
	 *            图片高度
	 * @return
	 */
	public synchronized static Bitmap getMusicBg(Bitmap mBitmap, int toWidth,
			int toHigth) {
		if (mBitmap == null)
			return null;
		try {
			int h = mBitmap.getHeight();
			int w = mBitmap.getWidth();
			float scale = ((toWidth / w) > (toHigth / h) ? (toWidth / w)
					: (toHigth / h)) + 1;

			Bitmap bmp = toScale(mBitmap, scale, scale);
			if (bmp == null)
				return null;
			h = bmp.getWidth();
			w = bmp.getWidth();
			Bitmap cutBmp = cutBitmap(bmp, new Rect((w - toWidth) / 2,
					(w - toHigth) / 2, (w + toWidth) / 2, (w + toHigth) / 2),
					BITMAP_CONFIG);
			if (cutBmp == null)
				return null;
			Bitmap result = BoxBlurFilter(cutBmp, 12);
			recycleBitmap(cutBmp);
			recycleBitmap(bmp);
			return result;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "BoxBlurFilter:OutOfMemoryError");
			return null;
		}
	}

	/** 水平方向模糊度 */
	private static float hRadius = 10;
	/** 竖直方向模糊度 */
	private static float vRadius = 10;

	/** 模糊迭代度 */
	public synchronized static Bitmap BoxBlurFilter(final Bitmap bmp,
			int iterations) {
		if (bmp == null)
			return null;
		if (!FileUtils.getAvailaleDisk())
			return null;
		try {
			int width = bmp.getWidth();

			int height = bmp.getHeight();

			int[] inPixels = new int[width * height];

			int[] outPixels = new int[width * height];

			final Bitmap bitmap = Bitmap.createBitmap(width, height,
					BITMAP_CONFIG);

			bmp.getPixels(inPixels, 0, width, 0, 0, width, height);

			for (int i = 0; i < iterations; i++) {

				blur(inPixels, outPixels, width, height, hRadius);

				blur(outPixels, inPixels, height, width, vRadius);

			}

			blurFractional(inPixels, outPixels, width, height, hRadius);

			blurFractional(outPixels, inPixels, height, width, vRadius);

			bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "BoxBlurFilter:OutOfMemoryError");
			return null;
		}

	}

	public static void blur(int[] in, int[] out, int width, int height,

	float radius) {

		int widthMinus1 = width - 1;

		int r = (int) radius;

		int tableSize = 2 * r + 1;

		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)

			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {

			int outIndex = y;

			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -r; i <= r; i++) {

				int rgb = in[inIndex + clamp(i, 0, width - 1)];

				ta += (rgb >> 24) & 0xff;

				tr += (rgb >> 16) & 0xff;

				tg += (rgb >> 8) & 0xff;

				tb += rgb & 0xff;

			}

			for (int x = 0; x < width; x++) {

				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)

				| (divide[tg] << 8) | divide[tb];

				int i1 = x + r + 1;

				if (i1 > widthMinus1)

					i1 = widthMinus1;

				int i2 = x - r;

				if (i2 < 0)

					i2 = 0;

				int rgb1 = in[inIndex + i1];

				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);

				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;

				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;

				tb += (rgb1 & 0xff) - (rgb2 & 0xff);

				outIndex += height;

			}

			inIndex += width;

		}

	}

	public static void blurFractional(int[] in, int[] out, int width,

	int height, float radius) {

		radius -= (int) radius;

		float f = 1.0f / (1 + 2 * radius);

		int inIndex = 0;

		for (int y = 0; y < height; y++) {

			int outIndex = y;

			out[outIndex] = in[0];

			outIndex += height;

			for (int x = 1; x < width - 1; x++) {

				int i = inIndex + x;

				int rgb1 = in[i - 1];

				int rgb2 = in[i];

				int rgb3 = in[i + 1];

				int a1 = (rgb1 >> 24) & 0xff;

				int r1 = (rgb1 >> 16) & 0xff;

				int g1 = (rgb1 >> 8) & 0xff;

				int b1 = rgb1 & 0xff;

				int a2 = (rgb2 >> 24) & 0xff;

				int r2 = (rgb2 >> 16) & 0xff;

				int g2 = (rgb2 >> 8) & 0xff;

				int b2 = rgb2 & 0xff;

				int a3 = (rgb3 >> 24) & 0xff;

				int r3 = (rgb3 >> 16) & 0xff;

				int g3 = (rgb3 >> 8) & 0xff;

				int b3 = rgb3 & 0xff;

				a1 = a2 + (int) ((a1 + a3) * radius);

				r1 = r2 + (int) ((r1 + r3) * radius);

				g1 = g2 + (int) ((g1 + g3) * radius);

				b1 = b2 + (int) ((b1 + b3) * radius);

				a1 *= f;

				r1 *= f;

				g1 *= f;

				b1 *= f;

				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;

				outIndex += height;

			}

			out[outIndex] = in[width - 1];

			inIndex += width;

		}

	}

	public static int clamp(int x, int a, int b) {

		return (x < a) ? a : (x > b) ? b : x;

	}

	/**
	 * 专题详情背景图片
	 * 
	 * @param mBitmap
	 * @param toWidth
	 *            图片宽度
	 * @param toHigth
	 *            图片高度
	 * @return
	 */
	public synchronized static Bitmap getSpecialDetailImage(
			final Bitmap mBitmap, int toWidth, int toHigth) {

		if (!FileUtils.getAvailaleDisk())
			return null;

		if (mBitmap == null)
			return null;

		try { // 获得模糊化背景
			int h = mBitmap.getHeight();
			int w = mBitmap.getWidth();
			// 高度不够放大，高度够剪切
			Bitmap above;
			if (h < w) {
				float scale = (float) toHigth / (float) h;
				above = toScale(mBitmap, scale, scale);
				if (above != null) {
					w = above.getWidth();
					above = cutBitmap(above, new Rect((w - toHigth) / 2, 0,
							(w + toHigth) / 2, toHigth), BITMAP_CONFIG);
				}
			} else {
				float scale = (float) toHigth / (float) w;
				above = toScale(mBitmap, scale, scale);
				if (above != null) {
					h = above.getHeight();
					above = cutBitmap(above, new Rect(0, (h - toHigth) / 2,
							toHigth, (h + toHigth) / 2), BITMAP_CONFIG);
				}
			}

			Bitmap below = getDetailBg(mBitmap, toWidth, toHigth);
			above = sideRenderBitmap(above, 50);
			final Bitmap result = overlying(below, above);
			recycleBitmap(above);
			recycleBitmap(below);
			return result;
		} catch (OutOfMemoryError e) {
			LogUtil.e(LOGTAG, "BoxBlurFilter:OutOfMemoryError");
			return null;
		}

	}

	/**
	 * 羽化
	 * 
	 * @param bitmap
	 * @return
	 */
	public synchronized static Bitmap renderBitmap(final Bitmap bitmap) {
		if (!FileUtils.getAvailaleDisk())
			return null;
		float mSize = 0.5f;
		if (bitmap == null || bitmap.isRecycled())
			return null;

		final int SIZE = 32768;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int ratio = width > height ? height * SIZE / width : width * SIZE
				/ height;// 这里有额外*2^15 用于放大比率；之后的比率使用时需要右移15位，或者/2^15.

		int cx = width >> 1;
		int cy = height >> 1;
		int max = cx * cx + cy * cy;
		int min = (int) (max * (1 - mSize));
		int diff = max - min;// ===>> int diff = (int)(max * mSize);

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pixel = pixels[i * width + j];
				int r = (pixel & 0x00ff0000) >> 16;
				int g = (pixel & 0x0000ff00) >> 8;
				int b = (pixel & 0x000000ff);

				int dx = cx - j;
				int dy = cy - i;

				if (width > height) {
					dx = (dx * ratio) >> 15;
				} else {
					dy = (dy * ratio) >> 15;
				}

				int dstSq = dx * dx + dy * dy;
				float v = ((float) dstSq / diff) * 255;
				r = (int) (r + v);
				g = (int) (g + v);
				b = (int) (b + v);
				r = (r > 255 ? 255 : (r < 0 ? 0 : r));
				g = (g > 255 ? 255 : (g < 0 ? 0 : g));
				b = (b > 255 ? 255 : (b < 0 ? 0 : b));
				pixels[i * width + j] = (pixel & 0xff000000) + (r << 16)
						+ (g << 8) + b;
			}
		}

		return Bitmap.createBitmap(pixels, width, height, BITMAP_CONFIG);
	}

	/**
	 * 两边模糊
	 * 
	 * @param bitmap
	 * @return
	 */
	public synchronized static Bitmap sideRenderBitmap(final Bitmap bitmap,
			int sideWidth) {
		if (!FileUtils.getAvailaleDisk())
			return null;
		if (bitmap == null)
			return null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < sideWidth; j++) {
				int pixel = pixels[i * width + j];
				int alp = 0;
				int r = (pixel & 0x00ff0000) >> 16;
				int g = (pixel & 0x0000ff00) >> 8;
				int b = (pixel & 0x000000ff);

				if (j < sideWidth) {
					alp = 255 * j / sideWidth;
				}
				alp = (alp > 255 ? 255 : (alp < 0 ? 0 : alp));
				pixels[i * width + j] = Color.argb(alp, r, g, b);

			}
			for (int j = width - sideWidth; j < width; j++) {
				int pixel = pixels[i * width + j];
				int alp = 0;
				int r = (pixel & 0x00ff0000) >> 16;
				int g = (pixel & 0x0000ff00) >> 8;
				int b = (pixel & 0x000000ff);
				alp = 255 * (width - j) / sideWidth;
				alp = (alp > 255 ? 255 : (alp < 0 ? 0 : alp));
				pixels[i * width + j] = Color.argb(alp, r, g, b);

			}
		}
		return Bitmap.createBitmap(pixels, width, height, BITMAP_CONFIG);
	}

	/**
	 * 组合图片
	 * 
	 * @param src
	 *            源图片
	 * @param watermark
	 *            涂鸦图片
	 * @return
	 */
	public synchronized static Bitmap overlying(final Bitmap below,
			final Bitmap above) {

		if (!FileUtils.getAvailaleDisk())
			return null;
		if (below == null) {
			return above;
		} else if (above == null) {
			return below;
		}
		// 另外创建一张图片
		final Bitmap newb = Bitmap.createBitmap(below.getWidth(),
				below.getHeight() + 1, BITMAP_CONFIG);// 创建一个新的和SRC长度宽度一样的位图
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(below, 0, 0, null);// 在 0，0坐标开始画入原图片src
		canvas.drawBitmap(above, (below.getWidth() - above.getWidth()) / 2,
				(below.getHeight() - above.getHeight()) / 2, null); // 涂鸦图片画到原图片中间位置
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		// below.recycle();
		return newb;
	}

	/**
	 * 获取res资源图片
	 * 
	 * @param context
	 * @param id
	 *            资源id
	 * @return
	 */
	public synchronized static Bitmap getResBitmap(Context context, int id) {
		return BitmapFactory.decodeResource(context.getResources(), id);
	}

	/**
	 * bitmap回收处理
	 * 
	 * @param bitmap
	 */
	public static void recycleBitmap(Bitmap bitmap) {
		// if (bitmap == null)
		// return;
		// if (!bitmap.isRecycled()) {
		// bitmap.recycle(); // 回收图片所占的内存
		// System.gc();
		// }
		if (bitmap != null) {
			bitmap.recycle();
		}
		bitmap = null;
	}

	/**
	 * convert Bitmap to byte array
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] bitmapToByte(Bitmap b) {
		if (b == null) {
			return null;
		}

		ByteArrayOutputStream o = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.PNG, 100, o);
		return o.toByteArray();
	}

	/**
	 * convert byte array to Bitmap
	 * 
	 * @param b
	 * @return
	 */
	public static Bitmap byteToBitmap(byte[] b) {
		return (b == null || b.length == 0) ? null : BitmapFactory
				.decodeByteArray(b, 0, b.length);
	}

	/**
	 * convert Drawable to Bitmap
	 * 
	 * @param d
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable d) {
		return d == null ? null : ((BitmapDrawable) d).getBitmap();
	}

	/**
	 * convert Bitmap to Drawable
	 * 
	 * @param b
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Drawable bitmapToDrawable(Bitmap b) {
		return b == null ? null : new BitmapDrawable(b);
	}

	/**
	 * convert Drawable to byte array
	 * 
	 * @param d
	 * @return
	 */
	public static byte[] drawableToByte(Drawable d) {
		return bitmapToByte(drawableToBitmap(d));
	}

	/**
	 * convert byte array to Drawable
	 * 
	 * @param b
	 * @return
	 */
	public static Drawable byteToDrawable(byte[] b) {
		return bitmapToDrawable(byteToBitmap(b));
	}

	// /**
	// * get input stream from network by imageurl, you need to close
	// inputStream yourself
	// *
	// * @param imageUrl
	// * @param readTimeOutMillis
	// * @return
	// * @see ImageUtils#getInputStreamFromUrl(String, int, boolean)
	// */
	// public static InputStream getInputStreamFromUrl(String imageUrl, int
	// readTimeOutMillis) {
	// return getInputStreamFromUrl(imageUrl, readTimeOutMillis, null);
	// }

	// /**
	// * get input stream from network by imageurl, you need to close
	// inputStream yourself
	// *
	// * @param imageUrl
	// * @param readTimeOutMillis read time out, if less than 0, not set, in
	// mills
	// * @param requestProperties http request properties
	// * @return
	// * @throws MalformedURLException
	// * @throws IOException
	// */
	// public static InputStream getInputStreamFromUrl(String imageUrl, int
	// readTimeOutMillis,
	// Map<String, String> requestProperties) {
	// InputStream stream = null;
	// try {
	// URL url = new URL(imageUrl);
	// // Log.e("ImageUtils", "getInputStreamFromUrl()--imageUrl:"+imageUrl);
	// HttpURLConnection con = (HttpURLConnection)url.openConnection();
	// HttpUtils.setURLConnection(requestProperties, con);
	// if (readTimeOutMillis > 0) {
	// con.setReadTimeout(readTimeOutMillis);
	// }
	// stream = con.getInputStream();
	// } catch (MalformedURLException e) {
	// closeInputStream(stream);
	// throw new RuntimeException("MalformedURLException occurred. ", e);
	// } catch (IOException e) {
	// closeInputStream(stream);
	// throw new RuntimeException("IOException occurred. ", e);
	// }
	// return stream;
	// }

	// /**
	// * get drawable by imageUrl
	// *
	// * @param imageUrl
	// * @param readTimeOutMillis
	// * @return
	// * @see ImageUtils#getDrawableFromUrl(String, int, boolean)
	// */
	// public static Drawable getDrawableFromUrl(String imageUrl, int
	// readTimeOutMillis) {
	// return getDrawableFromUrl(imageUrl, readTimeOutMillis, null);
	// }

	// /**
	// * get drawable by imageUrl
	// *
	// * @param imageUrl
	// * @param readTimeOutMillis read time out, if less than 0, not set, in
	// mills
	// * @param requestProperties http request properties
	// * @return
	// */
	// public static Drawable getDrawableFromUrl(String imageUrl, int
	// readTimeOutMillis,
	// Map<String, String> requestProperties) {
	// InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOutMillis,
	// requestProperties);
	// Drawable d = Drawable.createFromStream(stream, "src");
	// closeInputStream(stream);
	// return d;
	// }

	// /**
	// * get Bitmap by imageUrl
	// *
	// * @param imageUrl
	// * @param readTimeOut
	// * @return
	// * @see ImageUtils#getBitmapFromUrl(String, int, boolean)
	// */
	// public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut) {
	// return getBitmapFromUrl(imageUrl, readTimeOut, null);
	// }

	// /**
	// * get Bitmap by imageUrl
	// *
	// * @param imageUrl
	// * @param requestProperties http request properties
	// * @return
	// */
	// public static Bitmap getBitmapFromUrl(String imageUrl, int readTimeOut,
	// Map<String, String> requestProperties) {
	// InputStream stream = getInputStreamFromUrl(imageUrl, readTimeOut,
	// requestProperties);
	// Bitmap b = BitmapFactory.decodeStream(stream);
	// closeInputStream(stream);
	// return b;
	// }

	/**
	 * scale image
	 * 
	 * @param org
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
		return scaleImage(org, (float) newWidth / org.getWidth(),
				(float) newHeight / org.getHeight());
	}

	/**
	 * scale image
	 * 
	 * @param org
	 * @param scaleWidth
	 *            sacle of width
	 * @param scaleHeight
	 *            scale of height
	 * @return
	 */
	public static Bitmap scaleImage(Bitmap org, float scaleWidth,
			float scaleHeight) {
		if (org == null) {
			return null;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		if (FileUtils.getAvailaleDisk())
			return Bitmap.createBitmap(org, 0, 0, org.getWidth(),
					org.getHeight(), matrix, true);
		else
			return null;
	}

	/**
	 * close inputStream
	 * 
	 * @param s
	 */
	private static void closeInputStream(InputStream s) {
		if (s == null) {
			return;
		}

		try {
			s.close();
		} catch (IOException e) {
			throw new RuntimeException("IOException occurred. ", e);
		}
	}

	/**
	 * 根据资源id获取bitmap
	 * 
	 * @param res
	 * @param id
	 * @return
	 */
	public static Bitmap getBitmap(Resources res, int id) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inPurgeable = true;
		// 与inPurgeable 一起使用
		options.inInputShareable = true;
		// 3. 减少对Aphla 通道
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		try {
			// 4. inNativeAlloc 属性设置为true，可以不把使用的内存算到VM里
			BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(
					options, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		InputStream is = res.openRawResource(id);

		return BitmapFactory.decodeStream(is, null, options);
	}

	/** 根据uri获取bitmap */
	@SuppressWarnings("resource")
	public static Bitmap decodeUriAsBitmap(Uri uri) {

		Bitmap bitmap = null;
		// if(!FileUtils.getAvailaleDisk())
		// return bitmap;
		File file = null;
		try {
			BitmapFactory.Options bfOptions = new BitmapFactory.Options();
			bfOptions.inDither = false;
			bfOptions.inPurgeable = true;
			bfOptions.inInputShareable = true;
			bfOptions.inTempStorage = new byte[32 * 1024];
			file = new File(uri.getPath());
			FileInputStream fs = null;
			if (file != null)
				fs = new FileInputStream(file);
			if (fs != null) {
				bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
						bfOptions);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}

	// // // 把图片存到SD卡中
	// public static String savePictrueToSDCard(String path, Bitmap bitmap) {
	// return savePictrueToSDCard(path, bitmap, false);
	// }

	/**
	 * 把图片存到SD卡中
	 * 
	 * @param path
	 * @param bitmap
	 * @param bCover
	 *            是否覆盖已存在的
	 * @return
	 */
	public static String savePictrueToSDCard(String path, Bitmap bitmap,
			boolean bCover) {

		if (bitmap == null) {
			Log.e(LOGTAG, "savePictrueToSDCard path:" + path + "bitmap == null");
			return "";
		}
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (!bCover)// 不覆盖，返回路径
				return path;
		}
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(path));
			if (bos == null)
				return null;
			// 采用压缩转档方法
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			// 调用flush()方法，更新BufferStream
			bos.flush();
			// 结束OutputStream
			bos.close();
			return path;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
