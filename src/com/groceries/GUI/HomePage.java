package com.groceries.GUI;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.groceries.R;
import com.groceries.adapters.ExpandableListAdapter;

public class HomePage extends Activity 
{
	ExpandableListView groceryListsView;
	ExpandableListAdapter listAdapter;
	ArrayList<String> parents = new ArrayList<String>();
	ArrayList<ArrayList<String>> children = new ArrayList<ArrayList<String>>();
	final HomePage CONTEXT = this;

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
	            listAdapter.AddGroup(value);
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
		     	            	listAdapter.AddChild(parentIndex, value);
		     	            	groceryListsView.expandGroup(parentIndex);
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
		ViewGroup row = (ViewGroup) v.getParent();
        for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
            View view = row.getChildAt(itemPos);          
            if (view instanceof TextView) 
            {
            	String nameOfView = ((TextView)view).getText().toString(); //Found it!
            	Log.w("TextView to delete:", nameOfView);
            	
                int parentIndex = listAdapter.getParentByChildText(nameOfView);
                if(parentIndex >= 0)
         	    {
         	    	int childIndex = listAdapter.GetChildIndexByText(nameOfView);
         	    	listAdapter.removeChild(parentIndex, childIndex);
         	    	Log.w("TextView DELETED:", nameOfView);
         	    }      	
                break;
            }
        } 	    
	}
	
	public void onCategoryItemDeleteClicked(View v)
	{
		String nameOfView = "";
        ViewGroup row = (ViewGroup) v.getParent();
        for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
            View view = row.getChildAt(itemPos);
            if (view instanceof TextView) 
            {
            	nameOfView = ((TextView)view).getText().toString(); //Found it!
            	Log.w("TextView to delete:", nameOfView);
            	
                int parentIndex = listAdapter.getParentByText(nameOfView);

 	            if(parentIndex >= 0)
 	            {
 	            	listAdapter.RemoveGroup(nameOfView);
 	            	Log.w("TextView DELETED:", nameOfView);
 	            }      
                break;
            }
        }
	}
	
	public void onGroceryListStart(View v)
	{
		String nameOfView = ((TextView)v).getText().toString();
    	Intent i = new Intent(CONTEXT, GroceryItems.class);
    	i.putExtra("nameOfList", nameOfView);
    	startActivity(i); 
	}
}
