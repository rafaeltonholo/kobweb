package com.varabyte.kobweb.silk.init

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.lightened
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.disclosure.TabVars
import com.varabyte.kobweb.silk.components.disclosure.TabsPanelStyle
import com.varabyte.kobweb.silk.components.disclosure.TabsStyle
import com.varabyte.kobweb.silk.components.disclosure.TabsTabRowStyle
import com.varabyte.kobweb.silk.components.disclosure.TabsTabStyle
import com.varabyte.kobweb.silk.components.forms.ButtonColorScheme
import com.varabyte.kobweb.silk.components.forms.ButtonSize
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.forms.CheckboxEnabledAnim
import com.varabyte.kobweb.silk.components.forms.CheckboxIconContainerStyle
import com.varabyte.kobweb.silk.components.forms.CheckboxIconStyle
import com.varabyte.kobweb.silk.components.forms.CheckboxInputVariant
import com.varabyte.kobweb.silk.components.forms.CheckboxSize
import com.varabyte.kobweb.silk.components.forms.CheckboxStyle
import com.varabyte.kobweb.silk.components.forms.CheckboxVars
import com.varabyte.kobweb.silk.components.forms.CheckedCheckboxIconContainerVariant
import com.varabyte.kobweb.silk.components.forms.FilledInputVariant
import com.varabyte.kobweb.silk.components.forms.FlushedInputVariant
import com.varabyte.kobweb.silk.components.forms.InputGroupStyle
import com.varabyte.kobweb.silk.components.forms.InputSize
import com.varabyte.kobweb.silk.components.forms.InputStyle
import com.varabyte.kobweb.silk.components.forms.InputVars
import com.varabyte.kobweb.silk.components.forms.OutlinedInputVariant
import com.varabyte.kobweb.silk.components.forms.SwitchInputVariant
import com.varabyte.kobweb.silk.components.forms.SwitchSize
import com.varabyte.kobweb.silk.components.forms.SwitchStyle
import com.varabyte.kobweb.silk.components.forms.SwitchThumbStyle
import com.varabyte.kobweb.silk.components.forms.SwitchTrackStyle
import com.varabyte.kobweb.silk.components.forms.SwitchVars
import com.varabyte.kobweb.silk.components.forms.UncheckedCheckboxIconContainerVariant
import com.varabyte.kobweb.silk.components.forms.UnstyledInputVariant
import com.varabyte.kobweb.silk.components.graphics.CanvasStyle
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.layout.SurfaceStyle
import com.varabyte.kobweb.silk.components.layout.VerticalDividerStyle
import com.varabyte.kobweb.silk.components.overlay.BottomLeftTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.BottomRightTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.BottomTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.LeftBottomTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.LeftTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.LeftTopTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.OverlayStyle
import com.varabyte.kobweb.silk.components.overlay.OverlayVars
import com.varabyte.kobweb.silk.components.overlay.PopupStyle
import com.varabyte.kobweb.silk.components.overlay.RightBottomTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.RightTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.RightTopTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.TooltipArrowStyle
import com.varabyte.kobweb.silk.components.overlay.TooltipStyle
import com.varabyte.kobweb.silk.components.overlay.TooltipTextContainerStyle
import com.varabyte.kobweb.silk.components.overlay.TooltipVars
import com.varabyte.kobweb.silk.components.overlay.TopLeftTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.TopRightTooltipArrowVariant
import com.varabyte.kobweb.silk.components.overlay.TopTooltipArrowVariant
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.common.DisabledStyle
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.vars.color.BackgroundColorVar
import com.varabyte.kobweb.silk.style.vars.color.BorderColorVar
import com.varabyte.kobweb.silk.style.vars.color.ColorVar
import com.varabyte.kobweb.silk.style.vars.color.FocusOutlineColorVar
import com.varabyte.kobweb.silk.style.vars.color.PlaceholderColorVar
import com.varabyte.kobweb.silk.theme.SilkTheme
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.ColorSchemes
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.border
import com.varabyte.kobweb.silk.theme.colors.palette.button
import com.varabyte.kobweb.silk.theme.colors.palette.checkbox
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.focusOutline
import com.varabyte.kobweb.silk.theme.colors.palette.input
import com.varabyte.kobweb.silk.theme.colors.palette.overlay
import com.varabyte.kobweb.silk.theme.colors.palette.placeholder
import com.varabyte.kobweb.silk.theme.colors.palette.switch
import com.varabyte.kobweb.silk.theme.colors.palette.tab
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobweb.silk.theme.colors.palette.tooltip
import com.varabyte.kobweb.silk.theme.colors.suffixedWith
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement

