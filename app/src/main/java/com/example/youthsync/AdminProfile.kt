package com.example.youthsync

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
            eventName = binding.ETSearch.text.toString().trim().lowercase()
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
                    binding.AttendeesEvent.text = eventName
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
                    attendeesList.add(Pair(name, attendedAt))
                }

                val recordTable = createRecordTable(attendeesList)
                binding.parentLayout.addView(recordTable)


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


    private fun createRecordTable(attendeesList: List<Pair<String, String>>): TableLayout {
        val context = requireContext()
        val recordTable = TableLayout(context).apply {
            setPadding(16, 32, 16, 32)
            setBackgroundResource(R.drawable.thin_et_border)
            val params = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 40)
            layoutParams = params
        }

        // Add header row
        val headerRow = createTableRow(context, "Name", "Attended At", isHeader = true)
        recordTable.addView(headerRow)

        // Add data rows
        for ((name, attendedAt) in attendeesList) {
            val dataRow = createTableRow(context, name, attendedAt)
            recordTable.addView(dataRow)
        }

        return recordTable
    }

    private fun createTableRow(context: Context, field: String, value: String, isHeader: Boolean = false): TableRow {
        return TableRow(context).apply {
            setPadding(16, 8, 16, 8)

            val fieldTextView = TextView(context).apply {
                text = field
                setTextSize(18f)
                setTypeface(null, if (isHeader) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                setTextColor(ContextCompat.getColor(context, if (isHeader) R.color.black else R.color.bg))
                setPadding(16, 16, 16, 16) // Increased right padding for more space
            }

            val valueTextView = TextView(context).apply {
                text = value
                setTextSize(18f)
                setTypeface(null, if (isHeader) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                setTextColor(ContextCompat.getColor(context, if (isHeader) R.color.black else R.color.bg))
                setPadding(16, 16, 16, 16)
            }

            addView(fieldTextView, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f))
            addView(valueTextView, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f))
        }
    }



}