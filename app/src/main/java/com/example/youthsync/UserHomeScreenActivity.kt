package com.example.youthsync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.youthsync.databinding.ActivityUserHomeScreenBinding

class UserHomeScreenActivity : AppCompatActivity() {
        private lateinit var binding : ActivityUserHomeScreenBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityUserHomeScreenBinding.inflate(layoutInflater)
            setContentView(binding.root)
            updateFragment(UserHomeFragment())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
                when(menuItem.itemId){
                    R.id.home -> updateFragment(UserHomeFragment())
                    /*R.id.qr -> updateFragment(FragmentAdminQr())
                    R.id.search -> updateFragment(AdminSearch())
                    R.id.profile -> updateFragment(AdminProfile())*/
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
