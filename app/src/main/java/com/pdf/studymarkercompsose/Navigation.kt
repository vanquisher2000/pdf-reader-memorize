package com.pdf.studymarkercompsose

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarkercompsose.data.SharedViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun navigation(
    openPdfPage: (Int) -> ImageBitmap,
    state: StartingScreenState,
    onSelectClick : () -> Unit,
    bookMap: Map<String , PdfData>,
    onCardClick : (String)-> Unit,
    onSwipeLeft : (String)-> Unit,
    sharedViewModel: SharedViewModel,
    onResumeReading : () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    openDialog: MutableState<Boolean>
) : NavHostController {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(route = Screen.MainScreen.route){
            StartingScreen(state = state,
                onSelectClick = { onSelectClick() },
                bookMap = bookMap,
                onCardClick = onCardClick,
                onSwipeLeft = onSwipeLeft,
                onConfirm = { onConfirm() },
                onDismiss = { onDismiss() },
                openDialog = openDialog
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
            ReadingScreen(sharedViewModel, openPdfPage , onResumeReading = onResumeReading )
        }
    }
    return  navController
}