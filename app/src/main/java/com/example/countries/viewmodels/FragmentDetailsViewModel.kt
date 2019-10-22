package com.example.countries.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.countries.models.Country
import com.example.countries.repositories.CountryRepository

class FragmentDetailsViewModel : ViewModel() {
    private val mRepository = CountryRepository.getInstance()

    fun getCountry(): LiveData<Country?> {
        return mRepository.getCountry()
    }

    fun cancelCountryRequest() {
        mRepository.cancelCountryRequest()
    }

    fun refreshCountry(name: String) {
        mRepository.refreshCountry(name)
    }

    fun isLoading(): LiveData<Boolean> {
        return mRepository.isLoadingCountry()
    }

    fun isRefreshing(): Boolean {
        return mRepository.isRefreshingCountry()
    }

    fun isRequestFailed(): Boolean {
        return mRepository.isRequestCountryFailed()
    }
}