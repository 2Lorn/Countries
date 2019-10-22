package com.example.countries

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import io.realm.Realm
import io.realm.RealmConfiguration

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // * Инициализируем Realm
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder().name("countries.realm").deleteRealmIfMigrationNeeded().build()
        Realm.setDefaultConfiguration(realmConfig)

        // * Инициализируем Navigation с поддержкой AppBar'а
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    /*
     * Перемещение назад с помощью кнопки в AppBar'e
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(findViewById(R.id.nav_host_fragment))
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()     //TODO Заменить на вызов obBackPressedCallback из текущего местоположения для корректной отмены текущего запроса
    }

    /*
    * Закрываем instance Realm'a
    */
    override fun onDestroy() {
        Realm.getDefaultInstance().close()
        super.onDestroy()
    }
}
