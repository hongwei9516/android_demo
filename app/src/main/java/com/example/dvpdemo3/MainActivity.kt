package com.example.dvpdemo3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dvpdemo3.ui.MyDeviceScreen
import com.example.dvpdemo3.ui.components.BottomBar
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DvpDemo3Theme {
                MainLayout()
            }
        }
    }
}

@Composable
fun MainLayout() {
    Scaffold(
        bottomBar = {
            BottomBar()
        }
    ) { paddingValues ->
        MyDeviceScreen(Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DvpDemo3Theme {
        MainLayout()
    }
}