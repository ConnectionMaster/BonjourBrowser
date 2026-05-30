package com.druk.servicebrowser.ui

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.druk.servicebrowser.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HTMLViewerActivity : AppCompatActivity() {

    private lateinit var mText: TextView
    private lateinit var mLoading: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_viewer)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mText = findViewById(R.id.text)
        mLoading = findViewById(R.id.loading)

        if (intent.hasExtra(Intent.EXTRA_TITLE)) {
            title = intent.getStringExtra(Intent.EXTRA_TITLE)
        }

        val assetPath = extractAssetPath(intent.data?.toString())
        if (assetPath == null) {
            Log.w(TAG, "Missing or unsupported URI: ${intent.data}")
            Toast.makeText(this, R.string.cannot_open_link, Toast.LENGTH_SHORT).show()
            mLoading.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching {
                    val raw = assets.open(assetPath).use {
                        it.reader(Charsets.UTF_8).readText()
                    }
                    reflowForMobile(raw)
                }.onFailure { Log.w(TAG, "Failed to read asset $assetPath", it) }
                    .getOrNull()
            }
            mLoading.visibility = View.GONE
            if (content != null) {
                mText.text = content
            } else {
                Toast.makeText(
                    this@HTMLViewerActivity,
                    R.string.cannot_open_link,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val TAG = "HTMLViewer"
        private const val ASSET_PREFIX = "file:///android_asset/"
        private const val INDENT_TITLE_THRESHOLD = 15

        private fun extractAssetPath(uri: String?): String? {
            if (uri.isNullOrEmpty() || uri == "null") return null
            return if (uri.startsWith(ASSET_PREFIX)) uri.removePrefix(ASSET_PREFIX) else null
        }

        /**
         * Re-flow pre-wrapped 80-column legal text for narrow screens.
         *
         * Each blank-line-separated paragraph is unwrapped: soft line
         * breaks become spaces so the TextView can wrap to the actual
         * viewport. Paragraphs whose every line is heavily indented
         * (a centered title block) keep their original line breaks
         * instead of being collapsed into a single sentence.
         */
        internal fun reflowForMobile(raw: String): String {
            // trimEnd only -- a leading title block may start with indent
            // whitespace that the heuristic below needs to see.
            val paragraphs = raw.trimEnd().split(Regex("\n\\s*\n"))
                .filter { it.isNotBlank() }
            return paragraphs.joinToString("\n\n") { para ->
                val lines = para.split("\n").filter { it.isNotBlank() }
                val isTitleBlock = lines.all { line ->
                    line.takeWhile { it.isWhitespace() }.length > INDENT_TITLE_THRESHOLD
                }
                if (isTitleBlock) {
                    lines.joinToString("\n") { it.trim() }
                } else {
                    lines.joinToString(" ") { it.trim() }
                }
            }
        }
    }
}
