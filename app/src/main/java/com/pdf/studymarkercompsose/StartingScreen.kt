package com.pdf.studymarkercompsose

import android.content.res.Resources.Theme
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.findNavController
import com.pdf.studymarker.data.PdfData
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


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun StartingScreen(
    state: StartingScreenState,
    onSelectClick: ()-> Unit,
    //bookList: List<PdfData>
    bookMap: Map<String, PdfData>,
    //fragment: startingScreenFragment,
    //sharedViewModel: SharedViewModel,
    onCardClick: (String) -> Unit,
    onSwipeLeft : (String) -> Unit,
    dynamicColor: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    //fileName: String
) {

    //val theme = dynamicLightColorScheme(LocalContext.current)

    val theme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { // …2
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,

        ) {

            Text(
                text = "select Pdf File",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = if(darkTheme) Color.White else Color.Black
            )
            Button(
                onClick = onSelectClick,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.secondary
                )
            ) {
                Text(text = "Select", style = MaterialTheme.typography.bodyMedium)
            }
        }
        //BooksLazyColumn(bookList = bookList)
        BooksLazyColumnMap(bookMap = bookMap , onCardClick , onSwipeLeft , theme , darkTheme )
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevatedBookCard(nameState : String  , description : String  , onCardClick : (String) -> Unit , timeCreated : String , lastOpened : String , theme : ColorScheme , textColor : Color) {



    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = theme.tertiaryContainer
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
            .padding(8.dp)
            .clickable() {}

    ) {
        val textModifier = Modifier.padding(8.dp)
        val title =  Text(
            text = nameState,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )

        val des = Text(
            text = description,
            modifier = textModifier,
            textAlign = TextAlign.Center,
            color = textColor
        )

        Text(text ="Created on $timeCreated",
            modifier = textModifier,
            textAlign = TextAlign.Center,
            color = textColor
        )

        Text(
            text = "last time Opened : $lastOpened",
            modifier = textModifier,
            textAlign = TextAlign.Center,
            color = textColor
        )

    }

}

@Composable
fun BooksLazyColumn(bookList : List<PdfData>){
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ){
        items(bookList.size){
            //ElevatedBookCard(bookList[it].name,bookList[it].lastPage)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksLazyColumnMap(bookMap : Map<String , PdfData>  , onCardClick: (String) -> Unit  , onSwipeLeft : (String) -> Unit , theme : ColorScheme , darkTheme: Boolean){
    //bookMap.
    val bookList = bookMap.toList()
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ){
        val color = if(darkTheme) Color.White else Color.Black
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
                    ElevatedBookCard(bookList[it].first,bookList[it].second.filePath, onCardClick , bookList[it].second.timeCreated , bookList[it].second.timeLastOpened , theme , color)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(state : DismissState ): @Composable() (RowScope.() -> Unit) {

    val color = when (state.dismissDirection) {
        DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.onSurface
        null -> Color.Transparent
    }

    return {


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




