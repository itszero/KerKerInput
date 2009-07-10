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
 * KerKerInputUI: The UI of the input method
 */
package idv.Zero.KerKerInput;

import idv.Zero.KerKerInput.VirtualKeyboard.KeyButton;
import idv.Zero.KerKerInput.VirtualKeyboard.KeyTouchHandler;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.EditText;
import android.text.method.TextKeyListener;
import android.text.ClipboardManager;

public class KerKerInputUI extends Activity {
	private static final int INPUT_NOTIFICATION = 0x1004;
	private ArrayList<Button> candidate_buttons;
	private String updateServiceURL = "http://zero.itszero.info/KerKerInput/version.dat";
	public KerKerInputMethod kIME = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final NotificationManager nm = (NotificationManager)this.getSystemService("notification");
        
        initSoftKeys();
        TextView tvBuffer = (TextView)findViewById(R.id.buffer);
        kIME = new KerKerInputMethod(this, tvBuffer, candidate_buttons);
        TextKeyListener bpmf = kIME;
        
        final EditText txtBox = (EditText)findViewById(R.id.text);
        txtBox.setKeyListener(bpmf);
        KeyTouchHandler.setParam(kIME, txtBox);
        
        final Button btnCopy = (Button)findViewById(R.id.CopyButton);
        final Button btnSW = (Button)findViewById(R.id.CESwitchButton);
        final ClipboardManager cm = (ClipboardManager)getSystemService("clipboard");
        
