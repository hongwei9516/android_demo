package com.example.dvpdemo3

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainLayout() {
    Scaffold(
        bottomBar = {
            BottomBar()
        }
    ) {
        MyDeviceScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DvpDemo3Theme {
        MainLayout()
    }
}