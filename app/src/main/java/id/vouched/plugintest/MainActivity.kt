package id.vouched.plugintest

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), VerificationListener {

    private val fileChooserLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        result.data?.let { data ->
            WebChromeClient.FileChooserParams.parseResult(RESULT_OK, data)?.let { uris ->
                webChromeClientFilePathCallback?.onReceiveValue(uris)
                webChromeClientFilePathCallback = null
                return@registerForActivityResult
            }
        }
        webChromeClientFilePathCallback?.onReceiveValue(null)
        webChromeClientFilePathCallback = null
    }

    private var webChromeClientFilePathCallback: ValueCallback<Array<Uri>>? = null

    private var cameraPermission: PermissionRequest? = null

    // request permission ids - these values can be any unique
    // integer, and are used to identity permission request callbacks
    private val REQUEST_CAMERA_PERMISSION = 100001
    private val REQUEST_FINE_LOCATION_PERMISSION = 100002
    private var geolocationOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null

    // point the webappUrl to your plugin instance
    private val webappUrl = "https://static.stage.vouched.id/widget/demo/index.html#"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.webview)
        setWebContentsDebuggingEnabled(true)

        // uncomment this section to allow web links in [myWebView], please make sure to only allow
        // trusted links
        /*
        val onBackPressedCallbackForWebView = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val currentIndex = myWebView.copyBackForwardList().currentIndex
                if (myWebView.canGoBack()) myWebView.goBack()
                if (currentIndex == 1) isEnabled = false
            }
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallbackForWebView)

        myWebView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.url?.let {
                    view?.loadUrl(it.toString())
                    onBackPressedCallbackForWebView.isEnabled = true
                }
                return true
            }
        } */

        myWebView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                webChromeClientFilePathCallback?.let {
                    it.onReceiveValue(null)
                    webChromeClientFilePathCallback = null
                }
                webChromeClientFilePathCallback = filePathCallback
                fileChooserParams?.createIntent()?.let { intent ->
                    try {
                        fileChooserLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this@MainActivity, "Cannot Open File Chooser", Toast.LENGTH_LONG)
                            .show()
                        return false
                    }
                }
                return true
            }

            // convenience method to expose console.log output.
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebView", consoleMessage.message())
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                // todo - probably best to check the request origin url
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
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
                            REQUEST_FINE_LOCATION_PERMISSION
                        )
                        // hold onto the origin and callback for
                        // permissions results callback
                        geolocationOrigin = origin
                        geolocationCallback = callback
                    }
                }
            }
        }
        // create a bridge between the javascript and the activity, which will
        // be referenced by the name 'VouchedJS' in our webapp's javascript
        myWebView.addJavascriptInterface(VouchedJSInterface(this), "VouchedJS")
        myWebView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
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
            REQUEST_FINE_LOCATION_PERMISSION -> {
                var allow = false
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed this permission
                    allow = true
                }
                geolocationCallback?.let {
                    it.invoke(geolocationOrigin, allow, false)
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
