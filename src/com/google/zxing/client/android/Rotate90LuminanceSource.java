package com.google.zxing.client.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class Rotate90LuminanceSource extends BaseLuminanceSource {
	private final Paint sPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private int dataWidth, dataHeight;
	private Bitmap bmpImage;
	private byte[] matrix = null;
	
	public Rotate90LuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top, int width, int height) {
		super(height, width);
		Bitmap bmpOriginal = new PlanarYUVLuminanceSource(yuvData, dataWidth, dataHeight, left, top, width, height).renderCroppedGreyscaleBitmap();
		
		bmpImage = rotate(bmpOriginal, 90);
		bmpOriginal.recycle();
		
		this.dataHeight = width;
		this.dataWidth = height;
	}

	@Override
	public byte[] getMatrix() {
		if (matrix != null)
			return matrix;
		
		Log.i("Rotate90LuminanceSource", "getMatrix() start.");
		int[] pixels = new int[getWidth() * getHeight()];
		bmpImage.getPixels(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight());
		
		matrix = new byte[getWidth() * getHeight()];
		for(int y=0;y<getHeight();y++)
			for(int x=0;x<getWidth();x++)
				matrix[y * getWidth() + x] = getGreyScale(pixels[y * getWidth() + x]);
		
		Log.i("Rotate90LuminanceSource", "getMatrix() end.");
		return matrix;
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		System.arraycopy(getMatrix(), y * getWidth(), row, 0, getWidth());
		return row;
	}

	@Override
	public int getDataHeight() {
		return dataHeight;
	}

	@Override
	public int getDataWidth() {
		return dataWidth;
	}

	@Override
	public Bitmap renderCroppedGreyscaleBitmap() {
		return bmpImage;
	}

	@Override
	public Bitmap renderFullColorBitmap(boolean halfSize) {
		return bmpImage;
	}
	
	private byte getGreyScale(int color)
	{
		return (byte)((0.3 * Color.red(color) + 0.59 * Color.green(color) + 0.11 * Color.blue(color)));
	}

	private Bitmap rotate(Bitmap bitmap, float angle) {
        final double radAngle = Math.toRadians(angle);

        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        final double cosAngle = Math.abs(Math.cos(radAngle));
        final double sinAngle = Math.abs(Math.sin(radAngle));

        final int width = (int) (bitmapHeight * sinAngle + bitmapWidth * cosAngle);
        final int height = (int) (bitmapWidth * sinAngle + bitmapHeight * cosAngle);

        final float x = (width - bitmapWidth) / 2.0f;
        final float y = (height - bitmapHeight) / 2.0f;

        final Bitmap decored = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(decored);

        canvas.rotate(angle, width / 2.0f, height / 2.0f);
        canvas.drawBitmap(bitmap, x, y, sPaint);

        return decored;
    }

}
