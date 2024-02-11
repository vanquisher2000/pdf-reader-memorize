package com.pdf.studymarkercompsose.data

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageData
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.logicClasses.Reader
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap


class SharedViewModel : ViewModel() {
    val sharedData = MutableLiveData<String>()

    val sharedUri = MutableLiveData<Uri>()
    val currentBook = MutableLiveData<String>()
    val currentPage = MutableLiveData<Int>()
    val sharedFilePath = MutableLiveData<String>()
    val scrollYRatio = MutableLiveData<Int>()
    val timeCreated = MutableLiveData<String>()
    val lastOpenedBookName = MutableLiveData<String>()

    val loaded = MutableLiveData<Boolean>()

    val currentBookData = MutableLiveData<PdfData>()

    val currentDrawingList = MutableLiveData<PersistentList<PageData>>()

    val currentDrawings = MutableLiveData<PersistentMap<Int , PageDrawings>>()
    val drawingMapLifeData = MutableLiveData<MutableMap<Int , PageDrawings>>()

    val drawingsLifeData = MutableLiveData<MutableMap<Int , MutableList<ComposeRect>>>()

    val currentColor = MutableLiveData<SerializedColor>()

    val animateScroll = MutableLiveData<Boolean>()

    val strokeWidth = MutableLiveData<Float>()

    val darkMode = MutableLiveData<Boolean>()

    val reader = MutableLiveData<Reader>()

    var imageInfoList = MutableLiveData<PersistentList<ImageInfo>>()
    val test: MutableState<Int>
        @Composable
        get() = remember{ mutableIntStateOf(0) }


    /*val selectedPdfUri: MutableLiveData<Uri>
        get() = selectedPdfUri

    fun setUri(value: Uri) {
        selectedPdfUri.value = value
    }*/

    private val _yourData = MutableLiveData<Uri>()
    val yourData: LiveData<Uri> get() = _yourData

    fun setYourData(data: Uri) {
        _yourData.value = data
    }
}