package id.vouched.plugintest

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myWebView = WebView(applicationContext)

        val permissions = arrayOf<String>(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA
        )

        // prompts for camera permissions om load
        ActivityCompat.requestPermissions(
            this,
            permissions,
            1010
        )

        myWebView.setWebChromeClient(object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d("test", request.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.resources)
                }
            }
        })
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.mediaPlaybackRequiresUserGesture = false
        // replace with the URL of your React App
        myWebView.loadUrl("https://webcamtoy.com")
        setContentView(myWebView)
    }
}