package com.example.youthsync

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.youthsync.databinding.FragmentAdminProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class AdminProfile : Fragment() {
    private lateinit var binding: FragmentAdminProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var eventName: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        binding.btnSearch.setOnClickListener {
            eventName = binding.ETSearch.text.toString().trim()
            if (eventName.isNotEmpty()) {
                searchEventByName(eventName)
            } else {
                Toast.makeText(requireContext(), "Please enter an event name to search", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchEventByName(eventName: String) {
        db.collection("Events")
            .whereEqualTo("eventName", eventName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents.first()
                    val eventID = document.id
                    binding.AttendeesEvent.text = "Attendees of $eventName"
                    fetchEventAttendees(eventID)
                } else {
                    Toast.makeText(requireContext(), "No event found with this name", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error searching event: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AdminProfile", "Error searching event", e)
            }
    }


    private fun fetchEventAttendees(eventID: String) {
        db.collection("Events").document(eventID).collection("Attendees")
            .get()
            .addOnSuccessListener { documents ->
                val parentLayout = binding.parentLayout
                parentLayout.removeAllViews()
                val attendeesList = mutableListOf<Pair<String, String>>()
                for (document in documents) {
                    val name = document.getString("Name") ?: "Unknown"
                    val attendedAt = document.getString("attendedAt") ?: "Not Available"
                    val recordView = createRecordView(name, attendedAt)
                    parentLayout.addView(recordView)
                    attendeesList.add(Pair(name, attendedAt))
                }

                if (attendeesList.isNotEmpty()) {
                    createSpreadsheetFile(attendeesList)
                    Toast.makeText(requireContext(), "Spreadsheet created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No attendees found for this event", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching attendees: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("AdminProfile", "Error fetching attendees", e)
            }
    }

    private fun createSpreadsheetFile(attendeesList: List<Pair<String, String>>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Attendees")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Attended At")

        attendeesList.forEachIndexed { index, attendee ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(attendee.first)
            row.createCell(1).setCellValue(attendee.second)
        }

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "Event_Attendees_$eventName.xlsx")
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
            workbook.close()
        }

        Toast.makeText(requireContext(), "Excel file saved to Downloads Folder", Toast.LENGTH_LONG).show()
    }




    private fun createRecordView(name: String, attendedAt: String): LinearLayout {
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

        val nameTextView = createLabeledTextView(context, "Name:", name)
        val attendedAtTextView = createLabeledTextView(context, "Attended At:", attendedAt)

        recordLayout.addView(nameTextView)
        recordLayout.addView(attendedAtTextView)

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
            setTextSize(18f)
            setTextColor(ContextCompat.getColor(context, R.color.bg))
            setPadding(0, 0, 8, 0)
        }

        val valueTextView = TextView(context).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(context, R.color.bg))
            setTextSize(18f)
        }

        layout.addView(labelTextView)
        layout.addView(valueTextView)

        return layout
    }


}