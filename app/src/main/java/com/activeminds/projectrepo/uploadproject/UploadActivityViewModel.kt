package com.activeminds.projectrepo.uploadproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activeminds.projectrepo.models.api.ApiClient
import com.dev.gka.plagiarismchecker.models.PlagiarismRequestBody
import com.dev.gka.plagiarismchecker.models.PlagiarismResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class UploadActivityViewModel : ViewModel() {

    private val _extractedText = MutableLiveData<String>()
    val extractedText: LiveData<String> = _extractedText

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _isSuccessful = MutableLiveData<Boolean>()
    val isSuccessful: LiveData<Boolean> = _isSuccessful

    private val _levelOfPlagiarism = MutableLiveData<Int?>()
    val levelOfPlagiarism: LiveData<Int?> = _levelOfPlagiarism

    fun checkPlagiarismWithCall(body: PlagiarismRequestBody) {
        viewModelScope.launch {
            _loading.value = true
            ApiClient.apiService.plagiarismWithCall(body).enqueue(object : Callback<PlagiarismResponse> {
                override fun onResponse(
                    call: Call<PlagiarismResponse>,
                    response: Response<PlagiarismResponse>
                ) {
                    if (response.isSuccessful) {
                        _levelOfPlagiarism.value = response.body()?.percentPlagiarism
                        _isSuccessful.value = true
                        _loading.value = false
                    } else {
                        _isSuccessful.value = false
                        _loading.value = false
                    }
                }

                override fun onFailure(call: Call<PlagiarismResponse>, t: Throwable) {
                    _isSuccessful.value = false
                    _loading.value = false
                    Timber.d(t)
                }

            })
        }
    }

    fun setExtractedData(text: String) {
        _extractedText.value = text
    }
}