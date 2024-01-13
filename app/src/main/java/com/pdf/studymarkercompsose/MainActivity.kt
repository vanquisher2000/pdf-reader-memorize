package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.SizeF
import android.util.TypedValue
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdf.studymarkercompsose.ui.theme.StudyMarkerCompsoseTheme
import java.io.File
import java.io.InputStream
import kotlin.math.absoluteValue


class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"


    private var pdfRenderer : PdfRenderer? = null


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageId = resources.assets.open("icons8-android-os-50.png")
        val bitmap = BitmapFactory.decodeStream(imageId)
        val currentRatio = getScreenWidth()
        val height =( currentRatio.x.toFloat() / bitmap.width ) * bitmap.height
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap , currentRatio.x ,height.toInt() , false )
        val testImage = scaledBitmap.asImageBitmap()
        val brush  = Brush.radialGradient(listOf(Color.Green, Color.Magenta))

        val intList = mutableListOf<Int>()

        initializePdf()
        val pageCount = pdfRenderer!!.pageCount

        for(i in 0 until pageCount){
            intList.add(i)
        }

       /* val rectMap : MutableMap<Int , SnapshotStateList<RectData>> = mutableMapOf()



        val items = listOf(
            MiniFabItem(
                icon = Icons.Default.Create
                ,
                label = "draw" ,
                id = ButtonId.Draw
            ),
            MiniFabItem(
                icon = Icons.Default.Delete
                ,
                label = "delete" ,
                id = ButtonId.Delete
            )
        )*/



        enableEdgeToEdge()
        setContent {

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,

                ) {

                    Navigation(intList = intList ) { i: Int ->
                        openPdfPage(i)
                    }

                // innerPadding ->
                    /*Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )*/

                  /*  val state = rememberLazyListState()


                    var offset by remember { mutableStateOf(Offset.Zero) }
                    var zoom by remember { mutableStateOf(1f) }
                    var multiFloatingState by remember {
                        mutableStateOf(MultiFloatingState.Collapsed)
                    }

                    val modeState = remember { mutableStateOf(ModeState.Idle) }
                    val currentPage = remember { mutableIntStateOf(0) }
                    val twoFingers = remember {
                        mutableStateOf(false)
                    }



                    Scaffold (floatingActionButton = {
                        MultiFab(
                            multiFloatingState = multiFloatingState ,
                            items = items ,
                            onMultiFloatingStateChange = {multiFloatingState = it},
                            modeState = modeState.value,
                            onModeStateChange = {
                                Log.d(TAG, "onCreate: changing : $modeState to $it")
                                modeState.value = it
                                Log.d(TAG, "onCreate: changed : $modeState to $it")
                            }
                            )

                    }) {

                        val pageNo = remember { derivedStateOf { state.firstVisibleItemIndex } }


                        LazyColumn(
                       modifier = Modifier
                           .fillMaxSize()
                           .clipToBounds()
                           .pointerInput(Unit) {
                               detectTapGestures(onDoubleTap = { tapOffset ->
                                   zoom = if (zoom > 1f) 1f else 2f
                                   offset = calculateDoubleTapOffset(zoom, size, tapOffset)
                               })
                           }
                           .pointerInput(Unit) {
                               detectTransformGestures(onGesture = { centroid, pan, gestureZoom, _ ->
                                   offset = offset.calculateNewOffset(
                                       centroid, pan, zoom, gestureZoom, size
                                   )
                                   zoom = maxOf(1f, zoom * gestureZoom)
                               })
                           }
                           .graphicsLayer {
                               translationX = -offset.x * zoom
                               translationY = -offset.y * zoom
                               scaleX = zoom; scaleY = zoom
                               transformOrigin = TransformOrigin(0f, 0f)
                           }
                           .pointerInput(Unit) {


                               awaitEachGesture {
                                   //val first = awaitPointerEvent()
                                   //val second = awaitPointerEvent()

                                   //if(first.)

                                   while (true) {
                                       val event = awaitPointerEvent()
                                       //Log.d(TAG, "onCreate: fingers : ${event.changes.size}")
                                       twoFingers.value = event.changes.size == 2
                                       if (event.changes.any { it.isConsumed }) {
                                           //Log.d(TAG, "onCreate: found it")
                                           // A pointer is consumed by another gesture handler
                                       } else {
                                           // Handle unconsumed event
                                       }
                                   }
                               }

                           }
                       ,
                       //contentPadding = PaddingValues(10.dp)
                       verticalArrangement = Arrangement.SpaceEvenly,
                       //horizontalAlignment = Alignment.Start
                       userScrollEnabled = true,
                       state = state

                   ) {

                       items(intList , key = { it }){
                           Log.d(TAG, "onCreate: adding canvas : $it")
                           currentPage.intValue = it
                           val image = openPdfPage(it)
                           if(rectMap[it] == null)  rectMap[it] = SnapshotStateList()

                           ImageCanvas(image = image  , modeState , pageNo , twoFingers , rectMap[it]!! , it)

                       }

                   }



                    }*/
                    //ImageCanvas(image = image)
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

    private fun initializePdf(){

        val fileName = "KAT27.pdf"
        var pfd : ParcelFileDescriptor? = null
       /* if(fileList().contains(fileName)){
            val file = getFileStreamPath(fileName)
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }else{
            val inputStream = this.assets.open(fileName)
            val file = File(this.cacheDir, fileName)
            val outputStream = file.outputStream()

            inputStream.copyTo(outputStream)

            saveFileToInternalStorage(fileName , inputStream)
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }*/
        val inputStream = this.assets.open(fileName)
        val file = File(this.cacheDir, fileName)
        val outputStream = file.outputStream()

        inputStream.copyTo(outputStream)

        if(!fileList().contains(fileName)) saveFileToInternalStorage(fileName , inputStream)

        //val outputStreamWrite = openFileOutput(fileName, Context.MODE_PRIVATE).bufferedWriter()


        pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        pdfRenderer = PdfRenderer(pfd)

    }

    private fun saveFileToInternalStorage(fileName: String, inputStream: InputStream) {
        try {
            // Open a private internal storage file for writing
            val fileOutput = openFileOutput(fileName, MODE_PRIVATE)

            // Copy the content from the input stream to the output stream
            inputStream.copyTo(fileOutput)

            // Close the streams
            fileOutput.close()

            // Optionally, log a message indicating that the file was saved successfully
            // Log.d("FileSave", "File saved successfully: $fileName")
        } catch (e: Exception) {
            // Handle the exception (e.g., log an error message)
            Log.e("MainActivity", "Failed to save file to internal storage.", e)
        }
    }

    private fun isFileExists(fileName: String): Boolean {
        val fileNames = fileList()
        return fileNames.contains(fileName)
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







