package com.tedx.qrfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LoadImage implements Runnable {
	private String URL;
	private BitmapFactory.Options options;
	private Handler handler;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Message msg = new Message();
		try {
			Bitmap bm = loadImage();
			msg.arg1 = 0;
			Bundle data = new Bundle();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
			byte[] bitmapdata = bos.toByteArray();
			data.putByteArray("bitmap", bitmapdata);
			msg.setData(data);
			handler.sendMessage(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			msg.arg1 = -1;
			handler.sendMessage(msg);
		}
	}

	public LoadImage(String URL, BitmapFactory.Options options, Handler handler) {
		super();
		// TODO Auto-generated constructor stub
		this.URL = URL;
		this.options = options;
		this.handler = handler;
	}

	private Bitmap loadImage() throws Exception
	{       
		Bitmap bitmap = null;
		InputStream in = null;       
		in = openHttpConnection(URL);
		bitmap = BitmapFactory.decodeStream(in, null, options);
		in.close();
		return bitmap;               
	}

	private InputStream openHttpConnection(String strURL) throws Exception {
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		try {
			HttpURLConnection httpConn = (HttpURLConnection)conn;
			httpConn.setConnectTimeout(4000);
			httpConn.setReadTimeout(10000);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpConn.getInputStream();
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		return inputStream;
	}

}
