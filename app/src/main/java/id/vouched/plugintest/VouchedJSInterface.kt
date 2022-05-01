package id.vouched.plugintest

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast

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