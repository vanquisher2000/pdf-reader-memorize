package com.pdf.studymarkercompsose

//import com.pdf.studymarkercompsose.data.RectData
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageData
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ImageInfo
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.MultiFloatingState
import com.pdf.studymarkercompsose.data.SharedViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun ReadingScreen(
    sharedModel: SharedViewModel,
    openPdfPage:  (Int) -> ImageBitmap,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onResumeReading : () -> Unit
) {

    val TAG = "ReadingScreen"


    val rectMap : MutableMap<Int , SnapshotStateList<ComposeRect>> = mutableMapOf()
    val pathMap : MutableMap<Int , SnapshotStateList<Path>> = mutableMapOf()

    val savedDrawings = sharedModel.currentDrawings.value
    if(sharedModel.drawingMapLifeData.value == null) sharedModel.drawingMapLifeData.value = mutableMapOf()
    //val drawingsMap = sharedModel.drawingMapLifeData.value
    val strokeWidth = remember { mutableFloatStateOf(sharedModel.strokeWidth.value?: 25f) }
    val animateScroll = sharedModel.animateScroll.value?: true

    val selectedButton = remember { mutableStateOf(ButtonId.None) }


    savedDrawings?.forEach{(t,u) ->
        if(pathMap[t] == null) pathMap[t] = SnapshotStateList()
        //if(drawingsMap!![t] == null) drawingsMap[t] = PageDrawings()
        savedDrawings[t]?.pathList?.forEach {
            Log.d(TAG, "ReadingScreen: retrieving saved path $t $it")
            pathMap[t]?.add(generateSmoothPath(it , false))
            //drawingsMap[t]?.pathList = drawingsMap[t]?.pathList?.mutate {list ->   list.add(it) }
        }
        if(rectMap[t] == null) rectMap[t] = SnapshotStateList()
        savedDrawings[t]?.rectList?.forEach { rectMap[t]?.add(it) }
    }



    //Log.d(TAG, "ReadingScreen: saved map loaded : ${savedMap?.size} , original size = ${sharedModel.currentDrawingList.value!!.size}")

    val pageCount = sharedModel.pageCount.value

    Log.d(TAG, "ReadingScreen: $pageCount")


    val items = listOf(
        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.dark_mode_icon),
            label = "Dark Mode",
            id = ButtonId.DarkMode
        ),
        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.color_wheel_3)
            ,
            label = "Color" ,
            id = ButtonId.Color
        ),

        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.stroke_width_icon)
            ,
            label = "Marker Thickness" ,
            id = ButtonId.Width
        ),
        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.marker_icon),
            label = "Marker" ,
            id = ButtonId.Marker
        ),
        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.eraser_tool_2_32)
            ,
            label = "Erase" ,
            id = ButtonId.Delete
        ),
        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.draw_rect_icon)
            ,
            label = "Rectangle" ,
            id = ButtonId.Rect
        ),
        MiniFabItem(
            icon = ImageVector.vectorResource(id = R.drawable.web_page)
            ,
            label = "Go To Page" ,
            id = ButtonId.GoToPage
        ),
    )

    val state = rememberLazyListState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentColor = sharedModel.currentColor
    if(currentColor.value == null) currentColor.value = SerializedColor()


     //val intList : MutableList<Int> = mutableListOf()
    var imageInfoList = persistentListOf<ImageInfo>()
    if(sharedModel.drawingMapLifeData.value == null) sharedModel.drawingMapLifeData.value = mutableMapOf()

     for(i in 0 until pageCount!!){
         if(sharedModel.drawingMapLifeData.value!![i] == null) sharedModel.drawingMapLifeData.value!![i] = PageDrawings()
         val tempImageInfo = ImageInfo(
             ///image = openPdfPage(i),
             rectMap = SnapshotStateList(),
             pageDrawings = sharedModel.drawingMapLifeData.value!![i],
             pageNo = i
         )
         imageInfoList = imageInfoList.mutate {
             it.add(tempImageInfo)
         }
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
    val darkModeToggle = remember { mutableStateOf(sharedModel.darkMode.value?: false) }


    DisposableEffect(key1 = lifecycleOwner  ) {
        val observer = LifecycleEventObserver{ source, event ->
            Log.d(TAG, "ReadingScreen: event is ${event.name}")
            if(event == Lifecycle.Event.ON_DESTROY){
            }else if(event == Lifecycle.Event.ON_PAUSE){
                lifecycleOwner.lifecycleScope.launch {
                    saveDrawings(
                        sharedModel = sharedModel,
                        context = context,
                        currentPage = currentPage.intValue,
                        state = state,
                        darkModeToggle = darkModeToggle,
                        strokeWidth = strokeWidth
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

    val pageNo = remember { derivedStateOf { state.firstVisibleItemIndex } }
    val bottomBarWidth = remember { mutableStateOf(64.dp) }
    val bottomBarAlpha = remember { mutableFloatStateOf(0f) }




    Scaffold (floatingActionButton = {
        Column(
            modifier = Modifier
                .width(
                    animateDpAsState(
                        targetValue = bottomBarWidth.value,
                        tween(250),
                        label = "bottom_slider_animation"
                    ).value
                )
                .padding(start = 8.dp)

        ) {

            MultiFab(
                currentColor = currentColor,
                multiFloatingState = multiFloatingState,
                bottomBarWidth = bottomBarWidth,
                bottomBarAlpha = bottomBarAlpha,
                items = items,
                onMultiFloatingStateChange = { multiFloatingState = it },
                modeState = modeState.value,
                onModeStateChange = { modeState.value = it },
                strokeWidth = strokeWidth,
                selectedButton = selectedButton,
                modifier = Modifier
                    .fillMaxWidth(),
                gotoFunction = {
                    val selectedPage = it.coerceIn(0 , pageCount)
                    coroutineScope.launch {
                        if(animateScroll) state.animateScrollToItem(selectedPage, 0)
                        else state.scrollToItem(selectedPage, 0)
                    }
                },
                darkModeToggle = darkModeToggle
            )

            Slider(
                value = pageNo.value.toFloat(),
                onValueChange = {
                    coroutineScope.launch {
                        //state.animateScrollToItem((it.toInt()), 0)
                        if(animateScroll) state.animateScrollToItem(it.toInt(), 0)
                        else state.scrollToItem(it.toInt(), 0)
                    }
                },
                valueRange = 0f..pageCount!!.toFloat(),
                steps = pageCount,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 16.dp)
                    .alpha(
                        animateFloatAsState(
                            targetValue = bottomBarAlpha.floatValue,
                            tween(250),
                            label = "bottom_bar_alpha_animation"
                        ).value
                    )
                ,
                enabled = (multiFloatingState == MultiFloatingState.Expanded)
            )
        }


    }) {


        LaunchedEffect(key1 = Unit) {
            if(animateScroll) state.animateScrollToItem((sharedModel.currentPage.value?: 0) , sharedModel.scrollYRatio.value?: 0)
            else state.scrollToItem((sharedModel.currentPage.value?: 0) , sharedModel.scrollYRatio.value?: 0)
        }

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
                    if (modeState.value == ModeState.Idle || twoFingers.value) {
                        detectTransformGestures(onGesture = { centroid, pan, gestureZoom, _ ->
                            offset = offset.calculateNewOffset(
                                centroid, pan, zoom, gestureZoom, size
                            )
                            zoom = maxOf(1f, zoom * gestureZoom)
                        })
                    }
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
            verticalArrangement = Arrangement.SpaceEvenly,
            userScrollEnabled = true,
            state = state

        ) {

            //items(pageCount!! , key = { it }){
            items(
                items =  imageInfoList ,
                key = { it.id }
                ){

                /*currentPage.intValue = it
                val image = openPdfPage(it)
                if(rectMap[it] == null)  rectMap[it] = SnapshotStateList()
                if(sharedModel.drawingMapLifeData.value == null) sharedModel.drawingMapLifeData.value = mutableMapOf()
                if(sharedModel.drawingMapLifeData.value!![it] == null) sharedModel.drawingMapLifeData.value!![it] = PageDrawings()
                //if(pathMap[it] == null) pathMap[it] = SnapshotStateList()*/


                /*ImageCanvas(
                    image = image,
                    modeState =  modeState,
                    pageNo =  pageNo,
                    twoFingers = twoFingers,
                    rectMap = rectMap[it]!!,
                    currentPage = it,
                    darkMode = isSystemInDarkTheme(),
                    pageDrawings = sharedModel.drawingMapLifeData.value!![it],
                    //pathMap =  pathMap[it]!!,
                    strokeWidth = strokeWidth,
                    currentColor = currentColor,
                    darkModeToggle = darkModeToggle
                )*/

               /* var image by remember { mutableStateOf<ImageBitmap?>(null) }
                coroutineScope.launch {
                    //val job = coroutineScope.async { image = openPdfPage(it.pageNo) }
                    //job.await()
                    withContext(Dispatchers.Main){
                        image = openPdfPage(it.pageNo)
                    }
                }*/

                //val image = openPdfPage(it.pageNo)

                    if(it.image == null)it.image = openPdfPage(it.pageNo)
                    ImageCanvas(
                        //imageLoader = openPdfPage,
                        image = it.image?: openPdfPage(it.pageNo),
                        modeState =  modeState,
                        pageNo =  pageNo,
                        twoFingers = twoFingers,
                        rectMap = it.rectMap,
                        //currentPage = it,
                        darkMode = isSystemInDarkTheme(),
                        pageDrawings = it.pageDrawings,
                        //pathMap =  pathMap[it]!!,
                        strokeWidth = strokeWidth,
                        currentColor = currentColor,
                        darkModeToggle = darkModeToggle
                    )


            }
        }

    }
}

private suspend fun saveDrawings(
    context: Context,
    sharedModel: SharedViewModel,
    state: LazyListState,
    currentPage: Int,
    darkModeToggle: MutableState<Boolean>,
    strokeWidth: MutableFloatState
) {
    val TAG = "saveDrawings"
    val bookName = sharedModel.currentBook.value!!
    val uri = sharedModel.sharedUri.value!!
    val timeCreated = sharedModel.timeCreated.value?: ""
    //val rectMap = sharedModel.drawingsLifeData.value!!
    //val drawingList = createPersistentList(rectMap)
    /*sharedModel.drawingMapLifeData.value?.forEach { (t, u )->
        Log.d(TAG, "before saveDrawings path map : $t , $u , ${u.pathList?.size} , ${u.rectList?.size}")
    }*/
    val mappedDrawing = sharedModel.drawingMapLifeData.value?.toPersistentMap()
    val currentColor = sharedModel.currentColor.value
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
                    timeLastOpened = getCurrentTime(),
                    //filePath = "",
                    //drawings = drawingList,
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

private fun createPersistentList(rectMap : MutableMap<Int , MutableList<ComposeRect>>) : PersistentList<PageData>{
    val TAG = "create persistentList"
    Log.d(TAG, "createPersistentList: creating list from ${rectMap.size} ")
    val retList : MutableList<PageData> = mutableListOf()
    rectMap.forEach {( t, u )->
        Log.d(TAG, "createPersistentList: items : $t , ${u.size}")
        if(!u.isEmpty()) {
            Log.d(TAG, "createPersistentList: adding ${u.size}")
            u.toList().forEach { rect ->
                val composeRect = ComposeRect(
                    rect.x,
                    rect.y,
                    rect.width,
                    rect.height,
                    rect.filled,
                    rect.strokeWidth,
                    rect.color
                )
                val pageData = PageData(t, composeRect)
                retList.add(pageData)
            }
        }
    }

    Log.d(TAG, "createPersistentList: created list size = ${retList.size}")

    return retList.toPersistentList()
}

private fun PersistentList<PageData>.toMap() : MutableMap< Int , MutableList<ComposeRect> >{
    val retMap : MutableMap<Int ,MutableList< ComposeRect>> = mutableMapOf()
    this.forEach {
        val rect = ComposeRect(
            it.rectF.x ,
            it.rectF.y,
            it.rectF.width,
            it.rectF.height,
            it.rectF.filled,
            it.rectF.strokeWidth,
            it.rectF.color
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




