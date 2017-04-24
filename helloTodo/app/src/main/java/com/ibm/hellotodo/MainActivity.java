package com.ibm.hellotodo;

/**
 * Copyright 2015, 2016 IBM Corp. All Rights Reserved.
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
 * Test
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
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.api.MCAAuthorizationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
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

    private BMSClient bmsClient; // IBM Mobile First Client SDK


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bmsClient = BMSClient.getInstance();
        try {
			//initialize SDK with IBM Bluemix application ID and route
			//You can find your backendRoute and backendGUID in the Mobile Options section on top of your Bluemix application dashboard
            //TODO: Please replace <APPLICATION_ROUTE> with a valid ApplicationRoute and <APPLICATION_ID> with a valid ApplicationId
            bmsClient.initialize(this, "<APPLICATION_ROUTE>", "<APPLICATION_ID>", BMSClient.REGION_US_SOUTH);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // Init UI
        initListView();
        initSwipeRefresh();

        // Must create and set default MCA Auth Manager in order for the delete endpoint to work. This is new in core version 2.0.
        MCAAuthorizationManager MCAAuthMan = MCAAuthorizationManager.createInstance(this);

        bmsClient.setAuthorizationManager(MCAAuthMan);
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

                // Grab TodoItem that was long clicked for deletion
                final TodoItem todoItem = mTodoItemList.get(position);

                // Grab TodoItem id number and append to the DELETE REST request using the Bluemix Mobile Services Client SDK
                String todoId = Integer.toString(todoItem.idNumber);
                Request request = new Request(bmsClient.getBluemixAppRoute() + "/api/Items/" + todoId, Request.DELETE);

                // Send the request and use the response listener to react
                request.send(getApplicationContext(), new ResponseListener() {
                    // Update the list if successful
                    @Override
                    public void onSuccess(Response response) {

                        Log.i(TAG, "Item " + todoItem.text + " deleted successfully");

                        loadList();
                    }

                    // If the request fails, log the errors
                    @Override
                    public void onFailure(Response response, Throwable throwable, JSONObject extendedInfo) {
                        String errorMessage = "";

			            // different responses can cause different parameters to be null, be sure to check for those cases
                        if (response != null) {
                            errorMessage += response.toString() + "\n";
                        }

                        if (throwable != null) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            throwable.printStackTrace(pw);
                            errorMessage += "THROWN" + sw.toString() + "\n";
                        }

                        if (extendedInfo != null){
                            errorMessage += "EXTENDED_INFO" + extendedInfo.toString() + "\n";
                        }

                        if (errorMessage.isEmpty())
                            errorMessage = "Request Failed With Unknown Error.";

                        Log.e(TAG, "deleteItem failed with error: " + errorMessage);
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

        // Set swipe refresh listener to update the local list on swipe down
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadList();
            }
        });
    }

    /**
     * Uses Bluemix Mobile Services SDK to get the TodoItems from Bluemix and updates the local list
     */
    private void loadList() {

        // Send GET Request to Bluemix backend to retreive item list with response listener
        Request request = new Request(bmsClient.getBluemixAppRoute()+"/api/Items", Request.GET);
        request.send(getApplicationContext(), new ResponseListener() {
            // Loop through JSON response and create an array of local TodoItems if successful
            @Override
            public void onSuccess(Response response) {
                if (response.getStatus() != 200) {
                    Log.e(TAG, "Error pulling items from Bluemix: " + response.toString());
                } else {

                    try {

                        mTodoItemList.clear();

                        JSONArray jsonArray = new JSONArray(response.getResponseText());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject tempTodoJSON = jsonArray.getJSONObject(i);
                            TodoItem tempTodo = new TodoItem();

                            tempTodo.idNumber = tempTodoJSON.getInt("id");
                            tempTodo.text = tempTodoJSON.getString("text");
                            tempTodo.isDone = tempTodoJSON.getBoolean("isDone");

                            mTodoItemList.add(tempTodo);
                        }

			            // Need to update adapter on main thread in order for list changes to update visually
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTodoItemAdapter.notifyDataSetChanged();
				 Log.i(TAG, "List updated successfully");
				
                                if (mSwipeLayout.isRefreshing()) {
                                    mSwipeLayout.setRefreshing(false);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response JSON: " + e.getLocalizedMessage());
                    }
                }
            }

            // Log Errors on failure
            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject extendedInfo) {
                String errorMessage = "";

                // Check for 404s and unknown host exception since this is the first request made by the app
                if (response != null) {
                    if (response.getStatus() == 404) {
                        errorMessage += "Application Route not found at:\n" + BMSClient.getInstance().getBluemixAppRoute() +
                                "\nPlease verify your Application Route and rebuild the app.";
                    } else {
                        errorMessage += response.toString() + "\n";
                    }
                }

                if (throwable != null) {
                    if (throwable.getClass().equals(UnknownHostException.class)) {
                        errorMessage = "Unable to access Bluemix host!\nPlease verify internet connectivity and try again.";
                    } else {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        throwable.printStackTrace(pw);
                        errorMessage += "THROWN" + sw.toString() + "\n";
                    }
                }

                if (extendedInfo != null){
                    errorMessage += "EXTENDED_INFO" + extendedInfo.toString() + "\n";
                }

                if (errorMessage.isEmpty())
                    errorMessage = "Request Failed With Unknown Error.";

                Log.e(TAG, "loadList failed with error: " + errorMessage);
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

	// UI settings for dialog pop-up
        addDialog.setContentView(R.layout.add_edit_dialog);
        addDialog.setTitle("Add Todo");
        TextView textView = (TextView) addDialog.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }
        addDialog.setCancelable(true);
        Button add = (Button) addDialog.findViewById(R.id.Add);
        addDialog.show();

        // When done is pressed, send POST request to create new TodoItem on Bluemix
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText itemToAdd = (EditText) addDialog.findViewById(R.id.todo);
                final String name = itemToAdd.getText().toString();
                // If text was added, continue with normal operations
                if (!name.isEmpty()) {

                    // Create JSON for new TodoItem, id should be 0 for new items
                    String json = "{\"text\":\"" + name + "\",\"isDone\":false,\"id\":0}";

                    // Create POST request with Bluemix Mobile Services SDK and set HTTP headers so Bluemix knows what to expect in the request
                    Request request = new Request(bmsClient.getBluemixAppRoute() + "/api/Items", Request.POST);

                    HashMap headers = new HashMap();
                    List<String> contentType = new ArrayList<>();
                    contentType.add("application/json");
                    List<String> accept = new ArrayList<>();
                    accept.add("Application/json");

                    headers.put("Content-Type", contentType);
                    headers.put("Accept", accept);

                    request.setHeaders(headers);

                    request.send(getApplicationContext(), json, new ResponseListener() {
                        // On success, update local list with new TodoItem
                        @Override
                        public void onSuccess(Response response) {

                            Log.i(TAG, "Item " + name + " created successfully");

                            loadList();
                        }

                        // On failure, log errors
                        @Override
                        public void onFailure(Response response, Throwable throwable, JSONObject extendedInfo) {
                            String errorMessage = "";

                            if (response != null) {
                                errorMessage += response.toString() + "\n";
                            }

                            if (throwable != null) {
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                throwable.printStackTrace(pw);
                                errorMessage += "THROWN" + sw.toString() + "\n";
                            }

                            if (extendedInfo != null){
                                errorMessage += "EXTENDED_INFO" + extendedInfo.toString() + "\n";
                            }

                            if (errorMessage.isEmpty())
                                errorMessage = "Request Failed With Unknown Error.";

                            Log.e(TAG, "addTodo failed with error: " + errorMessage);
                        }
                    });
                }

                // Close dialog when finished, or if no text was added
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
        final Dialog editDialog = new Dialog(this);

	// UI settings for dialog pop-up
        editDialog.setContentView(R.layout.add_edit_dialog);
        editDialog.setTitle("Edit Todo");
        TextView textView = (TextView) editDialog.findViewById(android.R.id.title);
        if (textView != null) {
            textView.setGravity(Gravity.CENTER);
        }
        editDialog.setCancelable(true);
        EditText todoName = (EditText) editDialog.findViewById(R.id.todo);

	// Get selected TodoItem values
        final String name = mTodoItemList.get(pos).text;
        final boolean isDone = mTodoItemList.get(pos).isDone;
        final int id = mTodoItemList.get(pos).idNumber;
        todoName.setText(name);

        Button editDone = (Button) editDialog.findViewById(R.id.Add);
        editDialog.show();

        // When done is pressed, send PUT request to update TodoItem on Bluemix
        editDone.setOnClickListener(new View.OnClickListener() {
            // Save inputted text when done is tapped
            @Override
            public void onClick(View view) {
                EditText editedText = (EditText) editDialog.findViewById(R.id.todo);

                final String updatedName = editedText.getText().toString();

                // If new text is not empty, create JSON with updated info and send PUT request
                if (!updatedName.isEmpty()) {
                    String json = "{\"text\":\"" + updatedName + "\",\"isDone\":" + isDone + ",\"id\":" + id + "}";

                    // Create PUT REST request using the Bluemix Mobile Services SDK and set HTTP headers so Bluemix knows what to expect in the request
                    Request request = new Request(bmsClient.getBluemixAppRoute() + "/api/Items", Request.PUT);

                    HashMap headers = new HashMap();
                    List<String> contentType = new ArrayList<>();
                    contentType.add("application/json");
                    List<String> accept = new ArrayList<>();
                    accept.add("Application/json");

                    headers.put("Content-Type", contentType);
                    headers.put("Accept", accept);

                    request.setHeaders(headers);

                    request.send(getApplicationContext(), json, new ResponseListener() {
                        // On success, update local list with updated TodoItem
                        @Override
                        public void onSuccess(Response response) {
                            Log.i(TAG, "Item " + updatedName + " updated successfully");

                            loadList();
                        }

                        // On failure, log errors
                        @Override
                        public void onFailure(Response response, Throwable throwable, JSONObject extendedInfo) {
                            String errorMessage = "";

                            if (response != null) {
                                errorMessage += response.toString() + "\n";
                            }

                            if (throwable != null) {
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                throwable.printStackTrace(pw);
                                errorMessage += "THROWN" + sw.toString() + "\n";
                            }

                            if (extendedInfo != null){
                                errorMessage += "EXTENDED_INFO" + extendedInfo.toString() + "\n";
                            }

                            if (errorMessage.isEmpty())
                                errorMessage = "Request Failed With Unknown Error.";

                            Log.e(TAG, "editTodoName failed with error: " + errorMessage);
                        }
                    });
                }
                editDialog.dismiss();
            }
        });
    }

    /**
     * When TodoItem image is tapped, switch boolean isDone value to indicate current completion status.
     * The TodoItem is updated on Bluemix and then the list is refreshed to reflect new status.
     * Uses same REST request as editTodoName.
     *
     * @param view The TodoItem that has been tapped.
     */
    public void isDoneToggle(View view) {
        Integer position = mListView.getPositionForView(view);
        final TodoItem todoItem = mTodoItemList.get(position);

        boolean isDone = !todoItem.isDone;

        String json = "{\"text\":\"" + todoItem.text + "\",\"isDone\":" + isDone + ",\"id\":" + todoItem.idNumber + "}";

        // Create PUT REST request using the Bluemix Mobile Services SDK and set HTTP headers so Bluemix knows what to expect in the request
        Request request = new Request(bmsClient.getBluemixAppRoute() + "/api/Items", Request.PUT);

        HashMap headers = new HashMap();

        List<String> contentType = new ArrayList<>();
        contentType.add("application/json");
        List<String> accept = new ArrayList<>();
        accept.add("Application/json");

        headers.put("Content-Type", contentType);
        headers.put("Accept", accept);

        request.setHeaders(headers);

        request.send(getApplicationContext(), json, new ResponseListener() {
            // On success, update local list with updated TodoItem
            @Override
            public void onSuccess(Response response) {
                Log.i(TAG, todoItem.text + " completeness updated successfully");

                loadList();
            }

            // On failure, log errors
            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject extendedInfo) {
                String errorMessage = "";

                if (response != null) {
                    errorMessage += response.toString() + "\n";
                }

                if (throwable != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    errorMessage += "THROWN" + sw.toString() + "\n";
                }

                if (extendedInfo != null){
                    errorMessage += "EXTENDED_INFO" + extendedInfo.toString() + "\n";
                }

                if (errorMessage.isEmpty())
                    errorMessage = "Request Failed With Unknown Error.";

                Log.e(TAG, "isDoneToggle failed with error: " + errorMessage);
            }
        });

    }
}

