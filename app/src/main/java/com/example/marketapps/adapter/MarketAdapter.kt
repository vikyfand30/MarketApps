package com.example.marketapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marketapps.R
import com.example.marketapps.model.MarketModel
import com.google.android.material.textview.MaterialTextView
import java.lang.StringBuilder

class MarketAdapter(private val context: Context, private val list: List<MarketModel>) :
    RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    class MarketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView? = null
        var tvName: MaterialTextView? = null
        var tvPrice: MaterialTextView? = null

        init {
            imageView = itemView.findViewById(R.id.iv_item) as ImageView
            tvName = itemView.findViewById(R.id.tv_item_name) as MaterialTextView
            tvPrice = itemView.findViewById(R.id.tv_item_price) as MaterialTextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.layout_market_item, parent, false)
        return MarketViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        Glide.with(context)
            .load(list[position].image)
            .into(holder.imageView!!)
        holder.tvName!!.text = StringBuilder().append(list[position].name)
        holder.tvPrice!!.text = StringBuilder("$").append(list[position].price)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}