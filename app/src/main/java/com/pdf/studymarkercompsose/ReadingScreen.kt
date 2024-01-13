package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.MultiFloatingState
import com.pdf.studymarkercompsose.data.RectData
import com.pdf.studymarkercompsose.data.toDp
import kotlin.math.absoluteValue

private const val TAG = "ReadingScreen"


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ReadingScreen(intList : MutableList<Int> , openPdfPage : (Int) -> ImageBitmap){

    val rectMap : MutableMap<Int , SnapshotStateList<RectData>> = mutableMapOf()



    val items = listOf(
        MiniFabItem(
            icon = Icons.Default.Create
            ,
            label = "draw" ,
            id = ButtonId.Draw
        ),
        MiniFabItem(
            icon = Icons.Default.Delete
            ,
            label = "delete" ,
            id = ButtonId.Delete
        )
    )

    val state = rememberLazyListState()


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



    Scaffold (floatingActionButton = {
        MultiFab(
            multiFloatingState = multiFloatingState ,
            items = items ,
            onMultiFloatingStateChange = {multiFloatingState = it},
            modeState = modeState.value,
            onModeStateChange = {
                Log.d(TAG, "onCreate: changing : $modeState to $it")
                modeState.value = it
                Log.d(TAG, "onCreate: changed : $modeState to $it")
            }
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

            items(intList , key = { it }){
                Log.d(TAG, "onCreate: adding canvas : $it")
                currentPage.intValue = it
                val image = openPdfPage(it)
                if(rectMap[it] == null)  rectMap[it] = SnapshotStateList()

                ImageCanvas(image = image  , modeState , pageNo , twoFingers , rectMap[it]!! , it)

            }
        }

    }
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

@Composable
fun ImageCanvas(
    image: ImageBitmap,
    //id: MutableState<Int>,
    modeState: MutableState<ModeState>,
    pageNo: State<Int>,
    twoFingers: MutableState<Boolean>,
    rectMap: SnapshotStateList<RectData>,
    i: Int
){
    //val fill : DrawStyle by remember { mutableStateOf(Stroke(15f)) }
    val fill  = remember { mutableStateOf<DrawStyle>(Stroke(15f)) }
    var filled  by rememberSaveable { mutableStateOf<Boolean>(false) }

    val pageNum = i

    //val rects = remember { mutableStateListOf<RectData>() }
    val rects = rectMap
    val currentRect = remember { mutableStateOf(RectData()) }
    /*rectMap?.forEach { rects.add(it)
        Log.d(TAG, "ImageCanvas: filling rects with $it , current size : ${rects.size}")
    }*/
    //else{ rectMap[pageNo.value] = mutableListOf<RectData>() }

    //var width by remember { mutableFloatStateOf(0f) }
    //val height = remember { mutableFloatStateOf(0f) }
    //val lines = remember { mutableStateListOf<Line>() }


    val rect = Rect(offset =  Offset(image.width / 3f , image.height/3f) , size = Size(300f,150f))

    Canvas(
        modifier = Modifier
            .size(image.width.toDp.dp, image.height.toDp.dp)
            .pointerInput(modeState.value, twoFingers.value) {

                //while (modeState == ModeState.Draw) {
                /* Log.d(TAG, "ImageCanvas: waiting")
                awaitEachGesture {
                    val event = awaitPointerEvent()
                    Log.d(TAG, "ImageCanvas: dragging : $id , ${event.changes.last().position}")
                    //event.changes.last().position
                    //event.changes.forEach { it.consume() }


                }
                awaitPointerEventScope {
                    val eventOnInitialPass = awaitPointerEvent(PointerEventPass.Initial)
                    val eventOnMainPass = awaitPointerEvent(PointerEventPass.Main) // default
                    val eventOnFinalPass = awaitPointerEvent(PointerEventPass.Final)

                    // eventOnInitialPass.changes.forEach{it.consume()}

                }*/
                // }


                /* detectDragGestures { change, dragAmount ->
                    change.consume()
                    //change.anyChangeConsumed()
                    Log.d(
                        TAG,
                        "ImageCanvas: drag  ${change.position} $dragAmount ${change.previousPosition}"
                    )

                    val rect = RectData(
                        change.position - dragAmount,
                        Size(dragAmount.x, dragAmount.y)
                    )
                    rects.add(rect)
                    Log.d(TAG, "ImageCanvas: rects size : ${rects.size}")
                }*/

                //var rect: RectData? = null
                Log.d(TAG, "ImageCanvas: current mode ${modeState.value}")
                if (modeState.value == ModeState.Draw && !twoFingers.value) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->

                            val width = change.position.x - currentRect.value.offset.x
                            val height = change.position.y - currentRect.value.offset.y

                            //currentRect.value.width = width
                            //currentRect.value.height = height.floatValue
                            currentRect.value = RectData(currentRect.value.offset, width, height)
                            Log.d(
                                TAG,
                                "ImageCanvas: drawing rect : ${currentRect.value} , ${modeState.value}"
                            )
                        },
                        onDragStart = { offset: Offset ->
                            currentRect.value = RectData(offset, 1f, 1f)
                        },
                        onDragCancel = {

                            Log.d(TAG, "ImageCanvas: drag cancelled")

                        },
                        onDragEnd = {
                            Log.d(TAG, "ImageCanvas: drag ended")
                            currentRect.let { rects.add(it.value) }

                            currentRect.value = RectData(Offset(0f, 0f), 0f, 0f)

                        }
                    )
                }

            }
            .pointerInput(pageNo.value) {
                detectTapGestures { offset ->

                    // Handle the click event
                    //onClick()
                    //fill.value = if (fill.value == Fill) Stroke(10f) else Fill
                    Log.d(
                        TAG,
                        "ImageCanvas: tap ${offset.x} , ${offset.y} , ${fill.value} , pageNo : ${pageNo.value}  , mode : ${modeState.value}"
                    )
                    Log.d(TAG, "ImageCanvas: tapped page : $pageNum ")

                    /* rects.forEach {
                        val tempRect = Rect(it.offset, Size(it.width,it.height))
                        if (tempRect.contains(offset)){
                            it.filled = !it.filled
                            //filled = !filled
                            val newRect = RectData(it.offset , it.width, it.height , it.filled , it.strokeWidth)
                            rects.remove(it)
                            rects.add(newRect)
                        }

                    }*/

                    Log.d(TAG, "ImageCanvas: curretn tap is $offset")

                    for (i in 0 until rects.size) {
                        val it = rects[i]
                        val x = if (it.width < 0) it.offset.x + it.width else it.offset.x
                        val y = if (it.height < 0) it.offset.y + it.height else it.offset.y
                        val tempRect = Rect(
                            Offset(x, y),
                            Size(it.width.absoluteValue, it.height.absoluteValue)
                        )
                        Log.d(
                            TAG,
                            "ImageCanvas: creating rect with ${tempRect.topLeft} , ${tempRect.width} , ${tempRect.height} ,  from ${it.offset} ${it.width} , ${it.height}"
                        )
                        if (tempRect.contains(offset)) {
                            Log.d(TAG, "ImageCanvas: tap hit!!!")
                            it.filled = !it.filled
                            //filled = !filled
                            val newRect =
                                RectData(it.offset, it.width, it.height, it.filled, it.strokeWidth)
                            //rectMap!!.remove(it)
                            //rectMap!!.add(it)
                            //rectMap!![i].filled = it.filled
                            rects.remove(it)
                            rects.add(newRect)
                            break
                        }
                    }

                    if (rect.contains(offset)) {
                        Log.d(TAG, "ImageCanvas: contains it")
                        filled = !filled
                        //fill.value = if (filled) Fill else Stroke(5f)
                        fill.value = if (fill.value == Fill) Stroke(10f) else Fill
                    }

                }
            },
        /*onDraw = {
            //drawRect(Color.Blue)
            drawImage(image)
            drawRoundRect( Color.Red , topLeft = rect.topLeft , style = fill  , size = rect.size)
            rects.forEach {
                drawRoundRect( Color.Red , topLeft = it.offset , style = fill  , size = it.size)
            }

        }*/
    ){
        drawImage(image , /*colorFilter = */)
        drawRoundRect( Color.Red , topLeft = rect.topLeft , style = fill.value  , size = rect.size , cornerRadius = CornerRadius(8f))

        if(currentRect.value.offset.x != 0f){
            Log.d(TAG, "ImageCanvas: drawing update rect $currentRect")
            drawRoundRect(
                Color.Red,
                topLeft = currentRect.value.offset,
                style = currentRect.value.let {   if(it.filled) Fill else Stroke(it.strokeWidth) },
                size = Size(currentRect.value.width, currentRect.value.height),
                cornerRadius = CornerRadius(8f)
            )

        }
        //rectMap?.clear()
        rects.forEach {
            //rectMap?.add(it)
            drawRoundRect(
                Color.Red,
                topLeft = it.offset,
                style = if(it.filled) Fill else Stroke(it.strokeWidth),
                size = Size(it.width, it.height),
                cornerRadius = CornerRadius(8f)
            )
        }
    }
    /*Text(
        text = id.toString(),
        Modifier.size(0.dp)

    )*/
}



