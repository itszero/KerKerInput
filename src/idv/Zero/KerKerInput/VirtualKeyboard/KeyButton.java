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
 * KeyButton: Providing the button with programmatically generated image overlay 
 */
package idv.Zero.KerKerInput.VirtualKeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class KeyButton extends ImageButton {
	private String[] texts;
	
	public KeyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public KeyButton(Context context, String[] texts) {
		super(context);
		this.texts = texts;
	}
	
	public void setTexts(String[] texts)
	{
		this.texts = texts;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (texts == null || texts.length == 0)
			return;
		// Center Text
		Paint pBlack = new Paint();
		pBlack.setColor(Color.BLACK);
		pBlack.setTextSize(24);
		pBlack.setAntiAlias(true);
		canvas.drawText(texts[0], this.getWidth() / 2 - 10, this.getHeight() / 2 + 6, pBlack);
		
		if (texts.length == 1)
			return;
		
		// Border Text URDL
		Paint pGray = new Paint();
		pGray.setColor(Color.GRAY);
		pGray.setTextSize(20);
		pGray.setAntiAlias(true);
		switch(texts.length)
		{
		case 5:
			canvas.drawText(texts[4], 6, this.getHeight() / 2 + 4, pGray);
		case 4:
			canvas.drawText(texts[3], (this.getWidth() - 16) / 2, this.getHeight() - 10, pGray);
		case 3:
			canvas.drawText(texts[2], (this.getWidth() - 25), this.getHeight() / 2 + 5, pGray);
		case 2:
			canvas.drawText(texts[1], (this.getWidth() - 16) / 2, 20, pGray);
			break;
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		setMeasuredDimension(77, 73);
	}
}