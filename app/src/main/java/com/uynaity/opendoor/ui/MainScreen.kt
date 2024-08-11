package com.uynaity.opendoor.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.uynaity.opendoor.R
import com.uynaity.opendoor.Routes
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val phone = sharedPreferences.getString("phone", "")
    var auto by remember { mutableStateOf(sharedPreferences.getBoolean("auto", false)) }
    var selectedDoor by remember {
        mutableStateOf(
            sharedPreferences.getString(
                "selected_door", "east"
            ) ?: "east"
        )
    }

    LaunchedEffect(Unit) {
        if (auto) {
            sendPostRequest(context, selectedDoor, phone)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = auto,
                onCheckedChange = {
                    auto = it
                    with(sharedPreferences.edit()) {
                        putBoolean("auto", it)
                        apply()
                    }
                },
            )
            Text(text = stringResource(id = R.string.auto_open))
        }
        Spacer(modifier = Modifier.size(16.dp))
        DoorSelection(
            selectedDoor = selectedDoor, onDoorSelected = { door ->
                selectedDoor = door
                with(sharedPreferences.edit()) {
                    putString("selected_door", door)
                    apply()
                }
            }, enabled = auto
        )
        Spacer(modifier = Modifier.size(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RoundBtn(text = stringResource(id = R.string.east_door),
                onClick = { sendPostRequest(context, "east", phone) })
            Spacer(modifier = Modifier.width(32.dp))
            RoundBtn(text = stringResource(id = R.string.west_door),
                onClick = { sendPostRequest(context, "west", phone) })
        }
        Spacer(modifier = Modifier.size(64.dp))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(CenterHorizontally)
        ) {
            Text(text = stringResource(id = R.string.btn_logout))
        }
    }
    if (showLogoutDialog) {
        LogoutAlert(onConfirm = {
            showLogoutDialog = false
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("phone", "")
                apply()
            }
            navController.navigate(Routes.login) {
                popUpTo(Routes.main) { inclusive = true }
            }
        }, onDismiss = { showLogoutDialog = false })
    }
}

@Composable
fun DoorSelection(selectedDoor: String, onDoorSelected: (String) -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RadioButton(
            selected = selectedDoor == "east",
            onClick = { onDoorSelected("east") },
            enabled = enabled
        )
        Text(text = stringResource(id = R.string.east_door))
        Spacer(modifier = Modifier.width(32.dp))
        RadioButton(
            selected = selectedDoor == "west",
            onClick = { onDoorSelected("west") },
            enabled = enabled
        )
        Text(text = stringResource(id = R.string.west_door))
    }
}

@Composable
fun RoundBtn(
    text: String, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Button(
        onClick = onClick, modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
    ) {
        Text(text = text)
    }
}

@Composable
fun LogoutAlert(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.logout_title)) },
        text = { Text(text = stringResource(id = R.string.logout_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        })
}

fun sendPostRequest(context: Context, door: String, phone: String?) {

    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    val equipmentId = if (door == "east") 15247 else 15248
    val doorName = if (equipmentId == 15247) "东门" else "西门"

    Toast.makeText(context, "$doorName 开门中...", Toast.LENGTH_SHORT).show()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response: HttpResponse =
                client.post("https://wxapi1210.grpu.com.cn/thirdParty/extOpenKey") {
                    contentType(ContentType.Application.Json)
                    headers {
                        append(HttpHeaders.ContentType, "application/json;charset=UTF-8")
                    }
                    setBody(RequestBody(phone = phone ?: "", equipmentId = equipmentId))
                }

            val responseBody = response.bodyAsText()

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context, "$doorName $responseBody", Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Serializable
data class RequestBody(val phone: String, val equipmentId: Int)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    MainScreen(navController = navController)
}
