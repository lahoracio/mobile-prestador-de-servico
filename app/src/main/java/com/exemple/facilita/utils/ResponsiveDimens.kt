package com.exemple.facilita.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sistema de dimensões responsivas para manter consistência em diferentes tamanhos de tela
 */
object ResponsiveDimens {
    // Dimensões base (calculadas para uma tela de referência de 360dp de largura)
    private const val BASE_WIDTH = 360f
    private const val BASE_HEIGHT = 800f

    @Composable
    fun getScreenWidth(): Int {
        return LocalConfiguration.current.screenWidthDp
    }

    @Composable
    fun getScreenHeight(): Int {
        return LocalConfiguration.current.screenHeightDp
    }

    @Composable
    fun getWidthScale(): Float {
        val screenWidth = getScreenWidth()
        return screenWidth / BASE_WIDTH
    }

    @Composable
    fun getHeightScale(): Float {
        val screenHeight = getScreenHeight()
        return screenHeight / BASE_HEIGHT
    }

    @Composable
    fun getMinScale(): Float {
        return minOf(getWidthScale(), getHeightScale())
    }

    /**
     * Converte um valor dp base para um valor responsivo baseado na largura da tela
     */
    @Composable
    fun width(baseDp: Float): Dp {
        return (baseDp * getWidthScale()).dp
    }

    /**
     * Converte um valor dp base para um valor responsivo baseado na altura da tela
     */
    @Composable
    fun height(baseDp: Float): Dp {
        return (baseDp * getHeightScale()).dp
    }

    /**
     * Converte um valor dp base para um valor responsivo usando a menor escala (mantém proporções)
     */
    @Composable
    fun size(baseDp: Float): Dp {
        return (baseDp * getMinScale()).dp
    }

    /**
     * Converte um valor sp base para um valor responsivo de texto
     */
    @Composable
    fun text(baseSp: Float): TextUnit {
        return (baseSp * getMinScale()).sp
    }

    // Dimensões padrão responsivas
    @Composable
    fun paddingSmall(): Dp = size(8f)

    @Composable
    fun paddingMedium(): Dp = size(16f)

    @Composable
    fun paddingLarge(): Dp = size(24f)

    @Composable
    fun paddingExtraLarge(): Dp = size(32f)

    @Composable
    fun cornerRadiusSmall(): Dp = size(8f)

    @Composable
    fun cornerRadiusMedium(): Dp = size(16f)

    @Composable
    fun cornerRadiusLarge(): Dp = size(24f)

    @Composable
    fun iconSizeSmall(): Dp = size(20f)

    @Composable
    fun iconSizeMedium(): Dp = size(24f)

    @Composable
    fun iconSizeLarge(): Dp = size(48f)

    @Composable
    fun iconSizeExtraLarge(): Dp = size(72f)

    @Composable
    fun buttonHeight(): Dp = height(48f)

    @Composable
    fun textFieldHeight(): Dp = height(56f)

    @Composable
    fun cardElevationSmall(): Dp = size(2f)

    @Composable
    fun cardElevationMedium(): Dp = size(4f)

    @Composable
    fun cardElevationLarge(): Dp = size(8f)
}

/**
 * Extension functions para facilitar o uso
 */
@Composable
fun Int.sdp(): Dp = ResponsiveDimens.size(this.toFloat())

@Composable
fun Float.sdp(): Dp = ResponsiveDimens.size(this)

@Composable
fun Int.ssp(): TextUnit = ResponsiveDimens.text(this.toFloat())

@Composable
fun Float.ssp(): TextUnit = ResponsiveDimens.text(this)

@Composable
fun Int.wdp(): Dp = ResponsiveDimens.width(this.toFloat())

@Composable
fun Float.wdp(): Dp = ResponsiveDimens.width(this)

@Composable
fun Int.hdp(): Dp = ResponsiveDimens.height(this.toFloat())

@Composable
fun Float.hdp(): Dp = ResponsiveDimens.height(this)

