package com.grappim.taigamobile.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.grappim.taigamobile.core.storage.ThemeSetting
import com.grappim.taigamobile.ui.utils.FilePicker
import com.grappim.taigamobile.ui.utils.LocalFilePicker
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @SuppressLint("Range")
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        val inputStream = contentResolver.openInputStream(it) ?: return@registerForActivityResult
        val fileName = contentResolver.query(it, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } ?: return@registerForActivityResult

        filePicker.filePicked(fileName, inputStream)
    }

    private val filePicker: FilePicker = object : FilePicker() {
        override fun requestFile(onFilePicked: (String, InputStream) -> Unit) {
            super.requestFile(onFilePicked)
            getContent.launch("*/*")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val theme by viewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                ThemeSetting.Light -> false
                ThemeSetting.Dark -> true
                ThemeSetting.System -> isSystemInDarkTheme()
            }

            TaigaMobileTheme(darkTheme) {
                CompositionLocalProvider(
                    LocalFilePicker provides filePicker
                ) {
                    MainContent(viewModel)
                }
            }
        }
    }
}
