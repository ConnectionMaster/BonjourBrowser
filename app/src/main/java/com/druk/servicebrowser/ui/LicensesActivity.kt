/*
 * Copyright (C) 2015 Andriy Druk
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
package com.druk.servicebrowser.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.druk.servicebrowser.R

class LicensesActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: OpenSourceComponentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = OpenSourceComponentAdapter(LICENSES)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        mAdapter.listener = this
    }

    override fun onPause() {
        super.onPause()
        mAdapter.listener = null
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onClick(v: View) {
        val position = mLayoutManager.getPosition(v)
        val license = LICENSES[position]
        val intent = Intent(v.context, HTMLViewerActivity::class.java).apply {
            data = Uri.parse(ANDROID_ASSETS_FILE_PATH + license.asset)
            putExtra(Intent.EXTRA_TITLE, license.name)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        try {
            v.context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Failed to find viewer", e)
        }
    }

    private data class OssComponent(val name: String, val asset: String)

    private class OpenSourceComponentAdapter(
        private val items: List<OssComponent>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var listener: View.OnClickListener? = null

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(R.layout.one_text_item, viewGroup, false)
            ) {}
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
            (viewHolder.itemView as AppCompatTextView).text = items[i].name
            viewHolder.itemView.setOnClickListener(listener)
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        private const val TAG = "LicensesActivity"
        private const val ANDROID_ASSETS_FILE_PATH = "file:///android_asset/"
        private const val APACHE_2_0 = "APACHE-LICENSE-2.0.txt"

        private val LICENSES = listOf(
            OssComponent("AndroidX Annotation", APACHE_2_0),
            OssComponent("AndroidX AppCompat", APACHE_2_0),
            OssComponent("AndroidX Browser", APACHE_2_0),
            OssComponent("AndroidX CardView", APACHE_2_0),
            OssComponent("AndroidX Core KTX", APACHE_2_0),
            OssComponent("AndroidX Fragment KTX", APACHE_2_0),
            OssComponent("AndroidX Lifecycle", APACHE_2_0),
            OssComponent("AndroidX RecyclerView", APACHE_2_0),
            OssComponent("AndroidX SlidingPaneLayout", APACHE_2_0),
            OssComponent("Kotlin Standard Library", APACHE_2_0),
            OssComponent("Kotlinx Coroutines", APACHE_2_0),
            OssComponent("Material Components for Android", APACHE_2_0),
        )
    }
}
