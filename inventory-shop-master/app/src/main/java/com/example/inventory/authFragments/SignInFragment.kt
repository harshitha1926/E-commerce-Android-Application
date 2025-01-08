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
import com.example.inventory.databinding.FragmentSignInBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSignInBinding
    private lateinit var userType: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
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
        binding.textViewSignUp.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.nextBtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.passEt.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        } else {
                            Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                        binding.progressBar.visibility = View.GONE
                    })
            } else {
                Toast.makeText(context, "Fill empty fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDashboard() {
        if (userType == "Salesperson") {
            navController.navigate(R.id.action_signInFragment_to_dashboardFragment)
        } else {
            navController.navigate(R.id.action_signInFragment_to_customerDashboardFragment)
        }
    }
}
