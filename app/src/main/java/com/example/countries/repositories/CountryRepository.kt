package com.example.countries.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.countries.db.RealmApi
import com.example.countries.models.Country
import com.example.countries.requests.CountryApiClient

class CountryRepository private constructor() {
    private val mRealmApi = RealmApi.getInstance()                      //Для работы с Realm'ом
    private val mCountryApiClient = CountryApiClient.getInstance()      //Для работы с сетью

    // * Медиатор который держит в себе список стран полученный либо из базы (кэш), либо из сети
    private val mCountriesList = MediatorLiveData<List<Country>?>()
    // * Медиатор который держит в себе данные о выбранной стране полученные либо из базы (кэш), либо из сети
    private val mCountry = MediatorLiveData<Country?>()

    // * Медиатор являющийся индикатором процесса загрузки списка стран либо из базы (кэш), либо из сети
    private val mLoadingCountriesList = MediatorLiveData<Boolean>()
    // * Медиатор являющийся индикатором процесса загрузки данных о выбранной стране либо из базы (кэш), либо из сети
    private val mLoadingCountry = MediatorLiveData<Boolean>()

    companion object {
        private var instance: CountryRepository? = null

        fun getInstance(): CountryRepository {
            if (instance == null) {
                instance = CountryRepository()
            }
            return instance as CountryRepository
        }
    }

    /*
     * Инициализация медиаторов
     */
    init {
        // * Первичное присваивание значений для срабатывания Observer'ов
        mCountriesList.value = null
        mCountry.value = null

        // * Добавление источника из сети
        mCountriesList.addSource(mCountryApiClient.getCountriesList()) { countriesList ->
            mCountriesList.value = countriesList

            if (countriesList != null) {                        //Если результат получен, то производим кэширование в базе
                mRealmApi.cacheCountriesList(countriesList)
            }
        }

        // * Добавление источника из базы
        mCountriesList.addSource(mRealmApi.getCountriesList()) { countriesList ->
            if (countriesList.isNullOrEmpty()) {                //Если результата нет
                mCountryApiClient.loadCountriesList()           //Начинаем загрузку из сети
            } else {
                mCountriesList.value = countriesList            //Если результат есть, обновляем значение
            }
        }

        // * Добавление источника из сети
        mCountry.addSource(mCountryApiClient.getCountry()) { country ->
            mCountry.value = country

            if (country != null) {                              //Если результат получен, то производим кэширование в базе
                mRealmApi.cacheCountry(country)
            }
        }

        // * Добавление источника из базы
        mCountry.addSource(mRealmApi.getCountry()) { country ->
            mCountry.value = country

            if (country == null) {                                                  //Если результата нет
                mCountryApiClient.loadCountry(mRealmApi.getLastLoadedCountry())     //Начинаем загрузку из сети
            }
        }

        // * Добавление источника из сети
        mLoadingCountry.addSource(mCountryApiClient.isLoadingCountry()) {
            mLoadingCountry.value = it
        }

        // * Добавление источника из базы
        mLoadingCountry.addSource(mRealmApi.isLoadingCountry()) {
            mLoadingCountry.value = it
        }

        // * Добавление источника из сети
        mLoadingCountriesList.addSource(mCountryApiClient.isLoadingCountriesList()) {
            mLoadingCountriesList.value = it
        }

        // * Добавление источника из базы
        mLoadingCountriesList.addSource(mRealmApi.isLoadingCountriesList()) {
            mLoadingCountriesList.value = it
        }
    }

    // * Запросы в сеть на обновление данных
    fun refreshCountriesList() = mCountryApiClient.refreshCountriesList()
    fun refreshCountry(name: String) = mCountryApiClient.refreshCountry(name)

    // * Индикаторы указывающие на то, что в данный момент идёт обновление данных
    fun isRefreshingCountriesList(): Boolean = mCountryApiClient.isRefreshingCountriesList()
    fun isRefreshingCountry(): Boolean = mCountryApiClient.isRefreshingCountry()

    // * Индикаторы указывающие на то, что ответ на запрос не был получен
    fun isRequestCountriesListFailed(): Boolean = mCountryApiClient.isRequestCountriesListFailed()
    fun isRequestCountryFailed(): Boolean = mCountryApiClient.isRequestCountryFailed()

    // * Индикаторы указывающие на то, что в данный момент идёт процесс загрузки данных
    fun isLoadingCountriesList(): LiveData<Boolean> = mLoadingCountriesList
    fun isLoadingCountry(): LiveData<Boolean> = mLoadingCountry

    /*
     * Запрос на загрузку списка стран, который пробует получить данные из кэша
     * Если данные из кэша получены не были, то в подключенном к mCountriesList Observer'e отправляется запрос на получение данных из сети
     */
    fun loadCountriesList() {
        mCountryApiClient.resetCountriesListFailState()     //Так как процесс отмены запроса ассинхронный и может занимать время, а данные нужно отобразить быстро
                                                            //Вручную убираем состояние ошибки на случай если таковая была
        mRealmApi.loadCountriesList()                       //И пускаем запрос
    }
    /*
     * Запрос на загрузку данных выбранной страны, который пробует получить данные из кэша
     * Если данные из кэша получены не были, то в подключенном к mCountry Observer'e отправляется запрос на получение данных из сети
     */
    fun loadCountry(name: String) {
        mCountryApiClient.resetCountryFailState()           //Так как процесс отмены запроса ассинхронный и может занимать время, а данные нужно отобразить быстро
                                                            //Вручную убираем состояние ошибки на случай если таковая была
        mRealmApi.loadCountry(name)                         //И пускаем запрос
    }

    // * Предоставляем медиаторы для наблюдения
    fun getCountriesList(): LiveData<List<Country>?> = mCountriesList
    fun getCountry(): LiveData<Country?> = mCountry

    // * Ручная отмена запроса
    fun cancelCountriesListRequest() = mCountryApiClient.cancelCountriesListRequest()
    fun cancelCountryRequest() = mCountryApiClient.cancelCountryRequest()
}