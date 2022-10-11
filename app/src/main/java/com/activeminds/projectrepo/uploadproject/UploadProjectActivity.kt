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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import com.activedev.projecttocloud.*
import com.activeminds.projectrepo.R
import com.activeminds.projectrepo.databinding.ActivityUploadProjectBinding
import com.activeminds.projectrepo.utils.gone
import com.activeminds.projectrepo.utils.showSnack
import com.activeminds.projectrepo.utils.viewBinding
import com.activeminds.projectrepo.utils.visible
import com.activeminds.projectrepo.widgets.WaitingDialog
import com.dev.gka.plagiarismchecker.models.PlagiarismRequestBody
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import timber.log.Timber
import java.io.File
import java.util.regex.Pattern

class UploadProjectActivity : AppCompatActivity() {

    private val binding: ActivityUploadProjectBinding by viewBinding(ActivityUploadProjectBinding::inflate)
    private val viewModel: UploadActivityViewModel by viewModels()

    private var fileName: String = ""
    private var fileNameWithoutExtension: String = ""
    private var fileBytes: ByteArray? = null
    private var extractedText = ""

    private lateinit var mUri: Uri
    private lateinit var mFirestoreDb: FirebaseFirestore
    private lateinit var mStorageReference: StorageReference

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mUri = result.data!!.data!!
                getFileName(result.data!!)

                // Get selected PDF file URI as bytes
                // Pass bytes to PDFTextExtractor to extract text
                fileBytes = mUri.let { uri ->
                    applicationContext.contentResolver.openInputStream(
                        uri
                    )?.readBytes()
                }
                if (fileBytes != null) {
                    extractedText = extractTextFromPdf(fileBytes!!)
                    viewModel.setExtractedData(extractedText)
                    binding.content.buttonSubmit.showSnack("Checking plagiarism. Please wait.")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.uploadToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mFirestoreDb = FirebaseFirestore.getInstance()
        mStorageReference = FirebaseStorage.getInstance().reference

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, DataManager.faculties.toList()
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.content.spinnerFaculties.adapter = spinnerAdapter

        setUpViewModels()

        binding.content.buttonSubmit.setOnClickListener {
            showUploadConfirmationDialog()
        }
    }

    private fun setUpViewModels() {
        viewModel.extractedText.observe(this) {
            val body = PlagiarismRequestBody(language = "en", text = it)
            viewModel.checkPlagiarismWithCall(body)
        }

        viewModel.loading.observe(this) { loading ->
            if (loading) {
                binding.content.tvPlagiarismLevel.gone()
                binding.content.indicatorPlagiarismPercent.apply {
                    visible()
                    isIndeterminate = true
                    setIndicatorColor(ResourcesCompat.getColor(resources, R.color.gradient_start_color, null))
                    trackColor = ResourcesCompat.getColor(resources, R.color.gradient_end_color, null)
                }
            }
        }

        viewModel.isSuccessful.observe(this) {
            binding.content.buttonSubmit.showSnack("Check complete. See indicator for result")
        }

        viewModel.levelOfPlagiarism.observe(this) { level ->
            if (level != null) {
                binding.content.tvPlagiarismLevel.visible()
                binding.content.tvPlagiarismLevel.text = getString(R.string.plagiarism_percent, level)
                if (level > 20) {
                    binding.content.buttonSubmit.isEnabled = false
                    binding.content.textViewPlagiarismStatus.visible()
                    binding.content.indicatorPlagiarismPercent.apply {
                        isIndeterminate = false
                        setProgress(level, true)
                        setIndicatorColor(getColor(R.color.colorRed))
                        trackColor = getColor(R.color.colorLightRed)
                    }
                } else {
                    binding.content.textViewPlagiarismStatus.gone()
                    binding.content.buttonSubmit.isEnabled = true
                    binding.content.indicatorPlagiarismPercent.apply {
                        isIndeterminate = false
                        setProgress(level, true)
                        setIndicatorColor(getColor(R.color.colorDeepGreen))
                        trackColor = getColor(R.color.colorLightGreen)
                    }
                }
            }
        }
    }

