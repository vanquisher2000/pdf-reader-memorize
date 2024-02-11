package com.pdf.studymarkercompsose.screenUI

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarkercompsose.Screen
import com.pdf.studymarkercompsose.StartingScreenState
import com.pdf.studymarkercompsose.data.SharedViewModel
import com.pdf.studymarkercompsose.logicClasses.Reader

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun navigation(
    state: StartingScreenState,
    onSelectClick: () -> Unit,
    bookMap: Map<String, PdfData>,
    onCardClick: (String) -> Unit,
    onSwipeLeft: (String) -> Unit,
    sharedViewModel: SharedViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    openDialog: MutableState<Boolean>,
    appSettings: State<AppSettings>,
    reader: Reader
) : NavHostController {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(route = Screen.MainScreen.route){
            StartingScreen(
                state = state,
                onSelectClick = { onSelectClick() },
                bookMap = bookMap,
                onCardClick = onCardClick,
                onSwipeLeft = onSwipeLeft,
                onConfirm = { onConfirm() },
                onDismiss = { onDismiss() },
                openDialog = openDialog,
                appSettings = appSettings
            )
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
            ReadingScreen(
                sharedViewModel,
                reader = reader
            )
        }
    }
    return  navController
}