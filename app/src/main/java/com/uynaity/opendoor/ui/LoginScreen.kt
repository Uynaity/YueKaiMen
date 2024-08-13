package com.uynaity.opendoor.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.uynaity.opendoor.PostClient
import com.uynaity.opendoor.R
import com.uynaity.opendoor.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController, modifier: Modifier = Modifier) {
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(128.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(64.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(text = stringResource(id = R.string.user_phone)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (phone.length == 11) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val client = PostClient()
                        val doorInfoList = client.getDoorInfo(context, phone)
                        if (doorInfoList != null) {
                            val sharedPreferences =
                                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putString("phone", phone)
                                apply()
                            }
                            withContext(Dispatchers.Main) {
                                navController.navigate(Routes.main) {
                                    popUpTo(Routes.login) { inclusive = true }
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "手机号码格式错误", Toast.LENGTH_SHORT).show()
                }
            }, modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(id = R.string.btn_login))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}