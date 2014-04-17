package com.groceries.GUI;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.groceries.R;
import com.groceries.adapters.GroceryItemListAdapter;

public class GroceryItems extends Activity
{
	ListView groceryItemsView;
	GroceryItemListAdapter listAdapter;
	ArrayList<String> data = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grocery_items);
		
		groceryItemsView = (ListView) findViewById(R.id.itemsView);
		
		Intent intent = getIntent();
		String name = intent.getExtras().getString("nameOfList");
		TextView header = (TextView)findViewById(R.id.groceryListHeader);
		header.setText(name);
		
		listAdapter = new GroceryItemListAdapter(this, data);
		
		groceryItemsView.setAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grocery_items, menu);
		return true;
	}
	
	public void onItemAddClicked(View v)
	{
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this)
	    .setTitle("Add Grocery Item")
	    .setMessage("Enter name of item: ")
	    .setView(input)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            String value = input.getText().toString(); 
	            listAdapter.addItem(value);
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}	
	
	public void onGroceryItemDeleteClicked(View v)
	{
		String nameOfView = "";
        ViewGroup row = (ViewGroup) v.getParent();
        for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
            View view = row.getChildAt(itemPos);
            if (view instanceof TextView) 
            {
            	nameOfView = ((TextView)view).getText().toString(); //Found it!
            	Log.w("TextView to delete:", nameOfView);
 	            listAdapter.removeItem(nameOfView);
 	            Log.w("TextView DELETED:", nameOfView);   	            
                break;
            }
        }
	}
	
	public void onItemSelected(View v)
	{
		if ((((TextView)v).getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)
		{
			((TextView)v).setPaintFlags( ((TextView)v).getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
		}
		else
		{
			((TextView)v).setPaintFlags(((TextView)v).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
	}

}
