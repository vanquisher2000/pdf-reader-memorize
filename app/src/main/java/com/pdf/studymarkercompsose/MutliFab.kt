package com.pdf.studymarkercompsose

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.MutableLiveData
import com.pdf.studymarker.data.SerializedColor
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.MultiFloatingState
import com.pdf.studymarkercompsose.data.toColor
import com.pdf.studymarkercompsose.data.toSerializedColor


@Composable
fun MultiFab(
    multiFloatingState: MultiFloatingState,
    items: List<MiniFabItem>,
    onMultiFloatingStateChange: (MultiFloatingState) -> Unit,
    modeState: ModeState,
    onModeStateChange: (ModeState) -> Unit,
    strokeWidth: MutableFloatState,
    modifier: Modifier = Modifier,
    bottomBarWidth: MutableState<Dp>,
    bottomBarAlpha: MutableFloatState,
    currentColor: MutableLiveData<SerializedColor>
) {

    val transition = updateTransition(targetState = multiFloatingState , label = "transition")
    val rotate by transition.animateFloat(label = "rotate") {
        if(it == MultiFloatingState.Expanded) 315f else 0f
    }

    val fabScale by transition.animateFloat(label = "fabScale" ) {
        if(it == MultiFloatingState.Expanded) 1f else 0f
    }

    val alpha by transition.animateFloat(label = "alpha" , transitionSpec = { tween(durationMillis = 50) }) {
        if(it == MultiFloatingState.Expanded) 36f else 0f
    }

    val textShadow by transition.animateDp(label = "textShadow" , transitionSpec = { tween(durationMillis = 50) }) {
        if(it == MultiFloatingState.Expanded) 2.dp else 0.dp
    }

    val modeTransient = updateTransition(targetState = modeState , label = "modeTransition")

    val configuration = LocalConfiguration.current

    val openColorPicker = remember { mutableStateOf(false) }

    val currentColorState = remember { mutableStateOf(currentColor.value!!.toColor()) }


    ColorPicker(openColorPicker = openColorPicker , currentColor = currentColor , currentColorState = currentColorState)

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {

        if(transition.currentState == MultiFloatingState.Expanded){
            items.forEach{
                MiniFab(item = it  , alpha = alpha , fabScale = fabScale , textShadow = textShadow , onMiniFabClick = { miniFabItem ->
                    when(miniFabItem.id){
                        ButtonId.Rect ->{

                            onModeStateChange(
                                if(modeTransient.currentState != ModeState.Rect) ModeState.Rect
                                else ModeState.Idle
                            )
                        }
                        ButtonId.Delete -> {
                            onModeStateChange(
                                if(modeTransient.currentState != ModeState.Delete) ModeState.Delete
                                else ModeState.Idle
                            )

                        }
                        ButtonId.Path -> {
                            onModeStateChange(
                                if(modeTransient.currentState != ModeState.Path) ModeState.Path
                                else ModeState.Idle
                            )
                        }
                        ButtonId.Marker -> {
                            onModeStateChange(
                                if(modeTransient.currentState != ModeState.Path) ModeState.Path
                                else ModeState.Idle
                            )
                        }
                        else -> {}
                    }
                },
                    strokeWidth = strokeWidth,
                    openColorPicker = openColorPicker,
                    currentColor = currentColor,
                    currentColorState = currentColorState
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                if(transition.currentState == MultiFloatingState.Expanded){
                    onMultiFloatingStateChange(MultiFloatingState.Collapsed)
                    onModeStateChange(ModeState.Idle)
                    bottomBarWidth.value = 64.dp
                    bottomBarAlpha.floatValue = 0f
                }else{
                    onMultiFloatingStateChange(MultiFloatingState.Expanded)
                    bottomBarWidth.value = (configuration.screenWidthDp).dp
                    bottomBarAlpha.floatValue = 1f

                }
               /* onMultiFloatingStateChange(
                    if (transition.currentState == MultiFloatingState.Expanded) {
                        MultiFloatingState.Collapsed
                    }
                    else MultiFloatingState.Expanded
                )*/
            },
            modifier = Modifier.rotate(rotate)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "tools menu"
            )
        }
    }

}

class MiniFabItem(
    val icon : ImageVector,
    val label : String,
    val id : ButtonId
)

