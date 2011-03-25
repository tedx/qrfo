package com.tedx.qrfo;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class CalendarEvent extends Activity {

	private static final String LOG_TAG = null;
	private CalendarEvent calendarEvent = this;
	public static final LinkedHashMap<String, String> displayKeysMap = new LinkedHashMap<String, String>() {
		{
			put("end", "End: ");            
			put("start", "Start: ");
			put("location", "Location: ");
			put("description", "Description: ");
			put("title", "Event: ");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event);
		Button okButton = (Button)this.findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject event = genJSONEvent();
				if (event != null) {
					try {
						Intent newQR = new Intent(calendarEvent, NewQR.class);
						newQR.putExtra("url", genQRUrlFromJSON(event));
						newQR.putExtra("obj", event.toString());
						newQR.putExtra("type", "event");
						startActivity(newQR);
					}
					catch (Exception ex) {

					}
				}
			}
		});
	}

	private JSONObject genJSONEvent() {
		JSONObject event = null;
		try {
			event = new JSONObject();
			EditText titleWidget = (EditText)findViewById(R.id.eventTitle);
			String title = titleWidget.getText().toString();
			if (title != null && title.compareTo("") != 0) {
				event.accumulate("title", title);			
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(calendarEvent);
				builder.setTitle(R.string.app_name);
				builder.setMessage(R.string.event_title_required);
				builder.setPositiveButton(R.string.ok, null);
				builder.show();
				return null;
			}

			EditText descriptionWidget = (EditText)findViewById(R.id.eventDescription);
			String description = descriptionWidget.getText().toString();
			if (description != null && description.compareTo("") != 0) {
				event.accumulate("description", description);
			}

			EditText locationWidget = (EditText)findViewById(R.id.eventLocation);
			String location = locationWidget.getText().toString();
			if (location != null && location.compareTo("") != 0) {
				event.accumulate("location", location);			
			}

			DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			DatePicker startDateWidget = (DatePicker)findViewById(R.id.eventStartDate);
			TimePicker startTimeWidget = (TimePicker)findViewById(R.id.eventStartTime);
			event.accumulate("start", String.format("%4d%02d%02d%02d%02d", startDateWidget.getYear(), startDateWidget.getMonth()+1, startDateWidget.getDayOfMonth(), startTimeWidget.getCurrentHour(), startTimeWidget.getCurrentMinute()));			
			Date startDate = df.parse(event.getString("start"));
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			GregorianCalendar local = new GregorianCalendar();
			local.setTimeZone(TimeZone.getTimeZone("GMT"));
			local.setTimeInMillis(cal.getTimeInMillis());
			String start = String.format("%4d%02d%02dT%02d%02d00Z", local.get(Calendar.YEAR), local.get(Calendar.MONTH)+1, local.get(Calendar.DAY_OF_MONTH), local.get(Calendar.HOUR_OF_DAY), local.get(Calendar.MINUTE));
			event.accumulate("startZ", start);

			DatePicker endDateWidget = (DatePicker)findViewById(R.id.eventEndDate);
			TimePicker endTimeWidget = (TimePicker)findViewById(R.id.eventEndTime);
			event.accumulate("end", String.format("%4d%02d%02d%02d%02d", endDateWidget.getYear(), endDateWidget.getMonth()+1, endDateWidget.getDayOfMonth(), endTimeWidget.getCurrentHour(), endTimeWidget.getCurrentMinute()));
			Date endDate = df.parse(event.getString("end"));
			cal.setTime(endDate);
			local.setTimeInMillis(cal.getTimeInMillis());
			String end = String.format("%4d%02d%02dT%02d%02d00Z", local.get(Calendar.YEAR), local.get(Calendar.MONTH)+1, local.get(Calendar.DAY_OF_MONTH), local.get(Calendar.HOUR_OF_DAY), local.get(Calendar.MINUTE));
			event.accumulate("endZ", end);
			Log.d(LOG_TAG, event.toString(4));
		}
		catch (Exception ex) {

		}
		return event;
	}

	private String genQRUrlFromJSON(JSONObject event) throws Exception {
		String qrEventUrl = null;
		// http://chart.apis.google.com/chart?cht=qr&chs=350x350&chl=BEGIN%3AVEVENT%0D%0ASUMMARY%3AAndroid+meeting%0D%0ADTSTART%3A20110321T174800Z%0D%0ADTEND%3A20110321T184800Z%0D%0ALOCATION%3ATed's+house%2C+3604+Turkey+Creek+Dr.%2C+Austin+TX%0D%0ADESCRIPTION%3ADiscuss+Android+development%0D%0AEND%3AVEVENT%0D%0A
		try {
			qrEventUrl = "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chl=BEGIN%3AVEVENT%0D%0A";
			try {
				String title = event.getString("title");
				String encodedString = URLEncoder.encode("SUMMARY:" + title + "\r\n", "UTF-8");

				qrEventUrl = qrEventUrl.concat(encodedString);
			}
			catch (JSONException jex) {
				throw jex;
			}

			try {
				String description = event.getString("description");
				String encodedString = URLEncoder.encode("DESCRIPTION:" + description + "\r\n", "UTF-8");

				qrEventUrl = qrEventUrl.concat(encodedString);
			}
			catch (JSONException jex) {

			}

			try {
				String location = event.getString("location");
				String encodedString = URLEncoder.encode("LOCATION:" + location + "\r\n", "UTF-8");

				qrEventUrl = qrEventUrl.concat(encodedString);
			}
			catch (JSONException jex) {

			}

			try {
				String encodedString = URLEncoder.encode(String.format("DTSTART:%s\r\n", event.get("startZ")), "UTF-8");
				qrEventUrl = qrEventUrl.concat(encodedString);

				encodedString = URLEncoder.encode(String.format("DTEND:%s\r\n", event.get("endZ")), "UTF-8");
				qrEventUrl = qrEventUrl.concat(encodedString);
			}
			catch (JSONException jex) {
				throw jex;
			}

			String encodedString = URLEncoder.encode(String.format("END:VEVENT\r\n"));
			qrEventUrl = qrEventUrl.concat(encodedString);
			Log.d(LOG_TAG, "Event qr generation url: " + qrEventUrl.toString());
		} catch (Exception ex) {
			Log.e(LOG_TAG, (String)(getResources().getText(R.string.url_gen_error)), ex);
			throw ex;
		}
		return qrEventUrl;
	}

}
