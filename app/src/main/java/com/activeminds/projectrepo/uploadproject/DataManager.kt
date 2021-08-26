package com.activeminds.projectrepo.uploadproject

object DataManager {
    val faculties = ArrayList<FacultyInfo>()

    init {
        initializeFaculties()
    }
    private fun initializeFaculties () {
        var faculty = FacultyInfo("Applied Science/Technology")
        faculties.add(faculty)

        faculty = FacultyInfo("Business/Management Studies")
        faculties.add(faculty)

        faculty = FacultyInfo("Engineering")
        faculties.add(faculty)

        faculty = FacultyInfo("Built/Natural Environment")
        faculties.add(faculty)

        faculty = FacultyInfo("Health/Allied Sciences")
        faculties.add(faculty)

    }
}