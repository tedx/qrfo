package com.tedx.qrfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class QRCodeDbAdapter 
{
	public static final String ROWID = "_id";
	public static final String TYPE = "type"; // contact or event
	public static final String TITLE = "title";
	public static final String QRCODE = "qrcode";
	public static final String URL = "url";
	public static final String DEFAULT = "defaultFlag";
	public static final String DATA = "data";
	private static final String TAG = "QRCodeDbAdapter";

	private static final String DATABASE_NAME = "qrcodes";
	private static final String DATABASE_TABLE = "codes";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE =
		"create table " + DATABASE_TABLE + " (" + ROWID + " integer primary key autoincrement, "
		+ TYPE + " text not null, " + TITLE + " text not null, " 
		+ URL + " text not null, " + DEFAULT + " integer, " + QRCODE + " blob not null, " + DATA + " text not null);";

	private final Context context; 

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public QRCodeDbAdapter(Context ctx) 
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, 
				int newVersion) 
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion 
					+ " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}    

	//---opens the database---
	public QRCodeDbAdapter open() throws SQLException 
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}

	//---closes the database---    
	public void close() 
	{
		DBHelper.close();
	}

	//---insert a title into the database---
	public long insert(String type, String title, byte[] qrcode, String url, int defaultFlag, String data) 
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(TYPE, type);
		initialValues.put(TITLE, title);
		initialValues.put(QRCODE, qrcode);
		initialValues.put(URL, url);
		initialValues.put(DEFAULT, defaultFlag);
		initialValues.put(DATA, data);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	//---deletes a row---
	public boolean delete(long rowId) 
	{
		return db.delete(DATABASE_TABLE, ROWID + 
				"=" + rowId, null) > 0;
	}

	//---retrieves all rows---
	public Cursor getAll() 
	{
		return db.query(DATABASE_TABLE, new String[] {
				ROWID, 
				TYPE,
				TITLE,
				URL,
				DEFAULT,
				QRCODE,
				DATA}, 
				null, 
				null, 
				null, 
				null, 
				null,
				null);
	}

	//---retrieves a particular title---
	public Cursor get(long rowId) throws SQLException 
	{
		Cursor mCursor =
			db.query(true, DATABASE_TABLE, new String[] {
					ROWID,
					TYPE, 
					TITLE,
					URL,
					DEFAULT,
					QRCODE,
					DATA
			}, 
			ROWID + "=" + rowId, 
			null,
			null, 
			null, 
			null,
			null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	//---updates a title---
	public boolean update(long rowId, String type, 
			String title, byte[] qrcode, String url, int defaultFlag, String data) 
	{
		ContentValues args = new ContentValues();
		args.put(TYPE, type);
		args.put(TITLE, title);
		args.put(URL, url);
		args.put(DEFAULT, defaultFlag);
		args.put(QRCODE, qrcode);
		args.put(DATA, data);
		return db.update(DATABASE_TABLE, args, 
				ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateDefault(long rowId, int defaultFlag) 
	{
		ContentValues args = new ContentValues();
		args.put(DEFAULT, defaultFlag);
		return db.update(DATABASE_TABLE, args, 
				ROWID + "=" + rowId, null) > 0;
	}

	//---retrieves a particular type---
	public Cursor getType(String type) throws SQLException 
	{
		Cursor mCursor =
			db.query(true, DATABASE_TABLE, new String[] {
					ROWID,
					TYPE, 
					TITLE,
					URL,
					DEFAULT,
					QRCODE,
					DATA
			}, 
			TYPE + "='" + type + "'", 
			null,
			null, 
			null, 
			null,
			null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	//---retrieves a particular URL---
	public Cursor getURL(String url) throws SQLException 
	{
		Cursor mCursor =
			db.query(true, DATABASE_TABLE, new String[] {
					ROWID,
					TYPE, 
					TITLE,
					URL,
					DEFAULT,
					QRCODE,
					DATA
			}, 
			URL + "='" + url + "'", 
			null,
			null, 
			null, 
			null,
			null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	//---retrieves a particular URL---
	public Cursor getDefault() throws SQLException 
	{
		Cursor mCursor =
			db.query(true, DATABASE_TABLE, new String[] {
					ROWID,
					TYPE, 
					TITLE,
					URL,
					DEFAULT,
					QRCODE,
					DATA
			}, 
			DEFAULT + "=1", 
			null,
			null, 
			null, 
			null,
			null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
}

