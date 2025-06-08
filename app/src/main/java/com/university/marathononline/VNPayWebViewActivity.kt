package com.university.marathononline

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.university.marathononline.R

class VNPayWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    companion object {
        const val EXTRA_PAYMENT_URL = "payment_url"
        const val EXTRA_RETURN_URL = "return_url"

        // Thay đổi method này để support startActivityForResult
        fun startForResult(activity: Activity, paymentUrl: String, requestCode: Int, returnUrl: String = "") {
            val intent = Intent(activity, VNPayWebViewActivity::class.java).apply {
                putExtra(EXTRA_PAYMENT_URL, paymentUrl)
                putExtra(EXTRA_RETURN_URL, returnUrl)
            }
            activity.startActivityForResult(intent, requestCode)
        }

        // Giữ lại method cũ để backward compatibility
        fun start(context: Context, paymentUrl: String, returnUrl: String = "") {
            val intent = Intent(context, VNPayWebViewActivity::class.java).apply {
                putExtra(EXTRA_PAYMENT_URL, paymentUrl)
                putExtra(EXTRA_RETURN_URL, returnUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Thiết lập để Activity có thể hiển thị bàn phím
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        setContentView(createWebViewLayout())

        val paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL) ?: ""
        if (paymentUrl.isEmpty()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setupWebView()
        webView.loadUrl(paymentUrl)
    }

    private fun createWebViewLayout(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Toolbar
            val toolbar = Toolbar(this@VNPayWebViewActivity).apply {
                title = "Thanh toán VNPay"
                setNavigationIcon(R.drawable.ic_back) // Thay bằng icon back của bạn
                setNavigationOnClickListener {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            addView(toolbar)

            // Progress Bar
            progressBar = ProgressBar(this@VNPayWebViewActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                visibility = View.GONE
            }
            addView(progressBar)

            // WebView
            webView = WebView(this@VNPayWebViewActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            addView(webView)
        }
    }

    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true

                // Quan trọng: Thiết lập để WebView có thể focus và nhận input
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { currentUrl ->
                        Log.d("VNPay", "URL Loading: $currentUrl")

                        if (currentUrl.contains("vnpay-return") ||
                            currentUrl.contains("marathononlineapi.onrender.com/api/v1/payment/vnpay-return")) {

                            // Trả kết quả về Activity trước
                            val resultIntent = Intent().apply {
                                putExtra("payment_result_url", currentUrl)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                            return true
                        }
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    Log.d("VNPay", "Page finished loading: $url")
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.progress = newProgress
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            setResult(RESULT_CANCELED)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}