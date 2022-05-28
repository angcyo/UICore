// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.angcyo.browser.custom

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.angcyo.browser.R

/**
 * This Activity is used as a fallback when there is no browser installed that supports
 * Chrome Custom Tabs
 */
class WebviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        val url = intent.getStringExtra(EXTRA_URL)
        val webView = findViewById<WebView>(R.id.webview)
        webView.webViewClient = WebViewClient()
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        title = url
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webView.loadUrl(url!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_URL = "extra.url"
    }
}