        final Context c = this;
        btnSW.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				kIME.handleClick(txtBox, 70);
			}
        });
        btnCopy.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				cm.setText(txtBox.getText() + " ");
				
				new AlertDialog.Builder(c).setTitle(R.string.app_name).setMessage(R.string.msg_copy).setNeutralButton(R.string.close, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						KerKerInputUI.this.finish();
					}
				}).show();
			}
        });
        
		Notification n = new Notification(R.drawable.icon, "KerKer: 準備完成", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, KerKerInputUI.class), 0);
		n.flags = Notification.FLAG_NO_CLEAR;
		n.setLatestEventInfo(this, "科科輸入法", "點選使用注音輸入法", contentIntent);
		nm.notify(INPUT_NOTIFICATION, n);
		setKeyButtonTexts("BPMF");
		
		final Handler AlertDialogShowCallBack = new Handler();
		
		// Check the latest version
		new Thread() {
		    public void run() {		        
        		try {
            		int currentVerCode = KerKerInputUI.this.getPackageManager().getPackageInfo("idv.Zero.KerKerInput", 0).versionCode;
            		String currentVersion = KerKerInputUI.this.getPackageManager().getPackageInfo("idv.Zero.KerKerInput", 0).versionName;
            		final String remoteVerInfo[] = FileDownload.getContent(updateServiceURL).split("\n");
            		if (remoteVerInfo.length >= 4)
            		{
            		    int remoteVerCode = Integer.parseInt(remoteVerInfo[0]);
            		    if (remoteVerCode > currentVerCode)
            		    {
            		        final String update_str = KerKerInputUI.this.getResources().getText(R.string.update_str).toString().replace("{CURRENT_VER}", currentVersion).replace("{REMOTE_VER}", remoteVerInfo[1]).replace("{UPDATE_MSG}", remoteVerInfo[3].replace("\\n", "\n")+"\n");
            		        final Runnable createAlertDialog = new Runnable() {
            		            public void run() {            		                
                                    new AlertDialog.Builder(c).setTitle(R.string.app_name).setMessage(update_str).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which) {
                                            KerKerInputUI.this.finish();
                                        }
                                    }).setPositiveButton(R.string.download, new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(remoteVerInfo[2]));
                                            KerKerInputUI.this.startActivity(i);
                                            KerKerInputUI.this.finish();
                                        }
                                    }).show();
                                }
                            };
                            AlertDialogShowCallBack.post(createAlertDialog);
            		    }
            		}
        		}
        		catch(Exception ex)
        		{
        		    ex.printStackTrace();
        		}
		    }
        }.start();

    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
		menu.findItem(0).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.finish));
		menu.findItem(1).setIcon(android.R.drawable.ic_menu_delete);
		
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent aboutIntent = new Intent();
			aboutIntent.setClass(getApplicationContext(), KerKerInputAbout.class);
			this.startActivity(aboutIntent);
			return true;
		case 1:
			final NotificationManager nm = (NotificationManager) this
					.getSystemService("notification");
			nm.cancel(INPUT_NOTIFICATION);
			KerKerInputUI.this.finish();
			return true;
		}
		return false;
	}
	
	// Virtual Keyboard
	private void initSoftKeys() {
		candidate_buttons = new ArrayList<Button>();
		candidate_buttons.add((Button) findViewById(R.id.candidate_1));
		candidate_buttons.add((Button) findViewById(R.id.candidate_2));
		candidate_buttons.add((Button) findViewById(R.id.candidate_3));
		candidate_buttons.add((Button) findViewById(R.id.candidate_4));
		candidate_buttons.add((Button) findViewById(R.id.candidate_5));
		candidate_buttons.add((Button) findViewById(R.id.candidate_6));

		ArrayList<KeyTouchHandler> softkeys = new ArrayList<KeyTouchHandler>();
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_bpmf), 0));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_dtnl), 1));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_gkh),	2));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_jqx),	3));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_zhchshr), 4));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_zcs), 5));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_iuyu),	6));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_aoeee), 7));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_aieiauou), 8));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_anenangenger), 9));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_12345), 10));
		softkeys.add(new KeyTouchHandler((ImageButton) findViewById(R.id.softkey_control), 11));

		for (int i=0;i<6;++i) {
			final int idx = i;
			candidate_buttons.get(i).setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v) {
					kIME.handleClick((EditText)findViewById(R.id.text), 60 + idx);
				}
			});
		}
	}
	
	private void setKeyButtonTexts(String type)
	{
		if (type == "BPMF")
		{
			((KeyButton)findViewById(R.id.softkey_bpmf)).setTexts(new String[]{"ㄅ", "ㄆ", "ㄇ", "ㄈ"});
			((KeyButton)findViewById(R.id.softkey_dtnl)).setTexts(new String[]{"ㄉ", "ㄊ", "ㄋ", "ㄌ"});
			((KeyButton)findViewById(R.id.softkey_gkh)).setTexts(new String[]{"ㄍ", "ㄎ", "ㄏ"});
			((KeyButton)findViewById(R.id.softkey_jqx)).setTexts(new String[]{"ㄐ", "ㄑ", "ㄒ"});
			((KeyButton)findViewById(R.id.softkey_zhchshr)).setTexts(new String[]{"ㄓ", "ㄔ", "ㄕ", "ㄖ"});
			((KeyButton)findViewById(R.id.softkey_zcs)).setTexts(new String[]{"ㄗ", "ㄘ", "ㄙ"});
			((KeyButton)findViewById(R.id.softkey_iuyu)).setTexts(new String[]{"一", "ㄨ", "ㄩ"});
			((KeyButton)findViewById(R.id.softkey_aoeee)).setTexts(new String[]{"ㄚ", "ㄛ", "ㄜ", "ㄝ"});
			((KeyButton)findViewById(R.id.softkey_aieiauou)).setTexts(new String[]{"ㄞ", "ㄟ", "ㄠ", "ㄡ"});
			((KeyButton)findViewById(R.id.softkey_anenangenger)).setTexts(new String[]{"ㄢ", "ㄣ", "ㄤ", "ㄥ", "ㄦ"});
			((KeyButton)findViewById(R.id.softkey_12345)).setTexts(new String[]{"一", "二", "三", "四", "輕"});
			((KeyButton)findViewById(R.id.softkey_control)).setTexts(new String[]{"BS", "前", "", "後"});
		}
		else if (type == "ENG")
		{
		
		}
	}
}