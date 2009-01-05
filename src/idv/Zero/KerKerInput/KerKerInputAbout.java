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
 * KerKerInputAbout: Show the about window
 */
package idv.Zero.KerKerInput;

import java.io.*;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.view.View;
import android.widget.TextView;

public class KerKerInputAbout extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TableLayout txtTitleBar = (TableLayout)findViewById(R.id.TitleBar);
        txtTitleBar.setBackgroundColor(Color.WHITE);
        TextView txtTitle = (TextView)findViewById(R.id.Title);
        txtTitle.setTextColor(Color.BLACK);

        TextView txtAbout = (TextView)findViewById(R.id.AboutString);
        String strAbout = "";
        try
        {
        	InputStreamReader ir = new InputStreamReader(this.getResources().openRawResource(R.raw.about));
        	BufferedReader br = new BufferedReader(ir);
        	while(br.ready())
        		strAbout += br.readLine() + "\n";
        }
        catch(Exception e)
        {
        	strAbout = "無法讀取版權宣告資訊。";
        }
        txtAbout.setText(strAbout);
        
        Button btnExit = (Button)findViewById(R.id.EndButton);
        btnExit.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		KerKerInputAbout.this.finish();
        	}
        });
        
    }
}