package com.pdf.studymarkercompsose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun Navigation(intList : MutableList<Int> , openPdfPage : (Int) -> ImageBitmap){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.ReadingScreen.route){
        composable(route = Screen.MainScreen.route){

        }
        composable(
            route = Screen.ReadingScreen.route,
            arguments = listOf(
                navArgument("pageCount" ){
                    type = NavType.IntType
                    defaultValue = 0
                    //nullable = true
                }
            )
            ){
            ReadingScreen(intList, openPdfPage )
        }
    }
}