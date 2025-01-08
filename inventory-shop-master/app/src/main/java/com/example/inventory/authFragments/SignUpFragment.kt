package com.example.inventory.authFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.inventory.R
import com.example.inventory.databinding.FragmentSignUpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.userTypeSpinner.adapter = adapter
        }

        binding.userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                userType = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun registerEvents() {
        binding.textViewSignIn.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.nextBtn.setOnClickListener {
            val shopName = binding.shopName.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val phoneNumber = binding.phoneNumber.text.toString().trim()
            val pass = binding.passEt.text.toString().trim()
            val verifyPass = binding.verifyPassEt.text.toString().trim()

            if (shopName.isNotEmpty() && email.isNotEmpty() && phoneNumber.isNotEmpty() && pass.isNotEmpty() && verifyPass.isNotEmpty()){
                if (pass == verifyPass){
                    binding.progressBar.visibility = View.VISIBLE
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(
                        OnCompleteListener { task ->
                            if (task.isSuccessful){
                                val userId = auth.currentUser?.uid
                                val user = hashMapOf(
                                    "shopName" to shopName,
                                    "email" to email,
                                    "phoneNumber" to phoneNumber,
                                    "userType" to userType
                                )

                                userId?.let {
                                    firestore.collection("users").document(it)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show()
                                            navigateToDashboard()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }else{
                                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                            binding.progressBar.visibility = View.GONE
                        }
                    )
                }else{
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "Fill empty fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDashboard() {
        if (userType == "Salesperson") {
            navController.navigate(R.id.action_signUpFragment_to_DashboardFragment)
        } else {
            navController.navigate(R.id.action_signUpFragment_to_customerFragment)
        }
    }
}
