package com.pdf.studymarkercompsose

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ModeState
import com.pdf.studymarkercompsose.data.MultiFloatingState


@Composable
fun MultiFab(
    multiFloatingState: MultiFloatingState,
    items : List<MiniFabItem>,
    onMultiFloatingStateChange: (MultiFloatingState) -> Unit,
    modeState: ModeState,
    onModeStateChange : (ModeState)-> Unit
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



    Column(
        horizontalAlignment = Alignment.End
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
                    }
                })
                Spacer(modifier = Modifier.size(8.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                if(transition.currentState == MultiFloatingState.Expanded){
                    onMultiFloatingStateChange(MultiFloatingState.Collapsed)
                    onModeStateChange(ModeState.Idle)
                }else{
                    onMultiFloatingStateChange(MultiFloatingState.Expanded)
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
    alpha : Float,
    textShadow : Dp,
    fabScale : Float,
    showLabel : Boolean = true
) {

    val color = MaterialTheme.colorScheme.secondary
    val shadow = Color.Black.copy(.5f)
    var fillColor by remember {
        mutableStateOf(Color.White)
    }
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

    Row {
        if(showLabel) {
            Text(
                text = item.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(
                        animateFloatAsState(
                            targetValue = alpha,
                            animationSpec = tween(50)
                        ).value
                    )
                    .shadow(textShadow)
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
        }

        FloatingActionButton(
            onClick =
            {
                onMiniFabClick.invoke(item)
                fillColor = if (fillColor == Color.White) Color.Black else Color.White
            },
            containerColor = color,
            contentColor = fillColor,
            modifier = Modifier
                //.border(width = 1.dp, color = shadow)
                .scale(fabScale)
                .alpha(alpha)

        ) {
            Icon(imageVector = item.icon, contentDescription = "draw")
        }
    }

}
