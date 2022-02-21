package com.example.marketapps.listener

import com.example.marketapps.model.MarketModel

interface LoadListener {
    fun onLoadSuccess(marketModelList:List<MarketModel>?)
    fun onLoadFailure(message:String?)
}