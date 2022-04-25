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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    private var cameraPermission: PermissionRequest? = null
    private val CAMERA_PERMISSION_REQUEST = 101299
    //the vouched app key you are using
    private val appId = "<Add your app id / public key here>"
    // this points to the demo flow in production,
    // replace this with whatever url you are using
    private val webappUrl = "https://static.vouched.id/widget/demo/index.html#/demo?recognizeIDThreshold=0.81&cardIDThreshold=0.2&generalThreshold=0.9&glareQualityThreshold=0.6&qualityThreshold=0.6&selfieThreshold=0&holdSteadyIntervalFace=1250&detectorRunFrameInterval=2&stepTitles%5BFrontId%5D=Upload%20ID&stepTitles%5BFace%5D=Upload%20Headshot&stepTitles%5BDone%5D=Finished&stepTitles%5BID_Captured%5D=ID%20Captured&stepTitles%5BFace_Captured%5D=Face%20Captured&stepTitles%5BStart%5D=Start&stepTitles%5BBackId%5D=ID%20%28Back%29&content%5BcrossDeviceShowOff%5D=true&showUploadFirst=true&showProgressBar=true&appId=${appId}&testingUri=https%3A%2F%2Fverify.vouched.id%2F&crossDeviceQRCode=false&crossDeviceHandoff=false&crossDevice=false&crossDeviceSMS=false&id=both&face=both&liveness=straight&enableEyeCheck=false&debug=true&showFPS=false&sandbox=false&theme%5Bname%5D=verbose&theme%5Bfont%5D=Arial%2C%20Helvetica%2C%20sans-serif&theme%5BfontColor%5D=%23333&theme%5BiconLabelColor%5D=%23333&theme%5BbgColor%5D=%23FFF&theme%5BbaseColor%5D=%232E159F&theme%5BnavigationDisabledBackground%5D=rgba%28203%2C%20203%2C%20203%2C%200.15%29&theme%5BnavigationDisabledText%5D=%23888&theme%5BbaseColorLight%5D=rgb%28232%2C244%2C252%29&theme%5BprogressIndicatorTextColor%5D=%23000&type=id&survey=true&includeBackId=true&includeBarcode=true&disableCssBaseline=false&showTermsAndPrivacy=false&maxRetriesBeforeNext=0&idShowNext=0&handoffView%5BonlyShowQRCode%5D=false&locale=en&userConfirmation%5BconfirmData%5D=false&userConfirmation%5BconfirmImages%5D=false&isStage=true&manualCaptureTimeout=35000"

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
                // todo - probably best to check the request origin url to be sure,
                // before you request permission
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
                cameraPermission = request
            }
        })
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
}