package com.ericg.neatflix.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@SuppressLint("SetJavaScriptEnabled")
@Destination
@Composable
fun VideoPlayerScreen(
    navigator: DestinationsNavigator,
    mediaId: Int,
    mediaType: String,
    mediaTitle: String,
    streamUrl: String = "",
    season: Int = 0,
    episode: Int = 0
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var finalStreamUrl by remember { mutableStateOf("") }

    // Generate vidsrc.net URL based on media type if no URL provided
    LaunchedEffect(mediaId, mediaType, season, episode, streamUrl) {
        finalStreamUrl = if (streamUrl.isNotEmpty()) {
            streamUrl
        } else {
            when (mediaType.lowercase()) {
                "movie" -> "https://vidsrc.net/embed/movie/$mediaId"
                "tv" -> {
                    if (season > 0 && episode > 0) {
                        "https://vidsrc.net/embed/tv/$mediaId/$season/$episode"
                    } else {
                        "https://vidsrc.net/embed/tv/$mediaId"
                    }
                }
                else -> "https://vidsrc.net/embed/movie/$mediaId"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = mediaTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = Color.Black,
                contentColor = Color.White,
                elevation = 0.dp
            )

            // WebView for streaming
            if (finalStreamUrl.isNotEmpty()) {
                AndroidView(
                    factory = {
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.mediaPlaybackRequiresUserGesture = false
                            
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    if (newProgress == 100) {
                                        isLoading = false
                                    }
                                }
                            }
                            
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    return false
                                }
                                
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }
                            }
                        }
                    },
                    update = { webView ->
                        webView.loadUrl(finalStreamUrl)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading stream...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}