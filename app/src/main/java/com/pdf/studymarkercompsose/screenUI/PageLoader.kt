package com.pdf.studymarkercompsose.screenUI

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import com.pdf.studymarkercompsose.logicClasses.Reader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnrememberedMutableState", "CoroutineCreationDuringComposition")
@Composable
fun pageLoader(
    reader: Reader,
    pageNo: Int,
    updateProgress: (Float) -> Unit,
    updateCallCounter: () -> Int,
) : MutableState<ImageBitmap?>{
    Log.d("TAG", "pageLoader: called $pageNo")
    //val coroutineScope = rememberCoroutineScope()
    val imageState = remember { mutableStateOf<ImageBitmap?>(null) }
    //imageState.value = ImageBitmap(100 , 100)
    updateCallCounter()

   /* val job = coroutineScope.launch {
            Log.d("TAG", "pageLoader: loading")
            imageState.value = async { reader.openPdfPage(pageNo) }.await()
            Log.d("TAG", "pageLoader: doneLoading")
        }*/

    LaunchedEffect(key1 = null) {
        withContext(Dispatchers.Default) {
            updateProgress(1f)
        Log.d("TAG", "pageLoader: loading $pageNo")
        //imageState.value = async { reader.openPdfPage(pageNo) }.await()
            this.launch{ imageState.value =  reader.loadPage(pageN0 =  pageNo , isAsync = true) }.join()
            Log.d("TAG", "pageLoader: doneLoading : $pageNo")
     }
    }

    return imageState
}