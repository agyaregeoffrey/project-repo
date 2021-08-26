package com.activeminds.projectrepo.uploadproject

import android.net.Uri
import com.google.android.gms.tasks.Task

data class FacultyInfo (private val faculty: String) {
    override fun toString(): String {
        return faculty
    }
}

data class ProjectInfo(var faculty: String, var projectTitle: String,
                       var fileUrl: String, var projectYear: String,
                       var email: String, var indexNumber: String) {

    override fun toString(): String {
        return fileUrl
    }
}