package com.hunterdavis.easyvisualinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import com.hunterdavis.easyinventory.R;

public class inventoryitem extends Activity {

	InventorySQLHelper InventoryData = new InventorySQLHelper(this);
	int rowId = 0;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item);

		// set up the units spinner
		Spinner spinner = (Spinner) findViewById(R.id.units);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.unitsofmeasurement,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());

		// set up splits spinner\
		spinner = (Spinner) findViewById(R.id.split);
		adapter = ArrayAdapter.createFromResource(this, R.array.splits,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MySplitsOnItemSelectedListener());
		
		// set up log clear button handler
		OnClickListener clearLogButtonListner = new OnClickListener() {
			public void onClick(View v) {
				EditText logFile = (EditText) findViewById(R.id.logfile);
				logFile.setText("begin log file\n");
			}
		};
		
		// attach the log button clear handler
		Button logButton = (Button) findViewById(R.id.clearlogbutton);
		logButton.setOnClickListener(clearLogButtonListner);

		// check for passed in row id
		Bundle extras = getIntent().getExtras();
		rowId = extras.getInt("id");
		Cursor rowCursor = getCursorByRowNumber(rowId);
		
		// grab a handle to the image
		ImageView imgPreView = (ImageView) findViewById(R.id.photopreview);

		// if we have a row id, load up the data, otherwise return
		if (rowCursor.getCount() > 0) {
			rowCursor.moveToFirst();

			// retrieve our values for this row
			String URI = rowCursor.getString(5);
			String available = rowCursor.getString(4);
			String units = rowCursor.getString(8);
			String name = rowCursor.getString(9);
			Integer divisor = rowCursor.getInt(7);
			String logFile = rowCursor.getString(6);
			String totalProfit = rowCursor.getString(3);
			String sellPrice = rowCursor.getString(2);
			String buyPrice = rowCursor.getString(1);
			
			// set the image from URI
			Uri tempuri = Uri.parse(URI);
			Boolean imageScaled = imageHelper.scaleURIAndDisplay(getBaseContext(),tempuri, imgPreView);
			if(imageScaled == false)
			{
				//rowCursor.close();
				//SQLiteDatabase db = InventoryData.getWritableDatabase();
				//db.delete(InventorySQLHelper.TABLE, "_ID = " + rowId, null);
				//db.close();
				//finish();
			}

			// set our visual items with values
			// set name
			EditText t = new EditText(this);
			t = (EditText) findViewById(R.id.name);
			t.setText(name);

			// set divisor with spinner already instantiated above
			spinner.setSelection(divisor);

			// re-instantiate units spinner and set units
			setUnitsByUnitName(units);

			// set amount
			t = (EditText) findViewById(R.id.amount);
			t.setText(available);

			// set sell price
			t = (EditText) findViewById(R.id.sellprice);
			t.setText(sellPrice);

			// set buy price
			t = (EditText) findViewById(R.id.buyprice);
			t.setText(buyPrice);

			// set total profit
			t = (EditText) findViewById(R.id.totalprofit);
			t.setText(totalProfit);

			// set log file
			t = (EditText) findViewById(R.id.logfile);
			t.setText(logFile);

		} else {
			finish();
		}

		// at this point all saved data has been loaded, time to implement
		// listeners for non-spinners

		// name listener
		EditText nameText = (EditText) findViewById(R.id.name);
		nameText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateSqlValues(rowId, "name", s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// current inventory listener
		EditText amountText = (EditText) findViewById(R.id.amount);
		amountText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateSqlValues(rowId, "amount", s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// current sell price
		EditText sellText = (EditText) findViewById(R.id.sellprice);
		sellText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateSqlValues(rowId, "sellprice", s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// current buy price
		EditText buyText = (EditText) findViewById(R.id.buyprice);
		buyText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateSqlValues(rowId, "buyprice", s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// running total profit
		EditText profitText = (EditText) findViewById(R.id.totalprofit);
		profitText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateSqlValues(rowId, "totalprofit", s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// log file
		EditText logFile = (EditText) findViewById(R.id.logfile);
		logFile.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateSqlValues(rowId, "logfile", s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		
		// photo on click listener
		imgPreView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						2); 
				
			}

		});
		
		

	};
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 2) {
				Uri selectedImageUri = data.getData();
				updateSqlValues(rowId, "uri", selectedImageUri.toString());
				
				// grab a handle to the image
				ImageView imgPreView = (ImageView) findViewById(R.id.photopreview);
				imageHelper.scaleURIAndDisplay(getBaseContext(),selectedImageUri, imgPreView);
				
			}
		}
	}
	private void setUnitsByUnitName(String units) {
		Spinner spinner = (Spinner) findViewById(R.id.units);
		Resources res = getResources();
		String[] unitsarray = res.getStringArray(R.array.unitsofmeasurement);

		for (int i = 0; i < unitsarray.length; i++) {
			if (units.equalsIgnoreCase(unitsarray[i])) {
				spinner.setSelection(i);
				return;
			}
		}
	}

	private void updateSqlValues(int id, String columnName, String value) {
		SQLiteDatabase db = InventoryData.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(columnName, value);
		String strFilter = " _id=" + id;
		db.update(InventorySQLHelper.TABLE, args, strFilter, null);
	}

	private Cursor getCursorByRowNumber(long rowId) {
		SQLiteDatabase db = InventoryData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "_ID = "
				+ rowId, null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}
	

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			Resources res = getResources();
			String[] unitsarray = res
					.getStringArray(R.array.unitsofmeasurement);
			updateSqlValues(rowId, "units", unitsarray[pos]);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	// set up the listener class for spinner
	class MySplitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String posString = String.valueOf(pos);
			updateSqlValues(rowId, "divisor", posString);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	protected void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setNeutralButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

};
