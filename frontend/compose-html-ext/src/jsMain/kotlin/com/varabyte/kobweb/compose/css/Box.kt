package com.varabyte.kobweb.compose.css

import org.jetbrains.compose.web.css.*

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/box-decoration-break
class BoxDecorationBreak private constructor(private val value: String) : StylePropertyValue {
    override fun toString() = value

    companion object {
        // Keyword
        val Slice get() = BoxDecorationBreak("slice")
        val Clone get() = BoxDecorationBreak("clone")

        // Global
        val Inherit get() = BoxDecorationBreak("inherit")
        val Initial get() = BoxDecorationBreak("initial")
        val Revert get() = BoxDecorationBreak("revert")
        val Unset get() = BoxDecorationBreak("unset")
    }
}

fun StyleScope.boxDecorationBreak(boxDecorationBreak: BoxDecorationBreak) {
    property("box-decoration-break", boxDecorationBreak)
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/box-sizing
class BoxSizing private constructor(private val value: String) : StylePropertyValue {
    override fun toString() = value

    companion object {
        // Keyword
        val BorderBox get() = BoxSizing("border-box")
        val ContentBox get() = BoxSizing("content-box")

        // Global
        val Inherit get() = BoxSizing("inherit")
        val Initial get() = BoxSizing("initial")
        val Revert get() = BoxSizing("revert")
        val Unset get() = BoxSizing("unset")
    }
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/box-sizing
fun StyleScope.boxSizing(boxSizing: BoxSizing) {
    boxSizing(boxSizing.toString())
}

// See: https://developer.mozilla.org/en-US/docs/Web/CSS/box-shadow
fun StyleScope.boxShadow(value: String) {
    property("box-shadow", value)
}

fun StyleScope.boxShadow(
    offsetX: CSSLengthNumericValue = 0.px,
    offsetY: CSSLengthNumericValue = 0.px,
    blurRadius: CSSLengthNumericValue? = null,
    spreadRadius: CSSLengthNumericValue? = null,
    color: CSSColorValue? = null,
    inset: Boolean = false,
) {
    boxShadow(
        CSSBoxShadow(
            offsetX = offsetX,
            offsetY = offsetY,
            blurRadius = blurRadius,
            spreadRadius = spreadRadius,
            color = color,
            inset = inset,
        ),
    )
}

fun StyleScope.boxShadow(vararg shadows: CSSBoxShadow) {
    boxShadow(shadows.toList())
}

fun StyleScope.boxShadow(shadows: List<CSSBoxShadow>) {
    boxShadow(value = shadows.joinToString(transform = CSSBoxShadow::toString))
}

/**
 * @property offsetX Specifies the **horizontal offset** of the shadow.
 * A positive value draws a shadow that is offset to the right
 * of the box, a negative length to the left.
 * @property offsetY Specifies the **vertical offset** of the shadow.
 * A positive value offsets the shadow down, a negative one up.
 * @property blurRadius Specifies the **blur radius**. Negative values are
 * invalid. If the blur value is zero, the shadow’s edge is sharp.
 * Otherwise, the larger the value, the more the shadow’s edge is blurred.
 * See [Shadow Blurring](https://www.w3.org/TR/css-backgrounds-3/#shadow-blur).
 * @property spreadRadius Specifies the **spread distance**. Positive values
 * cause the shadow to expand in all directions by the specified radius.
 * Negative values cause the shadow to contract.
 * See [Shadow Shape](https://www.w3.org/TR/css-backgrounds-3/#shadow-shape).
 * @property color Specifies the color of the shadow. If the color is absent,
 * it defaults to `currentColor` on CSS.
 * @property inset If `true`, the `inset` keyword is inserted at the end and
 * changes the drop shadow from an outer box-shadow (one that shadows the box
 * onto the canvas, as if it were lifted above the canvas) to an inner
 * box-shadow (one that shadows the canvas onto the box, as if the box were
 * cut out of the canvas and shifted behind it).
 */
data class CSSBoxShadow(
    val offsetX: CSSLengthNumericValue = 0.px,
    val offsetY: CSSLengthNumericValue = 0.px,
    val blurRadius: CSSLengthNumericValue? = null,
    val spreadRadius: CSSLengthNumericValue? = null,
    val color: CSSColorValue? = null,
    val inset: Boolean = false,
) : CSSStyleValue {
    companion object {
        val None = "none".unsafeCast<CSSBoxShadow>()
    }

    override fun toString(): String = buildString {
        if (inset) {
            append("inset")
            append(' ')
        }
        append(offsetX)
        append(' ')
        append(offsetY)

        if (blurRadius != null) {
            append(' ')
            append(blurRadius)
        }

        if (spreadRadius != null) {
            if (blurRadius == null) {
                append(' ')
                append('0')
            }
            append(' ')
            append(spreadRadius)
        }

        if (color != null) {
            append(' ')
            append(color)
        }
    }
}
