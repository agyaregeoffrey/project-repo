package com.dev.gka.plagiarismchecker.models

data class Source(
    val matches: List<Match>,
    val title: String,
    val url: String
)