package com.pdf.studymarkercompsose

//import com.pdf.studymarkercompsose.data.RectData
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.MutableLiveData
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.PathInfo
import com.pdf.studymarkercompsose.data.PathPoint
import com.pdf.studymarkercompsose.data.toColor
import com.pdf.studymarkercompsose.data.toDp
import kotlinx.collections.immutable.mutate
import kotlin.math.absoluteValue

@Composable
fun ImageCanvas(
    image: ImageBitmap,
    //id: MutableState<Int>,
    modeState: MutableState<ModeState>,
    pageNo: State<Int>,
    twoFingers: MutableState<Boolean>,
    rectMap: SnapshotStateList<ComposeRect>,
    //currentPage: Int,
    //saveData: MutableList<ComposeRect>?,
    darkMode: Boolean = true,
    pageDrawings: PageDrawings?,
    //pathMap: SnapshotStateList<Path>,
    strokeWidth: MutableFloatState,
    currentColor: MutableLiveData<SerializedColor>,
    darkModeToggle: MutableState<Boolean>,
    //imageLoader: suspend (Int) -> ImageBitmap
){

    val TAG = "ImageCanvas /*${pageNo.value}*/"
    //val fill : DrawStyle by remember { mutableStateOf(Stroke(15f)) }
    val fill  = remember { mutableStateOf<DrawStyle>(Stroke(15f)) }
    //var filled  by rememberSaveable { mutableStateOf<Boolean>(false) }

    //var image by remember { mutableStateOf<ImageBitmap?>(null) }
    /*LaunchedEffect(key1 = Unit) {
        image = imageLoader(LocalContext.current).newBuilder().build()
    }*/

    //val pageNum = currentPage

    val rects = rectMap
    val currentRect = remember { mutableStateOf(ComposeRect(0f,0f , color = SerializedColor(alpha = 0f))) }
    val rect = Rect(offset =  Offset(image!!.width / 3f , image!!.height/3f) , size = Size(300f,150f))

    val currentPath = remember { mutableStateOf(PathInfo()) }
    val pathInfoList = mutableListOf<PathInfo>()
    val pathPointList = remember { mutableStateListOf<PathPoint>() }
    //val pathList = remember { mutableStateListOf<Path>() }

    //val pathList = pathMap

   /* val lines = remember {
        mutableStateListOf<Line>()
    }*/


    val colorMatrix = floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
    )

   /* AsyncImage(
        model = image,
        contentDescription = "",
        modifier = Modifier
            .size(image.width.toDp.dp, image.height.toDp.dp)


    )*/

   /*Image(
       bitmap = image,
       contentDescription = "",
       modifier = Modifier
           .size(image.width.toDp.dp, image.height.toDp.dp)
   )*/


    Canvas(
        modifier = Modifier
            .size(image!!.width.toDp.dp, image!!.height.toDp.dp)
            .background(
                color = if(!darkModeToggle.value) Color.White else Color.Transparent
            )
            .pointerInput(modeState.value, twoFingers.value, Unit) {

                Log.d(TAG, "ImageCanvas: current mode ${modeState.value}")
                if ((modeState.value == ModeState.Rect || modeState.value == ModeState.Marker) && !twoFingers.value) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->

                            val width = change.position.x - currentRect.value.x
                            val height =
                                if (modeState.value == ModeState.Rect) change.position.y - currentRect.value.y
                                else (strokeWidth.floatValue)
                            Log.d(
                                TAG,
                                "ImageCanvas: marker :  $width , $height , ${strokeWidth.floatValue} "
                            )
                            currentRect.value = ComposeRect(
                                currentRect.value.x,
                                currentRect.value.y,
                                width,
                                height,
                                color = currentColor.value!!
                            )
                            Log.d(
                                TAG,
                                "ImageCanvas: drawing rect : ${currentRect.value} , ${modeState.value}"
                            )
                        },
                        onDragStart = { offset: Offset ->
                            currentRect.value = ComposeRect(
                                offset.x,
                                y = if (modeState.value == ModeState.Rect) offset.y else offset.y - (strokeWidth.floatValue / 2),
                                //y = offset.y,
                                color = currentColor.value!!
                            )
                        },
                        onDragCancel = {},
                        onDragEnd = {
                            currentRect.let { rects.add(it.value) }

                            currentRect.value =
                                ComposeRect(0f, 0f, color = SerializedColor(alpha = 0f))

                        }
                    )
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
            .pointerInput(pageNo.value, rectMap, modeState.value) {
                detectTapGestures { offset ->
                    //Log.d(TAG, "ImageCanvas: tap ${offset.x} , ${offset.y} , ${fill.value} , pageNo : ${pageNo.value}  , mode : ${modeState.value}")
                    //Log.d(TAG, "ImageCanvas: tapped page : $currentPage ")

                    //Log.d(TAG, "ImageCanvas: curretn tap is $offset")

                    /*if(modeState.value == ModeState.Delete) {
                        pathInfoList.forEachIndexed { index, pathInfo ->
                            val point = PathPoint(offset.x,offset.y)
                            if(pathInfo.pointsList.contains(point)){
                                pathInfoList.removeAt(index)
                                pathList.removeAt(index)
                                //break
                            }
                        }
                        for (i in 0 until pathList.size) {
                            if (pathList[i]
                                    .getBounds()
                                    .contains(offset)
                            ) {
                                pathList.removeAt(i)
                                pageDrawings?.pathList =
                                    pageDrawings?.pathList?.mutate { it.removeAt(i) }
                                break
                            }
                        }
                    }*/

                    for (i in 0 until rects.size) {
                        val it = rects[i]
                        val x = if (it.width < 0) it.x + it.width else it.x
                        val y = if (it.height < 0) it.y + it.height else it.y
                        val tempRect = Rect(
                            Offset(x, y),
                            Size(it.width.absoluteValue, it.height.absoluteValue)
                        )
                        Log.d(
                            TAG,
                            "ImageCanvas: creating rect with ${tempRect.topLeft} , ${tempRect.width} , ${tempRect.height} ,  from ${it.x} ${it.width} , ${it.height}"
                        )
                        if (tempRect.contains(offset)) {
                            Log.d(TAG, "ImageCanvas: tap hit!!!")
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
                            rects.remove(it)
                            if (modeState.value != ModeState.Delete) rects.add(newRect)
                            break
                        }
                    }

                    if (rect.contains(offset)) {
                        Log.d(TAG, "ImageCanvas: contains it")
                        //filled = !filled
                        fill.value = if (fill.value == Fill) Stroke(10f) else Fill
                    }

                }
            },
    ){
        if(darkModeToggle.value){
            drawImage(
                image!! ,
                colorFilter =  ColorFilter.colorMatrix(ColorMatrix(colorMatrix)),
                blendMode =  BlendMode.Src
            )
        }else { drawImage(image!!) }

        //drawRoundRect( Color.Red , topLeft = rect.topLeft , style = fill.value  , size = rect.size , cornerRadius = CornerRadius(8f))

        if(currentRect.value.x != 0f){
            Log.d(TAG, "ImageCanvas: drawing update rect $currentRect")
            drawRoundRect(
                currentColor.value!!.toColor(),
                topLeft = Offset(currentRect.value.x,currentRect.value.y) ,
                style = currentRect.value.let {   if(it.filled) Fill else Stroke(it.strokeWidth) },
                size = Size(currentRect.value.width, currentRect.value.height),
                cornerRadius = CornerRadius(8f)
            )

        }
        //saveData?.clear()
        pageDrawings?.rectList = pageDrawings?.rectList?.mutate { it.clear() }
        //pageDrawings?.rectList?.mutate { it.clear() }
        rects.forEach {
            //saveData?.add(it)
            pageDrawings?.rectList = pageDrawings?.rectList?.mutate {list -> list.add(it) }

            //pageDrawings?.rectList?.mutate { it.add() }
            drawRoundRect(
                color = it.color.toColor(),
                topLeft = Offset(it.x,it.y),
                style = if(it.filled) Fill else Stroke(it.strokeWidth),
                size = Size(it.width, it.height),
                cornerRadius = CornerRadius(8f)
            )
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