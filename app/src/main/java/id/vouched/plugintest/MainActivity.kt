package id.vouched.plugintest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity(), VerificationListener {

    private var cameraPermission: PermissionRequest? = null
    private val CAMERA_PERMISSION_REQUEST = 101299
    //add your app key in gradle.properties
    private val appId = BuildConfig.API_KEY
    // the app url - this points to the Vouched production demo widget
    // if hosting your own endpoint, change the url below.
    private val webappUrl = "https://static.vouched.id/widget/demo/index.html#/demo?detectorRunFrameInterval=2&stepTitles%5BFrontId%5D=Upload%20ID&stepTitles%5BFace%5D=Upload%20Headshot&stepTitles%5BDone%5D=Finished&stepTitles%5BID_Captured%5D=ID%20Captured&stepTitles%5BFace_Captured%5D=Face%20Captured&stepTitles%5BStart%5D=Start&stepTitles%5BBackId%5D=ID%20%28Back%29&content%5BcrossDeviceShowOff%5D=true&showUploadFirst=true&showProgressBar=true&appId=${appId}&testingUri=https%3A%2F%2Fverify.vouched.id%2F&crossDevice=false&id=both&face=both&liveness=straight&debug=true&showFPS=false&sandbox=true&theme%5Bname%5D=verbose&theme%5BnavigationDisabledBackground%5D=rgba%28203%2C%20203%2C%20203%2C%200.15%29&theme%5BnavigationDisabledText%5D=%23888&theme%5BbaseColorLight%5D=rgb%28232%2C244%2C252%29&theme%5BprogressIndicatorTextColor%5D=%23000&type=id&survey=true&includeBackId=true&includeBarcode=true&disableCssBaseline=false&showTermsAndPrivacy=false&maxRetriesBeforeNext=0&idShowNext=0&handoffView%5BonlyShowQRCode%5D=false&locale=en&userConfirmation%5BconfirmData%5D=false&userConfirmation%5BconfirmImages%5D=false&isStage=true&manualCaptureTimeout=35000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.webview)
        setWebContentsDebuggingEnabled(true);

        myWebView.setWebChromeClient(object : WebChromeClient() {

            // convenience method to expose console.log output.
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebView", consoleMessage.message())
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                // todo - probably best to check the request origin url
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
                cameraPermission = request
            }
        })
        // create a bridge between the javascript and the activity, which will
        // be referenced by the name 'VouchedJS' in our webapp's javascript
        myWebView.addJavascriptInterface(VouchedJSInterface(this), "VouchedJS")
        myWebView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            // remember to replace webAppUrl with the URL of your Camera App
            loadUrl(webappUrl)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermission?.grant(cameraPermission?.resources)
            } else {
                cameraPermission?.deny()
            }
        }
    }

    // listens for verification results that are return from javaqscript
    override fun onVerificationResults(success:Boolean, results: String) {
        Log.d("VerificationListener", success.toString())
        Log.d("VerificationListener", results)
    }
}