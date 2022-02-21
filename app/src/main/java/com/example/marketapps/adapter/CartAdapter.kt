package com.example.marketapps.adapter

import android.app.AlertDialog
import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.marketapps.R
import com.example.marketapps.eventbus.UpdateCartEvent
import com.example.marketapps.model.CartModel
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class CartAdapter(private val context: Context, private val cartlist: List<CartModel>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {


    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivPlus: ImageView? = null
        var ivMinus: ImageView? = null
        var tvName: MaterialTextView? = null
        var tvPrice: MaterialTextView? = null
        var tvQty: MaterialTextView? = null
        var ivItem: ImageView? = null
        var ivDelete : ImageView? = null

        init {
            ivPlus = itemView.findViewById(R.id.iv_plus) as ImageView
            ivMinus = itemView.findViewById(R.id.iv_minus) as ImageView
            tvName = itemView.findViewById(R.id.tv_item_cart_name) as MaterialTextView
            tvPrice = itemView.findViewById(R.id.tv_item_cart_price) as MaterialTextView
            tvQty = itemView.findViewById(R.id.tv_item_cart_qty) as MaterialTextView
            ivItem = itemView.findViewById(R.id.iv_item_cart) as ImageView
            ivDelete = itemView.findViewById(R.id.iv_delete) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.layout_cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        Glide.with(context)
            .load(cartlist[position].image)
            .into(holder.ivItem!!)
        holder.tvName!!.text = StringBuilder().append(cartlist[position].name)
        holder.tvPrice!!.text = StringBuilder("$").append(cartlist[position].price)
        holder.tvQty!!.text = StringBuilder("").append(cartlist[position].quantity)

        holder.ivMinus!!.setOnClickListener { minusCartItem(holder,cartlist[position]) }
        holder.ivPlus!!.setOnClickListener { plusCartItem(holder,cartlist[position]) }
        holder.ivDelete!!.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Do you want to delete this item ?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Delete"){
                        _, _-> notifyItemRemoved(position)
                    FirebaseDatabase.getInstance()
                        .getReference("Cart")
                        .child("UNIQUE_USER_ID")
                        .child(cartlist[position].key!!)
                        .removeValue()
                        .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCartEvent()) }
                }
                .create()
                dialog.show()
        }


    }

    private fun plusCartItem(holder: CartViewHolder, cartModel: CartModel) {
        cartModel.quantity += 1
        cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
        holder.tvQty!!.text = StringBuilder("").append(cartModel.quantity)
        updateFirebase(cartModel)
    }

    private fun minusCartItem(holder: CartViewHolder, cartModel: CartModel){
         if (cartModel.quantity > 1){
            cartModel.quantity -= 1
             cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
             holder.tvQty!!.text = StringBuilder("").append(cartModel.quantity)
             updateFirebase(cartModel)
         }
    }

    private fun updateFirebase(cartModel: CartModel){
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .child(cartModel.key!!)
            .setValue(cartModel)
            .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCartEvent()) }

    }

    override fun getItemCount(): Int {
        return cartlist.size
    }
}