package com.example.youthsync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.youthsync.databinding.ActivityHomeScreenBinding
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

class HomeScreenActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHomeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateFragment(AdminHomeFragment())
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.home -> updateFragment(AdminHomeFragment())
                R.id.qr -> updateFragment(FragmentAdminQr())
                R.id.search -> updateFragment(AdminSearch())
                R.id.profile -> updateFragment(AdminProfile())
            }
            true
        }
    }


    private fun updateFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}