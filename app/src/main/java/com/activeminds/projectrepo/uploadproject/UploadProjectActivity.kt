package com.activeminds.projectrepo.uploadproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.activedev.projecttocloud.*
import com.activeminds.projectrepo.R
import com.activeminds.projectrepo.widgets.WaitingDialog
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.content_upload_project.*
import java.io.File
import java.util.regex.Pattern

class UploadProjectActivity : AppCompatActivity() {

    private var fileName: String = ""
    private var fileNameWithoutExtension: String = ""
    private var mView: View? = null

    private lateinit var mUri: Uri
    private lateinit var mFirestoreDb: FirebaseFirestore
    private lateinit var mStorageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_project)
        setSupportActionBar(findViewById(R.id.upload_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mView = upload_project_root

        mFirestoreDb = FirebaseFirestore.getInstance()
        mStorageReference = FirebaseStorage.getInstance().reference

        val spinnerAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, DataManager.faculties.toList())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFaculties.adapter = spinnerAdapter

        buttonSubmit.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.upload_caution))
                    .setMessage(getString(R.string.message_disclaimer))
                    .setPositiveButton(getString(R.string.upload_upload)) { _, _ ->
                        uploadProjectToFirebase()
                    }
                    .setNegativeButton(getString(R.string.upload_review), null)
                    .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.upload_project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_select_pdf -> {
                selectPdfFile()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_CODE) {
            mUri = data!!.data!!
            getFileName(data)
            buttonSubmit.isEnabled = true
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        val spinnerSelection = getSpinnerSelection(spinnerFaculties.selectedItem.toString())
        val projectTitle: String = textProjectTitle.editText?.text.toString().trim()
        val indexNumber: String = textIndexNumber.editText?.text.toString().trim()
        val projectYear: String = textProjectYear.editText?.text.toString().trim()

        bundle.putString(SPINNER_SELECTION, spinnerSelection)
        bundle.putString(PROJECT_TITLE, projectTitle)
        bundle.putString(INDEX_NUMBER, indexNumber)
        bundle.putString(PROJECT_YEAR, projectYear)
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        super.onRestoreInstanceState(bundle)
        textProjectTitle.editText?.setText(bundle.getString(PROJECT_TITLE))
        textIndexNumber.editText?.setText(bundle.getString(INDEX_NUMBER))
        textProjectYear.editText?.setText(bundle.getString(PROJECT_YEAR))
    }

    private fun selectPdfFile() {
        val intent = Intent()
        intent.type = FILE_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_select_pdf)), RESULT_CODE)
    }

    private fun uploadProjectToFirebase() {
        val spinnerSelection = getSpinnerSelection(spinnerFaculties.selectedItem.toString())
        val collectionPath = assignFacultyInitial(spinnerSelection)

        val projectTitle: String = textProjectTitle.editText?.text.toString().trim()
        var indexNumber: String = textIndexNumber.editText?.text.toString().trim()
        var projectYear: String = textProjectYear.editText?.text.toString().trim()

        if (TextUtils.isEmpty(textProjectTitle.editText?.text.toString().trim())) {
            textProjectTitle.editText?.error = getString(R.string.title_cannot_empty)
        }
        if ((!validateIndexNumber(indexNumber))) {
            textIndexNumber.editText?.error = getString(R.string.invalid_index_number)
            textIndexNumber.editText?.setText("")
            indexNumber = ""
        }
        if (projectYear.length != 4) {
            textProjectYear.editText?.error = getString(R.string.invalid_date)
            textProjectYear.editText?.setText("")
            projectYear = ""
        }

        if (projectTitle.isNullOrBlank() || indexNumber.isNullOrBlank() || projectYear.isNullOrBlank()) {
            showSnackBar(getString(R.string.all_fields_required))
        } else {
            val storageReference: StorageReference = mStorageReference.child("$FB_STORAGE_PATH/$fileName")
            storageReference.putFile(mUri)
                    .addOnSuccessListener { taskSnapshot ->
                        // insert download url to fb
                        val uri: Task<Uri> = taskSnapshot.storage.downloadUrl
                        while (!uri.isComplete);
                        val url: Uri? = uri.result

                        val accountDetails: String? = FirebaseAuth.getInstance().currentUser?.email

                        val projectInfo = accountDetails?.let { email ->
                            ProjectInfo(spinnerSelection, projectTitle,
                                    url.toString(), projectYear, email, indexNumber)
                        }
                        if (projectInfo != null) {
                            mFirestoreDb.collection(PROJECTS)
                                    .document(FACULTIES)
                                    .collection(collectionPath)
                                    .add(projectInfo)
                        }

                        showProgress(false)
                        showSnackBar("File Uploaded")
                        resetFields()
                    }
                    .addOnProgressListener {
                        showProgress(true)
                    }
        }

    }


    // helpers
    companion object {
        const val RESULT_CODE: Int = 1
        const val FILE_TYPE = "application/pdf"
        const val SPINNER_SELECTION = "spinnerSelection"
        const val PROJECT_TITLE = "projectTitle"
        const val INDEX_NUMBER = "indexNumber"
        const val PROJECT_YEAR = "year"
        const val FB_STORAGE_PATH = "projectUploads"

    }

    private fun validateIndexNumber (indexNumber: String): Boolean {
        val pattern = Pattern.compile("^[0-9]{2}/[0-9]{4}/[0-9]{4}D$")
        val matcher = pattern.matcher(indexNumber)

        return matcher.matches()
    }

    private fun getSpinnerSelection(faculty: String): String {
        var selectedFaculty = ""

        if (faculty == getString(R.string.faculty_engine))
            selectedFaculty = faculty

        if (faculty == getString(R.string.faculty_fast))
            selectedFaculty = faculty

        if (faculty == getString(R.string.faculty_fbms))
            selectedFaculty = faculty

        if (faculty == getString(R.string.faculty_fbne))
            selectedFaculty = faculty

        if (faculty == getString(R.string.faculty_fhas))
            selectedFaculty = faculty

        return selectedFaculty
    }

    private fun assignFacultyInitial(word: String): String {
        var selectedFaculty = ""
        if (word == getString(R.string.faculty_engine))
            selectedFaculty = FACULTY_ENGINE
        if (word == getString(R.string.faculty_fast))
            selectedFaculty = FACULTY_FAST
        if (word == getString(R.string.faculty_fbms))
            selectedFaculty = FACULTY_FBMS
        if (word == getString(R.string.faculty_fbne))
            selectedFaculty = FACULTY_FBNE
        if (word == getString(R.string.faculty_fhas))
            selectedFaculty = FACULTY_FHAS

        return selectedFaculty
    }

    private fun getFileName(returnIntent: Intent) {
        returnIntent.data?.let { uri ->
            contentResolver.query(uri, null, null, null, null)
        }?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            textViewFileStatus.text = "${cursor.getString(index)}"
            fileName = "${cursor.getString(index)}"
            fileNameWithoutExtension = File(fileName).nameWithoutExtension
        }
    }

    private fun resetFields() {
        textProjectTitle.editText?.setText("")
        textIndexNumber.editText?.setText("")
        textProjectYear.editText?.setText("");
        textViewFileStatus.text = getString(R.string.file_status)
        buttonSubmit.isEnabled = false
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            WaitingDialog.show(this);
        } else {
            WaitingDialog.dismiss();
        }
    }

    private fun showSnackBar(message: String) {
        mView?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                    .show()
        }
    }
}