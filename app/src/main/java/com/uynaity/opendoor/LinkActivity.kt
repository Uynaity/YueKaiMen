package com.uynaity.opendoor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.uynaity.opendoor.ui.RoundBtn
import com.uynaity.opendoor.ui.theme.悦开门Theme
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.toString

class LinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        setContent {
            悦开门Theme {
                val deepLinkData = intent?.data?.host
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DefaultScreen(
                        deepLinkData.toString(), modifier = Modifier.padding(innerPadding)
                    ) {
                        finishAffinity()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultScreen(id: String, modifier: Modifier = Modifier, onTaskCompleted: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val phone = sharedPreferences.getString("phone", "")
    val client = PostClient()
    val doorInfoJson = sharedPreferences.getString("door_info", null)
    val doorInfoList: List<DoorInfo> = if (doorInfoJson != null) {
        Json.decodeFromString(doorInfoJson)
    } else {
        emptyList()
    }
    val doorInfo = doorInfoList.find { it.equipmentId.toString() == id }
    val doorName = doorInfo?.equipmentName ?: "Unknown Door"
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
        LaunchedEffect(Unit) {
            if (phone != null) {
                val (code, message) = client.sendPostRequest(context, id, phone)
                Toast.makeText(context, "$code $message", Toast.LENGTH_SHORT).show()
                if (message == "ok") {
                    delay(3000)
                    onTaskCompleted()
                }
            }
        }

        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RoundBtn(text = doorName, onClick = { client.sendPostRequest(context, id, phone) })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    悦开门Theme {
        DefaultScreen("", onTaskCompleted = {})
    }
}
