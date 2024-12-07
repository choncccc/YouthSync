package com.example.youthsync

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.youthsync.databinding.FragmentAdminQrBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

private const val CAMERA_REQUEST_CODE = 101

class FragmentAdminQr : Fragment() {
    private var eventID: String? = null
    private lateinit var binding: FragmentAdminQrBinding
    private var codeScanner: CodeScanner? = null
    private var scannedUserUID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = FragmentAdminQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showCustomDialog()
    }

    private fun setupCodeScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {

            codeScanner = CodeScanner(requireContext(), binding.scannerView).apply {
                camera = CodeScanner.CAMERA_BACK
                formats = CodeScanner.ALL_FORMATS
                autoFocusMode = AutoFocusMode.SAFE
                scanMode = ScanMode.CONTINUOUS
                isAutoFocusEnabled = true
                isFlashEnabled = false

                decodeCallback = DecodeCallback { result ->
                    requireActivity().runOnUiThread {
                        scannedUserUID = result.text
                        binding.txtResult.text = scannedUserUID
                    }
                }
                errorCallback = ErrorCallback {
                    requireActivity().runOnUiThread {
                        Log.e("Main", "Camera initialization error: ${it.message}")
                    }
                }
            }
            codeScanner?.startPreview()
            binding.btnAttend.setOnClickListener {
                val uid = scannedUserUID
                if (uid != null && eventID != null) {
                    eventAttendees(uid, eventID!!)
                } else {
                    Toast.makeText(requireContext(), "Scan a user QR code first!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            makeRequest()
        }
    }

    private fun eventAttendees(userUID: String, eventID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userUID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "Unknown"
                    val fullName = "$firstName $lastName"

                    val dateFormat = SimpleDateFormat("MM-dd-yy hh:mm a", Locale.getDefault())
                    val formattedDate = dateFormat.format(System.currentTimeMillis())

                    val attendeeData = hashMapOf(
                        "userID" to userUID,
                        "Name" to fullName,
                        "attendedAt" to formattedDate
                    )
                    db.collection("Events").document(eventID)
                        .collection("Attendees").add(attendeeData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Attendee added!", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error adding attendee: ${e.message}", Toast.LENGTH_LONG).show()
                            Log.e("FragmentAdminQr", "Error adding attendee", e)
                        }
                } else {
                    Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user data: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FragmentAdminQr", "Error fetching user data", e)
            }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_enter_event, null)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.border))
        dialog.setContentView(dialogView)

        val getEventName = dialogView.findViewById<EditText>(R.id.eventName)
        val btnOk = dialogView.findViewById<Button>(R.id.okButton)

        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        btnOk.setOnClickListener {
            val eventName = getEventName.text.toString().trim().lowercase()
            if (eventName.isNotEmpty() && user != null) {
                val eventData = hashMapOf(
                    "eventName" to eventName,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("Events").add(eventData)
                    .addOnSuccessListener { documentReference ->
                        eventID = documentReference.id
                        Toast.makeText(requireContext(), "Event added with ID: $eventID", Toast.LENGTH_SHORT).show()
                        setUpPermission()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error adding event: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please enter an event name or ensure you are logged in", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        codeScanner?.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner?.releaseResources()
    }

    private fun setUpPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        } else {
            setupCodeScanner()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCodeScanner()
                } else {
                    Toast.makeText(requireContext(), "CAMERA PERMISSION REQUIRED!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
