package com.example.countries.requests

import com.example.countries.util.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServiceProvider {
    companion object{
        private val retrofitBuilder = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

        private val retrofit = retrofitBuilder.build()

        private val countryApi = retrofit.create(CountryApi::class.java)

        fun getCountryApi(): CountryApi = countryApi
    }
}