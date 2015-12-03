# Android HelloTodo sample for Bluemix Mobile Services
---
This HelloTodo sample contains an Android project to be used to communicate with a StrongLoop based mobile backend created using MobileFirst Services Boilerplate on IBM Bluemix. You can either watch the video tutorial or follow the below instructions that take you step by step through a process of creating a mobile backend and getting this sample running.

->![image](video-coming-soon.png)<-
> We're working on creating a video tutorial. It will be published here once ready

Use the following steps to configure the helloTodo sample for Objective-C:

1. [Download the helloTodo sample](#download-the-hellotodo-sample)
2. [Configure the mobile backend for your helloTodo application](#configure-the-mobile-backend-for-your-hellotodo-application)
3. [Configure the front end in the helloTodo sample](#configure-the-front-end-in-the-hellotodo-sample)
4. [Run the helloTodo sample application](#run-the-hellotodo-sample-application)

### Before you begin
Before you start, make sure you have the following:

- A [Bluemix](http://bluemix.net) account.

### Download the helloTodo sample
Clone the sample from Github with the following command:

```git clone https://github.com/ibm-bluemix-mobile-services/bms-samples-android-hellotodo```

### Configure the mobile backend for your helloTodo application

> If you have already followed steps described in other tutorials and created a mobile backend using MobileFirst Services Boilerplate you might want to skip to the [Configuring the front end in the helloTodo sample](#configuring-the-front-end-in-the-hellotodo-sample) section
 
Before you can run the helloTodo application, you must set up an app on Bluemix.  The following procedure shows you how to create a MobileFirst Services Starter application. This will provision a Node.JS runtime and populate it with with a default helloTodo application created using StrongLoop. This application uses LoopBack framework to expose the `/api/Items` API which will be used by both Web UI and the helloTodo app sample from this Github repository. . The Cloudant®NoSQL DB, IBM Push Notifications, and Mobile Client Access services are also added to the app.

Create a mobile backend in the  Bluemix dashboard:

1.	In the **Boilerplates** section of the Bluemix catalog, click **MobileFirst Services Starter**.
2.	Enter a name and host for your mobile backend and click **Create**.
3.	Click **Finish**.

Once the above provisioning process is complete you'll be taken to a Bluemix Dashboard for your newly provisioned mobile backend. Click the **Mobile Options** link in top right part of a screen to find your **appRoute* and *appGUID*. Keep this screen open in your browser as you you will need these parameters shortly. 

Open the appRoute URL in your browser. You will see the web interface for the helloTodo backend. Start by following the guided experience steps described in the web UI. Eventually you will try to DELETE a todo item and will discover that this action can only be complete when using the helloTodo mobile apps sample from this Github repository. This is due to a fact that the mobile backend is by default protected by a Mobile Client Access - a Bluemix service that provides security and monitoring functionality for mobile backends. Following steps will guide you through obtaining and running the helloTodo mobile application. 

(Optionally you might want to hit the "View API Reference" button on web UI to see the API specs)

### Configure the front end in the helloTodo sample

1. Using Android Studio, open the `bms-samples-android-hellotodo` directory where the project was cloned.
2. Run a Gradle sync (usually starts automatically) to import the required `core` SDK. You can view the **build.gradle** file in the following directory:

	`helloTodo\app\build.gradle` where you'll find:

```Gradle
    compile group: 'com.ibm.mobilefirstplatform.clientsdk.android',
            name: 'core',
            version: '1.+',
            ext: 'aar',
            transitive: true
```

This section in build.gradle file makes Android Studio to automatically download the Bluemix Mobile Services Core SDK and add it to the project. 

> **Note**: This sample depends on 1.+ version of the Core SDK. This means that the most recent 1.* version will be downloaded automatically. When creating a production applications it is recommended to define the version explicitly (1.0.0 for example) to ensure consistent builds.

3. Once that is complete, open `MainActivity.java` and locate the try block within the ```onCreate()``` function.
4. In the ```BMSClient.getInstance().initialize()``` function replace ```<APPLICATION_ROUTE>``` and ```<APPLICATION_ID>``` with the application route and ID you were given when creating your application on Bluemix.

```Java
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

You can now run your application. 

### Run the helloTodo sample application

The HelloTodo sample is a single view application with a simple list of todo items. If you previously added data through your web application you will see the data automatically pulled into the application. You can create and modify items directly in the application. Note that the HelloTodo sample applications uses Bluemix Mobile Services SDK which knows how to handle Mobile Client Access security. Therefore, unlike the web UI, you can also DELETE items from mobile app by long-pressing them. You can also mark items as completed by clicking to the left of the corresponding todo item. When you update an item in the mobile app it will automatically be updated in the web app (you will need to refresh the web UI). If you make a change in the web UI and want to see it reflected in the mobile app, simply pull down the todo list to refresh.

As you recall the DELETE endpoint can only be accessed by mobile applications since it is protected by a Mobile Client Access service. That said, by default Mobile Client Access is not configured to require any interactive authentication (e.g. ask for username and password). Next step is learning how to configure authentication using the Mobile Client Access dashboard and instrument your app with required components. You can either check [Mobile Client Documentation](https://www.ng.bluemix.net/docs/services/mobileaccess/index.html) for that or watch the below video showing the process in details. 

->![image](video-coming-soon.png)<-
> We're working on creating a video tutorial. It will be published here once ready

### License
This package contains sample code provided in source code form. The samples are licensed under the under the Apache License, Version 2.0 (the "License"). You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 and may also view the license in the license.txt file within this package. Also see the notices.txt file within this package for additional notices.
