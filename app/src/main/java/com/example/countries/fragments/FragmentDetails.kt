package com.example.countries.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.example.countries.R
import com.example.countries.viewmodels.FragmentDetailsViewModel
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import kotlinx.android.synthetic.main.fragment_detail.view.*

class FragmentDetails : Fragment() {

    private val args: FragmentDetailsArgs by navArgs()

    private lateinit var detailsViewModel: FragmentDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detailsViewModel =
            activity?.run { ViewModelProviders.of(this).get(FragmentDetailsViewModel::class.java) }
                ?: throw Exception("This should never happen")

        // * Callback на onBackPressed отменяющий запрос на обновление данных о стране
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            this.remove()
            detailsViewModel.cancelCountryRequest()
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        /*
         * Ставим Observer'a на индикатор выполнения запроса по обновлению данных о стране
         * Содержит в себе 3 основных сценария развития событий
         */
        detailsViewModel.isLoading().observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading && !detailsViewModel.isRefreshing()) {            //1-й сценарий в котором идёт загрузка данных, но это не обновление
                view.details_progress_bar.visibility = View.VISIBLE         //Отображается индикатор загрузки
                view.details_body.visibility = View.GONE                    //Скрывается всё остальное
                view.details_error_view.visibility = View.GONE              //
                view.details_refresher.isEnabled = false                    //И убирается возможность запустить обновление в этот момент
            }
            else if (isLoading && detailsViewModel.isRefreshing()) {        //2-й сценарий в котором идёт обновление данных
                view.details_progress_bar.visibility = View.GONE            //Не отображается индикатор загрузки
                view.details_refresher.isRefreshing = true                  //Но отображается индикатор обновления
                                                                            //
                if (detailsViewModel.isRequestFailed()) {                   //Для случаев в которых View пересоздаётся в процессе обновления
                    view.details_body.visibility = View.GONE                //Устанавливаем исходное состояние
                    view.details_error_view.visibility = View.VISIBLE       //в зависимости от того обновляли ли мы уже загруженные данные,
                } else {                                                    //или же имели сообщение об ошибке загрузки и пытались обновиться из него
                    view.details_body.visibility = View.VISIBLE             //
                    view.details_error_view.visibility = View.GONE          //
                }
            }
            else {                                                          //3-й сценарий в котором загрузка и обновление завершены
                view.details_progress_bar.visibility = View.GONE            //Убираем индикаторы загрузки
                view.details_refresher.isEnabled = true                     //И включаем возможность обновления
                view.details_refresher.isRefreshing = false                 //
                                                                            //
                if (detailsViewModel.isRequestFailed()) {                   //В зависимости от результата выполнения запроса
                    view.details_body.visibility = View.GONE                //Показываем либо сообщение об ошибке
                    view.details_error_view.visibility = View.VISIBLE       //Либо данные которые мы получили
                } else {                                                    //
                    view.details_body.visibility = View.VISIBLE             //
                    view.details_error_view.visibility = View.GONE          //
                }
            }
        })

        // * Listener для SwipeRefreshLayout
        view.details_refresher.setOnRefreshListener {
            detailsViewModel.refreshCountry(args.COUNTRYNAME)
        }

        // * Ставим Observer'a на объект страны данные которой хотим получить
        detailsViewModel.getCountry().observe(viewLifecycleOwner, Observer { country ->
            if (country != null) {                                                                  //Если получить данные не удалось, ничего не делаем

                GlideToVectorYou.justLoadImage(activity, Uri.parse(country.flag), view.flag_view)   //Загружаем флаг

                view.name_view.text = country.name                                                  //Устанавливаем название страны

                if (country.capital.isNullOrBlank()) {                                              //Устанавливаем столицу страны если она есть
                    view.capital_view.visibility = View.GONE                                        //Либо скрываем это поле
                } else {
                    view.capital_view.text = (resources.getString(R.string.capital) + " " + country.capital)
                }

                if (country.population == null) {                                                   //Устанавливаем кол-во населения страны если оно есть
                    view.population_view.visibility = View.GONE                                     //Либо скрываем это поле
                } else {
                    view.population_view.text = (resources.getString(R.string.population) + " " + country.population.toString())
                }

                if (country.currencies.isNullOrEmpty()) {                                           //Устанавливаем список валют в стране если они есть
                    view.currency_view.visibility = View.GONE                                       //Либо скрываем это поле
                } else {
                    view.currency_view.text = resources.getString(R.string.currencies)
                    var i = 0
                    while (country.currencies?.getOrNull(i) != null) {                              //Выводим все валюты через запятую
                        if (country.currencies?.getOrNull(i + 1) != null) {
                            view.currency_view.append(" " + country.currencies?.get(i)?.name + ",")
                        } else {
                            view.currency_view.append(" " + country.currencies?.get(i)?.name)
                        }
                        i++
                    }
                }

                if (country.languages.isNullOrEmpty()) {                                            //Устанавливаем список языков в стране если они есть
                    view.language_view.visibility = View.GONE                                       //Либо скрываем это поле
                } else {
                    view.language_view.text = resources.getString(R.string.languages)
                    var i = 0
                    while (country.languages?.getOrNull(i) != null) {                               //Выводим все языки через запятую
                        if (country.languages?.getOrNull(i + 1) != null) {
                            view.language_view.append(" " + country.languages?.get(i)?.name + ",")
                        } else {
                            view.language_view.append(" " + country.languages?.get(i)?.name)
                        }
                        i++
                    }
                }
            }
        })

        return view
    }
}