@Composable
fun MultiFab(
    multiFloatingState: MultiFloatingState,
    items : List<MiniFabItem>,
    onMultiFloatingStateChange: (MultiFloatingState) -> Unit,
    modeState: ModeState,
    onModeStateChange : (ModeState)-> Unit
) {

    val transition = updateTransition(targetState = multiFloatingState , label = "transition")
    val rotate by transition.animateFloat(label = "rotate") {
        if(it == MultiFloatingState.Expanded) 315f else 0f
    }

    val fabScale by transition.animateFloat(label = "fabScale" ) {
        if(it == MultiFloatingState.Expanded) 1f else 0f
    }

    val alpha by transition.animateFloat(label = "alpha" , transitionSpec = { tween(durationMillis = 50) }) {
        if(it == MultiFloatingState.Expanded) 36f else 0f
    }

    val textShadow by transition.animateDp(label = "textShadow" , transitionSpec = { tween(durationMillis = 50) }) {
        if(it == MultiFloatingState.Expanded) 2.dp else 0.dp
    }

    val modeTransient = updateTransition(targetState = modeState , label = "modeTransition")



    Column(
        horizontalAlignment = Alignment.End
    ) {

        if(transition.currentState == MultiFloatingState.Expanded){
            items.forEach{
                MiniFab(item = it  , alpha = alpha , fabScale = fabScale , textShadow = textShadow , onMiniFabClick = { miniFabItem ->
                    when(miniFabItem.id){
                        ButtonId.Draw ->{

                            onModeStateChange(
                                if(modeTransient.currentState != ModeState.Draw) ModeState.Draw
                                else ModeState.Idle
                            )
                        }
                        ButtonId.Delete -> {
                            onModeStateChange(
                                if(modeTransient.currentState != ModeState.Delete) ModeState.Delete
                                else ModeState.Idle
                            )

                        }
                    }
                })
                Spacer(modifier = Modifier.size(8.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                onMultiFloatingStateChange(
                    if (transition.currentState == MultiFloatingState.Expanded) MultiFloatingState.Collapsed
                    else MultiFloatingState.Expanded
                )
            },
            modifier = Modifier.rotate(rotate)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "tools menu"
            )
        }
    }

}

class MiniFabItem(
    val icon : ImageVector,
    val label : String,
    val id : ButtonId
)

@Composable
fun MiniFab(
    item: MiniFabItem,
    onMiniFabClick: (MiniFabItem) -> Unit,
    alpha : Float,
    textShadow : Dp,
    fabScale : Float,
    showLabel : Boolean = true
) {

    val color = MaterialTheme.colorScheme.secondary
    val shadow = Color.Black.copy(.5f)
    var fillColor by remember {
        mutableStateOf(Color.White)
    }
    /*   Canvas(
           modifier = Modifier
               .size(32.dp)
               .clickable(
                   //interactionSource = ,
                   onClick = {
                       onMiniFabClick.invoke(item)
                   },

                   )

       ) {

           drawCircle(
               color = color ,
               radius = 36f
           )
           *//*drawImage(
            image = item.icon,
            topLeft = Offset(center.x - (item.icon.width) /2 , center.y - (item.icon.height) / 2 )
        )*//*
    }*/

    Row {
        if(showLabel) {
            Text(
                text = item.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(
                        animateFloatAsState(
                            targetValue = alpha,
                            animationSpec = tween(50)
                        ).value
                    )
                    .shadow(textShadow)
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
        }

        FloatingActionButton(
            onClick =
            {
                onMiniFabClick.invoke(item)
                fillColor = if (fillColor == Color.White) Color.Black else Color.White
            },
            containerColor = color,
            contentColor = fillColor,
            modifier = Modifier
                //.border(width = 1.dp, color = shadow)
                .scale(fabScale)
                .alpha(alpha)

        ) {
            Icon(imageVector = item.icon, contentDescription = "draw")
        }
    }

}
