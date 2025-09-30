package com.navigine.indoornavigationdemo.domain.model

import android.graphics.PointF

sealed class TapEvent {
    abstract val pointF: PointF

    data class SingleTap(override val pointF: PointF) : TapEvent()
    data class DoubleTap(override val pointF: PointF) : TapEvent()
    data class LongTap(override val pointF: PointF) : TapEvent()
}