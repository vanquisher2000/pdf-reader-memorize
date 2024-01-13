
package com.pdf.studymarker.data

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import kotlinx.serialization.Serializable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URI

@Serializable
data class AppSettings(
    val language : Language = Language.English,
    val knownLocation : PersistentList<Location> = persistentListOf(),
    //val bookList : MutableList<PdfData> = mutableListOf()
    @Serializable(PersistentListSerializer::class)
    val bookList : PersistentList<PdfData> = persistentListOf(),
    @Serializable(PersistentMapSerializer::class)
    val bookHashMap: PersistentMap<String , PdfData> = persistentHashMapOf(),
)

//@Serializable
enum class Language{
    English,French
}

@Serializable
data class Location(
    val lat : Double,
    val lng : Double
)

@Serializable
data class PdfData(
    val name : String,
    val lastPage : Int = 1,
    val uriString : String,
    val scrollPositionRatio : Float = 0f,
    val filePath : String,
    //@Serializable(PersistentPageDrawingsMapSerializer::class)
    //val  drawingsMap  : PersistentMap<Int , PersistentList<Page>> = persistentHashMapOf()
    @Serializable(PersistentPageDrawingsListSerializer :: class)
    val drawings : PersistentList<PageData>  = persistentListOf(),
    val timeCreated : String = "",
    val timeLastOpened : String = ""
)

@Serializable
data class PageData(
    val pageNo : Int,
    val rectF: SerializedRectF,
    val paint: SerializedPaint
)



@Serializable
data class Page(
    val rectF: SerializedRectF,
    val paint : SerializedPaint,
)

@Serializable
data class SerializedRectF(
    val top : Float,
    val bottom : Float,
    val left : Float,
    val right : Float
)

@Serializable
data class SerializedPaint(
    val strokeWidth : Float,
    val style : Int,
    val color : Int,
    val isAntiAlias : Boolean
)


/*
@Serializable
data class AppSettings(
    @Serializable(PersistentListSerializer::class)
    val favoriteApps: PersistentList<String> = persistentListOf()
)
*/

@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentListSerializer(private val dataSerializer: KSerializer<PdfData>) : KSerializer<PersistentList<PdfData>> {
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }
    override val descriptor: SerialDescriptor = PersistentListDescriptor()
    override fun serialize(encoder: Encoder, value: PersistentList<PdfData>) {
        return ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }
    override fun deserialize(decoder: Decoder): PersistentList<PdfData> {
        return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
    }
}

@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentMap::class)
class PersistentMapSerializer(private val dataSerializer: KSerializer<String> , private val valueSerializer:KSerializer<PdfData>) : KSerializer<PersistentMap<String , PdfData>> {
    private class PersistentMapDescriptor : SerialDescriptor by serialDescriptor<Map<String,Int>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }
    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<String , PdfData>) {
        return MapSerializer(dataSerializer,valueSerializer).serialize(encoder,value.toMap())
    }

    override fun deserialize(decoder: Decoder): PersistentMap<String , PdfData> {
        return MapSerializer(dataSerializer , valueSerializer).deserialize(decoder).toPersistentMap()
    }
}


@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentRectFListSerializer(private val dataSerializer: KSerializer<RectF>) : KSerializer<PersistentList<RectF>> {
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }
    override val descriptor: SerialDescriptor = PersistentListDescriptor()
    override fun serialize(encoder: Encoder, value: PersistentList<RectF>) {
        return ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }
    override fun deserialize(decoder: Decoder): PersistentList<RectF> {
        return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
    }
}


@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentMap::class)
class PersistentPageMapSerializer(private val dataSerializer: KSerializer<Int> , private val valueSerializer:KSerializer<PageData>) : KSerializer<PersistentMap<Int , PageData>> {
    private class PersistentMapDescriptor : SerialDescriptor by serialDescriptor<Map<String,PageData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }
    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<Int , PageData>) {
        return MapSerializer(dataSerializer,valueSerializer).serialize(encoder,value.toMap())
    }

    override fun deserialize(decoder: Decoder): PersistentMap<Int , PageData> {
        return MapSerializer(dataSerializer , valueSerializer).deserialize(decoder).toPersistentMap()
    }
}


@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentMap::class)
class PersistentPageDrawingsMapSerializer(private val dataSerializer: KSerializer<Int> , private val valueSerializer:KSerializer<PersistentList<Page>>) : KSerializer<PersistentMap<Int , PersistentList<Page>>> {
    private class PersistentMapDescriptor : SerialDescriptor by serialDescriptor<Map<String,PageData>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }
    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentMap<Int , PersistentList<Page>>) {
        return MapSerializer(dataSerializer,valueSerializer).serialize(encoder,value.toMap())
    }

    override fun deserialize(decoder: Decoder): PersistentMap<Int , PersistentList<Page>> {
        return MapSerializer(dataSerializer , valueSerializer).deserialize(decoder).toPersistentMap()
    }
}

@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentPageDrawingsListSerializer(private val dataSerializer: KSerializer<PageData>) : KSerializer<PersistentList<PageData>> {
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }
    override val descriptor: SerialDescriptor = PersistentListDescriptor()
    override fun serialize(encoder: Encoder, value: PersistentList<PageData>) {
        return ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }
    override fun deserialize(decoder: Decoder): PersistentList<PageData> {
        return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
    }
}


object PathSerializer : KSerializer<Path> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Path) {
        val flattenedPath = flattenPath(value)
        encoder.encodeString(flattenedPath)
    }

    override fun deserialize(decoder: Decoder): Path {
        val flattenedPath = decoder.decodeString()
        return createPathFromFlattenedString(flattenedPath)
    }

    private fun flattenPath(path: Path): String {
        val pathMeasure = PathMeasure(path, false)
        val length = pathMeasure.length
        val points = FloatArray(2)

        val flattenedPath = StringBuilder()

        var distance = 0f
        while (distance <= length) {
            pathMeasure.getPosTan(distance, points, null)
            flattenedPath.append("${points[0]},${points[1]},")
            distance += 1f // Increment manually
        }

        return flattenedPath.toString()
    }

    private fun createPathFromFlattenedString(flattenedPath: String): Path {
        val path = Path()
        val points = flattenedPath.split(",")

        for (i in 0 until points.size / 2) {
            val x = points[i * 2].toFloat()
            val y = points[i * 2 + 1].toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        return path
    }
}

