package com.example.floatingkeyskeyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.floatingkeyskeyboard.ui.theme.FloatingKeysKeyboardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Open keyboard settings
        val imeSettingsIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        imeSettingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(imeSettingsIntent)

        // Show input picker
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()

        // UI
        setContent {
            FloatingKeysKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InfoScreen()
                }
            }
        }
    }
}

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Floating Keyboard Activated!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Please enable the keyboard in settings and switch to it using the input picker.")
    }
}