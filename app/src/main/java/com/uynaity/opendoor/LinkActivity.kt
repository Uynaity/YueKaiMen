package com.uynaity.opendoor

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.uynaity.opendoor.ui.theme.悦开门Theme
import kotlinx.coroutines.delay

class LinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            悦开门Theme {
                val deepLinkData = intent.data?.host
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DefaultScreen(deepLinkData.toString(), modifier = Modifier.padding(innerPadding)) {
                        finishAffinity()
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultScreen(door: String, modifier: Modifier = Modifier, onTaskCompleted: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val phone = sharedPreferences.getString("phone", "")
    val client = PostClient()

    LaunchedEffect(Unit) {
        if (phone != null) {
            client.sendPostRequest(context, door, phone)
        }
        delay(5000)
        onTaskCompleted()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = door)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    悦开门Theme {
        DefaultScreen("east", onTaskCompleted = {})
    }
}
