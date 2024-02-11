package com.pdf.studymarkercompsose.logicClasses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.SizeF
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.MutableLiveData
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.ComposeRect
import com.pdf.studymarker.data.PageDrawings
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.R
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ImageInfo
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.SharedViewModel
import com.pdf.studymarkercompsose.dataStore
import com.pdf.studymarkercompsose.screenUI.MiniFabItem
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class Reader(_context: Context , _sharedViewModel : SharedViewModel) {

    private val TAG = "Reader"

    private var appSettings: AppSettings? = null
    private var context = _context
    var pdfRenderer : PdfRenderer? = null
    private var sharedViewModel = _sharedViewModel
    private var uri  : Uri? = null
    var imageInfoList = persistentListOf<ImageInfo>()
    //var imageInfoList = sharedViewModel.imageInfoList.value!!
    private var currentJob : Job? = null
    private var scope = CoroutineScope(Dispatchers.Default)
    var loaded = false
    private var currentPage = 0
    var initialHeight = 600f

    var displayMetrics = Point(0,0)

    val colorMatrix = floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
    )



    init {
        MainScope().launch { fetchAppSettings() }
        displayMetrics = getScreenWidth()
    }


    val items = listOf(
        MiniFabItem(
            icon =  R.drawable.dark_mode_icon,
            label = "Dark Mode",
            id = ButtonId.DarkMode,
        ),
        MiniFabItem(
            icon = R.drawable.color_wheel_3
            ,
            label = "Color" ,
            id = ButtonId.Color
        ),

        MiniFabItem(
            icon =  R.drawable.stroke_width_icon
            ,
            label = "Marker Thickness" ,
            id = ButtonId.Width
        ),
        MiniFabItem(
            icon = R.drawable.marker_icon,
            label = "Marker" ,
            id = ButtonId.Marker,
            modeState = ModeState.Marker
        ),
        MiniFabItem(
            icon = R.drawable.eraser_tool_2_32
            ,
            label = "Erase" ,
            id = ButtonId.Delete,
            modeState = ModeState.Delete
        ),
        MiniFabItem(
            icon = R.drawable.draw_rect_icon
            ,
            label = "Rectangle" ,
            id = ButtonId.Rect,
            modeState = ModeState.Rect
        ),
        MiniFabItem(
            icon = R.drawable.pan
            ,
            label = "Rectangle" ,
            id = ButtonId.Idle,
            modeState = ModeState.Idle
        ),
        /*MiniFabItem(
            icon = R.drawable.web_page
            ,
            label = "Go To Page" ,
            id = ButtonId.GoToPage
        ),*/
    )


    private fun getScreenWidth(): Point {
        val windowManager =context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }



    fun openPdfPage(pageN0 : Int) : ImageBitmap {
        Log.d(TAG, "openPdfPage: loading page $pageN0 in UI")
        if(pdfRenderer == null) initializePdf(uri!!)
        return fetchPage(pdfRenderer = pdfRenderer!! , pageN0 = pageN0)
    }

    suspend fun asyncOpenPdfPage(pageN0 : Int) : ImageBitmap? {
        var ret : ImageBitmap? = null
        if(pdfRenderer == null) initializePdf(uri!!)
        val job = scope.launch {
            if(currentJob?.isActive == true) {
                Log.d(TAG, "asyncOpenPdfPage: returned")
                return@launch
            }
            else {
                ret = async { fetchPage(pdfRenderer = pdfRenderer!!, pageN0 = pageN0) }.await()
                imageInfoList[pageN0].image = ret
            }
        }
        Log.d(TAG, "openPdfPage: loading page $pageN0 in Default")
        if(currentJob?.isActive == true) currentJob = job
        return ret
    }

    fun fetchPageImage(pageN0: Int){
        val buffer = 10
        if(imageInfoList[pageN0].image == null) imageInfoList[pageN0].image = openPdfPage(pageN0)
        //val pair = createTempRenderer(uri!!)
        //val renderer = pair.first
        //val pfd = pair.second
       /* for(i in pageN0 + 1..pageN0 + buffer){
            imageInfoList[i]?.let {
                if(it.image == null && pageN0 < pdfRenderer!!.pageCount){
                    scope.launch {
                        //scope.async { imageInfoList[pageN0 + 1].image = asyncLoadOutOfLine(pageN0 = pageN0, _uri = uri!!) }
                        //imageInfoList[pageN0 + 1].image = scope.async { fetchPage (pageN0 = pageN0, renderer) }.await()
                        imageInfoList[i].image = scope.async { asyncLoadOutOfLine(pageN0 = pageN0, _uri = uri!!) }.await()
                    }
                }
            }
        }*/
        //pfd.close()
        //renderer.close()
        if(pageN0 + 1 < pdfRenderer!!.pageCount && imageInfoList[pageN0 + 1].image != null) scope.launch {
            imageInfoList[pageN0 + 1].image = scope.async { asyncLoadOutOfLine(pageN0 = pageN0, _uri = uri!!) }.await()
        }
    }

    private suspend fun fetchAppSettings()  {
        appSettings = context.dataStore.data.first()
    }


    fun initializePdf(_uri: Uri = sharedViewModel.sharedUri.value!!) : Int{
        if(uri != _uri) {
            //scope.cancel()
            currentJob?.cancel()
            currentJob = null
            imageInfoList = persistentListOf()
            pdfRenderer?.close()
            uri = _uri
            //val uri = sharedViewModel.sharedUri.value
            Log.d(TAG, "initializePdf: $_uri")
            val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(_uri, "r")
            pdfRenderer = PdfRenderer(pfd!!)
            sharedViewModel.loaded.value = false
        }

        if(appSettings == null) scope.launch { fetchAppSettings() }

        val savedDrawings = sharedViewModel.currentDrawings.value
        if(sharedViewModel.drawingMapLifeData.value == null) sharedViewModel.drawingMapLifeData.value = mutableMapOf()
        for(i in 0 until pdfRenderer!!.pageCount){
            if(sharedViewModel.drawingMapLifeData.value!![i] == null) sharedViewModel.drawingMapLifeData.value!![i] = PageDrawings()
            val tempImageInfo = ImageInfo(
                ///image = openPdfPage(i),
                rectMap = getRectList(pageNo = i , savedMap =  savedDrawings ),
                pageDrawings = sharedViewModel.drawingMapLifeData.value?.get(i) ?: PageDrawings(),
                pageNo = i
            )
            imageInfoList = imageInfoList.mutate {
                it.add(tempImageInfo)
            }
        }
        //if(currentJob == null) currentJob =  loadImages(uri!!)
        //loadImages_2(uri!!)

        return pdfRenderer!!.pageCount
    }

    private fun getRectList(pageNo : Int , savedMap : PersistentMap<Int, PageDrawings>?) : SnapshotStateList<ComposeRect> {
        val rectList = SnapshotStateList<ComposeRect>()
        savedMap?.get(pageNo)?.rectList?.forEach { rectList.add(it) }
        return rectList
    }

    fun getCurrentColor() : MutableLiveData<SerializedColor>{
        val currentColor = sharedViewModel.currentColor
        if(currentColor.value == null) currentColor.value = SerializedColor(1f,0f,1f)
        return currentColor
    }


    private fun calculateTime( startTime: LocalTime  , list : MutableList<Job> = mutableListOf()){
        scope.launch {
            currentJob?.join()
            list.forEach { it.join() }
            val endTime = LocalTime.now()
            loadingRenderer?.close()
            loadingPfd?.close()
            val loadingTime = endTime.toSecondOfDay() - startTime.toSecondOfDay()
            Log.d(TAG, "calculateTime: passed time : $loadingTime , $startTime , $endTime")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "finished loading in $loadingTime seconds", Toast.LENGTH_LONG)
                    .show()
                loaded = true
                sharedViewModel.loaded.value = true
            }
        }
    }
    private fun loadImages(uri: Uri) : Job {
        var startTime = LocalTime.now()
        val job =  scope.launch {
            startTime = LocalTime.now()
            initializeLoadingAssets(uri)
            imageInfoList.forEach{
                if(it.image == null) {
                    /*async {
                        //Log.d(TAG, "loadImages: getting page : ${it.pageNo}")
                        it.image = asyncOpenPdfPage(pageN0 = it.pageNo, _uri = uri)
                    }*/
                    it.image = async{ asyncOpenPdfPage(pageN0 = it.pageNo, _uri = uri) }.await()
                }
            }
        }
        calculateTime(startTime = startTime)
        return job
    }


    fun loadImages(pageN0: Int) {
        Log.d(TAG, "loadImages: called with $pageN0")
        if(pageN0 == currentPage)return
        Log.d(TAG, "loadImages: invoked with $pageN0")
        val up = pageN0 > currentPage
        currentPage = pageN0
        var startTime = LocalTime.now()
         scope.launch {
            currentJob?.join()
            startTime = LocalTime.now()
            //initializeLoadingAssets(uri)
            val job = scope.launch {
                for (i in 1..2) {
                    val page = if(up) pageN0 + 1 + i else pageN0 - 1 - i
                    if (page < pdfRenderer!!.pageCount && page >= 0 && imageInfoList[page].image == null ) {
                        imageInfoList[page].image = scope.async { loadPage(pageN0 = page , isAsync = true) }.await()
                    }
                }
            }
            /*imageInfoList.forEach{
                if(it.image == null) {
                    *//*async {
                        //Log.d(TAG, "loadImages: getting page : ${it.pageNo}")
                        it.image = asyncOpenPdfPage(pageN0 = it.pageNo, _uri = uri)
                    }*//*
                    it.image = async{ asyncOpenPdfPage(pageN0 = it.pageNo, _uri = uri) }.await()
                }
            }*/
            currentJob = job
        }
        //calculateTime(startTime = startTime)
        //return job
    }



    var loadingPfd : ParcelFileDescriptor? = null
    var loadingRenderer : PdfRenderer? = null
    private fun asyncOpenPdfPage(pageN0 : Int , _uri: Uri) : ImageBitmap {
        Log.d(TAG, "asyncOpenPdfPage: loading image :$pageN0")
        if(loadingRenderer == null) initializeLoadingAssets(_uri)
        val ret = fetchPage(pdfRenderer =  loadingRenderer!! , pageN0 = pageN0 )
        Log.d(TAG, "asyncOpenPdfPage: image :$pageN0 loaded")
        return ret

    }

    private fun createTempRenderer(uri: Uri) : Pair< PdfRenderer,ParcelFileDescriptor>{
        val loadingPfd = context.contentResolver.openFileDescriptor(uri , "r")
        val loadingRenderer = PdfRenderer(loadingPfd!!)
        //loadingPfd.close()
        return Pair(loadingRenderer , loadingPfd)
    }
    private fun initializeLoadingAssets(uri: Uri){
        loadingPfd = context.contentResolver.openFileDescriptor(uri , "r")
        loadingRenderer = PdfRenderer(loadingPfd!!)
    }

    private fun asyncLoadOutOfLine(pageN0 : Int , _uri: Uri) : ImageBitmap {
        Log.d(TAG, "asyncOpenPdfPage: loading image :$pageN0")
        val pfd : ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(_uri , "r")
        val pdfRenderer = PdfRenderer(pfd!!)
        /*val page = pdfRenderer!!.openPage(pageN0)
        val displayMetrics = getScreenWidth()
        //val relativeHeight : Float = (displayMetrics.x.toFloat() / page.width.toFloat()) * page.height
        val pageScaleFactor = if(appSettings!!.pageScaling == 0) (displayMetrics.x.toFloat() / page.width.toFloat()) else appSettings!!.pageScaling.toFloat()
        //val pageScaleFactor = (displayMetrics.x.toFloat() / page.width.toFloat())
        val bitmapSize  = SizeF(page.width * pageScaleFactor , page.height * pageScaleFactor)
        val bitmap = Bitmap.createBitmap(bitmapSize.width.toInt(),bitmapSize.height.toInt(), Bitmap.Config.ARGB_8888)
        page.render(bitmap,null,null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()*/
        val ret = fetchPage(pageN0 = pageN0 , pdfRenderer)
        pfd!!.close()
        pdfRenderer!!.close()
        Log.d(TAG, "asyncOpenPdfPage: image :$pageN0 loaded")
        return ret
    }

    private fun fetchPage(pageN0: Int , pdfRenderer: PdfRenderer) : ImageBitmap{
        val page = pdfRenderer.openPage(pageN0)
        if(displayMetrics == Point(0,0)) displayMetrics = getScreenWidth()
        initialHeight = page.height / page.width.toFloat() * displayMetrics.y
       /* val pageScaleFactor =
            if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
                if(appSettings!!.pageScaling == 0) (displayMetrics.y.toFloat() / page.width.toFloat()) else appSettings!!.pageScaling.toFloat()
                //1f
            }else{
                if(appSettings!!.pageScaling == 0) (displayMetrics.x.toFloat() / page.width.toFloat()) else appSettings!!.pageScaling.toFloat()
            }*/
        val pageScaleFactor = if(appSettings!!.pageScaling == 0) (displayMetrics.x.toFloat() / page.width.toFloat()) else appSettings!!.pageScaling.toFloat()

        //val relativeHeight : Float = (displayMetrics.x.toFloat() / page.width.toFloat()) * page.height
        //val pageScaleFactor =  0.5f //(displayMetrics.x.toFloat() / page.width.toFloat())
        val bitmapSize  = SizeF(page.width * pageScaleFactor , page.height * pageScaleFactor)
        val bitmap = Bitmap.createBitmap(bitmapSize.width.toInt(),bitmapSize.height.toInt(), Bitmap.Config.ARGB_8888)
        page.render(bitmap,null,null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        val imageBitmap = bitmap.asImageBitmap()
        imageBitmap.prepareToDraw()
        imageInfoList[pageN0].image = imageBitmap
        return imageBitmap
    }
    
    fun loadPage(
        uri : Uri = sharedViewModel.sharedUri.value!!,
        isAsync : Boolean = false,
        pageN0: Int
    ) : ImageBitmap {
        if(pdfRenderer == null) initializePdf()
        var pdfRenderer = pdfRenderer
        var pair : Pair<PdfRenderer , ParcelFileDescriptor>? = null
        if(isAsync){
            pair = createTempRenderer(uri)
            pdfRenderer = pair.first
        }
        val ret = fetchPage(pageN0, pdfRenderer!!)
        if(isAsync){
            pair?.second?.close()
            pair?.first?.close()
        }
        return ret
    }
    

    fun cancelLoading(){
        currentJob?.cancel()
    }

    fun resumeLoading(){
        if(currentJob != null) {
            if (!currentJob!!.isActive)
                CoroutineScope(Dispatchers.Default).launch {
                    currentJob?.start()
                }
        }
    }

    fun releaseImages(){
        imageInfoList.forEach { it.image?.asAndroidBitmap()?.recycle() }
    }

}