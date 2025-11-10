package com.astro.space.apps.kshnzk

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BrowserScreen(contentLink: String) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isFirstLoad by remember { mutableStateOf(true) }
    var browserView by remember { mutableStateOf<WebView?>(null) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    
    BackHandler(enabled = true) {
        browserView?.let { view ->
            if (view.canGoBack()) {
                view.goBack()
            }
        } ?: run {
            backDispatcher?.onBackPressed()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    browserView = this
                    
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, address: String?) {
                            super.onPageFinished(view, address)
                            if (isFirstLoad) {
                                isLoading = false
                                isFirstLoad = false
                            }
                        }
                    }
                    
                    webChromeClient = WebChromeClient()
                    
                    loadUrl(contentLink)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
            }
        )
        
        if (isLoading && isFirstLoad) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
        }
    }
}

