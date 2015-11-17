package com.ibm.hellotodo;

/**
 * Copyright 2015 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * An an adapter for mapping TodoItems to ListView rows
 */
public class TodoItemAdapter extends BaseAdapter implements ListAdapter{

    private static final String TAG = TodoItemAdapter.class.getCanonicalName();
    private List<TodoItem> mTodoItems;
    private Context mContext;

    public TodoItemAdapter(Context context, List<TodoItem> todoItems) {
        mContext = context;
        mTodoItems = todoItems;
    }

    /**
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mTodoItems.size();
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return mTodoItems.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Inflate ListView row layout
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_layout, parent, false);
        }

        // Get Views from layout
        ImageButton button = (ImageButton) convertView.findViewById(R.id.priority);
        TextView text = (TextView) convertView.findViewById(R.id.name);

        // Get relevant TodoItem
        TodoItem todoItem = mTodoItems.get(position);

        // Set TodoItem text
        String name = todoItem.text;
        text.setText(name);

        // Set TodoItem completed image
        boolean isDone = todoItem.isDone;
        button.setImageResource(getDoneImageResourceId(isDone));

        return convertView;
    }

    private int getDoneImageResourceId(boolean isDone) {
        if (isDone) {
            return R.mipmap.confirm;
        }else {
            return R.mipmap.low;
        }
    }
}

