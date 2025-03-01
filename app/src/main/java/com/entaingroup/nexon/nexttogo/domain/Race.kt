package com.entaingroup.nexon.nexttogo.domain

import java.time.Instant

data class Race(
    val id: String,
    val meetingName: String,
    val raceNumber: Int,
    val category: RacingCategory,
    val startTime: Instant,
)
