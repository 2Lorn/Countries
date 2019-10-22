package com.example.countries.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.countries.R
import com.example.countries.models.Country
import kotlinx.android.synthetic.main.country_list_item.view.*
import java.util.ArrayList

class CountryRecyclerAdapter : RecyclerView.Adapter<CountryRecyclerAdapter.CountryViewHolder>() {

    private var itemList: List<Country> = ArrayList()

    private var clickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.country_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION)
                    clickListener?.onItemClick(itemList[adapterPosition])
            }
        }

        fun bind(item: Country) {
            itemView.country_list_name_view.text = item.name
        }
    }

    fun setCountries(list: List<Country>) {
        itemList = list
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(country: Country)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        clickListener = listener
    }
}