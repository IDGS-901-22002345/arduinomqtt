package com.example.arduinomqtt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import kotlin.text.Charsets.UTF_8

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    ControlLEDsScreen()
                }
            }
        }
    }
}

@Composable
fun ControlLEDsScreen() {
    val host = "f8256c139ff44f64a24b74f9aebca6d9.s1.eu.hivemq.cloud"
    val username = "maico"
    val password = "123456Maico"
    val topic = "control-led"

    val client: Mqtt5BlockingClient = remember {
        MqttClient.builder()
            .useMqttVersion5()
            .serverHost(host)
            .serverPort(8883)
            .sslWithDefaultConfig()
            .buildBlocking()
    }

    var connectionStatus by remember { mutableStateOf("Desconectado") }

    LaunchedEffect(Unit) {
        try {
            client.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send()
            connectionStatus = "Conectado"
        } catch (e: Exception) {
            connectionStatus = "Error: ${e.message}"
        }
    }

    ControlLEDsUI(connectionStatus, topic, client)
}

@Composable
fun ControlLEDsUI(connectionStatus: String, topic: String, client: Mqtt5BlockingClient) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(colorScheme.primary, colorScheme.secondary)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Control de LEDs MQTT",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Estado: $connectionStatus",
            fontSize = 16.sp,
            color = if (connectionStatus == "Conectado") colorScheme.primary else colorScheme.error
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StyledButton("Turn All ON", colorScheme.primary) {
                client.publishWith().topic(topic).payload(UTF_8.encode("ALL_ON")).send()
            }
            StyledButton("Turn All OFF", colorScheme.error) {
                client.publishWith().topic(topic).payload(UTF_8.encode("ALL_OFF")).send()
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        LedButtonRow(client, topic, 1)
        Spacer(modifier = Modifier.height(12.dp))
        LedButtonRow(client, topic, 2)
        Spacer(modifier = Modifier.height(12.dp))
        LedButtonRow(client, topic, 3)
    }
}

@Composable
fun LedButtonRow(client: Mqtt5BlockingClient, topic: String, ledNumber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StyledButton("LED $ledNumber ON", colorScheme.secondary) {
            client.publishWith().topic(topic).payload(UTF_8.encode("L${ledNumber}_ON")).send()
        }
        StyledButton("LED $ledNumber OFF", colorScheme.tertiary) {
            client.publishWith().topic(topic).payload(UTF_8.encode("L${ledNumber}_OFF")).send()
        }
    }
}

@Composable
fun StyledButton(text: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .width(140.dp)
            .height(50.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = colorScheme.onPrimary
        )
    }
}