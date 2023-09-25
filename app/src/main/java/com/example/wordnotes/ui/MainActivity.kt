package com.example.wordnotes.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wordnotes.R
import com.example.wordnotes.databinding.ActivityMainBinding
import com.example.wordnotes.ui.addeditword.AddEditWordFragment
import com.example.wordnotes.ui.auth.AuthFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @VisibleForTesting
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setUpNavigation()
        controlBottomNavVisibility()
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNav.apply {
            setupWithNavController(navController)
            setOnItemReselectedListener { /* Do nothing */ }
        }
    }

    private fun controlBottomNavVisibility() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, fragment: Fragment) {
                when (fragment) {
                    is AddEditWordFragment, is AuthFragment -> {
                        setBottomNavVisibility(View.GONE)
                        resetBottomNavAnimation()
                    }
                }
            }

            override fun onFragmentStopped(fm: FragmentManager, fragment: Fragment) {
                when (fragment) {
                    is AddEditWordFragment, is AuthFragment -> {
                        setBottomNavVisibility(View.VISIBLE)
                    }
                }
            }
        }, true)
    }

    fun setBottomNavVisibility(visibility: Int) {
        binding.bottomNav.visibility = visibility
    }

    fun slideOutBottomNav(duration: Long = 200, vararg relatedView: View) {
        ValueAnimator.ofInt(binding.bottomNav.height, 0).apply {
            setDuration(duration)
            addUpdateListener { updatedAnimation ->
                val translationAmount = binding.bottomNav.height.toFloat() - updatedAnimation.animatedValue as Int
                binding.bottomNav.translationY = translationAmount
                relatedView.forEach { it.translationY = translationAmount }
            }
            start()
        }
    }

    fun slideInBottomNav(duration: Long = 200, vararg relatedView: View) {
        ValueAnimator.ofInt(0, binding.bottomNav.height).apply {
            setDuration(duration)
            addUpdateListener { updatedAnimation ->
                val translationAmount = binding.bottomNav.height.toFloat() - updatedAnimation.animatedValue as Int
                binding.bottomNav.translationY = translationAmount
                relatedView.forEach { it.translationY = translationAmount }
            }
            start()
        }
    }

    fun resetBottomNavAnimation(vararg relatedView: View) {
        binding.bottomNav.translationY = 0f
        relatedView.forEach { it.translationY = 0f }
    }
}