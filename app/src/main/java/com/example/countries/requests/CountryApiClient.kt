package com.example.countries.requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.countries.models.Country
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CountryApiClient private constructor(){

    // * Список стран для наблюдения из репозитория
    private val mCountriesList = MutableLiveData<List<Country>?>()
    // * Обработчик запросов на список стран
    private val mReceiverCountriesList = ReceiverCountriesList()

    // * Данные по выбранной стране для наблюдения из репозитория
    private val mCountry = MutableLiveData<Country?>()
    // * Обработчик запросов на данные по выбранной стране
    private val mReceiverCountry = ReceiverCountry()

    // * Индикатор для наблюдения из репозитория, показывающий идёт ли загрузка списка стран
    private val mLoadingCountriesList = MutableLiveData<Boolean>()
    // * Индикатор для наблюдения из репозитория, показывающий идёт ли загрузка данных по выбранной стране
    private val mLoadingCountry = MutableLiveData<Boolean>()

    // * Индикатор показывающий идёт ли обновление списка стран
    private var mRefreshingCountriesList = false
    // * Индикатор показывающий идёт ли обновление данных по выбранной стране
    private var mRefreshingCountry = false

    // * Индикатор показывающий была ли ошибка при загрузке списка стран
    private var mFailureCountriesList = false
    // * Индикатор показывающий была ли ошибка при загрузке данных по выбранной стране
    private var mFailureCountry = false


    companion object {
        private var instance: CountryApiClient? = null

        fun getInstance(): CountryApiClient {
            if (instance == null) {
                instance = CountryApiClient()
            }
            return instance as CountryApiClient
        }
    }

    // * Запросы в сеть на загрузку данных
    fun loadCountriesList() = mReceiverCountriesList.loadCountriesList()
    fun loadCountry(name: String) = mReceiverCountry.loadCountry(name)

    // * Возвращают LiveData для наблюдения
    fun getCountriesList(): LiveData<List<Country>?> = mCountriesList
    fun getCountry(): LiveData<Country?> = mCountry

    // * Отмена запроса на загрузку списка стран и возвращение индикаторов в исходное состояние
    fun cancelCountriesListRequest() {
        mFailureCountriesList = false
        mRefreshingCountriesList = false
        mLoadingCountriesList.value = false

        mReceiverCountriesList.cancelRequest()
    }

    // * Отмена запроса на загрузку данных по стране и возвращение индикаторов в исходное состояние
    fun cancelCountryRequest() {
        mFailureCountry = false
        mRefreshingCountry = false

        mReceiverCountry.cancelRequest()
    }

    // * Возвращают LiveData для наблюдения
    fun isLoadingCountriesList(): LiveData<Boolean> = mLoadingCountriesList
    fun isLoadingCountry(): LiveData<Boolean> = mLoadingCountry

    // * Запрос на обновление списка стран
    fun refreshCountriesList() {
        mRefreshingCountriesList = true
        loadCountriesList()
    }
    // * Запрос на обновление данных по выбранной стране
    fun refreshCountry(name: String) {
        mRefreshingCountry = true
        loadCountry(name)
    }

    // * Индикаторы указывающие на то, что в данный момент идёт обновление данных
    fun isRefreshingCountriesList(): Boolean = mRefreshingCountriesList
    fun isRefreshingCountry(): Boolean = mRefreshingCountry

    // * Индикаторы указывающие на то, что ответ на запрос не был получен
    fun isRequestCountriesListFailed(): Boolean = mFailureCountriesList
    fun isRequestCountryFailed(): Boolean = mFailureCountry

    // * Убрать состояние ошибки по запросу на список стран
    fun resetCountriesListFailState() {
        mFailureCountriesList = false
    }
    // * Убрать состояние ошибки по запросу на данные по стране
    fun resetCountryFailState() {
        mFailureCountry = false
    }

    // * Обработчик запросов на получение данных по выбранной стране
    inner class ReceiverCountry {
        private var call: Call<List<Country>>? = null

        fun loadCountry(name: String) {
            mLoadingCountry.value = true

            call = ServiceProvider.getCountryApi().getCountry(name)

            call?.enqueue(object : Callback<List<Country>> {
                override fun onFailure(call: Call<List<Country>>, t: Throwable) {

                    if (!call.isCanceled) {             //Если запрос был отменён, то ничего не меняем, так как всё устанавливается в нужное положение при начале запроса

                        if (!mRefreshingCountry) {      //Если ошибка произошла при обновлении данных, то индикатор ошибки не меняется
                            mFailureCountry = true      //для того чтобы была возможность отобразить данные из кэша, если таковые были
                        }
                        mRefreshingCountry = false
                        mLoadingCountry.value = false
                    }
                }

                override fun onResponse(
                    call: Call<List<Country>>,
                    response: Response<List<Country>>
                ) {
                    mFailureCountry = false
                    mRefreshingCountry = false
                    mLoadingCountry.value = false

                    if (!response.isSuccessful) {
                        return
                    }

                    mCountry.value = response.body()?.get(0)
                }
            })
        }

        fun cancelRequest() {
            call?.cancel()
        }
    }

    // * Обработчик запросов на получение данных по выбранной стране
    inner class ReceiverCountriesList {
        private var call: Call<List<Country>>? = null

        fun loadCountriesList() {
            mLoadingCountriesList.value = true

            call = ServiceProvider.getCountryApi().getAllCountries()

            call?.enqueue(object : Callback<List<Country>> {
                override fun onFailure(call: Call<List<Country>>, t: Throwable) {

                    if (!call.isCanceled) {                     //Если запрос был отменён, то ничего не меняем, так как всё устанавливается в нужное положение при начале запроса

                        if (!mRefreshingCountriesList) {        //Если ошибка произошла при обновлении данных, то индикатор ошибки не меняется
                            mFailureCountriesList = true        //для того чтобы была возможность отобразить данные из кэша, если таковые были
                        }
                        mRefreshingCountriesList = false
                        mLoadingCountriesList.value = false
                    }
                }

                override fun onResponse(
                    call: Call<List<Country>>,
                    response: Response<List<Country>>
                ) {
                    mFailureCountriesList = false
                    mRefreshingCountriesList = false
                    mLoadingCountriesList.value = false

                    if (!response.isSuccessful) {
                        return
                    }

                    mCountriesList.value = response.body()
                }
            })
        }

        fun cancelRequest() {
            call?.cancel()
        }
    }
}