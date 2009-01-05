/* ZeroBPMFInput for Android Platform Version 0.1
 * 
 * Copyright (c) 2008 Zero, Chien-An Cho
 * (MIT License)
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * Contact:: itszero at gmail dot com
 * 
 * KeyTouchHandler: Touch event handler for Virtual Keyboard
 * This code is written by Romulus Ts'ai. 
 */
package idv.Zero.KerKerInput.VirtualKeyboard;

import idv.Zero.KerKerInput.KerKerInputMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class KeyTouchHandler {
	private ImageButton mButton;
	private float xPushed;
	private float yPushed;
	private int mDirection;
	private int mBase;
	
	static private KerKerInputMethod sIME;
	static private EditText sText;
	
	static final int DIR_NONE = 0;
	static final int DIR_UP = 1;
	static final int DIR_DOWN = 3;
	static final int DIR_LEFT = 4;
	static final int DIR_RIGHT = 2;
	
	public static void setParam(KerKerInputMethod kernel, EditText text) {
		sIME = kernel;
		sText = text;
	}

	public KeyTouchHandler(ImageButton button, int base) {
		mButton = button;
		mBase = base;
		init();
	}

	private void init() { 
		mButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					xPushed = event.getX();
					yPushed = event.getY();
					mButton.clearFocus();
					return true;
				case MotionEvent.ACTION_UP:
					float deltaX = event.getX() - xPushed;
					float deltaY = event.getY() - yPushed;
					
					if (Math.abs(deltaX) + Math.abs(deltaY) < 15) {
						mDirection = DIR_NONE;
					}
					else if (Math.abs(deltaX) >= Math.abs(deltaY)) {
						mDirection = deltaX > 0 ? DIR_RIGHT : DIR_LEFT;
					}
					else {
						mDirection = deltaY > 0 ? DIR_DOWN : DIR_UP;
					}
					sIME.handleClick(sText, mBase*5 + mDirection);
					return true;
				}
				return true;
			}
		});
	}
}
