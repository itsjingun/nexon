package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import java.time.Instant
import javax.inject.Inject

internal class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): Instant = Instant.now()
}
