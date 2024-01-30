package com.pdf.studymarkercompsose

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pdf.studymarker.data.AppSettings
import com.pdf.studymarker.data.AppStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsDrawer(appSettings: AppSettings){
    val showSelectStyleDialog = remember { mutableStateOf(false) }
    val showSetEnhancementDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    SelectStyleDialog(
        selectStyleDialog = showSelectStyleDialog,
        currentStyle = appSettings.appStyle
    ) {selectedStyle ->
        coroutineScope.launch {
            context.dataStore.updateData {
                it.copy(
                    appStyle = selectedStyle
                )
            }
        }
    }

    SetEnhancementDialog(
        setEnhancementDialog = showSetEnhancementDialog,
        currentEnhancement = appSettings.pageScaling
    ) {selectedEnhancement ->
        coroutineScope.launch {
            context.dataStore.updateData {
                it.copy(
                    pageScaling = selectedEnhancement
                )
            }
        }
    }

    Surface() {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .padding(start = 8.dp)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                    ,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start

        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.33f))
            SettingsRow(item = "Style :",
                imageVector = ImageVector.vectorResource(id = R.drawable.dark_mode_icon),
                value = appSettings.appStyle.name
            ){ showSelectStyleDialog.value = true }
            SettingsRow(
                item = "Scroll Animation :",
                imageVector = ImageVector.vectorResource(id = R.drawable.scroll_animation_icon),
                value =
                    if(appSettings.scrollAnimation) "ON" else "OFF"
            ){}
            SettingsRow(item ="Page Enhancement :"
                , imageVector = ImageVector.vectorResource(id = R.drawable.page_enhancement_icon),
                value =
                    if(appSettings.pageScaling == 0) "automatic"
                    else appSettings.pageScaling.toString()
            ){ showSetEnhancementDialog.value = true }
            Spacer(modifier = Modifier.fillMaxHeight(0.33f))

        }
    }
}

@Composable
fun SettingsRow(item : String ,imageVector: ImageVector , value: String  , onClick : ()-> Unit){
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(40.dp)
            .border(
                BorderStroke(
                    1.dp,
                    color = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .clickable { onClick() }

        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically){
            Icon(
                imageVector = imageVector,
                contentDescription = item,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 6.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = item)
            Spacer(modifier = Modifier.padding(4.dp))
        }
        //Spacer(modifier = Modifier.padding(16.dp))
        if(item.contains("Scroll")){
            var scrollAnimationSwitch by remember { mutableStateOf(false)}
            Log.d("TAG", "SettingsRow: state value is $scrollAnimationSwitch")
            scrollAnimationSwitch =  (value == "ON")
            Switch(
                modifier = Modifier
                    .padding(end = 6.dp, top = 8.dp, bottom = 8.dp)
                    .height(4.dp)
                    //.size(8.dp)
                ,
                checked = scrollAnimationSwitch,
                onCheckedChange = {
                    scrollAnimationSwitch = !scrollAnimationSwitch
                    coroutineScope.launch { context.dataStore.updateData {
                        it.copy(
                            scrollAnimation = scrollAnimationSwitch
                        )
                        }
                        val appSettings = context.dataStore.data
                        Log.d("TAG", "SettingsRow: ${appSettings.first().scrollAnimation}")
                    }
                }
            )
        }
        else {
            Text(
                text = value,
                modifier = Modifier
                    .padding(end = 12.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun SelectStyleDialog(
    selectStyleDialog: MutableState<Boolean>,
    currentStyle: AppStyle,
    onConfirm: (AppStyle)-> Unit,
){

    val selectedStyle = remember { mutableStateOf(currentStyle) }
    if(selectStyleDialog.value) {
        Dialog(onDismissRequest = { selectStyleDialog.value = false }) {
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

                    AppStyle.entries.forEach{
                        SelectStyleDialogRow(appStyle = it, selectedOption = selectedStyle)
                    }
                    
                    Spacer(modifier = Modifier.padding(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        TextButton(
                            onClick = { selectStyleDialog.value = false },
                            //modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            Text(text = "Cancel")
                        }
                        Spacer(modifier = Modifier.fillMaxWidth(0.6f))
                        TextButton(onClick = {
                            onConfirm(selectedStyle.value)
                            selectStyleDialog.value = false
                        }
                        ) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectStyleDialogRow(appStyle: AppStyle , selectedOption : MutableState<AppStyle>){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = appStyle.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
        RadioButton(
            selected = (appStyle == selectedOption.value),
            onClick = { selectedOption.value = appStyle }
        )
    }
}

@Composable
fun SetEnhancementDialog(
    setEnhancementDialog: MutableState<Boolean>,
    currentEnhancement: Int,
    onConfirm: (Int)-> Unit,
){

    val selectedEnhancement = remember { mutableIntStateOf(currentEnhancement) }
    var automatic by remember { mutableStateOf(false)}
    automatic = (currentEnhancement == 0)
    if(setEnhancementDialog.value) {
        Dialog(onDismissRequest = { setEnhancementDialog.value = false }) {
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

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(40.dp)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = selectedEnhancement.intValue.toFloat(),
                            onValueChange ={ selectedEnhancement.intValue = it.toInt() },
                            valueRange = 0f..10f,
                            enabled = !automatic,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                   .padding(start = 6.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text =  selectedEnhancement.intValue.toString(),
                            modifier = Modifier.padding(end = 8.dp)
                            )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))


                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(40.dp)
                            .border(
                                BorderStroke(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automatic",
                            modifier = Modifier.padding(start = 6.dp)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Switch(
                            modifier = Modifier
                                .padding(end = 6.dp, top = 8.dp, bottom = 8.dp)
                                .height(4.dp)
                            ,
                            checked = automatic,
                            onCheckedChange = {
                                automatic = !automatic
                                if(automatic)selectedEnhancement.intValue = 0
                            }
                        )
                    }

                    Spacer(modifier = Modifier.padding(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        TextButton(
                            onClick = { setEnhancementDialog.value = false },

                        ) {
                            Text(text = "Cancel")
                        }
                        Spacer(modifier = Modifier.fillMaxWidth(0.6f))
                        TextButton(onClick = {
                            onConfirm(selectedEnhancement.intValue)
                            setEnhancementDialog.value = false
                        }
                        ) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}

