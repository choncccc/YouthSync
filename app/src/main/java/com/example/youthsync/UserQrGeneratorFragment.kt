package com.example.youthsync

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Bitmap
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.youthsync.databinding.FragmentUserQrGeneratorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.firebase.firestore.FirebaseFirestore

class UserQrGeneratorFragment : Fragment() {
    private lateinit var binding: FragmentUserQrGeneratorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDB: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = FragmentUserQrGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestoreDB = FirebaseFirestore.getInstance()
        val user= auth.currentUser
        //Null check kung null nga
        if (user != null){
            val ref = firestoreDB.collection("users").document(user.uid)
            ref.get().addOnSuccessListener {
                if (it != null && it.exists()) {
                    val fName = it.getString("firstName")
                    val lName = it.getString("lastName")
                    binding.TVName.text = "$fName $lName"
                    val qrCodeBitmap = generateQRCode(user.uid, 900, 900)
                    binding.QRCode.setImageBitmap(qrCodeBitmap)
                }
            }
        }else{
            Toast.makeText(requireContext(), "Error generating your QR Code", Toast.LENGTH_LONG).show()
        }


    }
    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
