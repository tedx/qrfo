package com.tedx.qrfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import org.json.JSONObject;

public class ContactInfo extends Activity {
	private static final String LOG_TAG = "ContactInfo";
	public static final LinkedHashMap<String, String> displayKeysMap = new LinkedHashMap<String, String>() {
		{
			put("address2", "City, State: ");            
			put("address", "Street: ");            
			put("url", "URL: ");
			put("email", "Email: ");
			put("phone", "Phone: ");
			put("name", "Name: ");
		}
	};

	private ContactInfo contactInfo = this;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact);
		Button okButton = (Button)this.findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject contact = genJSONContact();
				if (contact != null) {
					try {
						Intent newQR = new Intent(contactInfo, NewQR.class);
						newQR.putExtra("url", genQRUrlFromJSON(contact));
						newQR.putExtra("obj", contact.toString());
						newQR.putExtra("type", "contact");
						startActivity(newQR);
					}
					catch (Exception ex) {

					}
				}
			}
		});
	}

	private JSONObject genJSONContact() {
		JSONObject contact = null;
		contact = new JSONObject();
		try {
			EditText nameWidget = (EditText)findViewById(R.id.name);
			String name = nameWidget.getText().toString();
			if (name != null && name.compareTo("") != 0) {
				contact.accumulate("name", name);
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(contactInfo);
				builder.setTitle(R.string.app_name);
				builder.setMessage(R.string.name_required);
				builder.setPositiveButton(R.string.ok, null);
				builder.show();
				return null;
			}

			EditText phoneWidget = (EditText)findViewById(R.id.phoneNumber);
			String phone = phoneWidget.getText().toString();
			if (phone != null && phone.compareTo("") != 0) {
				contact.accumulate("phone", phone);
			}

			EditText urlWidget = (EditText)findViewById(R.id.url);
			String url = urlWidget.getText().toString();
			if (url != null && url.compareTo("") != 0) {
				contact.accumulate("url", url);
			}

			EditText emailWidget = (EditText)findViewById(R.id.email);
			String email = emailWidget.getText().toString();
			if (email != null && email.compareTo("") != 0) {
				contact.accumulate("email", email);
			}

			EditText addressWidget = (EditText)findViewById(R.id.address);
			String address = addressWidget.getText().toString();
			if (address != null && address.compareTo("") != 0) {
				contact.accumulate("address", address);
			}

			EditText address2Widget = (EditText)findViewById(R.id.address2);
			String address2 = address2Widget.getText().toString();
			if (address2 != null && address2.compareTo("") != 0) {
				contact.accumulate("address2", address2);
			}
		}
		catch (Exception ex) {
			contact = null;
		}
		return contact;
	}

	private String genQRUrlFromJSON(JSONObject contact) {
		String qrContactUrl = "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chl=MECARD%3A";
		String name = null;
		try {
			name = contact.getString("name");
			String encodedString = URLEncoder.encode("N:" + name + ";", "UTF-8");

			qrContactUrl = qrContactUrl.concat(encodedString);
		} catch (Exception ex) {
		}

		String phone = null;

		try {
			phone = contact.getString("phone");
			String encodedString = URLEncoder.encode("TEL:" + phone + ";", "UTF-8");

			qrContactUrl = qrContactUrl.concat(encodedString);
		} catch (Exception ex) {
		}

		try {
			String url = null;
			url = contact.getString("url");
			String encodedString = URLEncoder.encode("URL:" + url + ";", "UTF-8");

			qrContactUrl = qrContactUrl.concat(encodedString);
		} catch (Exception ex) {
		}

		String email = null;
		try {
			email = contact.getString("email");
			String encodedString = URLEncoder.encode("EMAIL:" + email + ";", "UTF-8");

			qrContactUrl = qrContactUrl.concat(encodedString);
		} catch (Exception ex) {
		}

		String address = null;
		try {
			address = contact.getString("address");
		} catch (Exception ex) {
		}

		String address2 = null;
		try {
			address2 = contact.getString("address2");
		} catch (Exception ex) {
		}

		if (address2 != null)
			if (address == null)
				address = address2;
			else
				address = address + ", " + address2;
		if (address != null) {
			String encodedString = null;
			try {
				encodedString = URLEncoder.encode("ADR:" + address + ";", "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			qrContactUrl = qrContactUrl.concat(encodedString);
		}
		Log.d(LOG_TAG, "contact qr generation url: " + qrContactUrl.toString());
		return qrContactUrl.toString();
	}
}
