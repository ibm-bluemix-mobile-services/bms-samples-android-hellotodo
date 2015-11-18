# Android HelloTodo sample for IBM MobileFirst Services on IBM Bluemix
---
This HelloTodo sample contains an Android project to be used to communicate with a StrongLoop based mobile backend created using MobileFirst Services Boilerplate on IBM Bluemix. Below instructions will take you step by step through a process of creating a mobile backend and getting this sample running. 

### Creating a mobile backend
Start by creating a mobile backend on IBM Bluemix by using the MobileFirst Services Boilerplate

1. Log in into your IBM Bluemix account
2. Open Bluemix Catalog [https://console.ng.bluemix.net/catalog/](https://console.ng.bluemix.net/catalog/)
3. Find and select the MobileFirst Services Starter under the Boilerplates section
4. Select the space you want to add your mobile backend to
5. Enter the name and host for your mobile backend. 
6. Optionally you can change the plans
7. Click CREATE button

As a result of the above steps IBM Bluemix will provision a Node.JS runtime and populate it with with a default HelloTodo application created using StrongLoop. This application uses LoopBack framework to expose the `/api/Items` API which will be used by both Web UI and the HelloTodo app sample from this Github repository. 

Once the above provisioning process is complete you'll be taken to a Bluemix Dashboard for your newly provisioned mobile backend. Click the `Mobile Options` link in top right part of a screen to find your `appRoute` and `appGUID`. Keep this screen open in your browser as you you will need these parameters shortly. 

Open the appRoute URL in your browser. You will see the web interface for the HelloTodo backend. Start by following the guided experience steps described in the web UI. Eventually you will try to DELETE a todo item and will discover that this action can only be complete when using the HelloTodo mobile apps sample from this Github repository. This is due to a fact that the mobile backend is by default protected by a Mobile Client Access - a Bluemix service that provides security and monitoring functionality for mobile backends. Following steps will guide you through obtaining and running the HelloTodo mobile application. 

(Optionally you might want to hit the "View API Reference" button on web UI to see the API specs)

### Cloning the sample
Clone the sample to your local development environment
```
git clone https://github.com/ibm-bluemix-mobile-services/bms-samples-android-hellotodo
```

### Installing Bluemix Mobile Services SDK using Gradle and Android Studio

Android Studio has a built in default Gradle wrapper that will pull your Android application dependencies automatically.

Please take a moment to check out the build.gradle file (helloTodo/app/build.gradle) where you'll find the core mobile first SDK:

```
    compile group: 'com.ibm.mobilefirstplatform.clientsdk.android',
            name: 'core',
            version: '1.+',
            ext: 'aar',
            transitive: true
```

The above code pulls the IBM Mobile First from an IBM managed Maven repository.

> Note: When creating a production app, be sure to define the version explicitly (1.0.0 for example) to ensure consistent builds.

Once you have understood the above code and ran a successful Gradle sync you will be able to initialize the SDK like so: 
```
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

    }
```

Be sure to replace `<APPLICATION_ROUTE>` and `<APPLICATION_ID>` with the `appRoute` and `appGUID` parameters you previously got from your Bluemix application dashboard.

You can now run your application. 

### Using the HelloTodo sample application

The HelloTodo sample a single view application with a simple list of todo items. If you previously added data through your web application you will see the data automatically pulled into the application. You can create and modify items directly in the application. Note that unlike the web UI you can also DELETE items from mobile app(press and hold) since it uses the Bluemix Mobile Services SDK which knows how to handle Mobile Client Access security. You can also mark items as completed by clicking to the left of the corresponding todo item. When you update an item in the mobile app it will automatically be updated in the web app (you will need to refresh the web UI). If you make a change in the web UI and want to see it reflected in the mobile app, simply pull down the todo list to refresh.

As you recall the DELETE endpoint can only be accessed by mobile applications since it is protected by a Mobile Client Access service. That said, by default Mobile Client Access is not configured to require any interactive authentication (e.g. ask for username and password). Next step is learning how to use Bluemix Mobile Services would be to configure authentication using the Mobile Client Access dashboard and instrument your app with required components. You can see full tutorial explaining how to do that in this blog [link](http://)

### License
This package contains sample code provided in source code form. The samples are licensed under the under the Apache License, Version 2.0 (the "License"). You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 and may also view the license in the license.txt file within this package. Also see the notices.txt file within this package for additional notices.
