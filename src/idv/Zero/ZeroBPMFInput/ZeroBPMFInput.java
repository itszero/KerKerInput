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
 * ZeroBPMFInput: The UI of the input method
 */
package idv.Zero.ZeroBPMFInput;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.text.method.TextKeyListener;
import android.text.ClipboardManager;

public class ZeroBPMFInput extends Activity {
	private static final int INPUT_NOTIFICATION = 0x1004;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final NotificationManager nm = (NotificationManager)this.getSystemService("notification");
        
        TextView tvBuffer = (TextView)findViewById(R.id.buffer);
        TextKeyListener bpmf = new ZeroBPMFInputMethod(this, tvBuffer);
        
        final EditText txtBox = (EditText)findViewById(R.id.text);
        txtBox.setKeyListener(bpmf);
        
        final Button btnCopy = (Button)findViewById(R.id.CopyButton);
        final ClipboardManager cm = (ClipboardManager)getSystemService("clipboard");
        
        final Context c = this;
        btnCopy.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				cm.setText(txtBox.getText() + " ");
				
				new AlertDialog.Builder(c).setTitle("ZeroBPMF 訊息").setMessage("複製完成，請在原程式貼上。").setNeutralButton("關閉", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						ZeroBPMFInput.this.finish();
					}
				}).show();
			}
        });
        Button btnExit = (Button)findViewById(R.id.EndButton);
        btnExit.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		nm.cancel(INPUT_NOTIFICATION);
        		ZeroBPMFInput.this.finish();
        	}
        });
        
		Notification n = new Notification(R.drawable.icon, "ZeroBPMF: 準備完成", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ZeroBPMFInput.class), 0);
		n.flags = Notification.FLAG_NO_CLEAR;
		n.setLatestEventInfo(this, "ZeroBPMFInput", "點選使用注音輸入法", contentIntent);
		nm.notify(INPUT_NOTIFICATION, n);
    }
}