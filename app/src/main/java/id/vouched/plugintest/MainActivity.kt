package id.vouched.plugintest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), VerificationListener {

    private var cameraPermission: PermissionRequest? = null
    // request permission ids - these values can be any unique
    // integer, and are used to identity permission request callbacks
    private val REQUEST_CAMERA_PERMISSION = 100001
    private val REQUEST_FINE_LOCATION_PERMISSION = 100002
    private val REQUEST_FILE_ACCESS_PERMISSION = 100003;
    private var geolocationOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null
    private var filePathCallback: ValueCallback<Array<Uri>?>? = null
    private var cameraPhotoPath: String? = null

    //point the webappUrl to your plugin instance
    private val webappUrl = "https://static.stage.vouched.id/widget/demo/index.html#"
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
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                cameraPermission = request
            }

//            override fun onShowFileChooser(
//                webView: WebView?, newFilePathCallback: ValueCallback<Array<Uri?>?>,
//                fileChooserParams: FileChooserParams?
//            ): Boolean {
//                filePathCallback?.onReceiveValue(null)
//                filePathCallback = newFilePathCallback
//                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                if (takePictureIntent!!.resolveActivity(packageManager) != null) {
//                    // Create the File where the photo should go
//                    var photoFile: File? = null
//                    // Create an image file name
//                    val timeStamp: String =
//                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//                    val imageFileName = "JPEG_" + timeStamp + "_"
//                    val storageDir: File =
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                    try {
//                        photoFile = File.createTempFile(imageFileName, ".jpg", storageDir)
//                    } catch (ex: IOException) {
//                        // Error occurred while creating the File
//                    }
//
//                    // Continue only if the File was successfully created
//                    if (photoFile != null) {
//                        cameraPhotoPath = "file:" + photoFile.getAbsolutePath()
//                        takePictureIntent!!.putExtra(
//                            MediaStore.EXTRA_OUTPUT,
//                            Uri.fromFile(photoFile)
//                        )
//                    } else {
//                        takePictureIntent = null
//                    }
//                }
//                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
//                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
//                contentSelectionIntent.type = "image/*"
//                val intentArray: Array<Intent?>
//                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
//                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
//                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
//                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
//                startActivityForResult(chooserIntent, REQUEST_FILE_ACCESS_PERMISSION)
//                return true
//            }

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