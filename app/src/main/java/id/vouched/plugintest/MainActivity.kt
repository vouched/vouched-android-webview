package id.vouched.plugintest

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.webview)
        setWebContentsDebuggingEnabled(true);

        //pre-approve permissions
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
                Log.d(">>> perms:", request.resources.contentToString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    runOnUiThread {
                        request.grant(request.resources)
                    }
                }
            }
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebView", consoleMessage.message())
                return true
            }
        })
        myWebView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            // replace with the URL of your Camera App
            loadUrl("https://df29-71-212-138-132.ngrok.io")
        }

        val reloadButton : Button = findViewById(R.id.reloadBtn)
        reloadButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                myWebView.reload()
            }
        })
    }
}