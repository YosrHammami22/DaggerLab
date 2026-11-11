package com.yosrhammami.daggerlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yosrhammami.daggerlab.data.GreetingRepository
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme
import javax.inject.Inject

class MainActivity: ComponentActivity() {

    @Inject
    lateinit var greetingRepository: GreetingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as DaggerLabApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaggerLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    Greeting(
                        name = greetingRepository.getName(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DaggerLabTheme {
        Greeting("Android")
    }
}