package com.pdf.studymarkercompsose.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pdf.studymarker.data.PageData
import kotlinx.collections.immutable.PersistentList

data class liveData(
    val selectedPdfUri : Uri
)

class SharedViewModel : ViewModel() {
    val sharedData = MutableLiveData<String>()

    val sharedUri = MutableLiveData<Uri>()
    val currentBook = MutableLiveData<String>()
    val currentPage = MutableLiveData<Int>()
    val sharedFilePath = MutableLiveData<String>()
    val scrollYRatio = MutableLiveData<Float>()
    val timeCreated = MutableLiveData<String>()

    val currentDrawingList = MutableLiveData<PersistentList<PageData>>()


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