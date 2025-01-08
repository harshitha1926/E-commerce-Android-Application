package com.example.inventory.appFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory.R
import com.example.inventory.databinding.ItemProductBinding
import com.example.inventory.utils.ProductUtil

class ProductAdapter(private val productList: List<ProductUtil>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductUtil) {
            binding.productName.text = product.productName
            binding.productDescription.text = product.productDescription
            binding.sellingPrice.text = product.sellingPrice

            binding.root.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("productName", product.productName)
                    putString("productDescription", product.productDescription)
                    putString("productPrice", product.sellingPrice)
                }
                it.findNavController().navigate(R.id.action_customerFragment_to_feedbackFragment, bundle)
            }
        }
    }
}
