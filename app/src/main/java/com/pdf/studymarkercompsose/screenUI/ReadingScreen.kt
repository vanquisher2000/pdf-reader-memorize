package com.pdf.studymarkercompsose.screenUI

//import com.pdf.studymarkercompsose.data.RectData
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.SharedViewModel
import com.pdf.studymarkercompsose.dataStore
import com.pdf.studymarkercompsose.logicClasses.Reader
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun ReadingScreen(
    sharedModel: SharedViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    reader: Reader
) {
    val strokeWidth = remember { mutableFloatStateOf(sharedModel.strokeWidth.value?: 30f) }
    //val animateScroll = sharedModel.animateScroll.value?: true
    val TAG = remember { "ReadingScreen" }
    //val reader = sharedModel.reader.value!!

    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = false
    //val selectedButton = remember { mutableStateOf(ButtonId.None) }

    val state = rememberLazyListState()

    val context = LocalContext.current
    //val coroutineScope = rememberCoroutineScope()

    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    //var multiFloatingState by remember { mutableStateOf(MultiFloatingState.Collapsed) }

    val modeState = remember { mutableStateOf(ModeState.Idle) }
    //val currentPage = remember { mutableIntStateOf(0) }
    val twoFingers = remember {
        mutableStateOf(true)
        //false
    }
    val darkModeToggle = remember { mutableStateOf(sharedModel.darkMode.value?: false) }

    val pageNo = remember { derivedStateOf { state.firstVisibleItemIndex } }
    var pointerCounter by remember { mutableIntStateOf(0) }
    var recomposeCounter = remember { 0 }
    var oldMode = remember { ModeState.Idle }
    //val bottomBarWidth = remember { mutableStateOf(64.dp) }
    //val bottomBarAlpha = remember { mutableFloatStateOf(1f) }

    var stateSize = remember {IntSize(0,0)}

    val drawState = remember {derivedStateOf {  modeState.value != ModeState.Idle || !twoFingers.value  } }
    val configuration = LocalConfiguration.current.screenWidthDp
    val draggableState = rememberDraggableState {
        if(twoFingers.value){
            val newX = (offset.x - it).coerceIn(0f, (stateSize.width / zoom) * (zoom - 1f))
            offset = Offset(newX , offset.y)
            /*offset = offset.calculateNewOffset(
                Offset(0f,0f), Offset(newX , offset.y), zoom, 0f , stateSize
            )*/
            Log.d(TAG, "ReadingScreen: new offSet is $offset")
        }
    }

    DisposableEffect(key1 = lifecycleOwner  ) {
        val observer = LifecycleEventObserver{ source, event ->
            Log.d("TAG", "ReadingScreen: event is ${event.name}")
            if(event == Lifecycle.Event.ON_PAUSE){
                (context as? ComponentActivity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                reader.cancelLoading()
                lifecycleOwner.lifecycleScope.launch {
                    saveDrawings(
                        sharedModel = sharedModel,
                        context = context,
                        state = state,
                        //currentPage = currentPage.intValue,
                        darkModeToggle = darkModeToggle,
                        strokeWidth = strokeWidth
                        //rectMap = savedMap
                    )
                }
            }else if(event == Lifecycle.Event.ON_RESUME){
                (context as? ComponentActivity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            }
            else if(event == Lifecycle.Event.ON_START){
                //reader.resumeLoading()
            }
            else if(event == Lifecycle.Event.ON_DESTROY){
                //reader.cancelLoading()
                reader.releaseImages()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Scaffold (floatingActionButton = {
        ReadingScreenFab(
            pageNo = pageNo,
            pageCount = reader.pdfRenderer?.pageCount?: reader.initializePdf() ,
            items = reader.items,
            darkModeToggle = darkModeToggle,
            modeState = modeState,
            sharedModel = sharedModel,
            state = state,
            strokeWidth = strokeWidth,
            reader = reader
    ) }) {

        LaunchedEffect(key1 = Unit) {
            //if(animateScroll) state.animateScrollToItem((sharedModel.currentPage.value?: 0) , sharedModel.scrollYRatio.value?: 0)
            //else state.scrollToItem((sharedModel.currentPage.value?: 0) , sharedModel.scrollYRatio.value?: 0)
            state.scrollToItem((sharedModel.currentPage.value?: 0) , sharedModel.scrollYRatio.value?: 0)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                //.draggable(state = draggableState, orientation = Orientation.Horizontal,)
                .pointerInput(modeState.value) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            //pointerCounter = event.changes.size
                            twoFingers.value = event.changes.size == 2

                        }
                    }
                }
                //Log.d("TAG", "ReadingScreen: await : ${twoFingers.value} . ${modeState.value}")
                /* if (event.changes.size == 2 && modeState.value != ModeState.Idle) {
                                twoFingers.value = true
                                oldMode = modeState.value
                                modeState.value = ModeState.Idle
                                Log.d(
                                    "TAG",
                                    "ReadingScreen: two fingers await  : ${twoFingers.value} . ${modeState.value}"
                                )

                            } else if (event.changes.size == 1 && modeState.value == ModeState.Idle && oldMode != ModeState.Idle) {
                                twoFingers.value = false
                                modeState.value = oldMode
                                oldMode = ModeState.Idle
                                Log.d(
                                    "TAG",
                                    "ReadingScreen: two fingers await : ${twoFingers.value}, ${modeState.value}"
                                )

                            }*/
                //twoFingers.value = event.changes.size == 2
                //Log.d("TAG", "ReadingScreen: two fingers : ${twoFingers.value}")
                /*if (event.changes.any { it.isConsumed }) {
                                //Log.d(TAG, "onCreate: found it")
                                // A pointer is consumed by another gesture handler
                            } else {
                                // Handle unconsumed event
                            }*/
                // }
                //}

                /*.pointerInput(modeState.value) {
                    detectTapGestures(onDoubleTap = { tapOffset ->
                        zoom = if (zoom > 1f) 1f else 2f
                        offset = calculateDoubleTapOffset(zoom, size, tapOffset)
                    })
                }*/
                .pointerInput(key1 = modeState.value) {
                    /*awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            //pointerCounter = event.changes.size
                            //twoFingers.value = event.changes.size == 2
                            //Psize = event.changes.size
                        }
                    }*/

                    recomposeCounter++
                    Log.d(
                        "TAG",
                        "ReadingScreen: outside , count : $recomposeCounter ,  $(${modeState.value == ModeState.Idle} || ${twoFingers.value})"
                    )
                    if (modeState.value == ModeState.Idle) {
                        Log.d(
                            "TAG",
                            "ReadingScreen:count : $recomposeCounter inside $(${modeState.value == ModeState.Idle} || ${twoFingers.value})"
                        )
                        println("twoFinger outside : ${twoFingers.value}")
                        detectTransformGestures(
                            onGesture = { centroid, pan, gestureZoom, _ ->
                                //println("twoFinger inside $recomposeCounter : ${twoFingers.value}")
                                //Log.d("TAG", "ReadingScreen: $centroid, $pan, $gestureZoom")
                                //if (modeState.value == ModeState.Idle || twoFingers.value) {
                                stateSize = size
                                offset = offset.calculateNewOffset(
                                    centroid, pan, zoom, gestureZoom, size
                                )
                                zoom = maxOf(1f, zoom * gestureZoom)
                                //}
                            },
                            //panZoomLock = twoFingers.value
                        )
                    }
                }
                .graphicsLayer {
                    translationX = -offset.x * zoom
                    translationY = -offset.y * zoom
                    scaleX = zoom; scaleY = zoom
                    transformOrigin = TransformOrigin(0f, 0f)
                }

            ,
            verticalArrangement = Arrangement.SpaceEvenly,
            userScrollEnabled = true// { modeState.value == ModeState.Idle || twoFingers.value }()
                    ,
            state = state

        ) {
            items(
                items = reader.imageInfoList,
                key = { it.id }
                ){
                /*if(it.image == null) {
                    //it.image = reader.openPdfPage(it.pageNo)
                    reader.fetchPageImage(it.pageNo)
                }*/
                //reader.loadImages(pageN0 = pageNo.value)


                ImageCanvas(
                //CachedImageWithCanvas(
                    imageData = it.image,
                    currentPage = it.pageNo,
                    modeState = { modeState.value },
                    pageNo = { pageNo.value },
                    twoFingers = { twoFingers.value },
                    rectMap = it.rectMap,
                    pageDrawings = it.pageDrawings,
                    strokeWidth = { strokeWidth.floatValue },
                    currentColor = reader.getCurrentColor(),
                    darkModeToggle = { darkModeToggle.value },
                    reader = reader
                )
            }
        }

    }
}

private suspend fun saveDrawings(
    context: Context,
    sharedModel: SharedViewModel,
    state: LazyListState,
    darkModeToggle: MutableState<Boolean>,
    strokeWidth: MutableFloatState
) {
    val TAG = "saveDrawings"
    val bookName = sharedModel.currentBook.value!!
    val uri = sharedModel.sharedUri.value!!
    val timeCreated = sharedModel.timeCreated.value?: ""
    val mappedDrawing = sharedModel.drawingMapLifeData.value?.toPersistentMap()
    val currentColor = sharedModel.currentColor.value
    val lastUsed = getCurrentTime()
    Log.d(TAG, "saveDrawings: saving path map : ${mappedDrawing?.size}")

    context.dataStore.updateData {
        it.copy(
            bookHashMap = it.bookHashMap.mutate { bookData ->
                bookData[bookName] = PdfData(
                    name = bookName,
                    lastPage = state.firstVisibleItemIndex,
                    scrollPositionRatio = state.firstVisibleItemScrollOffset,
                    uriString = uri.toString(),
                    timeCreated = timeCreated,
                    timeLastOpened = lastUsed,
                    allDrawings = mappedDrawing,
                    lastUsedColor = currentColor!!,
                    darkMode = darkModeToggle.value,
                    strokeWidth = strokeWidth.floatValue
                    )
            }
        )
    }
    sharedModel.drawingsLifeData.value = null
    val appSettings : Flow<AppSettings> = context.dataStore.data
    //var uri : Uri? = null
    val pdfData : PdfData? = appSettings.first().bookHashMap[bookName]
    //sharedModel.currentDrawingList.value = pdfData?.drawings
    sharedModel.currentDrawings.value = pdfData?.allDrawings
    Log.d(TAG, "saveDrawings: saved path map value :${sharedModel.currentDrawings.value?.size} ")
    sharedModel.currentDrawings.value?.forEach { (t, u )->
        Log.d(TAG, "saveDrawings path map : $t , $u , ${u.pathList?.size} , ${u.rectList?.size}")
    }
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




