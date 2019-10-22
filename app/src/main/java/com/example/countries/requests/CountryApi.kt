package com.example.countries.requests

import com.example.countries.models.Country
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CountryApi {

    @GET("all?fields=name")
    fun getAllCountries(): Call<List<Country>>

    @GET("name/{name}")
    fun getCountry(@Path("name") name: String): Call<List<Country>>
}