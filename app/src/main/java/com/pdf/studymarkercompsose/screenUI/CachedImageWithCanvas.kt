package com.pdf.studymarkercompsose.screenUI

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.MutableLiveData
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.toColor
import com.pdf.studymarkercompsose.logicClasses.Reader
import kotlinx.collections.immutable.mutate

@Composable
fun CachedImageWithCanvas(
    image: ImageBitmap,
    modeState: MutableState<ModeState>,
    pageNo: State<Int>,
    twoFingers: MutableState<Boolean>,
    rectMap: SnapshotStateList<ComposeRect>,
    pageDrawings: PageDrawings?,
    strokeWidth: MutableFloatState,
    currentColor: MutableLiveData<SerializedColor>,
    darkModeToggle: MutableState<Boolean>,
    reader: Reader,
){

    val TAG = "ImageCanvas /*${pageNo.value}*/"
    val currentRect = remember { mutableStateOf(ComposeRect(0f,0f , color = SerializedColor(alpha = 0f))) }

    Image(
        bitmap = image,
        contentDescription = "page",
        contentScale = ContentScale.Fit,
        colorFilter = if(darkModeToggle.value) ColorFilter.colorMatrix(ColorMatrix( reader.colorMatrix)) else null,
        modifier = Modifier
            //.size(image.width.toDp.dp, image.height.toDp.dp)
            .drawWithCache {
                onDrawBehind {
                    if (currentRect.value.x != 0f) {
                        Log.d(TAG, "ImageCanvas: drawing update rect $currentRect")
                        drawRoundRect(
                            currentColor.value!!.toColor(),
                            topLeft = Offset(currentRect.value.x, currentRect.value.y),
                            style = currentRect.value.let { if (it.filled) Fill else Stroke(it.strokeWidth) },
                            size = Size(currentRect.value.width, currentRect.value.height),
                            cornerRadius = CornerRadius(8f)
                        )
                    }
                    pageDrawings?.rectList = pageDrawings?.rectList?.mutate { it.clear() }
                    rectMap.forEach {
                        pageDrawings?.rectList =
                            pageDrawings?.rectList?.mutate { list -> list.add(it) }
                        drawRoundRect(
                            color = it.color.toColor(),
                            topLeft = Offset(it.x, it.y),
                            style = if (it.filled) Fill else Stroke(it.strokeWidth),
                            size = Size(it.width, it.height),
                            cornerRadius = CornerRadius(8f)
                        )
                    }
                }
            }

    )


}