fun initSilkWidgets(ctx: InitSilkContext) {
    val mutableTheme = ctx.theme

    ctx.theme.palettes.apply {
        val focusOutline = ColorSchemes.Blue._500.toRgb().copyf(alpha = 0.5f)
        val placeholder = ColorSchemes.Gray._500

        run { // init light palette
            val color = Colors.Black
            light.background = Colors.White
            light.color = color
            light.border = color.copyf(alpha = 0.2f)
            light.focusOutline = focusOutline
            light.overlay = color.copyf(alpha = 0.5f)
            light.placeholder = placeholder

            val buttonBase = Colors.White.darkened(byPercent = 0.2f)
            light.button.set(
                default = buttonBase,
                hover = buttonBase.darkened(byPercent = 0.2f),
                focus = Colors.CornflowerBlue,
                pressed = buttonBase.darkened(byPercent = 0.4f),
            )

            light.checkbox.set(
                background = ColorSchemes.Blue._500,
                hover = ColorSchemes.Blue._600,
                color = Colors.White,
            )

            val inputFilled = ColorSchemes.Gray._200
            light.input.set(
                filled = inputFilled,
                filledFocus = Colors.Transparent,
                hoveredBorder = ColorSchemes.Gray._500,
                invalidBorder = ColorSchemes.Red._900,
                filledHover = inputFilled.darkened(0.1f),
            )

            light.switch.set(
                thumb = Colors.White,
                backgroundOn = Colors.DodgerBlue,
                backgroundOff = Colors.LightGray,
            )

            light.tab.set(
                color = Colors.Black,
                background = Colors.White,
                selectedColor = Colors.CornflowerBlue,
                hover = Colors.LightGray,
                pressed = Colors.WhiteSmoke,
                disabled = Colors.White,
            )

            light.tooltip.set(
                // Intentionally inverted from main colors, for contrast
                background = light.color,
                color = light.background,
            )
        }

        run { // init dark palette
            val color = Colors.White
            dark.background = Colors.Black
            dark.color = color
            dark.border = color.copyf(alpha = 0.2f)
            dark.focusOutline = focusOutline
            dark.overlay = color.copyf(alpha = 0.5f)
            dark.placeholder = placeholder

            val buttonBase = Colors.Black.lightened(byPercent = 0.2f)
            dark.button.set(
                default = buttonBase,
                hover = buttonBase.lightened(byPercent = 0.2f),
                focus = Colors.LightSkyBlue,
                pressed = buttonBase.lightened(byPercent = 0.4f),
            )

            dark.checkbox.set(
                background = ColorSchemes.Blue._200,
                hover = ColorSchemes.Blue._300,
                color = Colors.Black,
            )

            val inputFilled = ColorSchemes.Gray._900
            dark.input.set(
                filled = inputFilled,
                filledFocus = Colors.Transparent,
                hoveredBorder = ColorSchemes.Gray._600,
                invalidBorder = ColorSchemes.Red._300,
                filledHover = inputFilled.lightened(0.1f),
            )

            dark.switch.set(
                thumb = Colors.White,
                backgroundOn = Colors.LightSkyBlue,
                backgroundOff = Colors.DarkGray,
            )

            dark.tab.set(
                color = Colors.White,
                background = Colors.Black,
                selectedColor = Colors.LightSkyBlue,
                hover = Colors.DarkSlateGray,
                pressed = Colors.DarkGray,
                disabled = Colors.Black,
            )

            dark.tooltip.set(
                // Intentionally inverted from main colors, for contrast
                background = dark.color,
                color = dark.background,
            )
        }
    }

    mutableTheme.registerStyle("silk-colors", SilkColorsStyle)

    // Register InputStyle early as it's wrapped by other components
    mutableTheme.registerStyle("silk-input", InputStyle)
    mutableTheme.registerVariant("-outlined", OutlinedInputVariant)
    mutableTheme.registerVariant("-filled", FilledInputVariant)
    mutableTheme.registerVariant("-flushed", FlushedInputVariant)
    mutableTheme.registerVariant("-unstyled", UnstyledInputVariant)
    mutableTheme.registerStyle("silk-input-group", InputGroupStyle)

    // TODO: Automate the creation of this list (by refactoring KSP processor into something we can use?)
    mutableTheme.registerStyle("silk-disabled", DisabledStyle)
    mutableTheme.registerStyle("silk-smooth-color", SmoothColorStyle)

    mutableTheme.registerStyle("silk-button", ButtonStyle)
    mutableTheme.registerStyle("silk-canvas", CanvasStyle)
    mutableTheme.registerStyle("silk-checkbox", CheckboxStyle)
    mutableTheme.registerVariant("-checkbox", CheckboxInputVariant)
    mutableTheme.registerStyle("silk-checkbox-icon-container", CheckboxIconContainerStyle)
    mutableTheme.registerStyle("silk-checkbox-icon", CheckboxIconStyle)
    mutableTheme.registerVariant("-checked", CheckedCheckboxIconContainerVariant)
    mutableTheme.registerVariant("-unchecked", UncheckedCheckboxIconContainerVariant)
    mutableTheme.registerStyle("silk-overlay", OverlayStyle)
    mutableTheme.registerStyle("silk-popup", PopupStyle)
    mutableTheme.registerStyle("silk-simple-grid", SimpleGridStyle)
    mutableTheme.registerStyle("silk-surface", SurfaceStyle)

    mutableTheme.registerStyle("silk-horizontal-divider", HorizontalDividerStyle)
    mutableTheme.registerStyle("silk-vertical-divider", VerticalDividerStyle)

    mutableTheme.registerStyle("silk-switch", SwitchStyle)
    mutableTheme.registerStyle("silk-switch-track", SwitchTrackStyle)
    mutableTheme.registerStyle("silk-switch-thumb", SwitchThumbStyle)
    mutableTheme.registerVariant("-switch", SwitchInputVariant)

    mutableTheme.registerStyle("silk-tabs", TabsStyle)
    mutableTheme.registerStyle("silk-tabs-tab-row", TabsTabRowStyle)
    mutableTheme.registerStyle("silk-tabs-tab", TabsTabStyle)
    mutableTheme.registerStyle("silk-tabs-panel", TabsPanelStyle)

    mutableTheme.registerStyle("silk-tooltip-arrow", TooltipArrowStyle)
    mutableTheme.registerVariant("-top-left", TopLeftTooltipArrowVariant)
    mutableTheme.registerVariant("-top", TopTooltipArrowVariant)
    mutableTheme.registerVariant("-top-right", TopRightTooltipArrowVariant)
    mutableTheme.registerVariant("-left-top", LeftTopTooltipArrowVariant)
    mutableTheme.registerVariant("-left", LeftTooltipArrowVariant)
    mutableTheme.registerVariant("-left-bottom", LeftBottomTooltipArrowVariant)
    mutableTheme.registerVariant("-right-top", RightTopTooltipArrowVariant)
    mutableTheme.registerVariant("-right", RightTooltipArrowVariant)
    mutableTheme.registerVariant("-right-bottom", RightBottomTooltipArrowVariant)
    mutableTheme.registerVariant("-bottom-left", BottomLeftTooltipArrowVariant)
    mutableTheme.registerVariant("-bottom", BottomTooltipArrowVariant)
    mutableTheme.registerVariant("-bottom-right", BottomRightTooltipArrowVariant)

    mutableTheme.registerStyle("silk-tooltip", TooltipStyle)
    mutableTheme.registerStyle("silk-tooltip-text", TooltipTextContainerStyle)

    mutableTheme.registerKeyframes("silk-checkbox-enabled", CheckboxEnabledAnim)

    // TODO (double): we definitely want to automate this
    //  Note: Be sure to add @CssPrefix to the relevant objects (or account for the prefix some other way)
    mutableTheme.registerStyle("silk-button-size_xs", ButtonSize.XS)
    mutableTheme.registerStyle("silk-button-size_sm", ButtonSize.SM)
    mutableTheme.registerStyle("silk-button-size_md", ButtonSize.MD)
    mutableTheme.registerStyle("silk-button-size_lg", ButtonSize.LG)
    mutableTheme.registerStyle("silk-checkbox-size_sm", CheckboxSize.SM)
    mutableTheme.registerStyle("silk-checkbox-size_md", CheckboxSize.MD)
    mutableTheme.registerStyle("silk-checkbox-size_lg", CheckboxSize.LG)
    mutableTheme.registerStyle("silk-input-size_xs", InputSize.XS)
    mutableTheme.registerStyle("silk-input-size_sm", InputSize.SM)
    mutableTheme.registerStyle("silk-input-size_md", InputSize.MD)
    mutableTheme.registerStyle("silk-input-size_lg", InputSize.LG)
    mutableTheme.registerStyle("silk-switch-size_sm", SwitchSize.SM)
    mutableTheme.registerStyle("silk-switch-size_md", SwitchSize.MD)
    mutableTheme.registerStyle("silk-switch-size_lg", SwitchSize.LG)

    mutableTheme.registerStyle("silk-button-color-scheme_red", ButtonColorScheme.Red)
    mutableTheme.registerStyle("silk-button-color-scheme_pink", ButtonColorScheme.Pink)
    mutableTheme.registerStyle("silk-button-color-scheme_purple", ButtonColorScheme.Purple)
    mutableTheme.registerStyle("silk-button-color-scheme_deep-purple", ButtonColorScheme.DeepPurple)
    mutableTheme.registerStyle("silk-button-color-scheme_indigo", ButtonColorScheme.Indigo)
    mutableTheme.registerStyle("silk-button-color-scheme_blue", ButtonColorScheme.Blue)
    mutableTheme.registerStyle("silk-button-color-scheme_light-blue", ButtonColorScheme.LightBlue)
    mutableTheme.registerStyle("silk-button-color-scheme_cyan", ButtonColorScheme.Cyan)
    mutableTheme.registerStyle("silk-button-color-scheme_teal", ButtonColorScheme.Teal)
    mutableTheme.registerStyle("silk-button-color-scheme_green", ButtonColorScheme.Green)
    mutableTheme.registerStyle("silk-button-color-scheme_light-green", ButtonColorScheme.LightGreen)
    mutableTheme.registerStyle("silk-button-color-scheme_lime", ButtonColorScheme.Lime)
    mutableTheme.registerStyle("silk-button-color-scheme_yellow", ButtonColorScheme.Yellow)
    mutableTheme.registerStyle("silk-button-color-scheme_amber", ButtonColorScheme.Amber)
    mutableTheme.registerStyle("silk-button-color-scheme_orange", ButtonColorScheme.Orange)
    mutableTheme.registerStyle("silk-button-color-scheme_deep-orange", ButtonColorScheme.DeepOrange)
    mutableTheme.registerStyle("silk-button-color-scheme_brown", ButtonColorScheme.Brown)
    mutableTheme.registerStyle("silk-button-color-scheme_gray", ButtonColorScheme.Gray)
    mutableTheme.registerStyle("silk-button-color-scheme_blue-gray", ButtonColorScheme.BlueGray)
}

