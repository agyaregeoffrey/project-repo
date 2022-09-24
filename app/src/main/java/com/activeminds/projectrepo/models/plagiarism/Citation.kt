package com.dev.gka.plagiarismchecker.models

data class Citation(
    val matchedContentEndIndex: Int,
    val matchedContentStartIndex: Int,
    val score: Int,
    val sentenceEndIndex: Int,
    val title: String,
    val url: String
)