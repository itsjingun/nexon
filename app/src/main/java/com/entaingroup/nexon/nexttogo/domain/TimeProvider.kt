package com.entaingroup.nexon.nexttogo.domain

import java.time.Instant

interface TimeProvider {
    fun now(): Instant
}
