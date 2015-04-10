package com.bignerd.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.R.integer;
import android.net.Uri;
import android.util.Log;

public class FlickerFetchr {
	private static final String TAG = "FlickerFetchr";

	private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
	private static final String API_KEY = "11d0e5916d7df8dd430226647c20f2fe";
	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
	private static final String PARAM_EXTRAS = "extras";
	private static final String EXTRAL_SMALL_URL = "url_s";

	private static final String XML_PHOTO = "photo";

	public FlickerFetchr() {

	}

	public byte[] getUrlBytes(String urlSpec) throws IOException {

		URL url = new URL(urlSpec);

		byte localhost[] = { 127, 0, 0, 1 };
		int porxyPort = 8590;

		Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(
				InetAddress.getByAddress(localhost), porxyPort));

		// System.out.println("localhost="+
		// InetAddress.getByAddress(localhost));

//		HttpURLConnection connection = (HttpURLConnection) url
//				.openConnection(proxy);
		
		HttpURLConnection connection = (HttpURLConnection) url
				.openConnection();

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			InputStream inputStream = connection.getInputStream();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.d(TAG,
						"connection.getResponseCode() ="
								+ connection.getResponseCode());
				return null;
			}

			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();

			return outputStream.toByteArray();

		} catch (Exception e) {

			Log.e(TAG, urlSpec + " connection failed!");
			connection.disconnect();
		}

		return null;

	}

	public String getUrl(String urlSpec) throws IOException {
		return new String(getUrlBytes(urlSpec));
	}

	public ArrayList<GalleryItem> fetchItems() {
		ArrayList<GalleryItem> items=new ArrayList<GalleryItem>();
		
		try {
			String urlString = Uri.parse(ENDPOINT).buildUpon()
					.appendQueryParameter("method", METHOD_GET_RECENT)
					.appendQueryParameter("api_key", API_KEY)
					.appendQueryParameter(PARAM_EXTRAS, EXTRAL_SMALL_URL)
					.build().toString();

			String xmlString = getUrl(urlString);

			Log.d(TAG, "Received xml :" + xmlString);
			
			
			XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
			XmlPullParser parser=factory.newPullParser();
			parser.setInput(new StringReader(xmlString));
			
			parseItems(items, parser);
			

		} catch (IOException ioe) {
			Log.e(TAG, "Failed to fetch items", ioe);

		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return items;

	}

	public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser)
			throws XmlPullParserException, IOException {
		int eventType = parser.next();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG
					&& XML_PHOTO.equals(parser.getName())) {
				String id = parser.getAttributeValue(null, "id");
				String caption = parser.getAttributeValue(null, "title");
				String smallUrl = parser.getAttributeValue(null,
						EXTRAL_SMALL_URL);

				GalleryItem item = new GalleryItem();
				item.setmId(id);
				item.setmCaption(caption);
				item.setmUrl(smallUrl);

				items.add(item);

			}
			eventType=parser.next();
		}

	}

}
