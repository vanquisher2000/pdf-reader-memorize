package com.pdf.studymarkercompsose.data

import android.content.res.Resources
import android.graphics.PointF
import android.util.TypedValue
import androidx.compose.ui.geometry.Offset


enum class ButtonId{
    Rect,
    Delete,
    Path
}

enum class ModeState{
    Rect,
    Delete,
    Idle,
    Path
}


fun Int.toDp(): Float {
    //val dpValue = TypedValue.deriveDimension(this , )
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )
}

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

enum class MultiFloatingState {
    Expanded,
    Collapsed
}

data class RectData(
    var offset : Offset = Offset(0f,0f),
    var width : Float = 0f,
    var height : Float = 0f,
    var filled : Boolean = false,
    var strokeWidth : Float = 5f
)

data class PathInfo(
    var offset : PointF = PointF(0f,0f),
    val pointsList : MutableList<PointF> = mutableListOf(),
)