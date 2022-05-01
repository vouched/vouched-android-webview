# Vouched WebView (Android) Quickstart

Vouched Webview allows you to integrate with a Vouch enabled web application, and use it as a means of user verification within your moblile application, using native Android components (Webview).

### Building the Demo

We include a demo, that will allow you to quickly run a simple ID check to verify functionality. If you have a Vouched account and Android Studio installed on your computer, you can quickly get the demo application up and running by cloning the repository:

```shell
git clone https://github.com/vouched/vouched-android-webview
cd vouched-android-webview
```

Once you have the repository downloaded, find the file ```gradle.properties```, and add the appId (public key) you normally use for your account. Modify the line that says ```API_KEY="<PUBLIC_KEY_GOES_HERE>"``` and replace ```<PUBLIC_KEY_GOES_HERE>``` with your appId.

Make sure a phone is connected to your computer, and then build and install the demo app:

```./gradlew installDebug```

Find and launch the app called Webview Demo. A simple ID verification flow will run, using a Vouched demo widget. 


### Webview Integration with Vouched JS-Plugin

In Android we use the ```android.webkit.WebView``` and ```android.webkit.WebChromeClient``` components to host a web application. Most integrations will likely integrate with a page hosting the Vouched JS Plugin. If you haven't yet configured your web application to use the plugin, take a look at our [JS Plugin quickstart guide](https://docs.vouched.id/docs/js-plugin) to get started. 

**Note:** Android offers many different ways to build an application. We'll focus on the changes as shown in the demo, which is an individual activity, but these steps can be applied just as easily to fragment based applications or other hybrid architectures. Likewise, in this document, we show how to allow the JS Plugin to communicate results to the native mobile application, to allow you to navigate or change behavior based on verification results, but you can learn of other approaches that may be of interest in the [Build apps in Webview](https://developer.android.com/guide/webapps/webview) Android documentation.

#### Camera access

To allow web view access to device cameras, it is necessary for the user to give permission for the camera to be accessed. In your application ```AndroidManifest.xml```be sure to request camera permissions (and optionally ask the camera to autofocus):

```
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

Likewise, in your activity or fragment, you will want to allow access to the camera both within the Webview itself, as well as within the activity or fragment that hosts the Webview. Let's look at the demo code to illustrate how that is done:

- [Permissions handling within the webview](https://github.com/vouched/vouched-android-webview/blob/main/app/src/main/java/id/vouched/plugintest/MainActivity.kt#:~:text=override%20fun%20onPermissionRequest(request%3A%20PermissionRequest))
- [Permissions handling within the application](https://github.com/vouched/vouched-android-webview/blob/main/app/src/main/java/id/vouched/plugintest/MainActivity.kt#:~:text=override%20fun%20onRequestPermissionsResult)

#### Sharing verification results

In mobile applications that use both web application content and native code, it is useful for the web application (more accurately, the JS Plugin) to be able to share information with the native application, say for changing behavior or navigation based on what occured in the verification flow. (This can be bidirectional, but that is beyond the scope of this document).

To do this, we can use an Android feature called the *Javascript bridge*. We create a javascript interface in native code, that specifies the function names and data passed, which is then implemented in both the web application and Android native code that implements the interface.

An example interface ``VouchedJSInterface``, included in the demo is shown below. Note the inclusion of a listener interface ```VerificationListener```, which will implemented by the Android components that wish to listen to verification results:

```kotlin
// Example code for how to connect Android Webview with the
// Vouched js-plugin
class VouchedJSInterface(private val listener: VerificationListener) {
    @JavascriptInterface
    fun onVerifyResults(success: Boolean, results: String) {
        listener.onVerificationResults(success, results)
    }
}

// generic interface
interface VerificationListener {
    fun onVerificationResults(success: Boolean, results: String)
}
```

In our webview demo code, we add the interface:

```myWebView.addJavascriptInterface(VouchedJSInterface(this), "VouchedJS")```

Note that we are assigning "VouchedJS", meaning that when we are in the web application code, the function that implements the shared interface will be called as ```VouchedJS.onVerifyResults(success, results)```

And likewise, in your plugin declaration in javascript, you will want to modify the ```onDone(job)``` callback to also implement the interface when the verification job is complete. Looking at the Quickstart example, we would expect the onDone section to look like this:

```javascript
// called when the verification is completed.
onDone: (job) => {
  console.log("Verification complete", { token: job.token });
  VouchedJS.onVerifyResults(job.result.success === true, JSON.stringify(job));
}
```





