package com.groceries.GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.groceries.R;
import com.groceries.GUI.HomePage.DeleteTask;
import com.groceries.GUI.HomePage.PostTask;
import com.groceries.GUI.HomePage.RequestTask;
import com.groceries.adapters.GroceryItemListAdapter;

public class GroceryItems extends Activity
{
	ListView groceryItemsView;
	GroceryItemListAdapter listAdapter;
	ArrayList<String> data = new ArrayList<String>();
	static HashMap<String, String> itemMap = new HashMap<String, String>();
	static String name;
	static String id;
	static boolean loaded = false;
	static String parentID;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grocery_items);
		
		groceryItemsView = (ListView) findViewById(R.id.itemsView);
		
		Intent intent = getIntent();
		name = intent.getExtras().getString("nameOfList");
		id = intent.getExtras().getString("id");
		parentID = intent.getExtras().getString("parentID");
		TextView header = (TextView)findViewById(R.id.groceryListHeader);
		header.setText(name);
		
		listAdapter = new GroceryItemListAdapter(this, data);
		
		groceryItemsView.setAdapter(listAdapter);
		
		new RequestTask().execute("http://whispering-springs-5771.herokuapp.com/categories/"+parentID+"/grocery_lists/"+id+"/grocery_items.json");
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
	            new PostTask().execute("http://whispering-springs-5771.herokuapp.com/categories/"+parentID+"/grocery_lists/"+id+"/grocery_items.json", value);             
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
            	String itemID = itemMap.get(nameOfView);
            	itemMap.remove(nameOfView);
 	            listAdapter.removeItem(nameOfView);
 	            new DeleteTask().execute("" +
 	            		"http://whispering-springs-5771.herokuapp.com/categories/"+parentID+"/grocery_lists/"+id+"/grocery_items/"+itemID+".json");
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
	
	public void updateItemMap(String json)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(json);
			for(int i = 0; i < json.length(); ++i)
			{
				JSONObject subObj = jsonArray.getJSONObject(i);
				String itemName = (String)subObj.get("name");
				if(!itemMap.containsKey(itemName))
				{
					String id = String.valueOf((Integer)subObj.get("id"));
					itemMap.put(itemName, id);
					Log.w("Map: ", "Added: " + itemName + " : " + id);
				}
			}
		}
		catch(Exception ex)
		{
						
		}
	}
	
	public void loadItems(String response)
	{
		try
		{
			JSONArray jsonArray = new JSONArray(response);
			for(int i = 0; i < response.length(); ++i)
			{
				JSONObject subObj = jsonArray.getJSONObject(i);
				String itemName = (String)subObj.get("name");
				listAdapter.addItem(itemName);
			}
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public String createJSONItem(String name)
	{
		return "{ \"grocery_item\" : {  \"grocery_list_id\" : \"" + id + "\", \"name\" : \"" + name + "\" } }";
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
	        loadItems(result);
	        updateItemMap(result);
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
	        	se = new StringEntity(createJSONItem(uri[1]));  
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
