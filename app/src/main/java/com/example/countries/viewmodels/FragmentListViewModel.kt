package com.example.countries.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.countries.models.Country
import com.example.countries.repositories.CountryRepository

class FragmentListViewModel : ViewModel() {
    private val mRepository = CountryRepository.getInstance()

    fun getCountriesList(): LiveData<List<Country>?> {
        return mRepository.getCountriesList()
    }

    fun loadCountriesList() {
        mRepository.loadCountriesList()
    }

    fun loadCountry(name: String) {
        mRepository.loadCountry(name)
    }

    fun cancelCountriesListRequest() {
        mRepository.cancelCountriesListRequest()
    }

    fun refreshCountriesList() {
        mRepository.refreshCountriesList()
    }

    fun isLoading(): LiveData<Boolean> {
        return mRepository.isLoadingCountriesList()
    }

    fun isRefreshing(): Boolean {
        return mRepository.isRefreshingCountriesList()
    }

    fun isRequestFailed(): Boolean {
        return mRepository.isRequestCountriesListFailed()
    }
}