package com.tarang.elite.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tarang.elite.TarangApp
import com.tarang.elite.ui.theme.ScoreHigh
import com.tarang.elite.ui.theme.ScoreLow
import com.tarang.elite.ui.theme.ScoreMid

/* ---------------------------------------------------------------------- */
/* ViewModel wiring                                                        */
/* ---------------------------------------------------------------------- */

/** Creates a ViewModel with access to the app's [com.tarang.elite.di.AppContainer]. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (TarangApp) -> VM,
): VM {
    val app = LocalContext.current.applicationContext as TarangApp
    return viewModel(
        key = key,
        factory = viewModelFactory { initializer { create(app) } },
    )
}

/* ---------------------------------------------------------------------- */
/* Colour helpers                                                          */
/* ---------------------------------------------------------------------- */

/** Red → amber → green interpolation for 0..1 scores. */
fun scoreColor(progress: Float): Color {
    val p = progress.coerceIn(0f, 1f)
    return if (p < 0.5f) lerp(ScoreLow, ScoreMid, p * 2f)
    else lerp(ScoreMid, ScoreHigh, (p - 0.5f) * 2f)
}

/* ---------------------------------------------------------------------- */
/* Containers                                                              */
/* ---------------------------------------------------------------------- */

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleEmoji: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(18.dp)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (titleEmoji != null) {
                        Text(titleEmoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                    )
                }
                Spacer(Modifier.size(12.dp))
            }
            content()
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevron",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (emoji != null) {
                    Text(emoji, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = accent,
                    modifier = Modifier.graphicsLayer { rotationZ = chevron },
                )
            }
            if (expanded) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */
/* Small widgets                                                           */
/* ---------------------------------------------------------------------- */

@Composable
fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun BrandToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(Modifier.padding(5.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val bg by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "toggleBg",
                )
                val fg by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "toggleFg",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(bg)
                        .clickable { onSelect(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
                }
            }
        }
    }
}

@Composable
fun AnimatedCheckCircle(
    done: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val scale by animateFloatAsState(
        targetValue = if (done) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale",
    )
    val bg by animateColorAsState(
        if (done) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "checkBg",
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Done",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(size * 0.55f),
            )
        }
    }
}

/** Gentle infinite scale pulse, used on the streak flame. */
@Composable
fun Modifier.pulse(scaleTo: Float = 1.12f, durationMs: Int = 900): Modifier {
    val transition = rememberInfiniteTransition(label = "pulse")
    val s by transition.animateFloat(
        initialValue = 1f,
        targetValue = scaleTo,
        animationSpec = infiniteRepeatable(
            tween(durationMs, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    return this.graphicsLayer {
        scaleX = s
        scaleY = s
    }
}

/* ---------------------------------------------------------------------- */
/* Data visualisation                                                      */
/* ---------------------------------------------------------------------- */

/**
 * Animated arc ring; the arc colour blends red → amber → green with [progress].
 * Place score text etc. in [content].
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    stroke: Dp = 14.dp,
    animate: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = if (animate) tween(1200, easing = FastOutSlowInEasing) else tween(0),
        label = "ring",
    )
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            val inset = sw / 2f
            val arcSize = Size(size.width - sw, size.height - sw)
            // Track
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
            if (animated > 0.005f) {
                val colour = scoreColor(animated)
                // Soft glow underneath
                drawArc(
                    color = colour.copy(alpha = 0.22f),
                    startAngle = -90f,
                    sweepAngle = animated * 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = sw * 1.9f, cap = StrokeCap.Round),
                )
                // Main arc
                drawArc(
                    color = colour,
                    startAngle = -90f,
                    sweepAngle = animated * 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** Minimal line chart with gradient fill for trends. */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    if (values.size < 2) return
    Canvas(modifier) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)
        val pad = size.height * 0.08f

        fun yFor(v: Float): Float =
            pad + (1f - (v - min) / range) * (size.height - pad * 2)

        val line = Path().apply {
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = yFor(v)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.28f), Color.Transparent),
            ),
        )
        drawPath(
            path = line,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
        // End dot
        val lastX = (values.size - 1) * stepX
        drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(lastX, yFor(values.last())))
    }
}
