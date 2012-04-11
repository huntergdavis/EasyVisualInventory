package com.hunterdavis.easyvisualinventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import com.hunterdavis.easyinventory.R;


public class InventorySQLHelper extends android.database.sqlite.SQLiteOpenHelper {
	private static final String DATABASE_NAME = "inventory.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "inventory";

	// Columns
	public static final String BUYPRICE = "buyprice";
	public static final String SELLPRICE = "sellprice";
	public static final String TOTALPROFIT = "totalprofit";
	public static final String AMOUNT = "amount";
	public static final String URI = "uri";
	public static final String LOGFILE = "logfile";
	public static final String DIVISOR = "divisor";
	public static final String UNITOFMEASUREMENT = "units";
	public static final String NAME = "name";

	public InventorySQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement, " + BUYPRICE
				+ " float, " + SELLPRICE + " float, " + TOTALPROFIT
				+ " float, " + AMOUNT + " integer, " + URI + " text, "
				+ LOGFILE + " text, " + DIVISOR + " integer, "
				+ UNITOFMEASUREMENT + " text, " + NAME + " text not null);";
		Log.d("Inventory", "onCreate: " + sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		String sql = null;
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add note text;";
		if (oldVersion == 2)
			sql = "";

		Log.d("Inventory", "onUpgrade	: " + sql);
		if (sql != null)
			db.execSQL(sql);
	}

}
