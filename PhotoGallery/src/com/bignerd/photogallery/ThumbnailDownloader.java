package com.bignerd.photogallery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.R.string;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.util.Log;

public class ThumbnailDownloader<Token> extends HandlerThread {
	private static final String TAG = "ThumbnailDownloader";

	private static final int MESSAGE_DOWNLOAD = 0;
	

	Handler mHandler;
	Map<Token, String> requestMap = Collections
			.synchronizedMap(new HashMap<Token, String>());
	
	public Handler mRespondHandler;
	


	public Listener<Token> mListener;

	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}

	public interface Listener<Token>{
		void onThumbnailDownloaded(Token imageview ,Bitmap thumbnail);		
	}
	

	public ThumbnailDownloader(String name, int priority) {
		super(name, priority);
		// TODO Auto-generated constructor stub
	}

	public ThumbnailDownloader(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ThumbnailDownloader(Handler responseHandler) {
		super(TAG);
		this.mRespondHandler=responseHandler;
	}

	public void queueThumbnail(Token token, String url) {
		Log.d(TAG, "got an URL " + url);
		requestMap.put(token, url);
		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
		.sendToTarget();
		
	}

	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_DOWNLOAD) {
					Token token = (Token) msg.obj;
					Log.d(TAG,"Got a request for url: " + requestMap.get(token));
					handleRequest(token);
				}
			}
		};

	}

	private void handleRequest(final Token token) {
		try {
			final String urlString = requestMap.get(token);
			if (urlString == null) {
				return;
			}

			byte bitmapBytes[] = new FlickerFetchr().getUrlBytes(urlString);
			final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0,
					bitmapBytes.length);
			Log.d(TAG, "Bitmap created");
			
			mRespondHandler.post(new Runnable() {
				
				@Override
				public void run() {
				if (requestMap.get(token)!=urlString) {
					return;
				}
				requestMap.remove(token);
				mListener.onThumbnailDownloaded(token, bitmap);
					
				}
			});

		} catch (IOException ioe) {
			Log.e(TAG, "Error downloading image",ioe);
		}

	}
	
	
	
	public void clearQueue(){
		
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	

}
