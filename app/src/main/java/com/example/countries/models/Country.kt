package com.example.countries.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Country(
    @PrimaryKey var name: String = "",
    var capital: String? = null,
    var currencies: RealmList<Currency>? = null,
    var flag: String? = null,
    var population: Int? = null,
    var languages: RealmList<Language>? = null
) : RealmObject()

open class Currency(@PrimaryKey var name: String? = null) : RealmObject()

open class Language(@PrimaryKey var name: String? = null) : RealmObject()