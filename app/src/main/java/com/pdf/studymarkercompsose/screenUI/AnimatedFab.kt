package com.pdf.studymarkercompsose.screenUI

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdf.studymarkercompsose.data.ButtonId
import com.pdf.studymarkercompsose.data.ModeState
import org.intellij.lang.annotations.Language

private const val buttonContentSize = 60

@Language("AGSL")
const val ShaderSource = """
    uniform shader composable;
    
    uniform float visibility;
    
    half4 main(float2 cord) {
        half4 color = composable.eval(cord);
        color.a = step(visibility, color.a);
        return color;
    }
"""
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BlurContainer(
    modifier: Modifier = Modifier,
    blur: Float = 60f,
    component: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .customBlur(blur),
            content = component,
        )
        Box(
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun Modifier.customBlur(blur: Float) = this.then(
    graphicsLayer {
        if (blur > 0f)
            renderEffect = RenderEffect
                .createBlurEffect(
                    blur,
                    blur,
                    Shader.TileMode.DECAL,
                )
                .asComposeRenderEffect()
    }
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ShaderContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val runtimeShader = remember {
        RuntimeShader(ShaderSource)
    }
    Box(
        modifier
            .graphicsLayer {
                runtimeShader.setFloatUniform("visibility", 0.2f)
                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(
                        runtimeShader, "composable"
                    )
                    .asComposeRenderEffect()
            }
    ) {
        content()
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BoxScope.ButtonComponent(
    modifier: Modifier = Modifier,
    background: Color = Color.Black,
    blur: Float = 60f,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    BlurContainer(
        modifier = modifier
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick,
            )
            .align(Alignment.BottomEnd),
        blur = blur,
        component = {
            Box(
                Modifier
                    .size(32.dp)
                    .background(color = background, CircleShape)
            )
        }
    ) {
        Box(
            Modifier.size(buttonContentSize.dp),
            content = content,
            contentAlignment = Alignment.Center,
        )
    }
}


private operator fun PaddingValues.times(factor: Float): PaddingValues {
    return PaddingValues(
        start = this.calculateStartPadding(LayoutDirection.Ltr) * factor,
        end = this.calculateEndPadding(LayoutDirection.Ltr) * factor,
        top = this.calculateTopPadding() * factor,
        bottom = this.calculateBottomPadding() * factor
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AnimatedFab(
    modifier: Modifier = Modifier,
    items: List<MiniFabItem>,
    darkModeToggle: MutableState<Boolean>,
    modeState: MutableState<ModeState>,
    alpha: State<Float>,
    expanded: MutableState<Boolean>,
    openColorPicker: MutableState<Boolean>,
    currentColorState: MutableState<Color>,
    strokeWidth: Float,
    showStrokeSlider: MutableState<Boolean>
) {

    //var expanded: Boolean by remember { mutableStateOf(false) }

    var selectedButton by remember {
        mutableStateOf(ButtonId.None)
    }

    /*val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = ""
    )*/

    ShaderContainer(
        modifier = modifier.fillMaxSize()
    ) {
        items.forEachIndexed { index , item  ->
            ButtonComponent(
                Modifier.padding(
                    paddingValues = PaddingValues(
                        bottom = ((index + 1) * buttonContentSize).dp //80.dp
                    ) * FastOutSlowInEasing
                        .transform((alpha.value))
                ),
                background = if(selectedButton == item.id) Color.Yellow else Color.Black ,
                blur = 52f * alpha.value
                ,
                onClick =
                {
                    if(item.modeState != ModeState.Idle) {
                        modeState.value = item.modeState
                        selectedButton = item.id
                    }else if(item.id == ButtonId.Idle){
                        modeState.value = item.modeState
                        selectedButton = item.id
                    }
                    when(item.id) {
                        ButtonId.DarkMode -> { darkModeToggle.value = !darkModeToggle.value }
                        ButtonId.Color -> { openColorPicker.value = true }
                        ButtonId.Width -> {showStrokeSlider.value = !showStrokeSlider.value}
                        else -> {
                            //expanded = !expanded
                        }
                    }
                }

            ) {
                if(item.id == ButtonId.Width){
                    val radius = ((strokeWidth/100f) * 26) + 25
                    Text(
                        text = strokeWidth.toInt().toString(),
                        modifier = Modifier
                            .alpha(alpha.value)
                            .drawBehind {
                            drawCircle(Color.White , radius = radius)
                        },
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }else {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = item.icon),
                        contentDescription = item.label,
                        tint = if (item.id == ButtonId.Color) currentColorState.value else Color.White,
                        modifier = Modifier.alpha(alpha.value)
                    )
                }

            }
        }
/*
        ButtonComponent(
            Modifier.padding(
                paddingValues = PaddingValues(
                    bottom = 80.dp
                ) * FastOutSlowInEasing
                    .transform((alpha))
            ),
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.alpha(alpha)
            )
        }

        ButtonComponent(
            Modifier.padding(
                paddingValues = PaddingValues(
                    bottom = 160.dp
                ) * FastOutSlowInEasing.transform(alpha)
            ),
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.alpha(alpha)
            )
        }

        ButtonComponent(
            Modifier.padding(
                paddingValues = PaddingValues(
                    bottom = 240.dp
                ) * FastOutSlowInEasing.transform(alpha)
            ),
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.alpha(alpha)
            )
        }*/

        ButtonComponent(
            Modifier
                .align(Alignment.BottomEnd),
            //blur =  0f,
            onClick = {
                expanded.value = !expanded.value
                modeState.value = ModeState.Idle
                selectedButton = ButtonId.None
                showStrokeSlider.value = false
            },
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (expanded.value) 45f else 0f,
                label = "",
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = Color.White
            )
        }
    }
}