val SilkColorsStyle = CssStyle.base {
    val palette = colorMode.toPalette()
    Modifier
        // region General color vars
        .setVariable(BackgroundColorVar, palette.background)
        .setVariable(ColorVar, palette.color)
        .setVariable(BorderColorVar, palette.border)
        .setVariable(FocusOutlineColorVar, palette.focusOutline)
        .setVariable(PlaceholderColorVar, palette.placeholder)
        // endregion

        // region Widget color vars
        .setVariable(ButtonVars.BackgroundDefaultColor, palette.button.default)
        .setVariable(ButtonVars.BackgroundHoverColor, palette.button.hover)
        .setVariable(ButtonVars.BackgroundPressedColor, palette.button.pressed)

        .setVariable(CheckboxVars.IconBackgroundColor, palette.checkbox.background)
        .setVariable(CheckboxVars.IconBackgroundHoverColor, palette.checkbox.hover)
        .setVariable(CheckboxVars.IconColor, palette.checkbox.color)

        .setVariable(InputVars.BorderHoverColor, palette.input.hoveredBorder)
        .setVariable(InputVars.BorderInvalidColor, palette.input.invalidBorder)
        .setVariable(InputVars.FilledColor, palette.input.filled)
        .setVariable(InputVars.FilledHoverColor, palette.input.filledHover)
        .setVariable(InputVars.FilledFocusColor, palette.input.filledFocus)

        .setVariable(OverlayVars.BackgroundColor, palette.overlay)

        .setVariable(SwitchVars.ThumbColor, palette.switch.thumb)

        .setVariable(TabVars.Color, palette.tab.color)
        .setVariable(TabVars.BackgroundColor, palette.tab.background)
        .setVariable(TabVars.DisabledBackgroundColor, palette.tab.disabled)
        .setVariable(TabVars.HoverBackgroundColor, palette.tab.hover)
        .setVariable(TabVars.PressedBackgroundColor, palette.tab.pressed)

        .setVariable(TooltipVars.BackgroundColor, palette.tooltip.background)
        .setVariable(TooltipVars.Color, palette.tooltip.color)
    // endregion
}

@Composable
fun Document.setSilkWidgetVariables() {
    val root = remember { this.getElementById("root") as HTMLElement }
    root.setSilkWidgetVariables()
}

@Composable
fun HTMLElement.setSilkWidgetVariables() {
    setSilkWidgetVariables(ColorMode.current)
}

fun HTMLElement.setSilkWidgetVariables(colorMode: ColorMode) {
    SilkTheme.nameFor(SilkColorsStyle).let { silkColorsStyleName ->
        removeClass(silkColorsStyleName.suffixedWith(colorMode.opposite))
        addClass(silkColorsStyleName.suffixedWith(colorMode))
    }
}
