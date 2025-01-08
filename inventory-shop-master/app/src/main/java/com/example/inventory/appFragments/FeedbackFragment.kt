package com.example.inventory.appFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.inventory.R
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment() {

    private lateinit var productName: TextView
    private lateinit var productDescription: TextView
    private lateinit var productPrice: TextView
    private lateinit var recommendGroup: RadioGroup
    private lateinit var usageDropdown: Spinner
    private lateinit var ratingBar: RatingBar
    private lateinit var suggestions: EditText
    private lateinit var submitButton: Button

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productName = view.findViewById(R.id.productName)
        productDescription = view.findViewById(R.id.productDescription)
        productPrice = view.findViewById(R.id.productPrice)
        recommendGroup = view.findViewById(R.id.recommendGroup)
        usageDropdown = view.findViewById(R.id.usageDropdown)
        ratingBar = view.findViewById(R.id.ratingBar)
        suggestions = view.findViewById(R.id.suggestions)
        submitButton = view.findViewById(R.id.submitButton)

        firestore = FirebaseFirestore.getInstance()

        val args = arguments
        if (args != null) {
            productName.text = args.getString("productName")
            productDescription.text = args.getString("productDescription")
            productPrice.text = args.getString("productPrice")
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.usage_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            usageDropdown.adapter = adapter
        }

        submitButton.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val feedback = hashMapOf(
            "productName" to productName.text.toString(),
            "productDescription" to productDescription.text.toString(),
            "productPrice" to productPrice.text.toString(),
            "recommend" to if (recommendGroup.checkedRadioButtonId == R.id.recommendYes) "Yes" else "No",
            "usageDuration" to usageDropdown.selectedItem.toString(),
            "rating" to ratingBar.rating,
            "suggestions" to suggestions.text.toString()
        )

        firestore.collection("feedback")
            .add(feedback)
            .addOnSuccessListener {
                Toast.makeText(context, "Feedback submitted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to submit feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
