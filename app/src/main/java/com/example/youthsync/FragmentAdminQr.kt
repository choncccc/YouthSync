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

private const val CAMERA_REQUEST_CODE = 101

class FragmentAdminQr : Fragment() {
    private lateinit var binding: FragmentAdminQrBinding
    private var codeScanner: CodeScanner? = null // Nullable to avoid crash

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

                decodeCallback = DecodeCallback {
                    requireActivity().runOnUiThread {
                        binding.txtResult.text = it.text
                    }
                }

                errorCallback = ErrorCallback {
                    requireActivity().runOnUiThread {
                        Log.e("Main", "Camera initialization error: ${it.message}")
                    }
                }
            }
            codeScanner?.startPreview()
        } else {
            // Request permission if not granted
            makeRequest()
        }
    }

    private fun showCustomDialog() {
        val dialog = Dialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_enter_event, null)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.border))
        dialog.setContentView(dialogView)

        val getEventName = dialogView.findViewById<EditText>(R.id.eventName)
        val btnOk = dialogView.findViewById<Button>(R.id.okButton)

        btnOk.setOnClickListener {
            dialog.dismiss()

            // Ensure permission before setting up scanner
            setUpPermission()
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
                    setupCodeScanner() // Setup scanner after permission granted
                } else {
                    Toast.makeText(requireContext(), "CAMERA PERMISSION REQUIRED!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
