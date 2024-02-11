package com.pdf.studymarkercompsose.screenUI

import CustomSlider
import CustomSliderDefaults
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.SharedViewModel
import com.pdf.studymarkercompsose.data.toColor
import com.pdf.studymarkercompsose.logicClasses.Reader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ReadingScreenFab(
    pageNo: State<Int>,
    pageCount: Int,
    items: List<MiniFabItem>,
    darkModeToggle: MutableState<Boolean>,
    modeState: MutableState<ModeState>,
    state: LazyListState,
    sharedModel: SharedViewModel,
    strokeWidth: MutableFloatState,
    reader : Reader
){

    val coroutineScope = rememberCoroutineScope()
    val animateScroll = sharedModel.animateScroll.value?: true
    val expanded = remember {
        mutableStateOf(false)
    }
    val alpha = animateFloatAsState(
        targetValue = if (expanded.value) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = ""
    )

    val showStrokeSlider = remember { mutableStateOf(false)}

    val strokeSliderAlpha = animateFloatAsState(
        targetValue = if (showStrokeSlider.value) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = ""
    )

    val textAlpha = animateFloatAsState(
        targetValue = if (state.isScrollInProgress) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (state.isScrollInProgress) 500 else 5000,
            easing = LinearEasing
        ),
        label = ""
    )

    val fabAlpha = animateFloatAsState(
        targetValue = if ((state.isScrollInProgress && !expanded.value) || expanded.value ) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = if ((state.isScrollInProgress && !expanded.value) || expanded.value ) 500 else 5000,
            easing = LinearEasing
        ),
        label = ""
    )

    val openColorPicker = remember { mutableStateOf(false) }
    val openGoToPageDialog = remember { mutableStateOf(false) }
    val currentColorState = remember { mutableStateOf(reader.getCurrentColor().value!!.toColor()) }

    ColorPicker(openColorPicker = openColorPicker , currentColor = sharedModel.currentColor , currentColorState = currentColorState)
    GoToPageDialog(
        openGoToPageDialog = openGoToPageDialog,
        onConfirm = {
            val selectedPage = it.coerceIn(0 , pageCount)
            coroutineScope.launch {
                if(animateScroll && sharedModel.loaded.value == true) state.animateScrollToItem(selectedPage, 0)
                else state.scrollToItem(selectedPage, 0)
            }})

    Box(
        modifier = Modifier
    ) {

        /* MultiFab(
             currentColor = reader.getCurrentColor(),
             multiFloatingState = multiFloatingState,
             bottomBarWidth = bottomBarWidth,
             bottomBarAlpha = bottomBarAlpha,
             //items = items,
             items = reader.items,
             onMultiFloatingStateChange = { multiFloatingState = it },
             modeState = modeState.value,
             onModeStateChange = { modeState.value = it },
             strokeWidth = strokeWidth,
             selectedButton = selectedButton,
             modifier = Modifier
                 .fillMaxWidth(),
             gotoFunction = {
                 val selectedPage = it.coerceIn(0 , reader.pdfRenderer!!.pageCount)
                 coroutineScope.launch {
                     if(animateScroll) state.animateScrollToItem(selectedPage, 0)
                     else state.scrollToItem(selectedPage, 0)
                 }
             },
             darkModeToggle = darkModeToggle
         )*/
        Box(
            modifier = Modifier
                .wrapContentSize()
                //.width(40.dp)
            //.padding(bottom = 160.dp)


        ) {
            AnimatedFab(
                modifier = Modifier.alpha(fabAlpha.value),
                items = items,
                darkModeToggle = darkModeToggle,
                modeState = modeState,
                alpha = alpha,
                expanded = expanded,
                openColorPicker = openColorPicker,
                currentColorState = currentColorState,
                strokeWidth = strokeWidth.floatValue,
                showStrokeSlider = showStrokeSlider
            )
        }


        if(strokeSliderAlpha.value != 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    //.padding(start = 24.dp)
            ) {
                /*Slider(
                    value = strokeWidth.floatValue,
                    onValueChange = { strokeWidth.floatValue = it },
                    valueRange = 10f..100f,
                    steps = 8,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        //.align(Alignment.CenterHorizontally)
                        .padding(end = 56.dp, bottom = 185.dp)
                        .alpha(strokeSliderAlpha.value),
                    enabled = showStrokeSlider.value
                )*/


                CustomSlider(
                    value = strokeWidth.floatValue,
                    onValueChange = { strokeWidth.floatValue = it },
                    valueRange = 10f..100f,
                    gap = 10,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        //.align(Alignment.CenterHorizontally)
                        .padding(end = 56.dp, bottom = 185.dp)
                        .alpha(strokeSliderAlpha.value),
                    enabled = showStrokeSlider.value,
                    thumb = { CustomSliderDefaults.Thumb(
                        thumbValue = it.toString()
                    ) },
                    track = {CustomSliderDefaults.Track(
                        sliderPositions = it ,
                        progressColor = MaterialTheme.colorScheme.primaryContainer,
                        height = 32.dp,
                        shape = RoundedCornerShape(8.dp)
                        )}
                )


            }
        }

        


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 32.dp , bottom = 12.dp)
                    .align(Alignment.BottomEnd)
                    .alpha(1f * textAlpha.value)
            ) {
                val bgColor = MaterialTheme.colorScheme.primaryContainer
                Text(
                    text = " ${pageNo.value + 1} of $pageCount ",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .drawBehind {
                            drawRoundRect(
                                color = bgColor,
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                        .clickable { openGoToPageDialog.value = true }
                        //.padding(start = 24.dp, top = 32.dp , bottom = 16.dp)
                )
            }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                //.padding(start = 16.dp, bottom = 0.dp)
                .align(Alignment.BottomEnd)

        ) {

            CustomSlider(
                value = pageNo.value.toFloat(),
                onValueChange = {
                    coroutineScope.launch {
                        //state.animateScrollToItem((it.toInt()), 0)
                        val pageIndex = it.toInt() - 1
                        if (animateScroll && sharedModel.loaded.value == true) state.animateScrollToItem(pageIndex, 0)
                        else state.scrollToItem(pageIndex, 0)
                    }
                },
                valueRange = 1f..pageCount.toFloat(),
                gap = 1,
                modifier = Modifier
                    .fillMaxWidth(0.85f * alpha.value)
                    //.align(Alignment.CenterHorizontally)
                    .padding(start = 16.dp)
                    .alpha(alpha.value)
                    //.clickable { openGoToPageDialog.value = true }
                ,
                enabled = expanded.value,
                thumb = {CustomSliderDefaults.Thumb(
                    thumbValue = it.toString(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.tertiary,
                    onClick = {openGoToPageDialog.value = true },
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                    )},
                track = {CustomSliderDefaults.Track(
                    sliderPositions = it ,
                    progressColor = MaterialTheme.colorScheme.primaryContainer,
                    height = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )}
            )
        }
    }




}