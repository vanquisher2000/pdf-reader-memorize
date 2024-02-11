package com.pdf.studymarkercompsose.screenUI

//import com.pdf.studymarkercompsose.data.RectData
import android.graphics.PointF
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.PathInfo
import com.pdf.studymarkercompsose.data.toColor
import com.pdf.studymarkercompsose.data.toDp
import com.pdf.studymarkercompsose.logicClasses.Reader
import kotlinx.collections.immutable.mutate
import kotlin.math.absoluteValue

@Composable
fun ImageCanvas(
    imageData: ImageBitmap?,
    modeState: () -> ModeState,
    pageNo: () -> Int,
    twoFingers: () -> Boolean,
    rectMap: SnapshotStateList<ComposeRect>,
    pageDrawings: PageDrawings?,
    strokeWidth: () -> Float,
    currentColor: MutableLiveData<SerializedColor>,
    darkModeToggle: () -> Boolean,
    reader: Reader,
    currentPage: Int,
){

    //val TAG = "ImageCanvas /*${pageNo()}*/"

    val currentRect = remember { mutableStateOf(ComposeRect(0f,0f , color = SerializedColor(alpha = 0f))) }

    var  progress by remember { mutableFloatStateOf(0f) }


    //val imageData = remember { mutableStateOf<ImageBitmap?>(null) }

    val image = imageData?: pageLoader(
        reader = reader,
        pageNo = currentPage,
        updateProgress = { progress = it },
        updateCallCounter = {
            Log.d("TAG", "ImageCanvas: called ${pageNo()} loadPage")
        }
    ).value

    if(image == null){
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(reader.initialHeight.dp)
                .padding(60.dp)
        ) {
            CircularProgressIndicator(
                progress = animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(1000),
                    label = ""
                ).value ,
                //color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp),
            )
            /*CircularProgressIndicator(
                progress = progress.value
            )*/
        }
    }

    image?.let {

       /* var height =
            (image.width.toFloat() / image.height.toFloat()).toFloat() * configuration.screenHeightDp
        val width = configuration.screenWidthDp.toFloat()
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = image.height * (width / image.width)
        }

        Log.d(TAG, "ImageCanvas: ${image.height.dp} , ${image.width.dp} , ${height.dp} , ${width.dp}")*/


        Canvas(
            modifier = Modifier
                .size(image.width.toDp.dp, image.height.toDp.dp)
                //.size(width.dp, height.dp)
                .background(color = if (!darkModeToggle()) Color.White else Color.Transparent)
                .pointerInput(modeState() , twoFingers(),Unit) {
                    /*awaitEachGesture {
                        //awaitFirstDown()
                        while(true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            if((modeState() == ModeState.Rect || modeState() == ModeState.Marker) && !twoFingers()) {
                                event.changes.forEach { it.consume() }
                                Log.d("", "ImageCanvas: event consumed")

                            }else{
                                Log.d("", "ImageCanvas: event NOT consumed")

                            }
                        }
                    }
*/                }
                .pointerInput(modeState(), twoFingers() , Unit) {
                    //Log.d(TAG, "ImageCanvas: current mode ${modeState.value}")
                    if ((modeState() == ModeState.Rect || modeState() == ModeState.Marker) && !twoFingers()) {
                        detectDragGestures(onDrag = { change, dragAmount ->
                            val width = change.position.x - currentRect.value.x
                            val height =
                                if (modeState() == ModeState.Rect) change.position.y - currentRect.value.y
                                else (strokeWidth())

                            val y = if (modeState() == ModeState.Rect) currentRect.value.y
                            else change.position.y - (strokeWidth() / 2)

                            currentRect.value = ComposeRect(
                                currentRect.value.x,
                                y,
                                width,
                                height,
                                color = currentColor.value!!
                            )
                        }, onDragStart = { offset: Offset ->
                            currentRect.value = ComposeRect(
                                offset.x,
                                y = if (modeState() == ModeState.Rect) offset.y else offset.y - (strokeWidth() / 2),
                                //y = offset.y,
                                color = currentColor.value!!
                            )
                        }, onDragCancel = {}, onDragEnd = {
                            currentRect.let { rectMap.add(it.value) }
                            currentRect.value =
                                ComposeRect(0f, 0f, color = SerializedColor(alpha = 0f))
                        })
                    }



                    /*else if (modeState.value == ModeState.Path && !twoFingers.value) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            val rectification = 100
                            repeat(rectification) { it ->
                                val x = change.position.x - (dragAmount.x / rectification) * it
                                val y = change.position.y - (dragAmount.y / rectification) * it
                                pathPointList.add(PathPoint(x, y))
                            }
                            pathPointList.add(PathPoint(change.position.x, change.position.y))
                            currentPath.value =
                                PathInfo(
                                    currentPath.value.x,
                                    currentPath.value.y,
                                    pathPointList.toPersistentList()
                                )

//                            val line = Line(
//                                start = change.position - dragAmount,
//                                end = change.position
//                            )

                            //lines.add(line)


                        },
                        onDragStart = { offset: Offset ->
                            currentPath.value.x = offset.x
                            currentPath.value.y = offset.y

                        },
                        onDragCancel = {},
                        onDragEnd = {
                            pageDrawings?.pathList?.mutate { list ->
                                //Log.d(TAG, "ImageCanvas: path map list before adding $currentPage :  ${list.size}")
                                //list.add(currentPath.value)
                                list += currentPath.value
                                //Log.d(TAG, "ImageCanvas: path map list after adding $currentPage :  ${list.size}")
                            }
                            pageDrawings?.pathList =
                                pageDrawings?.pathList?.mutate { it += currentPath.value }

                            pathInfoList.add(currentPath.value)

                            //pathList.add(generateSmoothPath(currentPath.value))
                            pathPointList.clear()
                            currentPath.value = PathInfo()
                        }
                    )
                }*/

                }
                .pointerInput(pageNo(), rectMap, modeState(), twoFingers()) {
                    if (!twoFingers()) {
                        detectTapGestures { offset ->
                            for (i in 0 until rectMap.size) {
                                val it = rectMap[i]
                                val x = if (it.width < 0) it.x + it.width else it.x
                                val y = if (it.height < 0) it.y + it.height else it.y
                                val tempRect = Rect(
                                    Offset(x, y),
                                    Size(it.width.absoluteValue, it.height.absoluteValue)
                                )

                                if (tempRect.contains(offset)) {
                                    it.filled = !it.filled
                                    val newRect = ComposeRect(
                                        it.x,
                                        it.y,
                                        it.width,
                                        it.height,
                                        it.filled,
                                        it.strokeWidth,
                                        it.color
                                    )
                                    rectMap.remove(it)
                                    if (modeState() != ModeState.Delete) rectMap.add(newRect)
                                    break
                                }
                            }
                        }
                    }
                },
        ) {
            drawImage(
                image = image!!,
                //srcOffset = IntOffset(),
                //dstOffset = IntOffset(),
                //srcSize = IntSize(),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                filterQuality = FilterQuality.High,
                colorFilter = if (darkModeToggle()) ColorFilter.colorMatrix(ColorMatrix(reader.colorMatrix)) else null,
                blendMode = if (darkModeToggle()) BlendMode.Src else DefaultBlendMode
            )

            if (currentRect.value.x != 0f) {
                drawRoundRect(
                    currentColor.value!!.toColor(),
                    topLeft = Offset(currentRect.value.x, currentRect.value.y),
                    style = currentRect.value.let { if (it.filled) Fill else Stroke(it.strokeWidth) },
                    size = Size(currentRect.value.width, currentRect.value.height),
                    cornerRadius = CornerRadius(16f)
                )
            }
            pageDrawings?.rectList = pageDrawings?.rectList?.mutate { it.clear() }
            rectMap.forEach {
                pageDrawings?.rectList = pageDrawings?.rectList?.mutate { list -> list.add(it) }
                drawRoundRect(
                    color = it.color.toColor(),
                    topLeft = Offset(it.x, it.y),
                    style = if (it.filled) Fill else Stroke(it.strokeWidth),
                    size = Size(it.width, it.height),
                    cornerRadius = CornerRadius(16f)
                )
            }
        }
    }

}

fun generateSmoothPath( pathData : PathInfo , thisFunction : Boolean = true) : Path{
    val TAG = "generateSmoothPath"
    val path = Path()
    path.moveTo(pathData.x , pathData.y)
    val prevPoint = PointF(pathData.x , pathData.y)
    val prevPoint_2 = PointF(pathData.x , pathData.y)

    pathData.pointsList.forEach {
        //Log.d(TAG, "generateSmoothPath: path points ${it.x} , ${it.y}  $it , $thisFunction")
        val controlX = (it.x + prevPoint.x + prevPoint_2.x ) / 3
        //val controlX = (it.x + prevPoint.x  ) / 2
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
        //path.quadraticBezierTo(prevPoint.x,prevPoint.y ,it.x,it.y)
        //path.rewind()
        /*path.relativeCubicTo(
            p1.x,
            p1.y,
            p2.x,
            p2.y,
            it.x,
            it.y
        )*/
        //path.lineTo(it.x,it.y)
        prevPoint_2.x = prevPoint.x
        prevPoint_2.y = prevPoint.y
        prevPoint.x = it.x
        prevPoint.y = it.y

    }
    //path.addOutline(Outline.Generic(path))
    //path.addOutline(Outline.Rounded(roundRect))
    return path
}