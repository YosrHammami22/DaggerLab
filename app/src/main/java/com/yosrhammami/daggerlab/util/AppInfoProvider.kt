package com.yosrhammami.daggerlab.util

import android.content.Context
import com.yosrhammami.daggerlab.R
import javax.inject.Inject

class AppInfoProvider  @Inject constructor( private val context: Context) {
    fun appName(): String = context.getString(R.string.app_name)
}