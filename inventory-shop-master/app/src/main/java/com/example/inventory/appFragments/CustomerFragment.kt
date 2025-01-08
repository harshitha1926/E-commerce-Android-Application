package com.example.inventory.appFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.databinding.FragmentCustomerBinding
import com.example.inventory.utils.ProductUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CustomerFragment : Fragment() {

    private lateinit var binding: FragmentCustomerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<ProductUtil>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        productList = ArrayList()

        productAdapter = ProductAdapter(productList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = productAdapter

        fetchProductsFromFirebase()
    }

    private fun fetchProductsFromFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val productsRef = database.reference.child("Products").child(userId)

        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductUtil::class.java)
                    product?.let { productList.add(it) }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
