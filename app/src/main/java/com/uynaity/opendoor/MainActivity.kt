package com.uynaity.opendoor

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uynaity.opendoor.ui.LoginScreen
import com.uynaity.opendoor.ui.MainScreen
import com.uynaity.opendoor.ui.theme.悦开门Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            悦开门Theme {
                val navController = rememberNavController()
                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val savedPhone = sharedPreferences.getString("phone", "")
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (savedPhone.isNullOrEmpty()) Routes.login else Routes.main,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.login) {
                            LoginScreen(navController)
                        }
                        composable(Routes.main) {
                            MainScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
