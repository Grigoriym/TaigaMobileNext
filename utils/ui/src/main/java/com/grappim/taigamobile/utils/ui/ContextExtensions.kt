package com.grappim.taigamobile.utils.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity

@Deprecated("I am not sure we need it")
val Context.activity: AppCompatActivity
    get() = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.activity
        else -> error("Context is not an Activity")
    }
