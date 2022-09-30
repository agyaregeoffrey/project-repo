package com.dev.gka.plagiarismchecker.models

data class Match(
    val plagiarismContext: PlagiarismContext?,
    val inputEnd: Int,
    val inputStart: Int,
    val matchText: String,
    val score: Double
)