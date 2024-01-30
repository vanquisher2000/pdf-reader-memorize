package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.animation.AnimationUtils.lerp
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.PdfData
import com.pdf.studymarkercompsose.data.toDp
import com.pdf.studymarkercompsose.ui.theme.Pink40
import com.pdf.studymarkercompsose.ui.theme.Pink80
import com.pdf.studymarkercompsose.ui.theme.Purple40
import com.pdf.studymarkercompsose.ui.theme.Purple80
import com.pdf.studymarkercompsose.ui.theme.PurpleGrey40
import com.pdf.studymarkercompsose.ui.theme.PurpleGrey80
import kotlinx.coroutines.launch

private const val TAG = "StartingScreen"

private val DarkColorScheme = darkColorScheme( // …1
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)


private val LightColorScheme = lightColorScheme( // …1
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("RestrictedApi")
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun StartingScreen(
    state: StartingScreenState,
    onSelectClick: () -> Unit,
    //bookList: List<PdfData>
    bookMap: Map<String, PdfData>,
    //fragment: startingScreenFragment,
    //sharedViewModel: SharedViewModel,
    onCardClick: (String) -> Unit,
    onSwipeLeft: (String) -> Unit,
    dynamicColor: Boolean = true,
    //darkTheme: Boolean = isSystemInDarkTheme(),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    openDialog: MutableState<Boolean>,
    appSettings: AppSettings
    //fileName: String
) {

    //val theme = dynamicLightColorScheme(LocalContext.current)

    val coroutineScope = rememberCoroutineScope()

    /*val theme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { // …2
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
*/
    //var drawerOpened by remember { mutableStateOf(false) }
    var drawerEnumState by remember { mutableStateOf(DrawerState.Closed) }


    val drawerWidth = (LocalConfiguration.current.screenWidthDp).toDp() * 0.64f
    val translationX = remember { Animatable(0f) }

    translationX.updateBounds(0f,drawerWidth)
    val decay = rememberSplineBasedDecay<Float>()

    val draggableState = rememberDraggableState(onDelta = {dragAmount ->
        coroutineScope.launch {
            translationX.snapTo(translationX.value + dragAmount)
        }
    })

    val anchors = DraggableAnchors{
        DrawerState.Open at drawerWidth
        DrawerState.Closed at 0f
    }

    val drawerState = remember {
        AnchoredDraggableState(
            initialValue = DrawerState.Closed,
            anchors = anchors,
            positionalThreshold = {totalDistance: Float -> totalDistance * 0.5f },
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = 100f
            ),
            velocityThreshold = {  50.dp.value },
        )
    }






    SettingsDrawer(appSettings = appSettings)
   /* Surface(
        modifier = Modifier

            .graphicsLayer {
                this.translationX =
                        //if (drawerOpened) drawerWidth.toPx() else 0f
                        //translationX.value
                    drawerState.requireOffset()
                val scale = lerp(1f, 0.8f, drawerState.requireOffset() / drawerWidth)
                this.scaleX =
                    scale
                //if (drawerOpened) 0.8f else  1f
                this.scaleY =
                    scale
                // if (drawerOpened) 0.8f else 1f
                //val corners = if (drawerOpened) 320.dp else 0.dp
                //this.shape = RoundedCornerShape(32.dp)
            }
            .background(color = Color.Blue,
                //shape = RoundedCornerShape(32.dp)
            )
            .anchoredDraggable(drawerState, orientation = Orientation.Horizontal)
            *//*.draggable(
                draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = { velocity: Float ->
                    val decayX = decay.calculateTargetValue(
                        translationX.value,
                        velocity
                    )
                    coroutineScope.launch {
                        val targetX =
                            if (decayX > drawerWidth * 0.5) drawerWidth
                            else
                                0f

                        val canReachTargetWithDecay =
                            (decayX > targetX || targetX == drawerWidth)
                                    || (decayX < targetX && targetX == 0f)

                        if (canReachTargetWithDecay) translationX.animateDecay(velocity, decay)
                        else translationX.animateTo(targetX, initialVelocity = velocity)
                    }
                }
            )*//*
    ) {*/

        Column(
            modifier = Modifier
                .fillMaxSize()
                //.padding(8.dp)
                .graphicsLayer {
                    this.translationX =
                            //if (drawerOpened) drawerWidth.toPx() else 0f
                            //translationX.value
                        drawerState.requireOffset()
                    val scale = lerp(1f, 0.8f, drawerState.requireOffset() / drawerWidth)
                    this.scaleX =
                        scale
                    //if (drawerOpened) 0.8f else  1f
                    this.scaleY =
                        scale
                    // if (drawerOpened) 0.8f else 1f
                    //val corners = if (drawerOpened) 320.dp else 0.dp
                    //this.shape = RoundedCornerShape(32.dp)
                }
                .anchoredDraggable(drawerState, orientation = Orientation.Horizontal)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape =
                    //RoundedCornerShape(32.dp)
                    if (drawerEnumState == DrawerState.Open) RoundedCornerShape(32.dp)
                    else RoundedCornerShape(0.dp)
                )
            ,
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, top = 32.dp , bottom = 16.dp)
                ,
                onClick = {
                    //drawerOpened = !drawerOpened
                    //drawerEnumState = if(drawerState.currentValue ==  DrawerState.Open) DrawerState.Closed else DrawerState.Open
                    Log.d(TAG, "StartingScreen: 1 ${drawerState.currentValue} , ${drawerState.progress}")

                    coroutineScope.launch {
                        //drawerState.animateTo(drawerEnumState)
                        //drawerEnumState = drawerState.currentValue
                        Log.d(TAG, "StartingScreen: 1 ${drawerState.currentValue} , ${drawerState.progress}")
                        if(drawerState.targetValue == DrawerState.Open){
                            Log.d(TAG, "StartingScreen: closing")
                            drawerState.animateTo(DrawerState.Closed)
                        }else if(drawerState.targetValue == DrawerState.Closed){
                            Log.d(TAG, "StartingScreen: opening")
                            drawerState.animateTo(DrawerState.Open)
                        }
                        Log.d(TAG, "StartingScreen: 2 ${drawerState.currentValue}")


                        /*if(drawerEnumState == DrawerState.Open) {
                            translationX.animateTo(drawerWidth)

                            Log.d(TAG, "StartingScreen: here 2")
                        }
                        else translationX.animateTo(0f)*/
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Menu
                    , contentDescription = "menu"
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentHeight()
                    .border(
                        BorderStroke(
                            1.dp,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    /*.shadow(
                        16.dp,
                        shape = RoundedCornerShape(16.dp)
                    )*/
                ,
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,

                ) {

                Text(
                    text = "select Pdf File",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    //color = if (darkTheme) Color.White else Color.Black
                )
                Button(
                    onClick = onSelectClick,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 6.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        //containerColor = theme.secondary
                    )
                ) {
                    Text(text = "Select", style = MaterialTheme.typography.bodyMedium)
                }
            }
            //BooksLazyColumn(bookList = bookList)
            BooksLazyColumnMap(bookMap = bookMap, onCardClick, onSwipeLeft)
            SelectedBookAlertDialog(
                onConfirm = { onConfirm() },
                onDismiss = { onDismiss() },
                openDialog = openDialog
            )

            //AdMobBanner(modifier = Modifier.weight(1f))
        }
    //}

}
/*
@Composable
fun CardList(cards: List<CardItem>) {
    LazyColumn {
        items(cards) { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = card.name, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = card.description, style = MaterialTheme.typography.body2)
                }
            }
        }
    }
}*/

