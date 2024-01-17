package com.pdf.studymarkercompsose

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.PathInfo
import com.pdf.studymarkercompsose.data.RectData
import com.pdf.studymarkercompsose.data.toDp
import kotlin.math.absoluteValue

@Composable
fun ImageCanvas(
    image: ImageBitmap,
    //id: MutableState<Int>,
    modeState: MutableState<ModeState>,
    pageNo: State<Int>,
    twoFingers: MutableState<Boolean>,
    rectMap: SnapshotStateList<RectData>,
    i: Int,
    saveData: MutableList<RectData>?,
    darkMode : Boolean = true
){

    val TAG = "ImageCanvas $i"
    //val fill : DrawStyle by remember { mutableStateOf(Stroke(15f)) }
    val fill  = remember { mutableStateOf<DrawStyle>(Stroke(15f)) }
    var filled  by rememberSaveable { mutableStateOf<Boolean>(false) }

    val pageNum = i

    val rects = rectMap
    val currentRect = remember { mutableStateOf(RectData()) }

    val rect = Rect(offset =  Offset(image.width / 3f , image.height/3f) , size = Size(300f,150f))

    var path by remember { mutableStateOf<Path?>(Path()) }
    //val path = remember { mutableStateOf<Path?>(Path()) }
    val currentPath = remember { mutableStateOf(PathInfo()) }
    val pathPointList = remember {
        mutableStateListOf<PointF>()
    }

    val pathList = remember { mutableStateListOf<Path>() }

    val colorMatrix = floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
    )
    var prevPoint = remember { mutableStateOf( PointF(0f,0f)) }

    Canvas(
        modifier = Modifier
            .size(image.width.toDp.dp, image.height.toDp.dp)
            //.background(Color.Transparent)
            .pointerInput(modeState.value, twoFingers.value , Unit) {

                Log.d(TAG, "ImageCanvas: current mode ${modeState.value}")
                if (modeState.value == ModeState.Rect && !twoFingers.value) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->

                            val width = change.position.x - currentRect.value.offset.x
                            val height = change.position.y - currentRect.value.offset.y

                            currentRect.value = RectData(currentRect.value.offset, width, height)
                            Log.d(
                                TAG,
                                "ImageCanvas: drawing rect : ${currentRect.value} , ${modeState.value}"
                            )
                        },
                        onDragStart = { offset: Offset ->
                            currentRect.value = RectData(offset, 1f, 1f)
                        },
                        onDragCancel = {},
                        onDragEnd = {
                            currentRect.let { rects.add(it.value) }

                            currentRect.value = RectData(Offset(0f, 0f), 0f, 0f)

                        }
                    )
                } else if (modeState.value == ModeState.Path && !twoFingers.value) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            /*val controlX = (change.position.x + prevPoint.value.x) / 2
                            val p1 = PointF(controlX, prevPoint.value.y)
                            val p2 = PointF(controlX, change.position.y)
                            path?.cubicTo(
                                p1.x,
                                p1.y,
                                p2.x,
                                p2.y,
                                change.position.x,
                                change.position.y
                            )
                            prevPoint.value.x = change.position.x
                            prevPoint.value.y = change.position.y*/


                            //currentPath.value.pointsList.add(PointF(change.position.x , change.position.y))
                            //currentPath.value.prevPoint = change.position
                            //path?.lineTo(change.position.x , change.position.y)
                            pathPointList.add(PointF(change.position.x, change.position.y))

                            currentPath.value = PathInfo(currentPath.value.offset , pathPointList )


                        },
                        onDragStart = { offset: Offset ->
                           //currentPath.value = PathInfo(currentPath.value.offset)
                            currentPath.value.offset = PointF(offset.x ,offset.y)

                            /*path = Path()
                            path?.moveTo(offset.x, offset.y)
                            prevPoint.value = PointF(offset.x, offset.y)*/
                        },
                        onDragCancel = {},
                        onDragEnd = {
                            //path.value.close()
                            pathList.add(generateSmoothPath(currentPath.value))
                            pathPointList.clear()
                            currentPath.value = PathInfo()
                            /*path.let { pathList.add(path!!) }
                            path = null*/
                        }
                    )
                }

            }
            .pointerInput(pageNo.value, rectMap) {
                detectTapGestures { offset ->
                    Log.d(
                        TAG,
                        "ImageCanvas: tap ${offset.x} , ${offset.y} , ${fill.value} , pageNo : ${pageNo.value}  , mode : ${modeState.value}"
                    )
                    Log.d(TAG, "ImageCanvas: tapped page : $pageNum ")

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
                            val newRect =
                                RectData(it.offset, it.width, it.height, it.filled, it.strokeWidth)
                            rects.remove(it)
                            rects.add(newRect)
                            break
                        }
                    }

                    if (rect.contains(offset)) {
                        Log.d(TAG, "ImageCanvas: contains it")
                        filled = !filled
                        fill.value = if (fill.value == Fill) Stroke(10f) else Fill
                    }

                }
            },
    ){
        if(darkMode){
            drawImage(
                image ,
                colorFilter =  ColorFilter.colorMatrix(ColorMatrix(colorMatrix)),
                blendMode =  BlendMode.Src
            )
        }else { drawImage(image) }

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
        saveData?.clear()
        rects.forEach {
            saveData?.add(it)
            drawRoundRect(
                Color.Red,
                topLeft = it.offset,
                style = if(it.filled) Fill else Stroke(it.strokeWidth),
                size = Size(it.width, it.height),
                cornerRadius = CornerRadius(8f)
            )
        }

        pathList.forEach{
            drawPath(
                color = Color.Red,
                path = it,
                style = Stroke(5.dp.toPx())
            )
        }
        drawPath(
            color = Color.Magenta,
            path = generateSmoothPath(currentPath.value),
            style = Stroke(5.dp.toPx())
        )
    }
}

private fun generateSmoothPath( pathData : PathInfo) : Path{
    val path = Path()
    path.moveTo(pathData.offset.x , pathData.offset.y)
    val prevPoint = PointF(pathData.offset)
    pathData.pointsList.forEach {
        val controlX = (it.x + prevPoint.x) / 2
        val p1 = PointF(controlX, prevPoint.y)
        val p2 = PointF(controlX, it.y)
        path.cubicTo(
            p1.x,
            p1.y,
            p2.x,
            p2.y,
            it.x,
            it.y
        )
        prevPoint.x = it.x
        prevPoint.y = it.y
    }
    return path
}