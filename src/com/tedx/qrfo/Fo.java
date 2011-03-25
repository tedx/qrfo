package com.tedx.qrfo;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Fo extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainoptionmenu, menu);
		return true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initUI();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showqr);
		initUI();
	}

	private void initUI() {
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		Cursor cursor = db.getDefault();
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
			ll.removeAllViewsInLayout();
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

			ImageView bmImage = new ImageView(getApplicationContext()); // (ImageView)findViewById(R.id.image);
			bmImage.setScaleType(ScaleType.CENTER);
			bmImage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			BitmapFactory.Options bmOptions;
			bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;
			byte[] qrcode = cursor.getBlob(cursor.getColumnIndex(QRCodeDbAdapter.QRCODE));
			Bitmap bm = BitmapFactory.decodeByteArray(qrcode, 0, qrcode.length, bmOptions);
			bmImage.setImageBitmap(bm);
			ll.addView(bmImage);
			/*
			ImageView bmImage = (ImageView)findViewById(R.id.image);
			BitmapFactory.Options bmOptions;
			bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;
			byte[] qrcode = cursor.getBlob(cursor.getColumnIndex(QRCodeDbAdapter.QRCODE));
			Bitmap bm = BitmapFactory.decodeByteArray(qrcode, 0, qrcode.length, bmOptions);
			bmImage.setImageBitmap(bm);
			TextView title = (TextView)findViewById(R.id.title);
			title.setText(cursor.getString(cursor.getColumnIndex(QRCodeDbAdapter.TITLE)));
			*/
			cursor.close();
			db.close();				
		}
		else {
			cursor.close();
			db.close();		
			Intent genQR = new Intent(this, GenQR.class);
			startActivity(genQR);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.newqrcode:
			Intent genQR = new Intent(this, GenQR.class);
			startActivity(genQR);
			break;
		case R.id.listqr:
			Intent listQR = new Intent(this, ListQR.class);
			startActivity(listQR);
			break;
		}
		return super.onOptionsItemSelected(item);
	}


}