package com.level.g_fit

import java.util.concurrent.TimeUnit

data class Days(
    var day: String,
    var steps: String,
    var duration: TimeUnit,
    var hp : Float,
    var speed : Float,
    var distance: Int,
    var calories: Float
)
