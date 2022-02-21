package com.example.marketapps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.marketapps.adapter.CartAdapter
import com.example.marketapps.eventbus.UpdateCartEvent
import com.example.marketapps.listener.CartLoadListener
import com.example.marketapps.model.CartModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_cart.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder

class CartActivity : AppCompatActivity(), CartLoadListener {

    var cartLoadListener: CartLoadListener? = null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateCartEvent(event: UpdateCartEvent){
        loadCartFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        init()
        loadCartFromFirebase()
    }

    private fun loadCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children){
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    cartLoadListener!!.onCartLoadSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener!!.onCartLoadFailure(error.message)
                }

            })
    }

    private fun init() {
        cartLoadListener = this

        val layoutManager = LinearLayoutManager(this)
        rv_cart!!.layoutManager = layoutManager
        rv_cart!!.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        iv_back_cart!!.setOnClickListener { finish() }
    }

    override fun onCartLoadSuccess(cartModelList: List<CartModel>?) {
        var sum = 0.0
        for (cartModel in cartModelList!!) {
            sum += cartModel!!.totalPrice
        }
        tv_total.text = StringBuilder("$").append(sum)
        val adapter = CartAdapter(
            this, cartModelList
        )
        rv_cart!!.adapter = adapter
    }

    override fun onCartLoadFailure(message: String?) {
       Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}