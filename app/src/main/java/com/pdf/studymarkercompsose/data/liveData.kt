package com.pdf.studymarkercompsose.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageData
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.SerializedColor
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

data class liveData(
    val selectedPdfUri : Uri
)

class SharedViewModel : ViewModel() {
    val sharedData = MutableLiveData<String>()

    val sharedUri = MutableLiveData<Uri>()
    val currentBook = MutableLiveData<String>()
    val currentPage = MutableLiveData<Int>()
    val sharedFilePath = MutableLiveData<String>()
    val scrollYRatio = MutableLiveData<Int>()
    val timeCreated = MutableLiveData<String>()
    val pageCount = MutableLiveData<Int>()

    val context = MutableLiveData<Context>()

    val currentDrawingList = MutableLiveData<PersistentList<PageData>>()

    val currentDrawings = MutableLiveData<PersistentMap<Int , PageDrawings>>()
    val drawingMapLifeData = MutableLiveData<MutableMap<Int , PageDrawings>>()

    val drawingsLifeData = MutableLiveData<MutableMap<Int , MutableList<ComposeRect>>>()

    val currentColor = MutableLiveData<SerializedColor>()

    val animateScroll = MutableLiveData<Boolean>()

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