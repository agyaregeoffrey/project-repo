package com.dev.gka.plagiarismchecker.models

data class PlagiarismRequestBody(
    val includeCitations: Boolean = false,
    val language: String,
    val scrapeSources: Boolean = false,
    val text: String
)