package com.example.marketapps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.marketapps.adapter.MarketAdapter
import com.example.marketapps.listener.LoadListener
import com.example.marketapps.model.MarketModel
import com.example.marketapps.utils.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LoadListener {


    lateinit var loadListener: LoadListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        loadFromFirebase()
    }


    private fun init(){
        loadListener = this
        val gridLayoutManager = GridLayoutManager(this,2)
        rv_drink.layoutManager = gridLayoutManager
        rv_drink.addItemDecoration(SpaceItemDecoration())
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
        val adapter = MarketAdapter(this,marketModelList!!)
        rv_drink.adapter = adapter
    }

    override fun onLoadFailure(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}