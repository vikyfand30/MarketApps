package com.example.marketapps.listener

import com.example.marketapps.model.CartModel
import com.example.marketapps.model.MarketModel

interface CartLoadListener {
    fun onCartLoadSuccess(cartModelList:List<CartModel>?)
    fun onCartLoadFailure(message:String?)
}