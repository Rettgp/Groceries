package com.groceries.GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
	String currentID = "";
	static boolean loadedCategory = false;
	static boolean loadedGroceryList = false;
	static boolean loadGroceryList = true;
	static boolean postList = false;
	static HashMap<String, String> categoryMap = new HashMap<String, String>();
	static HashMap<String, String> groceryListMap = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_page);
		groceryListsView = (ExpandableListView) findViewById(R.id.groceryExpandableList);
		listAdapter = new ExpandableListAdapter(this, parents, children);
		groceryListsView.setAdapter(listAdapter);		

		new RequestTask().execute("http://whispering-springs-5771.herokuapp.com/categories.json");
		

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
	            
	            // Make a post request to save the added category
	            new PostTask().execute("http://whispering-springs-5771.herokuapp.com/categories.json", value); 
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
		     	            	
		     	            	// Get the caregory ID for the given grocery list
		     	            	String parentID = categoryMap.get(nameOfView);
		     	            	// Indicate this is a grocery list post
		     	            	postList = true;
		     	            	
		     	            	Log.w("POSTING:", "http://whispering-springs-5771.herokuapp.com/categories/" + 
		     	            			parentID + "/grocery_lists.json" + " : " + value + " " + parentID);
		     	            	// Post a request to save the grocery list added to the given category ID
		     	            	new PostTask().execute("http://whispering-springs-5771.herokuapp.com/categories/" + 
		     	            			parentID + "/grocery_lists.json", value, parentID);
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
                	// Get the grocery list id
                	String childID = groceryListMap.get(nameOfView);
                	// Get the category id
                	String parentID = categoryMap.get((String)listAdapter.getGroup(parentIndex));
                	// Get the grocery list position
         	    	int childIndex = listAdapter.GetChildIndexByText(nameOfView);
         	    	// Remove the grocery list from view
         	    	listAdapter.removeChild(parentIndex, childIndex);
         	    	// Send DELETE request to remove grocery list from server
         	    	sendGroceryListDeleteRequest(childID, parentID);
         	    	// Remove the grocery list from the transient map
         	    	groceryListMap.remove(nameOfView);
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
 	            	// Remove the category from view
 	            	listAdapter.RemoveGroup(nameOfView);
 	            	// Send DELETE request to remove category from server
 	            	sendDeleteRequest(categoryMap.get(nameOfView));
 	            	// Remove category from transient map
 	            	categoryMap.remove(nameOfView);
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
	
	// Put all categories from GET response into the view
	public void loadCategories(String response)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(response);
			for(int i = 0; i < response.length(); ++i)
			{
				JSONObject subObj = jsonArray.getJSONObject(i);
				String categoryName = (String)subObj.get("name");
				listAdapter.AddGroup(categoryName);
			}
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Put all grocery lists from GET response into the view
	public void loadGroceryLists(String response)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(response);
			for(int i = 0; i < response.length(); ++i)
			{
				JSONObject subObj = jsonArray.getJSONObject(i);
				String listName = (String)subObj.get("name");
				String categoryID = String.valueOf(subObj.get("category_id"));
				
				String categoryName = findCategoryName(categoryID);
				
				int parentPos = listAdapter.getParentByText(categoryName);
				Log.w("View: ", "Added: " + listName + " : Pos:" + parentPos);
				listAdapter.AddChild(parentPos, listName);
				groceryListsView.expandGroup(parentPos);
			}
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String createJSONCategory(String name)
	{
		return "{ \"category\" : { \"name\" : \"" + name + "\" } }";
	}
	
	public String createJSONGroceryList(String name, String parentID)
	{
		return "{ \"grocery_list\" : { \"name\" : \"" + name + 
				"\", \"category_id\" : \"" + parentID + "\" } }";
	}
	
	// Add the category to the map from POST reponse
	public void updateCategoryMap(String json)
	{
		categoryMap.clear();
		try
		{
			JSONArray jsonArray = new JSONArray(json);
			for(int i = 0; i < json.length(); ++i)
			{
				JSONObject subObj = jsonArray.getJSONObject(i);
				String categoryName = (String)subObj.get("name");
				String id = String.valueOf((Integer)subObj.get("id"));
				categoryMap.put(categoryName, id);
				Log.w("Map: ", "Added: " + categoryName + " : " + id);
			}
		}
		catch(Exception ex)
		{
					
		}
	}
	
	// Add the grocery list to the map from POST reponse
	public void updateGroceryListMap(String json)
	{
		groceryListMap.clear();
		try
		{
			JSONArray jsonArray = new JSONArray(json);
			for(int i = 0; i < json.length(); ++i)
			{
				JSONObject subObj = jsonArray.getJSONObject(i);
				String listName = (String)subObj.get("name");
				String id = String.valueOf((Integer)subObj.get("id"));
				groceryListMap.put(listName, id);
				Log.w("Map: ", "Added: " + listName + " : " + id);
			}
		}
		catch(Exception ex)
		{
					
		}
	}
	
	public String findCategoryName(String id)
	{
		Iterator it = categoryMap.entrySet().iterator();
        while (it.hasNext()) 
        {
            Map.Entry pairs = (Map.Entry)it.next();
            if(((String)pairs.getValue()).equals(id))
            {
            	return (String)pairs.getKey();
            }
        }
        
        return null;
	}
	
	public void sendDeleteRequest(String id)
	{
		new DeleteTask().execute("http://whispering-springs-5771.herokuapp.com/categories/" + id + ".json");   
	}
	
	public void sendGroceryListDeleteRequest(String id, String parentID)
	{
		new DeleteTask().execute("http://whispering-springs-5771.herokuapp.com/categories/" + 
				parentID + "/grocery_lists/" + id + ".json");   
	}
	
	class RequestTask extends AsyncTask<String, String, String>{

	    @Override
	    protected String doInBackground(String... uri) {
	    	DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
		    HttpGet httpget = new HttpGet(uri[0]);
		    // Depends on your web service
		    httpget.setHeader("Content-type", "application/json");

		    InputStream inputStream = null;
		    String result = null;
		    try {
		        HttpResponse response = httpclient.execute(httpget);   
		        Log.w("Connection: ", "Connected");
		        HttpEntity entity = response.getEntity();
		        
		        inputStream = entity.getContent();
		        // json is UTF-8 by default
		        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		        StringBuilder sb = new StringBuilder();

		        String line = null;
		        while ((line = reader.readLine()) != null)
		        {
		            sb.append(line + "\n");
		        }
		        result = sb.toString();
		    } catch (Exception e) { 
		    	Log.w("Connection: ", "Disconnected");
		    }
		    finally {
		        try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
		    }
			return result; 
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        
	        // If category has not been loaded before load and update map
	        if(!loadedCategory)
	        {
		        loadCategories(result);
	        	updateCategoryMap(result);
	        	loadedCategory = true;
	        	
	        	// Iterate through categories getting the grocery lists for each one
	    		Iterator it = categoryMap.entrySet().iterator();
	            while (it.hasNext()) 
	            {
	                Map.Entry pairs = (Map.Entry)it.next();
	                new RequestTask().execute("http://whispering-springs-5771.herokuapp.com/categories/" +
	                		pairs.getValue() + "/grocery_lists.json");
	            }
	        }
	        // If grocery list has not been loaded before load and update map
	        else if(loadGroceryList)
	        {
	        	if(!loadedGroceryList)
	        	{
	        		loadGroceryLists(result);
	        	}
		        updateGroceryListMap(result);
	        	loadedGroceryList = true;
	        	loadGroceryList = false;
	        }
	    }
	}
	
	class PostTask extends AsyncTask<String, String, String>{

	    @Override
	    protected String doInBackground(String... uri) 
	    {
	    	DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
	        HttpPost httppost = new HttpPost(uri[0]);

	        try {
	            // Add your data
	        	StringEntity se;
	        	if(postList)
	        	{
	        		se = new StringEntity(createJSONGroceryList(uri[1], uri[2]));  
	        	}
	        	else
	        	{
	        		se = new StringEntity(createJSONCategory(uri[1]));  
	        	}
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	            httppost.setEntity(se);

	            // Execute HTTP Post Request
	            HttpResponse response = httpclient.execute(httppost);

	        } catch (ClientProtocolException e) {
	        } catch (IOException e) {
	        }
	        
	        return "Done";
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        
	        // If we are posting a grocery list then send a request to update the view
	        if(postList)
	        {
	        	Iterator it = categoryMap.entrySet().iterator();
	            while (it.hasNext()) 
	            {
	                Map.Entry pairs = (Map.Entry)it.next();
	                loadGroceryList = true;
	                new RequestTask().execute("http://whispering-springs-5771.herokuapp.com/categories/" +
	                		pairs.getValue() + "/grocery_lists.json");
	            }
	            postList = false;
	        }
	        // If we are posting a category then send a request to update the view
	        else
	        {
	        	new RequestTask().execute("http://whispering-springs-5771.herokuapp.com/categories.json");
	        }
	    }
	}
	
	class DeleteTask extends AsyncTask<String, String, String>{

	    @Override
	    protected String doInBackground(String... uri) 
	    {
	    	DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
	        HttpDelete httpdelete = new HttpDelete(uri[0]);

	        try {
	            // Execute HTTP Delete Request
	            HttpResponse response = httpclient.execute(httpdelete);
	            Log.w("Connection: ", "DELETED " + uri[0]);

	        } catch (ClientProtocolException e) {
	        } catch (IOException e) {
	        }
	        
	        return "Done";
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	    }
	}
}
