package com.pdf.studymarkercompsose.data

import android.content.res.Resources
import android.graphics.PointF
import android.util.TypedValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdf.studymarker.data.PersistentPathInfoListSerializer
import com.pdf.studymarker.data.SerializedColor
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable


enum class ButtonId{
    Rect,
    Delete,
    Path,
    Color,
    ColorWheel,
    Marker,
    Width,
    GoToPage,
    DarkMode
}

enum class ModeState{
    Rect,
    Delete,
    Idle,
    Path,
    Marker
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

/*data class RectData(
    var offset : Offset = Offset(0f,0f),
    var width : Float = 0f,
    var height : Float = 0f,
    var filled : Boolean = false,
    var strokeWidth : Float = 5f
)*/

@Serializable
data class PathInfo(
    //var offset : PointF = PointF(0f,0f),
    var x : Float = 0f,
    var y : Float = 0f,
    @Serializable(PersistentPathInfoListSerializer :: class)
    val pointsList : PersistentList<PathPoint> = persistentListOf(),
)
@Serializable
data class PathPoint(
    var x : Float,
    var y : Float
)

data class Line(
    val start: Offset,
    val end: Offset,
    val strokeWidth: Dp = 10.dp
)

fun SerializedColor.toColor() : Color {
    return Color(this.red , this.green , this.blue,this.alpha)
}

fun Color.toSerializedColor() : SerializedColor{
    return SerializedColor(this.red  ,this .green , this.blue , this.alpha)
}