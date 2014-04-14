package com.groceries.GUI;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.groceries.R;
import com.groceries.adapters.ExpandableListAdapter;
import com.groceries.adapters.ExpandableListParent;

public class HomePage extends Activity
{
	ExpandableListView groceryListsView;
	ExpandableListAdapter listAdapter;
	ArrayList<String> parents = new ArrayList<String>();
	ArrayList<ArrayList<String>> children = new ArrayList<ArrayList<String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_page);
		groceryListsView = (ExpandableListView) findViewById(R.id.groceryExpandableList);
		listAdapter = new ExpandableListAdapter(this, parents, children);
		groceryListsView.setAdapter(listAdapter);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_page, menu);
		return true;
	}
	
	public void onCategoryAddClicked(View v)
	{
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this)
	    .setTitle("Add Grocery Category")
	    .setMessage("Enter name of category: ")
	    .setView(input)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            String value = input.getText().toString(); 
	            addCategory(value);
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();		
	}
	
	public void onCategoryItemAddClicked(View v)
	{
		final View va = v;
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this)
	    .setTitle("Add Grocery List")
	    .setMessage("Enter name of list: ")
	    .setView(input)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        @SuppressLint("NewApi")
			public void onClick(DialogInterface dialog, int whichButton) {
	            String value = input.getText().toString(); 
	            if(!value.isEmpty())
	            {
		            String nameOfView = "";
		            ViewGroup row = (ViewGroup) va.getParent();
		            for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
		                View view = row.getChildAt(itemPos);
		                if (view instanceof TextView) 
		                {
		                	nameOfView = ((TextView)view).getText().toString(); //Found it!
		                	
		                    int parentIndex = listAdapter.getParentByText(nameOfView);
		     	            Log.w("myApp", nameOfView);
		     	            Log.w("myApp", String.valueOf(parentIndex));
		     	            if(parentIndex >= 0)
		     	            {
		     	            	addGroceryList(value, parentIndex);
		     	            }      
		                    break;
		                }
		            }
	            }  
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();		
	}
	
	public void onGroceryListItemDeleteClicked(View v)
	{
		
	}
	
	public void addCategory(String name)
	{		
	    parents.add(name);
	    	    	    
	    listAdapter.notifyDataSetChanged();
		listAdapter.notifyDataSetInvalidated();
	}
	
	public void addGroceryList(String name, int index)
	{
		if(children.size() <= index)
		{
			ArrayList<String> tempList = new ArrayList<String>();
			tempList.add(name);
			children.add(tempList);
		}
		else
		{
			children.get(index).add(name);
		}
		
	    listAdapter.notifyDataSetChanged();
	    listAdapter.notifyDataSetInvalidated();
		
	}

}