@Composable
fun MiniFab(
    item: MiniFabItem,
    onMiniFabClick: (MiniFabItem) -> Unit,
    alpha: Float,
    textShadow: Dp,
    fabScale: Float,
    showLabel: Boolean = true,
    strokeWidth: MutableFloatState,
    openColorPicker: MutableState<Boolean>,
    currentColor: MutableLiveData<SerializedColor>,
    currentColorState: MutableState<Color>
) {

    //val color = MaterialTheme.colorScheme.primary
    val shadow = Color.Black.copy(.5f)
    var fillColor by remember {
        mutableStateOf(Color.White)
    }

    //val strokeWidth = remember { mutableFloatStateOf(1f) }

    var width by remember {
        mutableStateOf(0.dp)
    }

    var alphaAux by remember { mutableFloatStateOf(0f) }
    var showColors by remember { mutableStateOf(false)}
    val textBg = MaterialTheme.colorScheme.primaryContainer

    /*   Canvas(
           modifier = Modifier
               .size(32.dp)
               .clickable(
                   //interactionSource = ,
                   onClick = {
                       onMiniFabClick.invoke(item)
                   },

                   )

       ) {

           drawCircle(
               color = color ,
               radius = 36f
           )
           *//*drawImage(
            image = item.icon,
            topLeft = Offset(center.x - (item.icon.width) /2 , center.y - (item.icon.height) / 2 )
        )*//*
    }*/

    if(item.id != ButtonId.Color) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showLabel) {
                Text(
                    text = if (item.id == ButtonId.Width) " ${item.label} : ${strokeWidth.floatValue.toInt()} " else " ${item.label} ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(
                            animateFloatAsState(
                                targetValue = alpha,
                                animationSpec = tween(50)
                            ).value
                        )
                        .shadow(textShadow)
                        .padding(start = 6.dp, end = 6.dp, top = 4.dp)
                        .height(24.dp)
                        .align(Alignment.CenterVertically)
                        //.padding(16.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = textBg,
                                cornerRadius = CornerRadius(5.dp.toPx())
                            )
                        }
                )
                Spacer(modifier = Modifier.size(16.dp))


                if (item.id == ButtonId.Width) {
                    Slider(
                        value = strokeWidth.floatValue,
                        onValueChange = {
                            strokeWidth.floatValue = it
                            Log.d("TAG", "MiniFab: current slider value $it ")
                        },
                        steps = 50,
                        valueRange = 1f..50f,
                        modifier = Modifier
                            .height(40.dp)
                            .width(
                                animateDpAsState(
                                    targetValue = width,
                                    tween(500),
                                    label = "widthAnimation"
                                ).value
                            )
                            .alpha(
                                animateFloatAsState(
                                    targetValue = alphaAux,
                                    tween(500),
                                    label = "alphaAnimation"
                                ).value
                            )
                    )
                }
            }

            FloatingActionButton(
                onClick =
                {
                    onMiniFabClick.invoke(item)
                    fillColor = if (fillColor == Color.White) Color.Black else Color.White
                    if (item.id == ButtonId.Width) {
                        width = if (width == 0.dp) 150.dp else 0.dp
                        alphaAux = if (alphaAux == 0f) 1f else 0f
                    }
                },
                //containerColor = color,
                contentColor = fillColor,
                modifier = Modifier
                    //.border(width = 1.dp, color = shadow)
                    .scale(fabScale)
                    .alpha(alpha)
                    .size(40.dp)
                /*.drawBehind {
                    if (item.id == ButtonId.Color) drawCircle(
                        color = Color.Red,
                        radius = 32.dp.toPx()
                    )
                }*/
                //.drawWithContent {  if(item.id == ButtonId.Color) drawCircle(color = Color.Red , radius = 32.dp.toPx()) }
            ) {
                 Icon(
                    imageVector = item.icon,
                    contentDescription = "draw"
                )
            }
        }
    }else{
        ColorsRow(
            fabScale = fabScale, alpha = alpha, openColorPicker = openColorPicker,
            currentColorLiveData = currentColor,
            currentColorState = currentColorState
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ColorsRow(
    fabScale: Float,
    alpha: Float,
    openColorPicker: MutableState<Boolean>,
    currentColorLiveData: MutableLiveData<SerializedColor>,
    currentColorState: MutableState<Color>,
    //item: MiniFabItem,
    //onMiniFabClick: (MiniFabItem) -> Unit,
    //textShadow : Dp,
    //showLabel : Boolean = true,
    //strokeWidth : MutableFloatState
) {
    var alphaAux by remember { mutableFloatStateOf(0f) }
    val configuration = LocalConfiguration.current
    val currentColor = remember { mutableStateOf(Color.Red) }
    val currentColorLive = derivedStateOf { currentColorLiveData.value }
    var expanded by remember { mutableStateOf(false)}
    var colorsRowWidth by remember { mutableStateOf(0.dp)}

    var colorsList = mutableListOf(Color.Black, Color.White, Color.Yellow, Color.Red)

    Row(

    ) {

        if (true) {
            Row(
                horizontalArrangement = Arrangement.End,
                //verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .width(
                        animateDpAsState(
                            targetValue = colorsRowWidth,
                            tween(500),
                            label = "color_row_animation"
                        ).value
                    )
            )
            {
                FloatingActionButton(
                    onClick = { openColorPicker.value = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "add Color")
                }
                colorsList.forEach {
                    ColorButton(
                        color = it,
                        alpha = alphaAux,
                        currentColor = currentColor,
                        currentColorLiveData = currentColorLiveData,
                        currentColorState = currentColorState
                    ) {
                        colorsRowWidth = 0.dp
                        alphaAux = 0f
                        expanded = false
                    }
                }

            }
        }

        FloatingActionButton(
            onClick =
            {
                //onMiniFabClick.invoke(item)
                colorsRowWidth =
                    if (colorsRowWidth == 0.dp) configuration.screenWidthDp.dp - 64.dp else 0.dp
                alphaAux = if (alphaAux == 0f) 1f else 0f
                expanded = !expanded
            },
            modifier = Modifier
                .scale(fabScale)
                .alpha(alpha)
                .size(40.dp)
        ) {
            Canvas(modifier = Modifier) {
                drawCircle(
                    color = currentColorState.value,
                    radius = 16.dp.toPx()
                )
            }

        }
        /*if (true) {
            ColorButton(color = Color.White, alpha = alphaAux, currentColor = currentColor)
            ColorButton(color = Color.Black, alpha = alphaAux, currentColor = currentColor)
            ColorButton(color = Color.Yellow, alpha = alphaAux, currentColor = currentColor)
        }*/


    }

}

@Composable
fun ColorButton(
    color: Color,
    alpha: Float,
    currentColor: MutableState<Color>,
    currentColorLiveData: MutableLiveData<SerializedColor>,
    currentColorState: MutableState<Color>,
    onClick: () -> Unit,
){
    FloatingActionButton(
        onClick =
        {
            onClick()
           currentColor.value = color
            currentColorLiveData.value = color.toSerializedColor()
            currentColorState.value = color
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        //contentColor = Color.Transparent,
        modifier = Modifier
            //.border(width = 1.dp, color = shadow)
            //.scale(fabScale)
            .alpha(
                animateFloatAsState(
                    targetValue = alpha,
                    tween(500),
                    label = "color_button_alpha"
                ).value
            )
            .size(40.dp)
    ) {
        Canvas(modifier = Modifier) {
            drawCircle(
                color = color,
                radius = 16.dp.toPx()
            )
        }

    }
}

@Composable
fun ColorPicker(
    openColorPicker: MutableState<Boolean>,
    currentColor: MutableLiveData<SerializedColor>,
    currentColorState: MutableState<Color>
){
    var red = remember { mutableFloatStateOf(1f) }
    var blue = remember { mutableFloatStateOf(0f) }
    var green = remember { mutableFloatStateOf(0f) }
    var alpha = remember { mutableFloatStateOf(1f) }
    val colorElementList = listOf(red, blue, green, alpha )
    val colorList = listOf(Color.Red , Color.Blue , Color.Green , Color.White)

    if(openColorPicker.value) {
        Dialog(onDismissRequest = { openColorPicker.value = false }) {
            //StudyMarkerCompsoseTheme {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp)
                        //.fillMaxWidth(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(128.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            drawCircle(
                                color = Color(
                                    red.floatValue,
                                    green.floatValue,
                                    blue.floatValue,
                                    alpha.floatValue
                                )
                            )
                        }
                        for(i in colorElementList.indices){
                            ColorSlider(
                                color = colorElementList[i],
                                sliderColor = colorList[i]
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ){
                            TextButton(
                                onClick = { openColorPicker.value = false },
                                //modifier = Modifier.fillMaxWidth(0.5f)
                            ) {
                                Text(text = "Cancel")
                            }
                            Spacer(modifier = Modifier.fillMaxWidth(0.6f))
                            TextButton(onClick = {
                                currentColor.value =  SerializedColor(red.floatValue , green.floatValue , blue.floatValue , alpha.floatValue)
                                openColorPicker.value = false
                                currentColorState.value = Color(red.floatValue , green.floatValue , blue.floatValue , alpha.floatValue)
                            }
                            ) {
                                Text(text = "Confirm")
                            }
                        }

                    }
                }
            //}
        }
    }
}

@Composable
fun ColorSlider(color : MutableFloatState , sliderColor : Color){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Slider(
            value = color.floatValue,
            onValueChange = {color.floatValue = it},
            colors = SliderDefaults.colors(
                thumbColor = sliderColor,
                activeTrackColor = sliderColor
            ),
            modifier = Modifier
                //.height(64.dp)
                .fillMaxWidth(0.80f)
        )
        Text(
            text = " : " + (color.floatValue * 100).toInt().toString(),
            modifier = Modifier
                //.fillMaxWidth(0.4f)
        )
    }
}
