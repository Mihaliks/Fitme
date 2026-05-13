package com.example.fitme.data.entities.enums

enum class TrainingMode {
    STRENGTH, // 1 - 5 повторений
    HYPERTROPHY, // 6 - 12 повторений
    ENDURANCE, // 13+ повторений
    NONE, // если не указано количество повторений (например для кардио)
    CUSTOM // если нужно свое количество повторений (текстом)
}