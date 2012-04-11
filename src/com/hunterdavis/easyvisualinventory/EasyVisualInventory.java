package com.hunterdavis.easyvisualinventory;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.hunterdavis.easyinventory.R;

/*
 * 
 * columns 
 0 - _ID
 1 - buyprice
 2 - sellprice
 3 - totalprofit
 4 - amount
 5 - uri
 6 - log file
 7 - divisor
 8 - units	
 * 
 * 
 * */

public class EasyVisualInventory extends Activity {

	private Gallery gallery;
	private ImageView imgView;
	InventorySQLHelper InventoryData = new InventorySQLHelper(this);
	private int currentPosition;
	private int currentBulkSaleNumber;
	public int currentBuySellSelection;
	private Context privateContext;

	public class UriD {
		Uri uri;
		int id;

		UriD(Uri URI, int ID) {
			uri = URI;
			id = ID;
		}
	}

	public ArrayList<UriD> Imgid = new ArrayList<UriD>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	};

	// this is used for the onclick select plusbutton
	final int SELECT_PICTURE = 1;
	final int MORE_INFO = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadImageURIsIntoGallery();
		currentPosition = 0;
		currentBulkSaleNumber = 1;

		// loadImageResourceIntoGallery(R.drawable.squid);
		// loadImageResourceIntoGallery(R.drawable.genocide);
		// loadImageResourceIntoGallery(R.drawable.balance);
		setContentView(R.layout.main);

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

		// Create an anonymous implementation of OnClickListener
		OnClickListener plusButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Inventory Photo"),
						SELECT_PICTURE);
			}
		};

		OnClickListener infoButtonListner = new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(v.getContext(),
						inventoryitem.class);
				myIntent.putExtra("id", Imgid.get(currentPosition).id);
				startActivityForResult(myIntent, MORE_INFO);
			}
		};

		OnClickListener DeleteButtonListner = new OnClickListener() {
			public void onClick(View v) {
				yesnoDeleteHandler("Are you sure?",
						"Are you sure you want to delete?");
			}
		};

		OnClickListener BuyButtonListner = new OnClickListener() {
			public void onClick(View v) {
				buyButtonHandler();
			}
		};

		OnClickListener SellButtonListner = new OnClickListener() {
			public void onClick(View v) {
				sellButtonHandler();
			}
		};
		
		OnClickListener BulkButtonListner = new OnClickListener() {
			public void onClick(View v) {
				bulkButtonHandler(v.getContext());
			}
		};

		Button sellButton = (Button) findViewById(R.id.sellbutton);
		sellButton.setOnClickListener(SellButtonListner);
		
		Button bulkButton = (Button) findViewById(R.id.bulkbutton);
		bulkButton.setOnClickListener(BulkButtonListner);

		Button buyButton = (Button) findViewById(R.id.buybutton);
		buyButton.setOnClickListener(BuyButtonListner);

		Button plusButton = (Button) findViewById(R.id.plusbutton);
		plusButton.setOnClickListener(plusButtonListner);

		Button infoButton = (Button) findViewById(R.id.infobutton);
		infoButton.setOnClickListener(infoButtonListner);

		Button deleteButton = (Button) findViewById(R.id.deletebutton);
		deleteButton.setOnClickListener(DeleteButtonListner);

		gallery = (Gallery) findViewById(R.id.photoInventory);
		gallery.setAdapter(new AddImgAdp(this));

		int imgSize = Imgid.size();
		imgView = (ImageView) findViewById(R.id.ImageView01);
		if (imgSize > 0) {

			Boolean scaleDisplay = imageHelper.scaleURIAndDisplay(
					getBaseContext(), Imgid.get(0).uri, imgView);

			Cursor rowCursor = getCursorByRowNumber(Imgid.get(0).id);
			if (rowCursor.getCount() > 0) {
				rowCursor.moveToFirst();
				String available = rowCursor.getString(4);
				String units = rowCursor.getString(8);
				String Name = rowCursor.getString(9);
				changeStatusText(Name + " " + available + " " + units
						+ " remaining");
			}
		} else {
			changeStatusText("No Inventory Items Selected");
			toggleButtons(false);
		}

		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				currentPosition = position;
				Cursor rowCursor = getCursorByRowNumber(Imgid.get(position).id);
				rowCursor.moveToFirst();
				String URI = rowCursor.getString(5);
				String available = rowCursor.getString(4);
				String units = rowCursor.getString(8);
				String Name = rowCursor.getString(9);

				Toast.makeText(getBaseContext(), Name, Toast.LENGTH_SHORT)
						.show();

				imageHelper.scaleURIAndDisplay(getBaseContext(),
						Imgid.get(position).uri, imgView);
				changeStatusText(Name + " " + available + " " + units
						+ " remaining");
			}

		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MORE_INFO) {
			RefreshMainGallery();
		}
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				String buyprice = "0";
				String sellprice = "0";
				String totalprofit = "0";
				String amount = "1";
				String logfile = "begin log file";
				String divisor = "0";
				String unitofmeasurement = "Units";
				String name = "Unnamed Item";
				// now that we have a picture uri, create a new table entry for
				// this inventory item
				SQLiteDatabase db = InventoryData.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(InventorySQLHelper.URI, selectedImageUri.toString());
				values.put(InventorySQLHelper.BUYPRICE, buyprice);
				values.put(InventorySQLHelper.SELLPRICE, sellprice);
				values.put(InventorySQLHelper.TOTALPROFIT, totalprofit);
				values.put(InventorySQLHelper.AMOUNT, amount);
				values.put(InventorySQLHelper.LOGFILE, logfile);
				values.put(InventorySQLHelper.DIVISOR, divisor);
				values.put(InventorySQLHelper.UNITOFMEASUREMENT,
						unitofmeasurement);
				values.put(InventorySQLHelper.NAME, name);
				long latestRowId = db.insert(InventorySQLHelper.TABLE, null,
						values);
				db.close();

				Intent myIntent = new Intent(getBaseContext(),
						inventoryitem.class);
				String rowIdString = String.valueOf(latestRowId);
				Integer rowId = Integer.valueOf(rowIdString);
				myIntent.putExtra("id", rowId);
				startActivityForResult(myIntent, MORE_INFO);

				// enable the buttons
				toggleButtons(true);

			}
		}
	}

	public void RefreshMainGallery() {
		// first clear the imgid cache
		Imgid.clear();

		// Now add new items to cache
		loadImageURIsIntoGallery();

		// grab an instance of gallery
		gallery = (Gallery) findViewById(R.id.photoInventory);

		// this refreshes the gallery view
		((BaseAdapter) gallery.getAdapter()).notifyDataSetChanged();

		int imgSize = Imgid.size();
		imgView = (ImageView) findViewById(R.id.ImageView01);
		currentPosition = 0;
		if (imgSize > 0) {
			imageHelper.scaleURIAndDisplay(getBaseContext(), Imgid.get(0).uri,
					imgView);
			Cursor rowCursor = getCursorByRowNumber(Imgid.get(0).id);
			rowCursor.moveToFirst();
			String available = rowCursor.getString(4);
			String units = rowCursor.getString(8);
			String Name = rowCursor.getString(9);
			changeStatusText(Name + " " + available + " " + units
					+ " remaining");
		} else {
			changeStatusText("No Inventory Items Selected");
			imgView.setImageResource(0);
			toggleButtons(false);
		}
	}

	public void toggleButtons(Boolean toggleOnOff) {
		// this ensures the info, buy, and sell buttons are there
		Button mybutton = (Button) findViewById(R.id.infobutton);
		mybutton.setEnabled(toggleOnOff);
		mybutton = (Button) findViewById(R.id.buybutton);
		mybutton.setEnabled(toggleOnOff);
		mybutton = (Button) findViewById(R.id.sellbutton);
		mybutton.setEnabled(toggleOnOff);
		mybutton = (Button) findViewById(R.id.deletebutton);
		mybutton.setEnabled(toggleOnOff);
		mybutton = (Button) findViewById(R.id.bulkbutton);
		mybutton.setEnabled(toggleOnOff);
		

	}
	
	public void bulkButtonHandler(Context context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(
				context);
		privateContext = context;
		alert.setTitle("How Many?");
		alert.setMessage("Please Enter An Amount To Sell");

		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText("1");
		alert.setView(input);

		alert.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						String tempitem = input.getText().toString()
								.trim();
						int itemNumber = 0;
						try {
							itemNumber = Integer.valueOf(tempitem);
						} catch (NumberFormatException e) {
							Toast.makeText(getBaseContext(), "Error with numeric value!", Toast.LENGTH_SHORT).show();
							return;
						}
						
						if(itemNumber > 0) {
							dialog.dismiss();
							bulkButtonHandlerStageTwo(privateContext,itemNumber);
						}
						

					}

				});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Canceled
					}
				});

		alert.show();
	}
	
	public void bulkButtonHandlerStageTwo(Context context, int itemNumber) {
		currentBulkSaleNumber = itemNumber;
		AlertDialog.Builder alert = new AlertDialog.Builder(
				context);

		alert.setTitle("How Much?");
		alert.setMessage("Please Enter The Total Sale Price For All Items Combined");

		// Set an EditText view to get user input
		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
		input.setText("");
		alert.setView(input);

		alert.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						String tempitem = input.getText().toString()
								.trim();
						float itemPrice = -1;
						try {
							itemPrice = Float.valueOf(tempitem);
						} catch (NumberFormatException e) {
							Toast.makeText(getBaseContext(), "Error with Price!", Toast.LENGTH_SHORT).show();
							return;
						}
						
						if(itemPrice >= 0.0) {
							Float divisorSellPrice = itemPrice/currentBulkSaleNumber;
							executeSellTransaction(currentBulkSaleNumber, divisorSellPrice);
						}
						else {
							Toast.makeText(getBaseContext(), "Cannot Set A Negative Price!", Toast.LENGTH_SHORT).show();
						}
						

					}

				});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Canceled
					}
				});

		alert.show();
		
		
	}

	public void changeStatusText(String newStatus) {
		TextView t = new TextView(this);
		t = (TextView) findViewById(R.id.ItemName);
		t.setText(newStatus);
	}

	public void loadImageURIsIntoGallery() {
		Cursor cursor = getInventoryCursor();
		while (cursor.moveToNext()) {
			// alertbox("col name",cursor.getColumnName(5));
			String URI = cursor.getString(5);
			Uri tempuri = Uri.parse(URI);
			int ID = cursor.getInt(0);
			// alertbox("here is the URI parsed",URI);
			UriD localUriD = new UriD(tempuri, ID);
			Imgid.add(localUriD);
		}
	};

	public void loadImageResourceIntoGallery(int resourceToLoad) {
		Resources resources = this.getResources();
		Uri tempuri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
				+ resources.getResourcePackageName(resourceToLoad) + '/'
				+ resources.getResourceTypeName(resourceToLoad) + '/'
				+ resources.getResourceEntryName(resourceToLoad));
		UriD localUriD = new UriD(tempuri, (Imgid.size() + 1));
		Imgid.add(localUriD);
	};

	public void DeleteRowByRowID(long rowId) {
		SQLiteDatabase db = InventoryData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "_ID = " + rowId, null);
		db.close();
	}

	private Cursor getCursorByRowNumber(long rowId) {
		SQLiteDatabase db = InventoryData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "_ID = "
				+ rowId, null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getInventoryCursor() {
		SQLiteDatabase db = InventoryData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private class PickerListener implements NumberPicker.ValueChangeListener {

		public void onNumberPickerValueChange(NumberPicker picker, int value) {

			if (picker.getId() == R.id.SpinRate) {
				// update our global val here
				currentBuySellSelection = value;
			}

			return;
		}

	}

	protected void sellButtonHandler() {
		// Preparing views
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.numpicklayout,
				(ViewGroup) findViewById(R.id.WholeScreen));
		// layout_root should be the name of the "top-level" layout node in the
		// dialog_layout.xml file.
		// final EditText nameBox = (EditText)
		// layout.findViewById(R.id.name_box);
		// final EditText phoneBox = (EditText)
		// layout.findViewById(R.id.phone_box);
		NumberPicker picker = (NumberPicker) layout.findViewById(R.id.SpinRate);

		// reset our buy and sel selection to 10 units
		currentBuySellSelection = 10;

		// picker.setValue(50);
		PickerListener pListen = new PickerListener();
		picker.setOnValueChangeListener(pListen);

		// Building dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setPositiveButton("Sell",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
						
						int rowId = Imgid.get(currentPosition).id;
						Cursor rowCursor = getCursorByRowNumber(rowId);
						Float sellPrice = 0.0f;
						if (rowCursor.moveToFirst()) {
							sellPrice = rowCursor.getFloat(2);
						}
						rowCursor.close();
						
						// save info where you want it
						executeSellTransaction(currentBuySellSelection, sellPrice);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void buyButtonHandler() {
		// Preparing views
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.numpicklayout,
				(ViewGroup) findViewById(R.id.WholeScreen));
		// layout_root should be the name of the "top-level" layout node in the
		// dialog_layout.xml file.
		// final EditText nameBox = (EditText)
		// layout.findViewById(R.id.name_box);
		// final EditText phoneBox = (EditText)
		// layout.findViewById(R.id.phone_box);
		NumberPicker picker = (NumberPicker) layout.findViewById(R.id.SpinRate);

		// reset our buy and sel selection to 10 units
		currentBuySellSelection = 10;

		// picker.setValue(50);
		PickerListener pListen = new PickerListener();
		picker.setOnValueChangeListener(pListen);

		// Building dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setPositiveButton("Buy", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// save info where you want it
				executeBuyTransaction(currentBuySellSelection);
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void executeSellTransaction(int amount, float price) {
		// get a cursor for all the info we'll need
		int rowId = Imgid.get(currentPosition).id;
		Cursor rowCursor = getCursorByRowNumber(rowId);
		if (rowCursor.moveToFirst()) {
			// retrieve our values for this row
			// String URI = rowCursor.getString(5);
			Integer available = rowCursor.getInt(4);
			String units = rowCursor.getString(8);
			String name = rowCursor.getString(9);
			Integer divisor = rowCursor.getInt(7);
			String logFile = rowCursor.getString(6);
			Float totalProfit = rowCursor.getFloat(3);
			
			// close the row cursor
			rowCursor.close();

			// calculate and set the new profit
			Float totalSalePrice = price * amount;
			Float newProfit = totalProfit + totalSalePrice;
			String profitString = String.valueOf(newProfit);
			updateSqlValues(rowId, "totalprofit", profitString);

			// calculate and set the new amount
			Integer newamount = available - amount;
			String amountString = String.valueOf(newamount);
			updateSqlValues(rowId, "amount", amountString);

			// calculate and set the new log file
			String currentDate = DateFormat.getDateFormat(this).format(
					new Date());
			Resources res = getResources();
			String[] currencyarray = res.getStringArray(R.array.splits);
			String newLogFileEntry = logFile + "\n" + currentDate + " Sold "
					+ amount + " " + units + " of " + name + " for "
					+ totalSalePrice + " " + currencyarray[divisor];
			updateSqlValues(rowId, "logfile", newLogFileEntry);

			// refresh gallery view
			RefreshMainGallery();

		}
	}

	protected void executeBuyTransaction(int amount) {
		// get a cursor for all the info we'll need
		int rowId = Imgid.get(currentPosition).id;
		Cursor rowCursor = getCursorByRowNumber(rowId);
		if (rowCursor.moveToFirst()) {
			// retrieve our values for this row
			// String URI = rowCursor.getString(5);
			Integer available = rowCursor.getInt(4);
			String units = rowCursor.getString(8);
			String name = rowCursor.getString(9);
			Integer divisor = rowCursor.getInt(7);
			String logFile = rowCursor.getString(6);
			Float totalProfit = rowCursor.getFloat(3);
			Float buyPrice = rowCursor.getFloat(1);

			// close the row cursor
			rowCursor.close();

			// calculate and set the new profit
			Float totalPurchasePrice = buyPrice * amount;
			Float newProfit = totalProfit - totalPurchasePrice;
			String profitString = String.valueOf(newProfit);
			updateSqlValues(rowId, "totalprofit", profitString);

			// calculate and set the new amount
			Integer newamount = available + amount;
			String amountString = String.valueOf(newamount);
			updateSqlValues(rowId, "amount", amountString);

			// calculate and set the new log file
			String currentDate = DateFormat.getDateFormat(this).format(
					new Date());
			Resources res = getResources();
			String[] currencyarray = res.getStringArray(R.array.splits);
			String newLogFileEntry = logFile + "\n" + currentDate + " Bought "
					+ amount + " " + units + " of " + name + " for "
					+ totalPurchasePrice + " " + currencyarray[divisor];
			updateSqlValues(rowId, "logfile", newLogFileEntry);

			// refresh gallery view
			RefreshMainGallery();

		}

	}

	private void updateSqlValues(int id, String columnName, String value) {
		SQLiteDatabase db = InventoryData.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(columnName, value);
		String strFilter = " _id=" + id;
		db.update(InventorySQLHelper.TABLE, args, strFilter, null);
	}

	protected void yesnoDeleteHandler(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// store the row id
								Integer rowId = Imgid.get(currentPosition).id;

								// remove image from main img list
								Imgid.remove(currentPosition);

								// remove sql table entry for item
								DeleteRowByRowID(rowId);

								// refresh gallery view
								RefreshMainGallery();

							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	public class AddImgAdp extends BaseAdapter {
		int GalItemBg;
		private Context cont;

		public AddImgAdp(Context c) {
			cont = c;
			TypedArray typArray = obtainStyledAttributes(R.styleable.GalleryTheme);
			GalItemBg = typArray.getResourceId(
					R.styleable.GalleryTheme_android_galleryItemBackground, 0);
			typArray.recycle();
		}

		public int getCount() {
			return Imgid.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imgView = new ImageView(cont);

			imageHelper.scaleURIAndDisplay(getBaseContext(),
					Imgid.get(position).uri, imgView);
			imgView.setLayoutParams(new Gallery.LayoutParams(80, 70));
			imgView.setScaleType(ImageView.ScaleType.FIT_XY);
			imgView.setBackgroundResource(GalItemBg);

			return imgView;
		}
	}

}
