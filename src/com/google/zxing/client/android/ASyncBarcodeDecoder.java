package com.google.zxing.client.android;

import com.google.zxing.Result;

import android.graphics.Bitmap;
import android.os.Handler;

public interface ASyncBarcodeDecoder {
	public Handler getHandler();

	public void handleDecode(Result obj, Bitmap barcode);

	public void drawViewfinder();
}
