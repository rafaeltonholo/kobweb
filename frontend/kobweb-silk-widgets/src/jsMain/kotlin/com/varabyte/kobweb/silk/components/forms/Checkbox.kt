package com.varabyte.kobweb.silk.components.forms

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.dom.ElementRefScope
import com.varabyte.kobweb.compose.dom.ref
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.isBright
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.animation.Keyframes
import com.varabyte.kobweb.silk.components.animation.toAnimation
import com.varabyte.kobweb.silk.components.icons.CheckIcon
import com.varabyte.kobweb.silk.components.icons.IndeterminateIcon
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.ComponentVariant
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.addVariantBase
import com.varabyte.kobweb.silk.components.style.base
import com.varabyte.kobweb.silk.components.style.common.DisabledStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.BorderColorVar
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.ColorScheme
import com.varabyte.kobweb.silk.theme.colors.FocusOutlineColorVar
import com.varabyte.kobweb.silk.theme.colors.SilkPalette
import com.varabyte.kobweb.silk.theme.shapes.RectF
import com.varabyte.kobweb.silk.theme.shapes.clip
import com.varabyte.kobweb.silk.theme.toSilkPalette
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

object CheckboxDefaults {
    const val Enabled = true
    val Size = CheckboxSize.MD
    val IconProvider: @Composable CheckboxIconScope.() -> Unit = {
        if (indeterminate) {
            IndeterminateIcon()
        } else {
            CheckIcon()
        }
    }
}

val CheckboxBorderColorVar by StyleVariable(prefix = "silk", defaultFallback = BorderColorVar.value())
val CheckboxBorderRadiusVar by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.125.cssRem)
val CheckboxSizeVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val CheckboxSpacingVar by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.5.cssRem)
val CheckboxFontSizeVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val CheckboxIconSizeVar by StyleVariable<CSSLengthValue>(prefix = "silk")
val CheckboxFocusOutlineColorVar by StyleVariable(prefix = "silk", defaultFallback = FocusOutlineColorVar.value())
val CheckboxIconColorVar by StyleVariable<CSSColorValue>(prefix = "silk")
val CheckboxIconBackgroundColorVar by StyleVariable<CSSColorValue>(prefix = "silk")

val CheckboxStyle by ComponentStyle(prefix = "silk") {
    base {
        Modifier
            .gap(CheckboxSpacingVar.value())
            .userSelect(UserSelect.None)
            .fontSize(CheckboxFontSizeVar.value())
            .cursor(Cursor.Pointer)
    }
}

val CheckboxEnabledAnim by Keyframes(prefix = "silk") {
    from { Modifier.opacity(0) }
    to { Modifier.opacity(1) }
}

val CheckboxIconContainerStyle by ComponentStyle.base(prefix = "silk") {
    Modifier
        .fontSize(CheckboxIconSizeVar.value())
        .size(CheckboxSizeVar.value())
        .backgroundColor(CheckboxIconBackgroundColorVar.value())
        .border(width = 0.125.cssRem, style = LineStyle.Solid, color = CheckboxIconBackgroundColorVar.value())
        .borderRadius(CheckboxBorderRadiusVar.value())
}

val CheckboxIconStyle by ComponentStyle.base(prefix = "silk") {
    Modifier
        .size(CheckboxSizeVar.value())
        .color(CheckboxIconColorVar.value())
}

val UncheckedCheckboxIconContainerVariant by CheckboxIconContainerStyle.addVariantBase {
    Modifier
        .backgroundColor(BackgroundColor.Inherit)
        .borderColor(CheckboxBorderColorVar.value())
}

val CheckboxInputVariant by InputStyle.addVariant {
    // We hide the checkbox itself since the Switch is rendered separately, but keep it a11y-friendly by only limiting
    // its size/appearance (instead of explicitly hiding), matching the approach of many other libraries.
    // See Switch for more context.
    base {
        Modifier
            .border(0.px)
            .size(1.px)
            .margin((-1).px)
            .padding(0.px)
            .clip(RectF(50f))
            .overflow(Overflow.Hidden)
            .whiteSpace(WhiteSpace.NoWrap)
            .position(Position.Absolute)
    }
    // Since the checkbox is hidden, we highlight its sibling (the switch track) when the checkbox is focused(-visible).
    cssRule(":focus-visible + *") {
        Modifier.boxShadow(spreadRadius = 0.1875.cssRem, color = CheckboxFocusOutlineColorVar.value())
    }
}

interface CheckboxSize {
    val boxSize: CSSLengthValue
    val iconSize: CSSLengthValue
    val fontSize: CSSLengthValue

