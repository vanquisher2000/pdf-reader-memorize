package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageData
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.MultiFloatingState
import com.pdf.studymarkercompsose.data.RectData
import com.pdf.studymarkercompsose.data.SharedViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReadingScreen(
    sharedModel: SharedViewModel,
    openPdfPage: (Int) -> ImageBitmap,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onResumeReading : () -> Unit
) {

    val TAG = "ReadingScreen"


    val rectMap : MutableMap<Int , SnapshotStateList<RectData>> = mutableMapOf()
    val savedMap = sharedModel.currentDrawingList.value!!.toMap()

    /*savedMap[it]?.let { savedList ->
        savedList.forEach { rect ->
            rectMap[it]?.add(rect)
        }
    }*/

    savedMap.forEach{(t,u) ->
        Log.d(TAG, "ReadingScreen: adding 1 initially")
        if(rectMap[t] == null) rectMap[t] = SnapshotStateList()
        savedMap[t]?.forEach { rectMap[t]?.add(it) }
    }

    if(sharedModel.drawingsLifeData.value == null) sharedModel.drawingsLifeData.value = mutableMapOf()

    Log.d(TAG, "ReadingScreen: saved map loaded : ${savedMap.size} , original size = ${sharedModel.currentDrawingList.value!!.size}")

    val pageCount = sharedModel.pageCount.value

    Log.d(TAG, "ReadingScreen: $pageCount")

    val items = listOf(
        MiniFabItem(
            icon = Icons.Default.Create
            ,
            label = "draw rect" ,
            id = ButtonId.Rect
        ),
        MiniFabItem(
            icon = Icons.Default.Delete
            ,
            label = "delete" ,
            id = ButtonId.Delete
        ),
        MiniFabItem(
            icon = Icons.Default.LocationOn,
            label = "path",
            id = ButtonId.Path
        )
    )

    val state = rememberLazyListState()

    val context = LocalContext.current

    val intList : MutableList<Int> = mutableListOf()

    for(i in 0 until pageCount!!){
        intList.add(i)
    }


    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    var multiFloatingState by remember {
        mutableStateOf(MultiFloatingState.Collapsed)
    }

    val modeState = remember { mutableStateOf(ModeState.Idle) }
    val currentPage = remember { mutableIntStateOf(0) }
    val twoFingers = remember {
        mutableStateOf(false)
    }

    DisposableEffect(key1 = lifecycleOwner  ) {
        val observer = LifecycleEventObserver{ source, event ->
            Log.d(TAG, "ReadingScreen: event is ${event.name}")
            if(event == Lifecycle.Event.ON_RESUME){

                /*onResumeReading()
                val savedMapRes = sharedModel.currentDrawingList.value!!.toMap()

                savedMapRes.forEach{(t,u) ->
                    Log.d(TAG, "ReadingScreen: adding 2 on resume")
                    if(rectMap[t] == null) rectMap[t] = SnapshotStateList()
                    savedMapRes[t]?.forEach { rectMap[t]?.add(it) }
                }
                sharedModel.drawingsLifeData.value?.forEach{(t,u) ->
                    if(rectMap[t] == null) rectMap[t] = SnapshotStateList()
                    sharedModel.drawingsLifeData.value!![t]?.forEach { rectMap[t]?.add(it) }
                }*/
            }
            if(event == Lifecycle.Event.ON_DESTROY){
                Log.d(TAG, "ReadingScreen: destroyed")
            }else if(event == Lifecycle.Event.ON_PAUSE){
                Log.d(TAG, "ReadingScreen: Here!!!!!!!!!")
                lifecycleOwner.lifecycleScope.launch {
                    saveDrawings(
                        sharedModel = sharedModel,
                        context = context,
                        currentPage = currentPage.intValue,
                        state = state,
                        //rectMap = savedMap
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    Scaffold (floatingActionButton = {
        MultiFab(
            multiFloatingState = multiFloatingState ,
            items = items ,
            onMultiFloatingStateChange = {multiFloatingState = it},
            modeState = modeState.value,
            onModeStateChange = { modeState.value = it }
        )

    }) {

        val pageNo = remember { derivedStateOf { state.firstVisibleItemIndex } }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { tapOffset ->
                        zoom = if (zoom > 1f) 1f else 2f
                        offset = calculateDoubleTapOffset(zoom, size, tapOffset)
                    })
                }
                .pointerInput(Unit) {
                    detectTransformGestures(onGesture = { centroid, pan, gestureZoom, _ ->
                        offset = offset.calculateNewOffset(
                            centroid, pan, zoom, gestureZoom, size
                        )
                        zoom = maxOf(1f, zoom * gestureZoom)
                    })
                }
                .graphicsLayer {
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom; scaleY = zoom
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .pointerInput(Unit) {


                    awaitEachGesture {
                        //val first = awaitPointerEvent()
                        //val second = awaitPointerEvent()

                        //if(first.)

                        while (true) {
                            val event = awaitPointerEvent()
                            //Log.d(TAG, "ReadingScreen: ${event.type}")
                            //Log.d(TAG, "onCreate: fingers : ${event.changes.size}")
                            twoFingers.value = event.changes.size == 2
                            if (event.changes.any { it.isConsumed }) {
                                //Log.d(TAG, "onCreate: found it")
                                // A pointer is consumed by another gesture handler
                            } else {
                                // Handle unconsumed event
                            }
                        }
                    }

                }
            ,
            //contentPadding = PaddingValues(10.dp)
            verticalArrangement = Arrangement.SpaceEvenly,
            //horizontalAlignment = Alignment.Start
            userScrollEnabled = true,
            state = state

        ) {

            items(pageCount!! , key = { it }){

                currentPage.intValue = it
                val image = openPdfPage(it)
                if(rectMap[it] == null)  rectMap[it] = SnapshotStateList()
                if(sharedModel.drawingsLifeData.value == null) sharedModel.drawingsLifeData.value = mutableMapOf()
                if(sharedModel.drawingsLifeData.value!![it] == null) sharedModel.drawingsLifeData.value!![it] = mutableListOf()
                Log.d(TAG, "onCreate: adding canvas : $it , current map size = ${rectMap.size}")


                rectMap.forEach{(t,u) ->
                    Log.d(TAG, "ReadingScreen: $t , ${u.size}")
                }


                ImageCanvas(image = image  , modeState , pageNo , twoFingers , rectMap[it]!! , it , sharedModel.drawingsLifeData.value!![it] )

            }
        }

    }
}

private suspend fun saveDrawings(context: Context, sharedModel: SharedViewModel, state: LazyListState , currentPage : Int ) {
    val bookName = sharedModel.currentBook.value!!
    val uri = sharedModel.sharedUri.value!!
    val timeCreated = sharedModel.timeCreated.value!!
    val rectMap = sharedModel.drawingsLifeData.value!!
    val drawingList = createPersistentList(rectMap)

    context.dataStore.updateData {
        it.copy(
            bookHashMap = it.bookHashMap.mutate { bookData ->
                bookData[bookName] = PdfData(
                    name = bookName,
                    lastPage = currentPage,
                    uriString = uri.toString(),
                    timeCreated = timeCreated,
                    timeLastOpened = getCurrentTime(),
                    filePath = "",
                    drawings = drawingList
                    )
            }
        )
    }
    sharedModel.drawingsLifeData.value = null
    val appSettings : Flow<AppSettings> = context.dataStore.data
    //var uri : Uri? = null
    val pdfData : PdfData? = appSettings.first().bookHashMap[bookName]
    sharedModel.currentDrawingList.value = pdfData?.drawings
}

private fun createPersistentList(rectMap : MutableMap<Int , MutableList<RectData>>) : PersistentList<PageData>{
    val TAG = "create persistentList"
    Log.d(TAG, "createPersistentList: creating list from ${rectMap.size} ")
    val retList : MutableList<PageData> = mutableListOf()
    rectMap.forEach {( t, u )->
        Log.d(TAG, "createPersistentList: items : $t , ${u.size}")
        if(!u.isEmpty()) {
            Log.d(TAG, "createPersistentList: adding ${u.size}")
            u.toList().forEach { rect ->
                val composeRect = ComposeRect(
                    rect.offset.x,
                    rect.offset.y,
                    rect.width,
                    rect.height,
                    rect.filled,
                    rect.strokeWidth
                )
                val pageData = PageData(t, composeRect)
                retList.add(pageData)
            }
        }
    }

    Log.d(TAG, "createPersistentList: created list size = ${retList.size}")

    return retList.toPersistentList()
}

private fun PersistentList<PageData>.toMap() : MutableMap< Int , MutableList< RectData> >{
    val retMap : MutableMap<Int ,MutableList< RectData>> = mutableMapOf()
    this.forEach {
        val rect = RectData(
            Offset(it.rectF.x , it.rectF.y),
            it.rectF.width,
            it.rectF.height,
            it.rectF.filled,
            it.rectF.strokeWidth
            )
        if (retMap[it.pageNo] == null){
            retMap[it.pageNo] = mutableListOf()
        }
        retMap[it.pageNo]?.add(rect)
    }
    return retMap
}

private fun getCurrentTime(): String {
    val currentDateTime = LocalDateTime.now()

    // Format the date as a string (optional)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentDateTime.format(formatter)
}

private fun calculateDoubleTapOffset(
    zoom: Float,
    size: IntSize,
    tapOffset: Offset
): Offset {
    val newOffset = Offset(tapOffset.x, tapOffset.y)
    return Offset(
        newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        newOffset.y.coerceIn(0f, (size.height / zoom) * (zoom - 1f))
    )
}

private fun Offset.calculateNewOffset(
    centroid: Offset,
    pan: Offset,
    zoom: Float,
    gestureZoom: Float,
    size: IntSize
): Offset {
    val newScale = maxOf(1f, zoom * gestureZoom)
    val newOffset = (this + centroid / zoom) -
            (centroid / newScale + pan / zoom)
    return Offset(
        newOffset.x.coerceIn(0f, (size.width / zoom) * (zoom - 1f)),
        newOffset.y.coerceIn(0f, (size.height / zoom) * (zoom - 1f))
    )
}


