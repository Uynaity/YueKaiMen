package com.uynaity.opendoor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uynaity.opendoor.ui.LoginScreen
import com.uynaity.opendoor.ui.MainScreen
import com.uynaity.opendoor.ui.theme.悦开门Theme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            悦开门Theme {
                val navController = rememberNavController()
                val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val savedPhone = sharedPreferences.getString("phone", "")
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ), title = {
                            Text(
                                stringResource(id = R.string.app_name),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }, scrollBehavior = scrollBehavior
                    )
                }) { innerPadding ->
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