    object SM : CheckboxSize {
        override val boxSize = 0.875.cssRem
        override val iconSize = 0.45.cssRem
        override val fontSize = 0.875.cssRem
    }

    object MD : CheckboxSize {
        override val boxSize = 1.cssRem
        override val iconSize = 0.625.cssRem
        override val fontSize = 1.cssRem
    }

    object LG : CheckboxSize {
        override val boxSize = 1.25.cssRem
        override val iconSize = 0.8.cssRem
        override val fontSize = 1.125.cssRem
    }
}

fun CheckboxSize.toModifier() = Modifier
    .setVariable(CheckboxSizeVar, boxSize)
    .setVariable(CheckboxIconSizeVar, iconSize)
    .setVariable(CheckboxFontSizeVar, fontSize)

class CheckboxIconScope internal constructor(val indeterminate: Boolean, val colorMode: ColorMode)

enum class CheckedState {
    Checked,
    Unchecked,
    Indeterminate;

    companion object {
        fun from(value: Boolean) = if (value) Checked else Unchecked
        fun from(vararg values: Boolean) = when (values.count { it }) {
            0 -> Unchecked
            values.size -> Checked
            else -> Indeterminate
        }
    }

    fun toBoolean() = this != Unchecked
}

@Composable
fun TriCheckbox(
    checked: CheckedState,
    onCheckedChange: (CheckedState) -> Unit,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    enabled: Boolean = CheckboxDefaults.Enabled,
    size: CheckboxSize = CheckboxDefaults.Size,
    spacing: CSSLengthValue? = null,
    colorScheme: ColorScheme? = null,
    borderColor: CSSColorValue? = null,
    iconColor: CSSColorValue? = null,
    focusOutlineColor: CSSColorValue? = null,
    ref: ElementRefScope<HTMLElement>? = null,
    icon: @Composable CheckboxIconScope.() -> Unit = CheckboxDefaults.IconProvider,
    content: (@Composable () -> Unit)? = null,
) {
    // Don't animate if a checkbox is being added to the DOM while already checked
    var shouldAnimate by remember { mutableStateOf(checked.toBoolean()) }
    fun fireOnCheckChanged() {
        onCheckedChange(
            when (checked) {
                CheckedState.Checked -> CheckedState.Unchecked
                CheckedState.Unchecked -> CheckedState.Checked
                CheckedState.Indeterminate -> CheckedState.Checked
            }
        )
        shouldAnimate = true
    }

    val colorMode = ColorMode.current
//    val switchPalette = colorMode.toSilkPalette().switch

    var checkboxInput by remember { mutableStateOf<HTMLInputElement?>(null) }

    Row(
        CheckboxStyle.toModifier(variant)
            .thenIf(!enabled, DisabledStyle.toModifier())
            .then(size.toModifier())
            .thenIf(spacing != null) { Modifier.setVariable(CheckboxSpacingVar, spacing!!) }
            .thenIf(colorScheme != null) {
                @Suppress("NAME_SHADOWING") val colorScheme = colorScheme!!
                val isDark = colorMode.isDark
                val isBrightColor = (if (isDark) colorScheme._200 else colorScheme._500).isBright
                Modifier
                    .setVariable(CheckboxIconBackgroundColorVar, if (isDark) colorScheme._200 else colorScheme._500)
                    .setVariable(CheckboxIconColorVar, (if (isBrightColor) ColorMode.LIGHT else ColorMode.DARK).toSilkPalette().color)
            }
            .thenIf(borderColor != null) { Modifier.setVariable(CheckboxBorderColorVar, borderColor!!) }
            .thenIf(iconColor != null) { Modifier.setVariable(CheckboxIconColorVar, iconColor!!) }
            .thenIf(focusOutlineColor != null) {
                Modifier.setVariable(
                    CheckboxFocusOutlineColorVar,
                    focusOutlineColor!!
                )
            }
            .then(modifier)
            .thenIf(enabled) { Modifier.onClick { fireOnCheckChanged() } },
        verticalAlignment = Alignment.CenterVertically,
        ref = ref,
    ) {
        // We base Switch on a checkbox input for a11y + built-in input/keyboard support, but hide the checkbox itself
        // and render the switch separately. We do however allow it to be focused, which combined with the outer label
        // means that both clicks and keyboard events will toggle the checkbox.
        Input(
            type = InputType.Checkbox,
            value = checked.toBoolean(),
            onValueChanged = { fireOnCheckChanged() },
            variant = CheckboxInputVariant,
            enabled = enabled,
            ref = ref { checkboxInput = it },
        )

        Box(
            CheckboxIconContainerStyle.toModifier(UncheckedCheckboxIconContainerVariant.takeUnless { checked.toBoolean() }),
            contentAlignment = Alignment.Center
        ) {
            if (checked.toBoolean()) {
                Box(
                    CheckboxIconStyle
                        .toModifier()
                        .thenIf(shouldAnimate) {
                            Modifier.animation(CheckboxEnabledAnim.toAnimation(colorMode, 200.ms))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CheckboxIconScope(
                        indeterminate = checked == CheckedState.Indeterminate,
                        colorMode
                    ).apply { icon() }
                }
            }
        }

        if (content != null) {
            Span {
                content()
            }
        }
    }
}

/**
 * Creates a checkbox.
 *
 * Note that visual control of the switch is fairly limited compared to many other widgets -- you can't directly modify
 * the width or height of the track or the thumb parts. Instead, configure your switch by passing in the relevant
 * parameters.
 *
 * @param checked Whether the switch is currently checked or not.
 * @param onCheckedChange A callback which is invoked when the switch is toggled.
 * @param enabled Whether the switch is enabled or not. If not, the switch will be rendered in a disabled state and will
 *   not be interactable.
 * @param size The size of the switch. Defaults to [CheckboxSize.MD]. You can implement your own [CheckboxSize] if you
 *   want custom sizing.
 * @param spacing An optional spacing parameter to use between the checkbox and any content drawn to the right of it.
 * @param colorScheme An optional color scheme to use for the switch. If not provided, the switch will use the
 *   appropriate colors from the [SilkPalette].
 * @param borderColor An optional override for the border color of the checkbox when unchecked.
 * @param iconColor An optional override for the color of the icon drawn in the checkbox.
 * @param focusOutlineColor An optional override for the border color when the input is focused.
 */
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    enabled: Boolean = CheckboxDefaults.Enabled,
    size: CheckboxSize = CheckboxDefaults.Size,
    spacing: CSSLengthValue? = null,
    colorScheme: ColorScheme? = null,
    borderColor: CSSColorValue? = null,
    iconColor: CSSColorValue? = null,
    focusOutlineColor: CSSColorValue? = null,
    ref: ElementRefScope<HTMLElement>? = null,
    icon: @Composable CheckboxIconScope.() -> Unit = CheckboxDefaults.IconProvider,
    content: (@Composable () -> Unit)? = null,
) {
    TriCheckbox(
        CheckedState.from(checked),
        { onCheckedChange(it.toBoolean()) },
        modifier,
        variant,
        enabled,
        size,
        spacing,
        colorScheme,
        borderColor,
        iconColor,
        focusOutlineColor,
        ref,
        icon,
        content
    )
}

