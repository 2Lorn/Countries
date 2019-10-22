package com.example.countries.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.countries.models.Country
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.kotlin.isValid

class RealmApi private constructor() {
    // * Instance of Realm
    private val mRealm = Realm.getDefaultInstance()

    // * Список стран для наблюдения из репозитория
    private val mCountriesList = MutableLiveData<List<Country>?>()
    // * Данные по выбранной стране для наблюдения из репозитория
    private val mCountry = MutableLiveData<Country?>()

    // * Индикатор для наблюдения из репозитория, показывающий идёт ли загрузка списка стран
    private val mLoadingCountriesList = MutableLiveData<Boolean>()
    // * Индикатор для наблюдения из репозитория, показывающий идёт ли загрузка данных по выбранной стране
    private val mLoadingCountry = MutableLiveData<Boolean>()

    // * Название последней загруженной страны для передачи оного в сетевой запрос при неудачной загрузке из кэша
    private var mLastLoadedCountry: String = ""

    companion object {
        private var instance: RealmApi? = null

        fun getInstance(): RealmApi {
            if (instance == null) {
                instance = RealmApi()
            }
            return instance as RealmApi
        }
    }


    fun getLastLoadedCountry(): String = mLastLoadedCountry

    // * Возвращают LiveData для наблюдения
    fun isLoadingCountriesList(): LiveData<Boolean> = mLoadingCountriesList
    fun isLoadingCountry(): LiveData<Boolean> = mLoadingCountry

    // * Возвращают LiveData для наблюдения
    fun getCountriesList(): LiveData<List<Country>?> = mCountriesList
    fun getCountry(): LiveData<Country?> = mCountry

    // * Загрузка списка стран
    fun loadCountriesList() {
        mLoadingCountriesList.value = true

        val result = mRealm.where(Country::class.java).findAllAsync()

        result.addChangeListener { resultList ->
            mLoadingCountriesList.value = false

            mCountriesList.value = resultList.toList()

            result.removeAllChangeListeners()
        }
    }

    // * Загрузка выбранной страны
    fun loadCountry(name: String) {
        mLoadingCountry.value = true

        mLastLoadedCountry = name

        val result =
            mRealm.where(Country::class.java).equalTo("name", name)
                .isNotNull("capital")
                .isNotEmpty("currencies")
                .isNotNull("flag")
                .isNotNull("population")
                .isNotEmpty("languages")
                .findFirstAsync()

        result.addChangeListener(RealmChangeListener { resultCountry ->
            mLoadingCountry.value = false

            if (resultCountry.isValid()) {
                mCountry.value = result
            } else {
                mCountry.value = null
            }

            result.removeAllChangeListeners()
        })
    }

    // * Добавление списка стран в кэш
    fun cacheCountriesList(list: List<Country>) {
        mRealm.beginTransaction()
        val realmList = RealmList<Country>()
        realmList.addAll(list)
        mRealm.insertOrUpdate(list)
        mRealm.commitTransaction()
    }

    // * Добавление данных по стране в кэш
    fun cacheCountry(country: Country) {
        mRealm.beginTransaction()
        mRealm.insertOrUpdate(country)
        mRealm.commitTransaction()
    }
}