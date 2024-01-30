package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.util.SizeF
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.google.android.gms.ads.MobileAds
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
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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



    @OptIn(ExperimentalLayoutApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        MobileAds.initialize(this){
            Log.d(TAG, "onCreate: current ads status : $it , ${it.adapterStatusMap}")
        }



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

        //window.setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // This flag will prevent the status bar disappearing animation from jerking the content view
        //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);



        enableEdgeToEdge()
        setContent {


            val state by viewModel.state.collectAsState()
            appSettings = application.dataStore.data.collectAsState(initial = AppSettings()).value
            openDialog = remember {
                mutableStateOf(false)
            }

            StudyMarkerCompsoseTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(WindowInsets.statusBarsIgnoringVisibility)
                    ,
                    color = MaterialTheme.colorScheme.background,

                ) {

                    navController = navigation(
                        openPdfPage = {
                                i: Int ->
                            openPdfPage(i)
                        },
                        appSettings = appSettings,
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

        if(pdfRenderer == null) initializePdf()
        val page = pdfRenderer!!.openPage(pageN0)

        val displayMetrics = getScreenWidth()

        //val relativeHeight : Float = (displayMetrics.x.toFloat() / page.width.toFloat()) * page.height



        val pageScaleFactor = if(appSettings.pageScaling == 0) (displayMetrics.x.toFloat() / page.width.toFloat()) else appSettings.pageScaling.toFloat()
        //val pageScaleFactor = (displayMetrics.x.toFloat() / page.width.toFloat())
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
            currentDrawings.value = pdfData.allDrawings
            currentColor.value = pdfData.lastUsedColor
            animateScroll.value = appSettings.first().scrollAnimation
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
            scrollYRatio.value   = 0
            timeCreated.value = currentTime
        }

        lifecycleScope.launch { updateBooksMap(newName , 0 , uri.toString() , filePath , currentTime) }
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







