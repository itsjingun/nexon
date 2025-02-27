package com.entaingroup.nexon.nexttogo.domain

import java.time.Instant

data class Race(
    val id: String,
    val name: String,
    val number: Int,
    val startTime: Instant,
)
