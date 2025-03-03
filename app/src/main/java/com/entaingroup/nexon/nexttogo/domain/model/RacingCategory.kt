package com.entaingroup.nexon.nexttogo.domain.model

enum class RacingCategory(val id: String) {
    GREYHOUND("9daef0d7-bf3c-4f50-921d-8e818c60fe61"),
    HARNESS("161d9be2-e909-4326-8c2c-35ed71fb460b"),
    HORSE("4a2788f8-e825-4d36-9894-efd4baf1cfae"),
    UNKNOWN(""),
    ;

    companion object {
        fun fromId(id: String): RacingCategory {
            return enumValues<RacingCategory>().find { it.id == id } ?: UNKNOWN
        }
    }
}
