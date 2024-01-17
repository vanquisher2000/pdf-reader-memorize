package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.util.SizeF
import android.util.TypedValue
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.AppSettingsSerializer
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarkercompsose.data.SharedViewModel
import com.pdf.studymarkercompsose.ui.theme.StudyMarkerCompsoseTheme
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

val Context.dataStore by dataStore("app-settings.json" , AppSettingsSerializer)

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"


    private var pdfRenderer : PdfRenderer? = null
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var composeView : ComposeView
    private val viewModel : StartingScreenViewModel by viewModels()
    private lateinit var appSettings: AppSettings

    private lateinit var navController : NavHostController
    private lateinit var openDialog: MutableState<Boolean>



    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)





        var similarFileCount = 0

        filePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->

                            Log.d(TAG, "onCreate: uri before : $uri")

                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION  //or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                            application.contentResolver.takePersistableUriPermission(uri , takeFlags)

                            Log.d(TAG, "onCreate: after uri : $uri")

                            val filePath = ""

                            Log.d(TAG, "onViewCreated: path 2 is $filePath")

                            sharedViewModel.sharedFilePath.value = filePath

                            sharedViewModel.sharedUri.value = uri
                            sharedViewModel.pageCount.value =  initializePdf()

                            var filename = getFileName(applicationContext , uri)?: "name not found"


                        lifecycleScope.launch {
                                val appSettings : Flow<AppSettings> = applicationContext.dataStore.data
                                appSettings.first().bookHashMap.forEach { (k, v) ->
                                    if(k.contains(filename)){
                                        similarFileCount++

                                    }
                                }
                                if(similarFileCount != 0){
                                    val date = getCurrentTime()
                                    var date_2 = appSettings.first().bookHashMap[filename]?.timeLastOpened
                                    var tempKey = filename
                                    var compareValue = date_2?.let { subtractDates(it, date) }
                                    appSettings.first().bookHashMap.forEach { (k, v) ->
                                        if(k.contains(filename)  ) {
                                            val tempCompareValue = subtractDates(v.timeLastOpened, date)
                                            Log.d(TAG, "onViewCreated: compare value :$compareValue  $date_2 , $date")
                                            Log.d(TAG, "onViewCreated: temp compare value :$tempCompareValue  ${v.timeLastOpened} , $date")
                                            if ( compareValue != null && compareValue!! > tempCompareValue) {
                                                date_2 = v.timeLastOpened
                                                Log.d(TAG, "onViewCreated: last opened : $date_2")
                                                compareValue = tempCompareValue
                                                tempKey = k
                                            }
                                        }
                                    }

                                    filename = tempKey
                                    Log.d(TAG, "onViewCreated: last last opened : $date_2 , $filename")

                                    sharedViewModel.currentBook.value = filename



                                    /*makeNewBookDialog(
                                        { startNewSession(filename, filePath!!, uri, similarFileCount) },
                                        {onCardClicked(filename)}
                                    )*/
                                    openDialog.value = true
                                }else{
                                    sharedViewModel.currentBook.value = filename

                                    lifecycleScope.launch { updateBooksMap(filename , 1 , uri.toString() , filePath!! , getCurrentTime()) }

                                    //val navi = view.findNavController()
                                    // TODO :  navi.navigate(R.id.action_startingScreenFragment_to_readingscreenFragment)
                                    navController.navigate(Screen.ReadingScreen.route)
                                }
                            }
                    }
                }
            }


        enableEdgeToEdge()
        setContent {

            val state by viewModel.state.collectAsState()
            appSettings = application.dataStore.data.collectAsState(initial = AppSettings()).value
            openDialog = remember {
                mutableStateOf(false)
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,

                ) {

                    navController = navigation(
                        openPdfPage = {
                                i: Int ->
                            openPdfPage(i)
                        },
                        bookMap = appSettings.bookHashMap,
                        onSelectClick = {openFilePicker()},
                        state = state,
                        onCardClick = { onCardClicked(it) },
                        onSwipeLeft = { onSwipeLeft(it) },
                        sharedViewModel = sharedViewModel,
                        onResumeReading = { MainScope().launch {   getBookData(sharedViewModel.currentBook.value?: "") } },
                        onConfirm = { startNewSession(sharedViewModel.currentBook.value!!, "", sharedViewModel.sharedUri.value!!, similarFileCount) },
                        onDismiss = { onCardClicked(sharedViewModel.currentBook.value!!) },
                        openDialog = openDialog
                    )
                }
            }
        }
    }

    private fun getScreenWidth(): Point {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getSize(size)
        return size
    }



    private fun openPdfPage(pageN0 : Int) : ImageBitmap{

        val page = pdfRenderer!!.openPage(pageN0)

        val displayMetrics = getScreenWidth()

        //val relativeHeight : Float = (displayMetrics.x.toFloat() / page.width.toFloat()) * page.height

        val pageScaleFactor = (displayMetrics.x.toFloat() / page.width.toFloat())

        val bitmapSize  = SizeF(page.width * pageScaleFactor , page.height * pageScaleFactor)

        val bitmap = Bitmap.createBitmap(bitmapSize.width.toInt(),bitmapSize.height.toInt(), Bitmap.Config.ARGB_8888)

        page.render(bitmap,null,null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        return bitmap.asImageBitmap()

        //return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

    }

    private fun initializePdf() : Int{

        pdfRenderer?.close()
        var pfd : ParcelFileDescriptor? = null
        val uri = sharedViewModel.sharedUri.value
        Log.d(TAG, "initializePdf: $uri")
        pfd = application.contentResolver.openFileDescriptor(uri!! , "r")
        pdfRenderer = PdfRenderer(pfd!!)
        return pdfRenderer!!.pageCount
    }


    private fun openFilePicker() {
        Log.d(TAG, "openFilePicker: button clicked")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf" // Set the MIME type to restrict selection to PDF files
        }

        filePickerLauncher.launch(intent)
    }


    private fun getFileName(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)


            cursor = context.contentResolver.query(uri, projection, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val fileNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                Log.d(TAG, "getFileName: name is ${cursor.getString(fileNameIndex)}")
                return cursor.getString(fileNameIndex)
            }
        } catch (e: Exception) {
            Log.e("GetFileName", "Error getting file name from URI", e)
        } finally {
            cursor?.close()
        }

        return null
    }



    private fun onCardClicked (fileName: String){
        Log.d(TAG, "testFun: testFun sucess : $fileName")

        applicationContext.dataStore.data.map{
            Log.d(TAG, "onCardClicked: scroll is ${it.bookHashMap[fileName]?.scrollPositionRatio}")
        }
        val job = lifecycleScope.launch {
            getBookData(fileName)
            sharedViewModel.pageCount.value = initializePdf()
            Log.d(TAG, "onCardClicked: got data : ${sharedViewModel.currentBook.value}")
        }
        lifecycleScope.launch {
            job.join()
            //TODO : findNavController().navigate(R.id.action_startingScreenFragment_to_readingscreenFragment)
            navController.navigate(Screen.ReadingScreen.route)
        }

    }

    private fun onSwipeLeft( bookName : String){
        Log.d(TAG, "onSwipeLeft: removing : $bookName")
        lifecycleScope.launch {
            val context : Context = baseContext
            context.dataStore.updateData {
                Log.d(TAG, "onSwipeLeft: size before deleting ${it.bookHashMap.size}")
                it.copy(
                    bookHashMap = it.bookHashMap.mutate { bookMap -> bookMap.remove(bookName) }
                )
            }
            val appSettings : Flow<AppSettings> = context.dataStore.data
            Log.d(TAG, "onSwipeLeft: size after deleting ${appSettings.first().bookHashMap.size}")

        }
        //appSettings.bookHashMap.mutate { it.remove(bookName) }
    }



    private suspend fun getBookData(fileName: String){
        Log.d(TAG, "getBookData: in")
        val appSettings : Flow<AppSettings> = applicationContext.dataStore.data
        //var uri : Uri? = null
        val pdfData : PdfData? = appSettings.first().bookHashMap[fileName]
        val uri = Uri.parse(pdfData!!.uriString)
        sharedViewModel.apply {
            sharedUri.value = uri
            currentBook.value = fileName
            currentPage.value = pdfData.lastPage
            sharedFilePath.value = pdfData.filePath
            currentDrawingList.value = pdfData.drawings
            scrollYRatio.value   = pdfData.scrollPositionRatio
            timeCreated.value = pdfData.timeCreated
            //pageCount.value = pdfRenderer?.pageCount
            Log.d(TAG, "getBookData: size :${currentDrawingList.value?.size}")
            Log.d(TAG, "onCardClicked: got data 6 : ${sharedViewModel.currentBook.value} , uri ${sharedUri.value} , uriString : ${pdfData.uriString}")
            Log.d(TAG, "getBookData: got path : ${pdfData.filePath}")
        }

        Log.d(TAG, "getBookData: out")
    }


    private fun makeNewBookDialog(pos :() -> Unit , neg : ()-> Unit){
        val dialogBuilder = MaterialAlertDialogBuilder(applicationContext , R.style.ThemeOverlay_App_MaterialAlertDialog)
        dialogBuilder.apply {
            this.setTitle("Wait a minute")
            this.setMessage("it appears there is another session of the same book you are trying to open \n would you like to make a new session or continue your" +
                    "previous session ?")
            this.setPositiveButton("new session") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                pos()
            }
            this.setNegativeButton("continue" ){ dialogInterface : DialogInterface, i : Int ->
                dialogInterface.cancel()
                neg()
            }
            val dialog = this.create()
            dialog.show()
        }
    }

    private fun startNewSession(filename: String ,filePath : String , uri: Uri , fileCount : Int ){
        val newName = "$filename #$fileCount"
        val currentTime = getCurrentTime()
        sharedViewModel.apply {
            sharedUri.value = uri
            currentBook.value = newName
            currentPage.value = 0
            sharedFilePath.value = filePath
            currentDrawingList.value = null
            scrollYRatio.value   = 0f
            timeCreated.value = currentTime
        }

        lifecycleScope.launch { updateBooksMap(newName , 0 , uri.toString() , filePath , currentTime) }

        //val navi = view?.findNavController()
        // TODO : navi?.navigate(R.id.action_startingScreenFragment_to_readingscreenFragment)
    }

    private suspend fun updateBooksMap(fileName : String, pageNo: Int,uriString : String , path: String , timeCreatedOn : String ){
        applicationContext.dataStore.updateData {
            it.copy( bookHashMap = it.bookHashMap.mutate { bookMap ->
                /*if(bookMap[fileName] != null){
                    bookMap[fileName]?.lastPage  = pageNo
                }else{
                    bookMap[fileName] = PdfData(fileName , pageNo , uriString)
                }*/
                Log.d(TAG, "updateBooksMap: updating : path $path")
                bookMap[fileName] = PdfData(fileName , pageNo , uriString , filePath = path , timeCreated = timeCreatedOn)
            } )
        }
    }


    private fun getCurrentTime(): String {
        val currentDateTime = LocalDateTime.now()

        // Format the date as a string (optional)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    fun compareDates(dateTimeString1: String, dateTimeString2: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        try {
            val date1 = LocalDateTime.parse(dateTimeString1, formatter)
            val date2 = LocalDateTime.parse(dateTimeString2, formatter)

            // Compare the dates
            return date1.compareTo(date2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Handle parsing error or other issues
        return 0
    }

    fun subtractDates(dateTimeString1: String, dateTimeString2: String): Duration? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        try {
            val date1 = LocalDateTime.parse(dateTimeString1, formatter)
            val date2 = LocalDateTime.parse(dateTimeString2, formatter)
            val diff = Duration.between(date1 , date2)

            // Compare the dates
            return diff
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Handle parsing error or other issues
        return null
    }



}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StudyMarkerCompsoseTheme {
        Greeting("Android")
    }
}







