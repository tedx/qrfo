package com.tedx.qrfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleExpandableListAdapter;

public class ListQR extends ExpandableListActivity {

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setContentView(R.layout.listqr);
		childMap.clear();
		SimpleExpandableListAdapter expListAdapter =
			new SimpleExpandableListAdapter(
					this,
					createGroupList(),	// groupData describes the first-level entries
					R.layout.child_row,	// Layout for the first-level entries
					new String[] { "type" },	// Key in the groupData maps to display
					new int[] { R.id.childname },		// Data under "type" key goes into this TextView
					createChildList(),	// childData describes second-level entries
					R.layout.child_row,	// Layout for second-level entries
					new String[] { "title" },	// Keys in childData maps to display
					new int[] { R.id.childname }	// Data under the keys above go into these TextViews
			);
		setListAdapter( expListAdapter );
		registerForContextMenu(getExpandableListView());
	}

	private static final int MENU_DISPLAY = 1;
	private static final int MENU_DELETE = 2;
	private HashMap childMap = new HashMap();
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		HashMap m = (HashMap) childMap.get(String.format("%d,%d", groupPosition, childPosition));
		long rowid = (Long) m.get("rowid");
		Intent showQR = new Intent(this, ShowQR.class);
		showQR.putExtra("id", rowid);
		startActivity(showQR);
		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info =
			(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type =
			ExpandableListView.getPackedPositionType(info.packedPosition);
		int group =
			ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int child =
			ExpandableListView.getPackedPositionChild(info.packedPosition);
		//Only create a context menu for child items
		if (type == 1) {
			//Array created earlier when we built the expandable list
			HashMap m = (HashMap) childMap.get(String.format("%d,%d", group, child));
			String title = (String) m.get("title");
			menu.setHeaderTitle(title);
			menu.add(0, MENU_DISPLAY, 0, "Display");
			menu.add(0, MENU_DELETE, 1, "Delete");
		}
	}

	public boolean onContextItemSelected(MenuItem menuItem) {
		ExpandableListContextMenuInfo info =
			(ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		int groupPos = 0, childPos = 0;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		}
		//Pull values from the array we built when we created the list
		HashMap m = (HashMap) childMap.get(String.format("%d,%d", groupPos, childPos));
		long rowId = (Long) m.get("rowid");
		switch (menuItem.getItemId()) {
		case MENU_DISPLAY:
			displayQR(rowId);
			return true;
		case MENU_DELETE:
			deleteQR(rowId);
			return true;
			//etcÉ.
		default:
			return super.onContextItemSelected(menuItem);
		}
	}

	private void deleteQR(long rowId) {
		// TODO Auto-generated method stub
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		db.delete(rowId);
		db.close();
	}

	private void displayQR(long rowId) {
		// TODO Auto-generated method stub
		Intent showQR = new Intent(this, ShowQR.class);
		showQR.putExtra("id", rowId);
		startActivity(showQR);		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.listqr);
		childMap.clear();
		SimpleExpandableListAdapter expListAdapter =
			new SimpleExpandableListAdapter(
					this,
					createGroupList(),	// groupData describes the first-level entries
					R.layout.child_row,	// Layout for the first-level entries
					new String[] { "type" },	// Key in the groupData maps to display
					new int[] { R.id.childname },		// Data under "type" key goes into this TextView
					createChildList(),	// childData describes second-level entries
					R.layout.child_row,	// Layout for second-level entries
					new String[] { "title" },	// Keys in childData maps to display
					new int[] { R.id.childname }	// Data under the keys above go into these TextViews
			);
		setListAdapter( expListAdapter );
		registerForContextMenu(getExpandableListView());
	}

	/**
	 * Creates the group list out of the colors[] array according to
	 * the structure required by SimpleExpandableListAdapter. The resulting
	 * List contains Maps. Each Map contains one entry with key "colorName" and
	 * value of an entry in the colors[] array.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List createGroupList() {
		ArrayList result = new ArrayList();
		HashMap m = new HashMap();
		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		Cursor cursor = db.getType("contact");
		if (cursor.getCount() > 0) {
			m.put( "type", "Contacts");
			result.add( m );
			m = new HashMap();
		}
		cursor.close();
		cursor = db.getType("event");
		if (cursor.getCount() > 0) {
			m.put( "type", "Events");
			result.add( m );
		}
		cursor.close();
		return (List)result;
	}

	/**
	 * Creates the child list out of the shades[] array according to the
	 * structure required by SimpleExpandableListAdapter. The resulting List
	 * contains one list for each group. Each such second-level group contains
	 * Maps. Each such Map contains two keys: "shadeName" is the name of the
	 * shade and "rgb" is the RGB value for the shade.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List createChildList() {
		int group = 0;
		int child = 0;
		ArrayList result = new ArrayList();

		QRCodeDbAdapter db = new QRCodeDbAdapter(getApplicationContext());
		db.open();
		Cursor cursor = db.getType("contact");
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			ArrayList secList = new ArrayList();
			while (cursor.isAfterLast() == false) {
				HashMap m = new HashMap();
				m.put( "title", cursor.getString(cursor.getColumnIndex(QRCodeDbAdapter.TITLE)) );
				m.put("rowid", cursor.getLong(cursor.getColumnIndex(QRCodeDbAdapter.ROWID)));
				secList.add( m );
				childMap.put(String.format("%d,%d", group,child), m);
				child++;
				cursor.moveToNext();
			}
			result.add( secList );
		}
		group++;
		child = 0;
		cursor.close();
		cursor = db.getType("event");
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			ArrayList secList = new ArrayList();
			while (cursor.isAfterLast() == false) {
				HashMap m = new HashMap();
				m.put( "title", cursor.getString(cursor.getColumnIndex(QRCodeDbAdapter.TITLE)) );
				m.put("rowid", cursor.getLong(cursor.getColumnIndex(QRCodeDbAdapter.ROWID)));
				secList.add( m );
				childMap.put(String.format("%d,%d", group,child), m);
				child++;
				cursor.moveToNext();
			}
			result.add( secList );
		}
		cursor.close();
		db.close();
		return result;
	}

}
