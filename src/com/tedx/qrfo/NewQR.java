package com.tedx.qrfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewQR extends Activity {
	private static final String LOG_TAG = "NewQR";
	private Bitmap bm;
	private String title;
	private String type;
	private String url;
	private JSONObject jo;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private ProgressDialog loadProgressDialog;
	private NewQR newQR = this;

	private Handler loadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			loadProgressDialog.dismiss();
			if (msg.arg1 == 0) {
				qrLoaded(msg.getData().getByteArray("bitmap"));
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(newQR);
				builder.setTitle(R.string.app_name);
				builder.setMessage(R.string.http_get_error);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						newQR.finish();
					}
				});
				builder.show();
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showqr);
		Intent intent = getIntent();
		url = intent.getStringExtra("url");
		type = intent.getStringExtra("type");

		try {
			jo = new JSONObject(intent.getStringExtra("obj"));
			if (type.compareTo("event") == 0)
				title = jo.getString("title");
			else
				title = jo.getString("name");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BitmapFactory.Options bmOptions;
		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;

		loadProgressDialog = ProgressDialog.show( this, " " , (String)(getResources().getText(R.string.gen_wait_msg)), true);

		LoadImage li = new LoadImage(url, bmOptions, loadHandler);
		Thread t = new Thread(li);
		t.start();

	}

	private void qrLoaded(byte[] bitmapData) {
		BitmapFactory.Options bmOptions;
		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, bmOptions);
		ImageView bmImage = (ImageView)findViewById(R.id.image);
		bmImage.setImageBitmap(bm);	
		TextView titleWidget = (TextView)findViewById(R.id.title);
		titleWidget.setText(title);
		// prepare the alert box
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

		// set the message to display
		alertbox.setMessage((String)(getResources().getText(R.string.save)));

		// set a positive/yes button and create a listener
		alertbox.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				saveQR(0);
			}
		});

		// set a negative/no button and create a listener
		alertbox.setNegativeButton(R.string.cancel, null);
		// display box
		alertbox.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.newoptions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.save:
			saveQR(0);
			return true;
		case R.id.savedefault:
			saveQR(1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void saveQR(int defaultFlag) {
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		if (defaultFlag == 1) {
			Cursor cursor = db.getDefault();
			if (cursor.getCount() == 1) {
				long defaultId = cursor.getLong(cursor.getColumnIndex(QRCodeDbAdapter.ROWID));

				cursor.close();
				Log.d(LOG_TAG, "update " + Long.toString(defaultId));
				db.updateDefault(defaultId, 0);
			}
			cursor.close();
		}
		else {
			Cursor cursor = db.getDefault();
			if (cursor.getCount() == 0) {
				defaultFlag = 1;
			}
			cursor.close();
		}

		Cursor cursor = db.getURL(url);
		if (cursor.getCount() == 0) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
			byte[] bitmapdata = bos.toByteArray();

			db.insert(type, title, bitmapdata, url, defaultFlag, jo.toString());
			Log.d(LOG_TAG, "insert " + title);
		}
		cursor.close();
		db.close();
		if (type.compareTo("event") == 0) {
			String startZ = null;
			String endZ = null;
			String location = null;
			String description = null;
			try {
				startZ = jo.getString("startZ");
			} catch (JSONException e) {
			}
			try {
				endZ = jo.getString("endZ");
			} catch (JSONException e) {
			}
			try {
				location = jo.getString("location");
			} catch (JSONException e) {
			}
			try {
				description = jo.getString("description");
			} catch (JSONException e) {
			}
			addCalendarEvent(title,
					startZ,
					endZ,
					location,
					description);
		}
		finish();
	}

	/**
	 * Sends an intent to create a new calendar event by prepopulating the Add Event UI. Older
	 * versions of the system have a bug where the event title will not be filled out.
	 *
	 * @param summary A description of the event
	 * @param start   The start time as yyyyMMdd or yyyyMMdd'T'HHmmss or yyyyMMdd'T'HHmmss'Z'
	 * @param end     The end time as yyyyMMdd or yyyyMMdd'T'HHmmss or yyyyMMdd'T'HHmmss'Z'
	 * @param location a text description of the event location
	 * @param description a text description of the event itself
	 */
	final void addCalendarEvent(String summary,
			String start,
			String end,
			String location,
			String description) {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("beginTime", calculateMilliseconds(start));
		boolean allDay = start.length() == 8;
		if (allDay) {
			intent.putExtra("allDay", true);
		}
		if (end == null) {
			end = start;
		}
		long endMilliseconds = calculateMilliseconds(end);
		if (allDay) {
			// Possible workaround when allDay isn't used properly
			endMilliseconds = lastSecondOfDay(endMilliseconds);
		}
		intent.putExtra("endTime", endMilliseconds);
		intent.putExtra("title", summary);
		intent.putExtra("eventLocation", location);
		intent.putExtra("description", description);
		launchIntent(intent);
	}

	private static long calculateMilliseconds(String when) {
		if (when.length() == 8) {
			// Only contains year/month/day
			Date date;
			synchronized (DATE_FORMAT) {
				date = DATE_FORMAT.parse(when, new ParsePosition(0));
			}
			return date.getTime();
		} else {
			// The when string can be local time, or UTC if it ends with a Z
			Date date;
			synchronized (DATE_TIME_FORMAT) {
				date = DATE_TIME_FORMAT.parse(when.substring(0, 15), new ParsePosition(0));
			}
			long milliseconds = date.getTime();
			if (when.length() == 16 && when.charAt(15) == 'Z') {
				Calendar calendar = new GregorianCalendar();
				int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
				milliseconds += offset;
			}
			return milliseconds;
		}
	}

	private static long lastSecondOfDay(long time) {
		Calendar timeInDay = Calendar.getInstance();
		timeInDay.setTimeInMillis(time);
		timeInDay.set(Calendar.HOUR_OF_DAY, timeInDay.getActualMaximum(Calendar.HOUR_OF_DAY));
		timeInDay.set(Calendar.MINUTE, timeInDay.getActualMaximum(Calendar.MINUTE));
		timeInDay.set(Calendar.SECOND, timeInDay.getActualMaximum(Calendar.SECOND));
		return timeInDay.getTimeInMillis();
	}


	void launchIntent(Intent intent) {
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			Log.d(LOG_TAG, "Launching intent: " + intent + " with extras: " + intent.getExtras());
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.app_name);
				builder.setMessage(R.string.msg_intent_failed);
				builder.setPositiveButton(R.string.ok, null);
				builder.show();
			}
		}
	}
}
