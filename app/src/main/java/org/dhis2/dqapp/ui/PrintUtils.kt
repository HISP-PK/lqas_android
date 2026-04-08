package org.dhis2.dqapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
fun exportHtmlToPdf(
    context: Context,
    html: String,
    jobName: String = "LQAS Report",
    onSuccess: (String) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val activity = context.findActivity()
    if (activity == null) {
        onError("Cannot open print preview from current screen.")
        return
    }

    val container = activity.findViewById<ViewGroup>(android.R.id.content)
    val webView = WebView(activity)
    var done = false

    fun cleanup() {
        try {
            container.removeView(webView)
        } catch (_: Exception) {
        }
        try {
            webView.destroy()
        } catch (_: Exception) {
        }
    }

    fun fail(message: String) {
        if (done) return
        done = true
        onError(message)
        cleanup()
    }

    webView.layoutParams = ViewGroup.LayoutParams(1, 1)
    webView.alpha = 0f
    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true
    webView.settings.loadWithOverviewMode = true
    webView.settings.useWideViewPort = true

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            if (done) return

            webView.postDelayed({
                if (done) return@postDelayed
                    try {
                        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        val attrs = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.NA_LEGAL.asPortrait())
                            .build()
                        printManager.print(jobName, printAdapter, attrs)
                    done = true
                    onSuccess("Print preview opened. Use printer selector or Save as PDF.")
                    webView.postDelayed({ cleanup() }, 120_000)
                } catch (ex: Exception) {
                    fail(ex.message ?: "Unable to open print preview.")
                }
            }, 500)
        }
    }

    try {
        container.addView(webView)
        webView.loadDataWithBaseURL("about:blank", html, "text/html", "UTF-8", null)
    } catch (ex: Exception) {
        fail(ex.message ?: "Failed to prepare print preview.")
        return
    }

    webView.postDelayed({
        if (!done) {
            fail("Print preview did not start. Check Android Print service.")
        }
    }, 15_000)
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
