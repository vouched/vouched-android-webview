package id.vouched.plugintest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(), VerificationListener {

    private var cameraPermission: PermissionRequest? = null
    private val REQUEST_CAMERA_PERMISSION = 101299
    private val REQUEST_FINE_LOCATION = 8052002
    private var geolocationOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null

    //point the webappUrl to your plugin instance
    private val webappUrl = "PLUGIN_URL_HERE"
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
                    REQUEST_CAMERA_PERMISSION
                )
                cameraPermission = request
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                val perm = Manifest.permission.ACCESS_FINE_LOCATION
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        perm
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // we're on SDK < 23 OR user has already granted permission
                    callback.invoke(origin, true, false)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            perm
                        )
                    ) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(perm),
                            REQUEST_FINE_LOCATION
                        )
                        // hold onto the origin and callback for
                        // permissions results callback
                        geolocationOrigin = origin
                        geolocationCallback = callback
                    }
                }
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
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_FINE_LOCATION -> {
                var allow = false
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed this permission
                    allow = true
                }
                if (geolocationCallback != null) {
                    // call back to web chrome client
                    geolocationCallback!!.invoke(geolocationOrigin, allow, false)
                }
            }
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermission?.grant(cameraPermission?.resources)
                } else {
                    cameraPermission?.deny()
                }
            }
        }
    }

    // listens for verification results that are return from javascript
    override fun onVerificationResults(success: Boolean, results: String) {
        // based on success / fail, implement the code you wish, ie
        // navigate to another fragment / activity
        Log.d("VerificationListener", success.toString())
        Log.d("VerificationListener", results)
    }
}