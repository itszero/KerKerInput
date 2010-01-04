/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.google.zxing.LuminanceSource;

import android.graphics.Bitmap;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 *
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP. Any subsequent color data will be ignored.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PlanarYUVLuminanceSource extends BaseLuminanceSource {
  private byte[] yuvData;
  private int dataWidth;
  private int dataHeight;
  private int left;
  private int top;
  private int cropWidth;
  private int cropHeight;

  public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top,
      int width, int height) {
    super(width, height);

    if (left + width > dataWidth || top + height > dataHeight) {
      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    }

    this.yuvData = yuvData;
    this.dataWidth = dataWidth;
    this.dataHeight = dataHeight;
    this.left = left;
    this.top = top;
    this.cropWidth = width;
    this.cropHeight = height;
    
    // rotateRight();
  }
  
  public void rotateRight()
  {
	  byte[] result = new byte[dataWidth * dataHeight];

      try {
    	  renderCroppedGreyscaleBitmap().compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream("/sdcard/before.jpg"));
      } catch (FileNotFoundException e) {
    	  e.printStackTrace();
      }
      
      for(int x=0;x<cropWidth;x++)
      {
    	  for(int y=0;y<cropHeight;y++)
    	  {
    		  result[(y + top) * dataWidth + (x + left)] = yuvData[(x + left) * dataHeight + (y + top)];
    	  }
      }
      
      this.yuvData = result;
      
      this.left = 0;
      this.top = 0;
      
      int tmp;
      tmp = this.cropWidth;
      this.dataWidth = this.cropWidth = this.cropHeight;
      this.dataHeight = this.cropHeight = tmp;
      
      try {
    	  renderCroppedGreyscaleBitmap().compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream("/sdcard/after.jpg"));
      } catch (FileNotFoundException e) {
    	  e.printStackTrace();
      }
  }

  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= cropHeight) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = cropWidth;
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    int offset = (y + top) * dataWidth + left;
    System.arraycopy(yuvData, offset, row, 0, width);
    return row;
  }

  @Override
  public byte[] getMatrix() {
    int width = cropWidth;
    int height = cropHeight;

    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    if (width == dataWidth && height == dataHeight) {
      return yuvData;
    }

    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = top * dataWidth + left;

    // If the width matches the full width of the underlying data, perform a single copy.
    if (width == dataWidth) {
      System.arraycopy(yuvData, inputOffset, matrix, 0, area);
      return matrix;
    }

    // Otherwise copy one cropped row at a time.
    byte[] yuv = yuvData;
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
      inputOffset += dataWidth;
    }
    return matrix;
  }

  @Override
  public boolean isCropSupported() {
    return true;
  }

  @Override
  public LuminanceSource crop(int left, int top, int width, int height) {
    return new PlanarYUVLuminanceSource(yuvData, dataWidth, dataHeight, left, top, width, height);
  }

  @Override
  public int getDataWidth() {
    return dataWidth;
  }

  @Override
  public int getDataHeight() {
    return dataHeight;
  }

  @Override
  public Bitmap renderCroppedGreyscaleBitmap() {
    int width = cropWidth;
    int height = cropHeight;
    int[] pixels = new int[width * height];
    byte[] yuv = yuvData;
    int inputOffset = top * dataWidth + left;

    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      for (int x = 0; x < width; x++) {
        int grey = yuv[inputOffset + x] & 0xff;
        pixels[outputOffset + x] = (0xff000000) | (grey * 0x00010101);
      }
      inputOffset += dataWidth;
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }

  // Can't be implemented here, as the color representations vary.
  @Override
  public Bitmap renderFullColorBitmap(boolean halfSize) {
    return null;
  }
}
