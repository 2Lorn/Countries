package com.example.countries.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.countries.R
import com.example.countries.adapters.CountryRecyclerAdapter
import com.example.countries.models.Country
import com.example.countries.viewmodels.FragmentListViewModel
import kotlinx.android.synthetic.main.fragment_list.view.*

class FragmentList : Fragment() {

    private val mAdapter = CountryRecyclerAdapter()

    private lateinit var listViewModel: FragmentListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listViewModel =
            activity?.run { ViewModelProviders.of(this).get(FragmentListViewModel::class.java) }
                ?: throw Exception("This should never happen")

        /*
         * Ставим Observer'a на список стран
         * Если списка нет просим его загрузить
         * Если есть кидаем в адаптер
         */
        listViewModel.getCountriesList().observe(this, Observer { countriesList ->
            if (countriesList == null) {
                listViewModel.loadCountriesList()
            } else {
                mAdapter.setCountries(countriesList)
            }
        })

        // * ClickListener для элементов списка
        mAdapter.setOnItemClickListener(object : CountryRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(country: Country) {
                listViewModel.cancelCountriesListRequest()      //Отменяем запрос на обновление списка, на случай если оно шло

                listViewModel.loadCountry(country.name)         //Начинаем загружать выбранную страну

                val action =                                    //Передаём название страны в аргументах для возможности последующей перезагрузки
                    FragmentListDirections.actionFragmentListToFragmentDetails(country.name)
                findNavController().navigate(action)
            }
        })


        // * Callback на onBackPressed отменяющий запрос на обновление списка
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            this.remove()
            listViewModel.cancelCountriesListRequest()
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        /*
         * Ставим Observer'a на индикатор выполнения запроса по обновлению списка
         * Содержит в себе 3 основных сценария развития событий
         */
        listViewModel.isLoading().observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading && !listViewModel.isRefreshing()) {           //1-й сценарий в котором идёт загрузка списка, но это не обновление
                view.list_progress_bar.visibility = View.VISIBLE        //Отображается индикатор загрузки
                view.recycler_view.visibility = View.GONE               //Скрывается всё остальное
                view.list_error_view.visibility = View.GONE             //
                view.list_refresher.isEnabled = false                   //И убирается возможность запустить обновление в этот момент
            }
            else if (isLoading && listViewModel.isRefreshing()) {       //2-й сценарий в котором идёт обновление списка
                view.list_progress_bar.visibility = View.GONE           //Не отображается индикатор загрузки
                view.list_refresher.isRefreshing = true                 //Но отображается индикатор обновления
                                                                        //
                if (listViewModel.isRequestFailed()) {                  //Для случаев в которых View пересоздаётся в процессе обновления
                    view.recycler_view.visibility = View.GONE           //Устанавливаем исходное состояние
                    view.list_error_view.visibility = View.VISIBLE      //в зависимости от того обновляли ли мы уже загруженный список,
                } else {                                                //или же имели сообщение об ошибке загрузки и пытались обновиться из него
                    view.recycler_view.visibility = View.VISIBLE        //
                    view.list_error_view.visibility = View.GONE         //
                }
            }
            else {                                                      //3-й сценарий в котором загрузка и обновление завершены
                view.list_progress_bar.visibility = View.GONE           //Убираем индикаторы загрузки
                view.list_refresher.isEnabled = true                    //И включаем возможность обновления
                view.list_refresher.isRefreshing = false                //
                                                                        //
                if (listViewModel.isRequestFailed()) {                  //В зависимости от результата выполнения запроса
                    view.recycler_view.visibility = View.GONE           //Показываем либо сообщение об ошибке
                    view.list_error_view.visibility = View.VISIBLE      //Либо список который мы получили
                } else {                                                //
                    view.recycler_view.visibility = View.VISIBLE        //
                    view.list_error_view.visibility = View.GONE         //
                }
            }
        })

        // * Listener для SwipeRefreshLayout
        view.list_refresher.setOnRefreshListener {
            listViewModel.refreshCountriesList()
        }

        // * Инициализируем RecyclerView
        view.recycler_view.setHasFixedSize(true)
        view.recycler_view.layoutManager = LinearLayoutManager(activity)
        view.recycler_view.adapter = mAdapter
        view.recycler_view.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        return view
    }

    /*
     * Удаляем ссылку на RecyclerView из адаптера для избежания утечек памяти
     */
    override fun onDestroyView() {
        view?.recycler_view?.adapter = null
        super.onDestroyView()
    }
}