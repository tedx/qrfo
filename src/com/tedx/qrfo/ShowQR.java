package com.tedx.qrfo;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ShowQR extends Activity {

	private long id;

	TextView text;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showqr);
		Intent intent = getIntent();
		id = intent.getLongExtra("id", 0);
		showQR(id);
	}
	
	private void showQR(long id) {
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		Cursor cursor = db.get(id);
		if (cursor.getCount() == 1) {
			JSONObject jo = null;
			String data = cursor.getString(cursor.getColumnIndex(QRCodeDbAdapter.DATA));
			try {
				jo = new JSONObject(data);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LinearLayout ll = (LinearLayout)findViewById(R.id.showLayout);
			LinkedHashMap<String, String> map = null;
			String type = cursor.getString(cursor.getColumnIndex(QRCodeDbAdapter.TYPE));
			if (type.compareTo("event") == 0) {
				map = CalendarEvent.displayKeysMap;
			}
			else {
				map = ContactInfo.displayKeysMap;
			}
			
			for (Iterator<String> it=map.keySet().iterator(); it.hasNext(); ) {
				String key = (String)it.next();
				String label = (String)map.get(key);
				TextView tv = new TextView(getApplicationContext());
				try {
					tv.setText(label + jo.getString(key));
				} catch (JSONException e) {
					continue;
				}
				ll.addView(tv, 0);
			}

			ImageView bmImage = (ImageView)findViewById(R.id.image);
			BitmapFactory.Options bmOptions;
			bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;
			byte[] qrcode = cursor.getBlob(cursor.getColumnIndex(QRCodeDbAdapter.QRCODE));
			Bitmap bm = BitmapFactory.decodeByteArray(qrcode, 0, qrcode.length, bmOptions);
			bmImage.setImageBitmap(bm);
		}
		cursor.close();
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.showoptions, menu);
	  return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.makedefault:
	        makeDefaultQR();
	        return true;
	    case R.id.delete:
	        deleteQR();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void deleteQR() {
		// TODO Auto-generated method stub
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		db.delete(id);
		db.close();
		finish();
	}

	private void makeDefaultQR() {
		// TODO Auto-generated method stub
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		Cursor cursor = db.get(id);
		if (cursor.getCount() == 1) {
			int defaultFlag = cursor.getInt(cursor.getColumnIndex(QRCodeDbAdapter.DEFAULT));
			cursor.close();
			if (defaultFlag == 1) {
				db.close();
				return;
			}
			cursor = db.getDefault();
			if (cursor.getCount() == 1) {
				long defaultId = cursor.getLong(cursor.getColumnIndex(QRCodeDbAdapter.ROWID));
				
				cursor.close();
				db.updateDefault(defaultId, 0);
			}
			db.updateDefault(id, 1);
		}
		db.close();
		finish();		
	}
	
}
