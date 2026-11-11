package com.yosrhammami.daggerlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yosrhammami.daggerlab.data.GreetingRepository
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme
import javax.inject.Inject

class MainActivity: ComponentActivity() {

    @Inject
    lateinit var greetingRepository: GreetingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as DaggerLabApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        val formatter = (application as DaggerLabApplication).appComponent.timeFormatter()
        val component = (application as DaggerLabApplication).appComponent
        enableEdgeToEdge()
        setContent {
            DaggerLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    Greeting(
                        nameApp = component.appInfo().appName(),
                        time = formatter.format(90_000) ,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    nameApp: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = " $nameApp!",
            modifier = modifier
        )
        Text(
            text = " $time!",
            modifier = modifier
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DaggerLabTheme {
        Greeting("Android", "time")
    }
}