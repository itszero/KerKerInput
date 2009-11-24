package idv.Zero.KerKerInput.Methods;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import idv.Zero.KerKerInput.IKerKerInputMethod;
import idv.Zero.KerKerInput.KerKerInputCore;
import idv.Zero.KerKerInput.R;

import com.google.zxing.Result;
import com.google.zxing.client.android.*;

public class BarcodeInput extends IKerKerInputMethod implements ASyncBarcodeDecoder, SurfaceHolder.Callback {
	private CaptureActivityHandler handler;
	private ViewGroup _inputView;
	private boolean hasSurface = false;
	private ViewfinderView _finderView = null;
	private Result lastResult = null;
	private ImageButton btnNextIME = null;
	private SoundPool sndPool = null;
	private HashMap<Integer, Integer> sndPoolMap = null;
	private SurfaceView surfaceView;
	private FrameLayout _finderLayout;

	public void initInputMethod(KerKerInputCore core)
	{
		super.initInputMethod(core);
	}
	
	@Override
	public String getName() {
		return "條碼輸入法";
	}
	
	public boolean hasCustomInputView() { return true; }
	
	public View onCreateInputView()
	{
		_core.getFrontend().setCandidatesViewShown(false);
		_inputView = (ViewGroup) View.inflate(_core.getFrontend(), R.layout.barcode_input_view, null);
		_finderView = (ViewfinderView)_inputView.findViewById(R.id.viewfinder_view);
		_finderLayout = (FrameLayout)_inputView.findViewById(R.id.finderLayout);
		hasSurface = false;
		return _inputView;
	}

	@Override
	public void onEnterInputMethod() {
		lastResult = null;
		_core.getFrontend().setCandidatesViewShown(false);
		CameraManager.init(_core.getFrontend().getApplication());
		btnNextIME = (ImageButton) _finderLayout.findViewById(R.id.btnNextIME);
		btnNextIME.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				_core.requestNextInputMethod();
			}
		});
	    surfaceView = (SurfaceView) _inputView.findViewById(R.id.preview_view);
	    SurfaceHolder surfaceHolder = surfaceView.getHolder();
	    surfaceHolder.addCallback(this);
	    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);	
	    initSounds();
	}

	@Override
	public boolean onKeyEvent(int keyCode, int[] keyCodes) {		
		return false;
	}

	@Override
	public void onLeaveInputMethod() {
		SurfaceView surfaceView = (SurfaceView) _inputView.findViewById(R.id.preview_view);
	    SurfaceHolder surfaceHolder = surfaceView.getHolder();
	    surfaceHolder.removeCallback(this);
	    
		releaseSounds();
		hasSurface = false;
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	public Handler getHandler() {
		return handler;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (Exception ioe) {
			return;
		}
		
		if (handler == null) {
			boolean beginScanning = lastResult == null;
			handler = new CaptureActivityHandler(this, beginScanning);
		}
		drawViewfinder();
	}

	public void drawViewfinder() {
		_finderView.drawViewfinder();
	}

	public void handleDecode(Result obj, Bitmap barcode) {
		if (lastResult != null && obj.getText().equalsIgnoreCase(lastResult.getText()))
			return;
		
		playAudioResource(R.raw.beep);
		((Vibrator)_core.getFrontend().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
	    lastResult = obj;
	    _core.commitText(obj.getText() + "\n");
	}

	public void initSounds()
	{
		if(sndPool == null)
			sndPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
		
		if (sndPoolMap != null)
			return;
		
		sndPoolMap = new HashMap<Integer, Integer>();
		sndPoolMap.put(R.raw.beep, sndPool.load(_core.getFrontend().getResources().openRawResourceFd(R.raw.beep), 1));
	}
	
	public void releaseSounds()
	{
		if (sndPoolMap != null)
		{
			sndPoolMap.clear();
			sndPoolMap = null;
		}
		
		if (sndPool != null)
		{
			sndPool.release();
			sndPool = null;
		}
	}
	
	private void playAudioResource(final int resourceID)
	{
		if (sndPool == null || sndPoolMap == null)
			initSounds();
		
		Integer rid = sndPoolMap.get(resourceID);
		if (rid != null)
			sndPool.play(rid, 1, 1, 1, 0, 1f);
	}
		
	/* Not Used */
	@Override
	public void setCurrentPage(int currentPage) { }

	@Override
	public void setTotalPages(int totalPages) {	}

	@Override
	public void commitCandidate(int currentCandidate) {	}

	@Override
	public void commitCurrentComposingBuffer() { }

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { drawViewfinder(); }
}
