package com.groceries.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.groceries.R;

public class GroceryItemListAdapter extends ArrayAdapter<String>
{
	private ArrayList<String> list = new ArrayList<String>();
	private Context context;

	public GroceryItemListAdapter(Context context, List<String> objects)
	{
		super(context, R.layout.grocery_item_widget, objects);
		this.context = context;
		for (int i = 0; i < objects.size(); ++i)
		{
			list.add(objects.get(i));
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.grocery_item_widget, parent, false);

		TextView labelView = (TextView) rowView.findViewById(R.id.groceryItem);

		if (labelView != null)
		{
			labelView.setText(getItem(position));
		}

		return rowView;
	}

	@Override
	public String getItem(int index)
	{
		return list.get(index);
	}
	
	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public long getItemId(int index)
	{
		return index;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	public void addItem(String item)
	{
		list.add(item);
		notifyDataSetChanged();
	}

	public void removeItem(String item)
	{
		list.remove(item);
		notifyDataSetChanged();
	}

}
