package com.pdf.studymarkercompsose

sealed class Screen(val route : String) {
    data object MainScreen : Screen("main_screen")
    object ReadingScreen : Screen("reading_screen")

}