package com.example.mvvmexample.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mvvmexample.R
import com.example.mvvmexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController

        // bottom navigation과 main container의 navcontroller를 연결시켜준다.
        binding.bottomNavigationView.setupWithNavController(navController)
        // main container, bottom navigation을 gone 처리해주어 login container를 화면에 보여준다.
        binding.mainContainer.visibility = View.GONE
        binding.bottomNavigationView.visibility = View.GONE
    }
}