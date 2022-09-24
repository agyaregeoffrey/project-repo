package com.dev.gka.plagiarismchecker.models

data class PlagiarismResponse(
    val citations: List<Citation>?,
    val percentPlagiarism: Int?,
    val sources: List<Source>?
)