package com.bignerd.photogallery;

import java.util.ArrayList;

import android.R.anim;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoGalleryFragment extends Fragment {

	private static final String TAG = "PhotogalleryFragment";

	GridView mGridView;
	ArrayList<GalleryItem> mItems;
	ThumbnailDownloader<ImageView> mThumbnailThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
		
		mThumbnailThread=new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {

			@Override
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				// TODO Auto-generated method stub
				if (isVisible()) {
					imageView.setImageBitmap(thumbnail);
				}
			}
			
		});
		mThumbnailThread.start();
		mThumbnailThread.getLooper();
		
		Log.d(TAG,"Background thread started");
		

	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_phote_gallery,
				container, false);

		mGridView = (GridView) view.findViewById(R.id.gridView);

		setupAdapter();

		return view;
	}

	private class FetchItemsTask extends
			AsyncTask<Void, Void, ArrayList<GalleryItem>> {
		@Override
		protected ArrayList<GalleryItem> doInBackground(Void... params) {
			// try {
			// String result=new
			// FlickerFetchr().getUrl("http://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=11d0e5916d7df8dd430226647c20f2fe&extras=url_s");
			// //String result=new FlickerFetchr().getUrl("http://google.com/");
			// //String result=new
			// FlickerFetchr().getUrl("https://www.flickr.com/");
			//
			// Log.d(TAG, result);
			//
			// } catch (Exception e) {
			// Log.e(TAG, "failed to fetche url");
			// }

			return new FlickerFetchr().fetchItems();

		}

		@Override
		protected void onPostExecute(ArrayList<GalleryItem> result) {
			mItems = result;
			setupAdapter();
		}

	}

	private void setupAdapter() {

		if (getActivity() == null || mGridView == null) {
			return;
		}

		if (mItems != null) {
//			mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
//					android.R.layout.simple_gallery_item, mItems));
			
			mGridView.setAdapter(new GalleryItemAdapter(mItems));
		} else {
			mGridView.setAdapter(null);
		}

	}

	public class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
		public GalleryItemAdapter(ArrayList<GalleryItem> items) {
			super(getActivity(), 0, items);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.gallery_item,parent, false);

			}
			
			ImageView imageView=(ImageView) convertView
					.findViewById(R.id.gallery_item_imageView);
			imageView.setImageResource(R.drawable.xiada);
			
			GalleryItem item=getItem(position);
			mThumbnailThread.queueThumbnail(imageView, item.getmUrl());

			return convertView;
		}

	}
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mThumbnailThread.quit();
		Log.d(TAG, "Background thread destroyed!");
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		mThumbnailThread.clearQueue();
	}

}
