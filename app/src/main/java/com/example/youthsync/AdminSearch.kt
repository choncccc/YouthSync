package com.example.youthsync

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.youthsync.databinding.FragmentAdminSearchBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class AdminSearch : Fragment() {
    private lateinit var binding: FragmentAdminSearchBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentAdminSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSearch.setOnClickListener {
            val toSearch = binding.ETSearch.text.toString().lowercase()
            if (toSearch.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name to search", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .whereEqualTo("firstName", toSearch)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        displaySearchResults(view, documents)
                    } else {
                        Toast.makeText(requireContext(), "No matching users found", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Search failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun displaySearchResults(view: View, documents: QuerySnapshot) {
        val cardContainer = view.findViewById<LinearLayout>(R.id.ResultContainer)
        cardContainer.removeAllViews()

        for (document in documents) {
            val firstName = document.getString("firstName") ?: "N/A"
            val lastName = document.getString("lastName") ?: "N/A"
            val email = document.getString("email") ?: "N/A"

            // Create dynamic layout for each result
            val recordLayout = createRecordView(firstName, lastName, email)
            cardContainer.addView(recordLayout)
        }
    }

    private fun createRecordView(firstName: String, lastName: String, email: String): LinearLayout {
        val context = requireContext()
        val recordLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 32, 16, 32)
            setBackgroundResource(R.drawable.thin_et_border)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 40)
            layoutParams = params
        }

        val firstNameTextView = createLabeledTextView(context, "First Name:", firstName)
        val lastNameTextView = createLabeledTextView(context, "Last Name:", lastName)
        val emailTextView = createLabeledTextView(context, "Email:", email)

        recordLayout.addView(firstNameTextView)
        recordLayout.addView(lastNameTextView)
        recordLayout.addView(emailTextView)

        return recordLayout
    }

    private fun createLabeledTextView(context: Context, label: String, text: String): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 0, 0, 0)
        }

        val labelTextView = TextView(context).apply {
            this.text = label
            setTypeface(null, android.graphics.Typeface.BOLD)
            typeface = ResourcesCompat.getFont(context, R.font.lovelo)
            setTextSize(18f)
            setTextColor(ContextCompat.getColor(context, R.color.bg))
            setPadding(0, 0, 8, 0)
        }

        val valueTextView = TextView(context).apply {
            this.text = text
            typeface = ResourcesCompat.getFont(context, R.font.glacial)
            setTextColor(ContextCompat.getColor(context, R.color.bg))
            setTextSize(18f)
        }

        layout.addView(labelTextView)
        layout.addView(valueTextView)

        return layout
    }




}