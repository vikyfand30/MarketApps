package com.example.marketapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marketapps.R
import com.example.marketapps.eventbus.UpdateCartEvent
import com.example.marketapps.listener.CartLoadListener
import com.example.marketapps.listener.ClickListener
import com.example.marketapps.model.CartModel
import com.example.marketapps.model.MarketModel
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class MarketAdapter(
    private val context: Context, private val list: List<MarketModel>,
    private val cartListener: CartLoadListener
) :
    RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    class MarketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var imageView: ImageView? = null
        var tvName: MaterialTextView? = null
        var tvPrice: MaterialTextView? = null

        private var clickListener: ClickListener? = null

        fun setClickListener(clickListener: ClickListener) {
            this.clickListener = clickListener
        }

        init {
            imageView = itemView.findViewById(R.id.iv_item) as ImageView
            tvName = itemView.findViewById(R.id.tv_item_name) as MaterialTextView
            tvPrice = itemView.findViewById(R.id.tv_item_price) as MaterialTextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClick(v, adapterPosition)
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

        holder.setClickListener(object : ClickListener {
            override fun onItemClick(view: View?, position: Int) {
                addToCart(list[position])
            }

        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun addToCart(marketModel: MarketModel) {
        val userCart = FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID") //similar user ID Firebase

        userCart.child(marketModel.key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) { //if item already
                        val cartModel = snapshot.getValue(CartModel::class.java)
                        val updateData: MutableMap<String, Any> = HashMap()

                        cartModel!!.quantity = cartModel!!.quantity + 1

                        updateData["quantity"] = cartModel!!.quantity + 1
                        updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()

                        userCart.child(marketModel.key!!)
                            .updateChildren(updateData)
                            .addOnSuccessListener {
                                EventBus.getDefault().postSticky(UpdateCartEvent())
                                cartListener.onCartLoadFailure("Success add to cart")
                            }
                            .addOnFailureListener {
                                e->
                                cartListener.onCartLoadFailure(e.message)
                            }
                    } else //if item not in cart
                    {
                            val cartModel = CartModel()
                            cartModel.key = marketModel.key
                        cartModel.image = marketModel.image
                        cartModel.name = marketModel.name
                        cartModel.price = marketModel.price
                        cartModel.quantity = 1
                        cartModel.totalPrice = marketModel.price!!.toFloat()

                        userCart.child(marketModel.key!!)
                            .setValue(cartModel)
                            .addOnSuccessListener {
                                EventBus.getDefault().postSticky(UpdateCartEvent())
                                cartListener.onCartLoadFailure("Success add to cart")
                            }
                            .addOnFailureListener {
                                    e->
                                cartListener.onCartLoadFailure(e.message)
                            }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cartListener.onCartLoadFailure(error.message)
                }

            })
    }
}