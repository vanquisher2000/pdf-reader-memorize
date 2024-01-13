package com.pdf.studymarkercompsose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StartingScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(StartingScreenState())
    val state = _state.asStateFlow()
}

class StartingScreenState