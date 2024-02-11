package com.pdf.studymarkercompsose.data

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.PersistentPathInfoListSerializer
import com.pdf.studymarker.data.SerializedColor
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import java.util.UUID


enum class ButtonId{
    Rect,
    Delete,
    Path,
    Color,
    ColorWheel,
    Marker,
    Width,
    GoToPage,
    DarkMode,
    None,
    Idle
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

@Immutable
data class ImageInfo(
    var image : ImageBitmap? = null,
    val rectMap : SnapshotStateList<ComposeRect>,
    val pageNo : Int,
    val pageDrawings : PageDrawings?,
    val id : String = UUID.randomUUID().toString()
)


enum class DrawerState{
    Open,
    Closed
}