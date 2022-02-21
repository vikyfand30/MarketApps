package com.example.marketapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.marketapps.adapter.MarketAdapter
import com.example.marketapps.eventbus.UpdateCartEvent
import com.example.marketapps.listener.CartLoadListener
import com.example.marketapps.listener.LoadListener
import com.example.marketapps.model.CartModel
import com.example.marketapps.model.MarketModel
import com.example.marketapps.utils.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), LoadListener, CartLoadListener {


    lateinit var loadListener: LoadListener
    lateinit var cartLoadListener : CartLoadListener

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
    fun onUpdateCartEvent(event:UpdateCartEvent){
        countCartFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        loadFromFirebase()
        countCartFromFirebase()
    }

    private fun countCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children){
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    cartLoadListener.onCartLoadSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener.onCartLoadFailure(error.message)
                }

            })
    }


    private fun init(){
        loadListener = this
        cartLoadListener = this

        val gridLayoutManager = GridLayoutManager(this,2)
        rv_drink.layoutManager = gridLayoutManager
        rv_drink.addItemDecoration(SpaceItemDecoration())

        iv_cart.setOnClickListener { startActivity(Intent(this,CartActivity::class.java)) }
    }

    private fun loadFromFirebase(){
        val marketModels : MutableList<MarketModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Drink")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (drinkSnapshot in snapshot.children){
                            val marketModel = drinkSnapshot.getValue(MarketModel::class.java)
                            marketModel!!.key = drinkSnapshot.key
                            marketModels.add(marketModel)
                        }
                        loadListener.onLoadSuccess(marketModels)

                    }else{
                        loadListener.onLoadFailure("Drink item not exists")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    loadListener.onLoadFailure(error.message)
                }

            })
    }

    override fun onLoadSuccess(marketModelList: List<MarketModel>?) {
        val adapter = MarketAdapter(this,marketModelList!!, cartLoadListener)
        rv_drink.adapter = adapter
    }

    override fun onLoadFailure(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCartLoadSuccess(cartModelList: List<CartModel>?) {
        var cartSum = 0
        for (cartModel in cartModelList!!) cartSum+= cartModel!!.quantity
        iv_badge!!.setNumber(cartSum)
    }

    override fun onCartLoadFailure(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}