@Composable
fun Checkbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    enabled: Boolean = CheckboxDefaults.Enabled,
    size: CheckboxSize = CheckboxDefaults.Size,
    spacing: CSSLengthValue? = null,
    colorScheme: ColorScheme? = null,
    borderColor: CSSColorValue? = null,
    iconColor: CSSColorValue? = null,
    focusOutlineColor: CSSColorValue? = null,
    ref: ElementRefScope<HTMLElement>? = null,
    icon: (@Composable CheckboxIconScope.() -> Unit) = CheckboxDefaults.IconProvider,
) {
    Checkbox(
        checked,
        onCheckedChange,
        modifier,
        variant,
        enabled,
        size,
        spacing,
        colorScheme,
        borderColor,
        iconColor,
        focusOutlineColor,
        ref,
        icon
    ) { Text(label) }
}

@Composable
fun TriCheckbox(
    label: String,
    checked: CheckedState,
    onCheckedChange: (CheckedState) -> Unit,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    enabled: Boolean = CheckboxDefaults.Enabled,
    size: CheckboxSize = CheckboxDefaults.Size,
    spacing: CSSLengthValue? = null,
    colorScheme: ColorScheme? = null,
    borderColor: CSSColorValue? = null,
    iconColor: CSSColorValue? = null,
    focusOutlineColor: CSSColorValue? = null,
    ref: ElementRefScope<HTMLElement>? = null,
    icon: (@Composable CheckboxIconScope.() -> Unit) = CheckboxDefaults.IconProvider,
) {
    TriCheckbox(
        checked,
        onCheckedChange,
        modifier,
        variant,
        enabled,
        size,
        spacing,
        colorScheme,
        borderColor,
        iconColor,
        focusOutlineColor,
        ref,
        icon
    ) { Text(label) }
}