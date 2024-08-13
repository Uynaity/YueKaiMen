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
import com.uynaity.opendoor.DoorInfo
import com.uynaity.opendoor.PostClient
import com.uynaity.opendoor.R
import com.uynaity.opendoor.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.toString

@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val doorInfoJson = sharedPreferences.getString("door_info", null)
    val doorInfoList: List<DoorInfo> = if (doorInfoJson != null) {
        Json.decodeFromString(doorInfoJson)
    } else {
        emptyList()
    }
    val phone = sharedPreferences.getString("phone", "")
    var auto by remember { mutableStateOf(sharedPreferences.getBoolean("auto", false)) }
    var selectedDoor by remember {
        mutableStateOf(
            sharedPreferences.getString(
                "selected_door", doorInfoList.firstOrNull()?.equipmentId.toString()
            ) ?: ""
        )
    }
    val client = PostClient()

    LaunchedEffect(Unit) {
        if (auto) {
            val (code, message) = client.sendPostRequest(context, selectedDoor, phone)
            Toast.makeText(context, "$code $message", Toast.LENGTH_SHORT).show()
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
            doorInfoList = doorInfoList, selectedDoor = selectedDoor, onDoorSelected = { door ->
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
            doorInfoList.forEach { doorInfo ->
                RoundBtn(text = doorInfo.equipmentName, onClick = {
                    client.sendPostRequest(
                        context, doorInfo.equipmentId.toString(), phone
                    )
                })
                Spacer(modifier = Modifier.size(16.dp))
            }
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
                putString("selected_door", "")
                putBoolean("auto", false)
                apply()
            }
            navController.navigate(Routes.login) {
                popUpTo(Routes.main) { inclusive = true }
            }
        }, onDismiss = { showLogoutDialog = false })
    }
}

@Composable
fun DoorSelection(
    doorInfoList: List<DoorInfo>,
    selectedDoor: String,
    onDoorSelected: (String) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        doorInfoList.forEach { doorInfo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                RadioButton(
                    selected = selectedDoor == doorInfo.equipmentId.toString(),
                    onClick = { onDoorSelected(doorInfo.equipmentId.toString()) },
                    enabled = enabled
                )
                Text(text = doorInfo.equipmentName)
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun RoundBtn(
    text: String, modifier: Modifier = Modifier, onClick: suspend () -> Pair<Int, String>
) {
    val context = LocalContext.current

    Button(
        onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                val (code, message) = onClick()
                Toast.makeText(context, "$code $message", Toast.LENGTH_SHORT).show()
            }
        }, modifier = modifier
            .size(128.dp)
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


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    MainScreen(navController = navController)
}
