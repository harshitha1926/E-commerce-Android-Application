package com.example.inventory.appFragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.MainActivity
import com.example.inventory.R
import com.example.inventory.databinding.FragmentCatalogBinding
import com.example.inventory.utils.ProductAdapter
import com.example.inventory.utils.ProductData
import com.example.inventory.utils.ProductUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class CatalogFragment : Fragment(), AddFragment.DialogNextBtnClickListener,
    ProductAdapter.ProductAdapterClicksInterface {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var parentDatabaseReference: DatabaseReference
    private lateinit var binding: FragmentCatalogBinding
    private var popUpFragment: AddFragment? = null
    private lateinit var adapter: ProductAdapter
    private lateinit var mList: MutableList<ProductUtil>
    private lateinit var filteredList: MutableList<ProductUtil>
    private var currentQuery: String? = null

    private fun handleKeyboardVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.GONE else View.VISIBLE
        (activity as MainActivity).setBottomNavigationVisibility(visibility)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getDataFromFirebase()
        registerEvents()
        bottomNavigation()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backStackCount = childFragmentManager.backStackEntryCount
                if (backStackCount > 0) {
                    childFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })

        binding.searchView.setOnClickListener {
            handleKeyboardVisibility(true)
        }

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                (activity as MainActivity).setBottomNavigationVisibility(View.GONE)
            } else {
                (activity as MainActivity).setBottomNavigationVisibility(View.VISIBLE)
            }
        }
    }

    private fun registerEvents() {
        binding.addBtn.setOnClickListener {
            if (popUpFragment != null)
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            popUpFragment = AddFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(
                childFragmentManager,
                AddFragment.ARG_PRODUCT_DATA
            )
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText?.toLowerCase(Locale.getDefault())
                filterList()
                return false
            }
        })
    }

    private fun filterList() {
        filteredList.clear()
        if (currentQuery.isNullOrEmpty()) {
            filteredList.addAll(mList)
        } else {
            for (productData in mList) {
                if (productData.productName?.toLowerCase(Locale.getDefault())
                        ?.contains(currentQuery!!) == true
                ) {
                    filteredList.add(productData)
                }
            }
        }
        adapter.setData(filteredList)
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        parentDatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Products").child(auth.currentUser?.uid.toString())
        databaseReference = parentDatabaseReference.child("Shop Products")

        binding.catalogRv.setHasFixedSize(true)
        binding.catalogRv.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        filteredList = mutableListOf()

        adapter = ProductAdapter(filteredList)
        adapter.setListener(this)
        binding.catalogRv.adapter = adapter
    }
    private fun deleteProduct(productData: ProductUtil) {
        val productId = productData.productId
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${productData.productName}?")
            .setPositiveButton("Delete") { _, _ ->
                if (productId != null) {
                    databaseReference.child(productId)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "${productData.productName} deleted successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getDataFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (productSnapshot in snapshot.children) {
                    val productName = productSnapshot.child("productName").getValue(String::class.java)
                    val productDescription = productSnapshot.child("productDescription").getValue(String::class.java)
                    val costPrice = productSnapshot.child("costPrice").getValue(String::class.java)
                    val sellingPrice = productSnapshot.child("sellingPrice").getValue(String::class.java)
                    val quantity = productSnapshot.child("quantity").getValue(String::class.java)
                    val productId = productSnapshot.key

                    val productData = ProductUtil(
                        productId ?: "",
                        productName ?: "No name",
                        productDescription ?: "No description",
                        costPrice ?: "0",
                        sellingPrice ?: "0",
                        quantity ?: "0"
                    )
                    mList.add(productData)
                }
                adapter.setData(mList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onSaveProduct(
        product: String,
        etProductName: TextInputEditText,
        productDesc: String,
        etProductDescription: TextInputEditText,
        productCost: String,
        etCostPrice: TextInputEditText,
        productSell: String,
        etSellingPrice: TextInputEditText,
        productQuan: String,
        etQuantity: TextInputEditText
    ) {
        val productName = etProductName.text.toString()
        val productDescription = etProductDescription.text.toString()
        val costPrice = etCostPrice.text.toString()
        val sellingPrice = etSellingPrice.text.toString()
        val quantity = etQuantity.text.toString()

        val productData = ProductData(
            productName,
            productDescription,
            costPrice,
            sellingPrice,
            quantity
        )
        val productKey = databaseReference.push().key

        if (productKey != null) {
            databaseReference.child(productKey)
                .setValue(productData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                    etProductName.text?.clear()
                    etProductDescription.text?.clear()
                    etCostPrice.text?.clear()
                    etSellingPrice.text?.clear()
                    etQuantity.text?.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to add product", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onUpdateProduct(
        productData: ProductUtil,
        etProductName: TextInputEditText,
        etProductDescription: TextInputEditText,
        etCostPrice: TextInputEditText,
        etSellingPrice: TextInputEditText,
        etQuantity: TextInputEditText
    ) {
        val productName = etProductName.text.toString()
        if (productName.isNotEmpty()) {
            val updatedData = mapOf<String, Any?>(
                "productName" to productName,
                "productDescription" to etProductDescription.text.toString(),
                "costPrice" to etCostPrice.text.toString(),
                "sellingPrice" to etSellingPrice.text.toString(),
                "quantity" to etQuantity.text.toString()
            )

            val productId = productData.productId ?: return
            databaseReference.child(productId)
                .updateChildren(updatedData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show()
                    }
                    etProductName.text = null
                    etProductDescription.text = null
                    etCostPrice.text = null
                    etSellingPrice.text = null
                    etQuantity.text = null
                    popUpFragment?.dismiss()
                }
        }
    }

    override fun onDeleteProductBtnClicked(productData: ProductUtil) {
        deleteProduct(productData)
    }

    override fun onEditProductBtnClicked(productData: ProductUtil) {
        if (popUpFragment != null)
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()

        popUpFragment = AddFragment.newInstance(productData)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(
            childFragmentManager,
            AddFragment.ARG_PRODUCT_DATA
        )
    }

    override fun onReadProductBtnClicked(productData: ProductUtil) {
        viewProduct(productData)
    }

    private fun viewProduct(productData: ProductUtil) {
        findNavController().navigate(R.id.action_catalogFragment2_to_viewFragment, Bundle().apply {
            putParcelable("productData", productData)
        })
    }

    private fun bottomNavigation() {
        binding.apply {
            homeIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_catalogFragment2_to_dashboardFragment2)
            }

            sellIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_catalogFragment2_to_sellFragment)
            }

            analyticIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_catalogFragment2_to_analyticsFragment2)
            }

            profileIconLayout.setOnClickListener {
                findNavController().navigate(R.id.action_catalogFragment2_to_profileFragment2)
            }
        }
    }
}
