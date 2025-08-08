package com.example.floatingkeyskeyboard

import android.inputmethodservice.InputMethodService
import android.view.View

class FloatingKeyboardService : InputMethodService() {

    private lateinit var keyboardView: FloatingKeyboardView

    override fun onCreateInputView(): View {
        keyboardView = FloatingKeyboardView(this)
        return keyboardView
    }
}