    private fun showUploadConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.upload_caution))
            .setMessage(getString(R.string.message_disclaimer))
            .setPositiveButton(getString(R.string.upload_upload)) { _, _ ->
                uploadProjectToFirebase()
            }
            .setNegativeButton(getString(R.string.upload_review), null)
            .show()
    }

    private fun extractTextFromPdf(data: ByteArray): String {
        var text = ""
        try {
            val reader = PdfReader(data)
            val pages = reader.numberOfPages
            for (i in 0 until pages) {
                text = PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n"
            }
        } catch (e: Exception) {
            text = "An error occurred"
            Timber.d(e, "extractTextFromPdf: %s", e.message)
        }

        return text
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

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        val spinnerSelection =
            getSpinnerSelection(binding.content.spinnerFaculties.selectedItem.toString())
        val projectTitle: String = binding.content.textProjectTitle.editText?.text.toString().trim()
        val indexNumber: String = binding.content.textIndexNumber.editText?.text.toString().trim()
        val projectYear: String = binding.content.textProjectYear.editText?.text.toString().trim()

        bundle.putString(SPINNER_SELECTION, spinnerSelection)
        bundle.putString(PROJECT_TITLE, projectTitle)
        bundle.putString(INDEX_NUMBER, indexNumber)
        bundle.putString(PROJECT_YEAR, projectYear)
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        super.onRestoreInstanceState(bundle)
        binding.content.textProjectTitle.editText?.setText(bundle.getString(PROJECT_TITLE))
        binding.content.textIndexNumber.editText?.setText(bundle.getString(INDEX_NUMBER))
        binding.content.textProjectYear.editText?.setText(bundle.getString(PROJECT_YEAR))
    }

    private fun selectPdfFile() {
        val intent = Intent()
        intent.type = FILE_TYPE
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(Intent.createChooser(intent, getString(R.string.upload_select_pdf)))
    }

    private fun uploadProjectToFirebase() {
        val spinnerSelection =
            getSpinnerSelection(binding.content.spinnerFaculties.selectedItem.toString())
        val collectionPath = assignFacultyInitial(spinnerSelection)

        val projectTitle: String = binding.content.textProjectTitle.editText?.text.toString().trim()
        val indexNumber: String = binding.content.textIndexNumber.editText?.text.toString().trim()
        var projectYear: String = binding.content.textProjectYear.editText?.text.toString().trim()

        if (TextUtils.isEmpty(binding.content.textProjectTitle.editText?.text.toString().trim())) {
            binding.content.textProjectTitle.editText?.error =
                getString(R.string.title_cannot_empty)
        }
        if (projectYear.length != 4) {
            binding.content.textProjectYear.editText?.error = getString(R.string.invalid_date)
            binding.content.textProjectYear.editText?.setText("")
            projectYear = ""
        }

        if (projectTitle.isBlank() || indexNumber.isBlank() || projectYear.isBlank()) {
            binding.content.buttonSubmit.showSnack(getString(R.string.all_fields_required))
        } else {
            val storageReference: StorageReference =
                mStorageReference.child("$FB_STORAGE_PATH/$fileName")
            storageReference.putFile(mUri)
                .addOnSuccessListener { taskSnapshot ->
                    // insert download url to fb
                    val uri: Task<Uri> = taskSnapshot.storage.downloadUrl
                    while (!uri.isComplete);
                    val url: Uri? = uri.result

                    val accountDetails: String? = FirebaseAuth.getInstance().currentUser?.email

                    val projectInfo = accountDetails?.let { email ->
                        ProjectInfo(
                            spinnerSelection, projectTitle,
                            url.toString(), projectYear, email, indexNumber
                        )
                    }
                    if (projectInfo != null) {
                        mFirestoreDb.collection(PROJECTS)
                            .document(FACULTIES)
                            .collection(collectionPath)
                            .add(projectInfo)
                    }

                    showProgress(false)
                    binding.content.buttonSubmit.showSnack("File Uploaded")
                    resetFields()
                }
                .addOnProgressListener {
                    showProgress(true)
                }
        }

    }


    // helpers
    companion object {
        const val FILE_TYPE = "application/pdf"
        const val SPINNER_SELECTION = "spinnerSelection"
        const val PROJECT_TITLE = "projectTitle"
        const val INDEX_NUMBER = "indexNumber"
        const val PROJECT_YEAR = "year"
        const val FB_STORAGE_PATH = "projectUploads"

    }

    private fun validateIndexNumber(indexNumber: String): Boolean {
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
            binding.content.textViewFileStatus.text = cursor.getString(index)
            fileName = cursor.getString(index)
            fileNameWithoutExtension = File(fileName).nameWithoutExtension
        }
    }

    private fun resetFields() {
        binding.content.textProjectTitle.editText?.setText("")
        binding.content.textIndexNumber.editText?.setText("")
        binding.content.textProjectYear.editText?.setText("");
        binding.content.textViewFileStatus.text = getString(R.string.file_status)
        binding.content.buttonSubmit.isEnabled = false
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            WaitingDialog.show(this);
        } else {
            WaitingDialog.dismiss();
        }
    }
}