package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import com.example.ui.theme.KoroFocusBorder

@Composable
fun Modifier.tvFocusable(
  onClick: () -> Unit,
  shape: Shape = RoundedCornerShape(14.dp),
  focusedScale: Float = 1.05f,
  borderWidth: Dp = 2.5.dp,
  focusedBorderColor: Color = KoroFocusBorder,
  unfocusedBorderColor: Color = Color.Transparent,
  onFocused: (() -> Unit)? = null,
): Modifier = composed {
  var isFocused by remember { mutableStateOf(false) }
  val interactionSource = remember { MutableInteractionSource() }

  val scale by
    animateFloatAsState(
      targetValue = if (isFocused) focusedScale else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "tvFocusScale",
    )

  val borderColor by
    animateColorAsState(
      targetValue = if (isFocused) focusedBorderColor else unfocusedBorderColor,
      label = "tvFocusBorder",
    )

  this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .border(width = borderWidth, color = borderColor, shape = shape)
    .onFocusChanged { focusState ->
      isFocused = focusState.isFocused
      if (focusState.isFocused) {
        onFocused?.invoke()
      }
    }
    .onPreviewKeyEvent { keyEvent ->
      if (
        keyEvent.type == KeyEventType.KeyUp &&
          (keyEvent.key == Key.DirectionCenter ||
            keyEvent.key == Key.Enter ||
            keyEvent.key == Key.NumPadEnter)
      ) {
        onClick()
        true
      } else {
        false
      }
    }
    .focusable(interactionSource = interactionSource)
    .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}
