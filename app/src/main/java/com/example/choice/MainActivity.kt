package com.example.choice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeApp()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyComposeApp() {
    val context = LocalContext.current
    var userInput by remember { mutableStateOf("") }
    val savedInputs = remember { mutableStateOf(listOf<String>()) }
    var answer by remember { mutableStateOf("") }
    var animating by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }
    val saveButtonColor by remember { mutableStateOf(Color.Unspecified) }
    val chooseButtonColor by remember { mutableStateOf(Color.Unspecified) }
    val clearButtonColor by remember { mutableStateOf(Color.Unspecified) }
    var openMapDialog by remember { mutableStateOf(false) }

    // Animation setup
    val infiniteTransition = rememberInfiniteTransition()
    val animatedValue = infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = savedInputs.value.size,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing), repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "今天吃啥",
            style = TextStyle(fontSize = 24.sp, color = Color.Black),
            modifier = Modifier.padding(top = 16.dp)
        )

        // Input field
        OutlinedTextField(value = userInput,
            onValueChange = { userInput = it },
            label = { Text("想要吃的") })

        // Buttons row
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        savedInputs.value = savedInputs.value + userInput
                        userInput = ""
                    } else {
                        Toast.makeText(context, "沒東西是要吃啥", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = saveButtonColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("加到名單")
            }

            Button(
                onClick = {
                    if (savedInputs.value.isNotEmpty()) {
                        if (savedInputs.value.size == 1) {
                            answer = savedInputs.value.first()
                            Toast.makeText(context, "今天就吃 $answer", Toast.LENGTH_LONG).show()
                            openMapDialog = true
                        } else {
                            animating = true
                        }
                    } else {
                        Toast.makeText(context, "吃土去", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = chooseButtonColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("老天決定")
            }


            Button(
                onClick = {
                    if (answer.isNotBlank()) {
                        userInput = ""
                        answer = ""
                        Toast.makeText(context, "再給你一次機會", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = clearButtonColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("這把不算")
            }

            Button(
                onClick = {
                    if (savedInputs.value.isEmpty()) {
                        Toast.makeText(context, "快點想", Toast.LENGTH_SHORT).show()
                    } else {
                        userInput = ""
                        savedInputs.value = emptyList()
                        answer = ""
                        animating = false
                        Toast.makeText(context, "要選多久", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = clearButtonColor),
                modifier = Modifier.weight(1f)
            ) {
                Text("重頭再來")
            }
        }

        // Display user inputs
        Text(
            text = "口袋名單: ${savedInputs.value.joinToString(", ")}",
            style = TextStyle(fontSize = 20.sp, color = Color.Gray),
            modifier = Modifier.padding(top = 8.dp)
        )

        // Answer text
        if (animating && savedInputs.value.isNotEmpty()) {
            LaunchedEffect(key1 = savedInputs.value) {
                val startTime = System.currentTimeMillis()
                while (animating && System.currentTimeMillis() - startTime < 3000 && savedInputs.value.isNotEmpty()) {
                    currentIndex = animatedValue.value % savedInputs.value.size
                    delay(100)
                }
                animating = false
                if (savedInputs.value.isNotEmpty()) {
                    answer = savedInputs.value.random()
                    Toast.makeText(context, "今天就吃 $answer", Toast.LENGTH_LONG).show()
                    openMapDialog = true
                }
            }
            if (savedInputs.value.isNotEmpty()) {
                Text(
                    text = savedInputs.value[currentIndex],
                    style = TextStyle(fontSize = 80.sp, color = Color.Blue)
                )
            }
        } else {
            Text(
                text = answer, style = TextStyle(fontSize = 80.sp, color = Color.Blue)
            )
        }
        if (openMapDialog && answer.isNotEmpty()) {
            AlertDialog(onDismissRequest = {  },
                title = { Text("確認") },
                text = { Text("是否要在Google地圖中搜尋「$answer」？") },
                confirmButton = {
                    TextButton(onClick = {
                        openMapDialog = false
                        val gmmIntentUri = Uri.parse("geo:0,0?q=$answer")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }) {
                        Text("確定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openMapDialog = false }) {
                        Text("取消")
                    }
                })
        }
    }
}