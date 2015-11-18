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

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The {@code MainActivity} is the primary visual activity shown when the app is being interacted with.
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mListView; // Main ListView
    private List<TodoItem> mTodoItemList; // The list of TodoItems
    private TodoItemAdapter mTodoItemAdapter; // Adapter for bridging the list of TodoItems with the ListView

    private SwipeRefreshLayout mSwipeLayout; // Swipe refresh to update local app if backend has changed

    private BMSClient client; // IBM Mobile First Client SDK


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = BMSClient.getInstance();
        try {
            client.initialize(getApplicationContext(), "<APPLICATION_ROUTE>", "<APPLICATION_ID>");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        initListView();
        initSwipeRefresh();
        loadList();
    }

    @Override
    public void onResume() {
        super.onResume();

        // load list of TodoItems
        loadList();
    }

    private void initListView() {
        // Get MainActivity's ListView
        mListView = (ListView) findViewById(R.id.listView);

        // Init array to hold TodoItems
        mTodoItemList = new ArrayList<>();

        // Create and set ListView adapter for displaying TodoItems
        mTodoItemAdapter = new TodoItemAdapter(getBaseContext(), mTodoItemList);
        mListView.setAdapter(mTodoItemAdapter);

        // Set long click listener for deleting TodoItems
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {

                // Grab TodoItem to delete from current showing list
                TodoItem todoItem = mTodoItemList.get(position);

                // Grab TodoItem id number and append to the DELETE rest request using the IBM Mobile First Client SDK
                String todoId = Integer.toString(todoItem.idNumber);
                Request request = new Request(client.getBluemixAppRoute() + "/api/Items/" + todoId, Request.DELETE);

                // Send the request and use the response listener to react
                request.send(getApplicationContext(), new ResponseListener() {
                    // Update the list if successful
                    @Override
                    public void onSuccess(Response response) {
                        Log.i(TAG, "Item  deleted successfully");

                        loadList();
                    }

                    // If the request fails, log the errors
                    @Override
                    public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {

                        if (t != null) {
                            Log.e(TAG, "deleteItem failed with error: " + t.getLocalizedMessage());
                        } else if (response != null) {
                            Log.e(TAG, "deleteItem failed with error: " + response.toString());
                        } else if (extendedInfo != null) {
                            Log.e(TAG, "deleteItem failed with error: " + extendedInfo.toString());
                        } else {
                            Log.e(TAG, "deleteItem failed with error: Reason Unkown");
                        }

                    }
                });

                return true;
            }
        });
    }

    /**
     * Enables swipe down refresh for the list
     */
    private void initSwipeRefresh() {

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        // Set swipe refresh listener to update the local list on pull down
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadList();
            }
        });
    }

    /**
     * Uses IBM Mobile First SDK to get the TodoItems from Bluemix and updates the local list
     */
    private void loadList() {

        // Identify and send GET Request with response listener
        Request request = new Request(client.getBluemixAppRoute()+"/api/Items", Request.GET);
        request.send(getApplicationContext(), new ResponseListener() {
            // Loop through JSON response and create local TodoItems if successfull
            @Override
            public void onSuccess(Response response) {
                if (response.getStatus() != 200) {
                    Log.e("MainActivity", "Error pulling items from Bluemix: " + response.toString());
                } else {

                    try {

                        mTodoItemList.clear();

                        JSONArray jsonArray = new JSONArray(response.getResponseText());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject temp = jsonArray.getJSONObject(i);
                            TodoItem tempTodo = new TodoItem();

                            tempTodo.idNumber = temp.getInt("id");
                            tempTodo.text = temp.getString("text");
                            tempTodo.isDone = temp.getBoolean("isDone");

                            mTodoItemList.add(tempTodo);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTodoItemAdapter.notifyDataSetChanged();

                                if (mSwipeLayout.isRefreshing()) {
                                    mSwipeLayout.setRefreshing(false);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("MainActivity", "Error reading response JSON: " + e.getLocalizedMessage());
                    }
                }
            }

            // Log Errors on failure
            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject jsonObject) {
                if (throwable != null) {
                    Log.e("MainActivity", "Failed sending request to Bluemix: " + throwable.getLocalizedMessage());
                } else if (response != null) {
                    Log.e("MainActivity", "Failed sending request to Bluemix: " + response.toString());
                } else if (jsonObject != null) {
                    Log.e("MainActivity", "Failed sending request to Bluemix: " + jsonObject.toString());
                } else {
                    Log.e("MainActivity", "Failed sending request to Bluemix: Reason Unkown");
                }
            }
        });
    }

    /**
     * Launches a dialog for adding a new TodoItem. Called when plus button is tapped.
     *
     * @param view The plus button that is tapped.
     */
    public void addTodo(View view) {

        final Dialog addDialog = new Dialog(this);

        addDialog.setContentView(R.layout.add_edit_dialog);
        addDialog.setTitle("Add Todo");
        TextView textView = (TextView) addDialog.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }

        addDialog.setCancelable(true);
        Button add = (Button) addDialog.findViewById(R.id.Add);
        addDialog.show();

        // When done is pressed, send POST request to create TodoItem on Bluemix
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText itemToAdd = (EditText) addDialog.findViewById(R.id.todo);
                final String name = itemToAdd.getText().toString();
                // If text was added, continue with normal operations
                if (!name.isEmpty()) {

                    // Create JSON for new TodoItem, id should be 0 for new items
                    String json = "{\"text\":\"" + name + "\",\"isDone\":false,\"id\":0}";

                    // Create POST request with IBM Mobile First SDK and set HTTP headers so Bluemix knows what to expect in the request
                    Request request = new Request(client.getBluemixAppRoute() + "/api/Items", Request.POST);

                    HashMap headers = new HashMap();
                    List<String> cType = new ArrayList<>();
                    cType.add("application/json");
                    List<String> accept = new ArrayList<>();
                    accept.add("Application/json");

                    headers.put("Content-Type", cType);
                    headers.put("Accept", accept);

                    request.setHeaders(headers);

                    request.send(getApplicationContext(), json, new ResponseListener() {
                        // On success, update local list with new TodoItem
                        @Override
                        public void onSuccess(Response response) {
                            Log.i("MainActivity", "Item created successfully");

                            loadList();
                        }

                        // On failure, log errors
                        @Override
                        public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                            if (response != null) {
                                Log.e("MainActivity", "createItem failed with error: " + response.getResponseText());
                            }
                            if (t != null) {
                                Log.e("MainActivity", "createItem failed with error: " + t.getLocalizedMessage(), t);
                            }
                            if (extendedInfo != null) {
                                Log.e("MainActivity", "createItem failed with error: " + extendedInfo.toString());
                            }

                        }
                    });
                }

                // Kill dialog when finished, or if no text was added
                addDialog.dismiss();
            }
        });
    }

    /**
     * Launches a dialog for updating the TodoItem name. Called when the list item is tapped.
     *
     * @param view The TodoItem that is tapped.
     */
    public void editTodoName(View view) {
        // Gets position in list view of tapped item
        final Integer pos = mListView.getPositionForView(view);
        final Dialog addDialog = new Dialog(this);

        addDialog.setContentView(R.layout.add_edit_dialog);
        addDialog.setTitle("Edit Todo");
        TextView textView = (TextView) addDialog.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }
        addDialog.setCancelable(true);
        EditText et = (EditText) addDialog.findViewById(R.id.todo);

        final String name = mTodoItemList.get(pos).text;
        final boolean isDone = mTodoItemList.get(pos).isDone;
        final int id = mTodoItemList.get(pos).idNumber;
        et.setText(name);

        Button addDone = (Button) addDialog.findViewById(R.id.Add);
        addDialog.show();

        // When done is pressed, send PUT request to update TodoItem on Bluemix
        addDone.setOnClickListener(new View.OnClickListener() {
            // Save text inputted when done is tapped
            @Override
            public void onClick(View view) {
                EditText editedText = (EditText) addDialog.findViewById(R.id.todo);

                String newName = editedText.getText().toString();

                // If new text is not empty, create JSON with updated info and send PUT request
                if (!newName.isEmpty()) {
                    String json = "{\"text\":\"" + newName + "\",\"isDone\":" + isDone + ",\"id\":" + id + "}";

                    // Create PUT REST request using the IBM Mobile First SDK and set HTTP headers so Bluemix knows what to expect in the request
                    Request request = new Request(client.getBluemixAppRoute() + "/api/Items", Request.PUT);

                    HashMap headers = new HashMap();
                    List<String> cType = new ArrayList<>();
                    cType.add("application/json");
                    List<String> accept = new ArrayList<>();
                    accept.add("Application/json");

                    headers.put("Content-Type", cType);
                    headers.put("Accept", accept);

                    request.setHeaders(headers);

                    request.send(getApplicationContext(), json, new ResponseListener() {
                        // On success, update local list with updated TodoItem
                        @Override
                        public void onSuccess(Response response) {
                            Log.i("MainActivity", "Item updated successfully");

                            loadList();
                        }

                        // On failure, log errors
                        @Override
                        public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                            if (response != null) {
                                Log.e("MainActivity", "updateItem failed with error: " + response.getResponseText());
                            }
                            if (t != null) {
                                Log.e("MainActivity", "updateItem failed with error: " + t.getLocalizedMessage(), t);
                            }
                            if (extendedInfo != null) {
                                Log.e("MainActivity", "updateItem failed with error: " + extendedInfo.toString());
                            }

                        }
                    });

                }
                addDialog.dismiss();
            }
        });
    }

    /**
     * Changes completed image and flips TodoItem isDone boolean value. Same request as editTodoName.
     *
     * @param view The TodoItem that has been tapped.
     */
    public void isDoneToggle(View view) {
        Integer pos = mListView.getPositionForView(view);
        TodoItem todoItem = mTodoItemList.get(pos);

        boolean isDone = !todoItem.isDone;

        String json = "{\"text\":\"" + todoItem.text + "\",\"isDone\":" + isDone + ",\"id\":" + todoItem.idNumber + "}";

        // Create PUT REST request using the IBM Mobile First SDK and set HTTP headers so Bluemix knows what to expect in the request
        Request request = new Request(client.getBluemixAppRoute() + "/api/Items", Request.PUT);

        HashMap headers = new HashMap();

        List<String> cType = new ArrayList<>();
        cType.add("application/json");
        List<String> accept = new ArrayList<>();
        accept.add("Application/json");

        headers.put("Content-Type", cType);
        headers.put("Accept", accept);

        request.setHeaders(headers);

        request.send(getApplicationContext(), json, new ResponseListener() {
            // On success, update local list with updated TodoItem
            @Override
            public void onSuccess(Response response) {
                Log.i("MainActivity", "Item completeness updated successfully");

                loadList();
            }

            // On failure, log errors
            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                if (response != null) {
                    Log.e("MainActivity", "isDoneToggle failed with error: " + response.getResponseText());
                }
                if (t != null) {
                    Log.e("MainActivity", "isDoneToggle failed with error: " + t.getLocalizedMessage(), t);
                }
                if (extendedInfo != null) {
                    Log.e("MainActivity", "isDoneToggle failed with error: " + extendedInfo.toString());
                }
            }
        });

    }
}