enum class DrawerState{
    Open,
    Closed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevatedBookCard(nameState : String  , description : String  , onCardClick : (String) -> Unit , timeCreated : String , lastOpened : String  ) {



    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            //containerColor = theme.tertiaryContainer
        ),
        onClick = {
            Log.d(TAG, "ElevatedBookCard: clicked")

            //fileName = nameState

            onCardClick(nameState)

            //val navi = findNavController(fragment)
            //navi.navigate(R.id.action_startingScreenFragment_to_readingscreenFragment)
                  },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp, bottom = 8.dp)
            .clickable() {}

    ) {
        val textModifier = Modifier.padding(8.dp)
        val title =  Text(
            text = nameState,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            //color = textColor
        )

        val des = Text(
            text = description,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            //color = textColor
        )

        Text(text ="Created on $timeCreated",
            modifier = textModifier,
            textAlign = TextAlign.Center,
            //color = textColor
        )

        Text(
            text = "last time Opened : $lastOpened",
            modifier = textModifier,
            textAlign = TextAlign.Center,
            //color = textColor
        )

    }

}

@Composable
fun BooksLazyColumn(bookList : List<PdfData>){
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp)
    ){
        items(bookList.size){
            //ElevatedBookCard(bookList[it].name,bookList[it].lastPage)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksLazyColumnMap(bookMap : Map<String , PdfData>  , onCardClick: (String) -> Unit  , onSwipeLeft : (String) -> Unit ){
    //bookMap.
    val bookList = bookMap.toList()
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.95f)
        .padding(16.dp)
    ){
        //val color = if(darkTheme) Color.White else Color.Black
        items(bookList.size){

            val state = rememberDismissState(
                confirmValueChange = { value ->
                    if(value == DismissValue.DismissedToStart) {
                        Log.d(TAG, "BooksLazyColumnMap: ${bookList[it].first} was swiped to start")
                        //bookList.drop(it)
                        onSwipeLeft(bookList[it].first)

                    }
                    true
                }
            )

            SwipeToDismiss(
                state = state ,
                background = SwipeBackground(state) ,
                dismissContent = {
                    ElevatedBookCard(bookList[it].first,bookList[it].second.filePath, onCardClick , bookList[it].second.timeCreated , bookList[it].second.timeLastOpened )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(state : DismissState ): @Composable() (RowScope.() -> Unit) {

    //Log.d(TAG, "SwipeBackground: state : $state , ${state.dismissDirection} , ${state.progress}")

    val color = when (state.dismissDirection) {
        DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.onSurface
        else -> Color.Transparent
    }
    return {
        if (state.progress < 1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    contentDescription = "delete"
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "edit",
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }
        //else { state.progress = 0f }
    }

    //return SwipeBackground(state)
}

@Preview(showSystemUi = true)
@Composable
fun BookmarkDrawer(){
    //val (drawerState, onDrawerStateChange) = state { DrawerValue.Closed }

    var drawerState by remember {
        mutableStateOf(DrawerValue.Open)
    }

    ModalNavigationDrawer(
        modifier = Modifier.width(width = 128.dp),
        drawerState = rememberDrawerState(initialValue = drawerState),
        gesturesEnabled = true,
        scrimColor = DrawerDefaults.scrimColor,
        drawerContent = {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = "drawer test",
                color = Color.White
            )
            Button(onClick = {
                drawerState = if (drawerState == DrawerValue.Closed) DrawerValue.Open
                else DrawerValue.Closed
            }
            ) {
                Text(text = "open close drawer")
            }
        }
        },
        content = {
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedBookAlertDialog( onConfirm : ()-> Unit , onDismiss : ()-> Unit  , openDialog : MutableState<Boolean>){

    if(openDialog.value){

        AlertDialog(
            icon = {Icons.Default.Info},
            title = { Text(text ="Wait a Mintue!"  ) },
            text = {
                Text(text =  "it appears there is another session of the same book you are trying to open \n would you like to make a new session or continue your previous session ?"
            )},
            onDismissRequest = {
                openDialog.value = false
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    openDialog.value = false
                }) {
                    Text("Start a new session")
                }

            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                    openDialog.value = false
                }) {
                    Text("Continue last session")
                }
            }
        )

    }

}

@Composable
fun AdMobBanner(modifier: Modifier = Modifier){
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = {context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = resources.getString(R.string.start_screen_bottom_banner_unit_id)
                loadAd(AdRequest.Builder().build())
            }
    })
}




