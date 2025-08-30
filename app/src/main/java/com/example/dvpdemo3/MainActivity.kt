package com.example.dvpdemo3

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dvpdemo3.ui.MyDeviceScreen
import com.example.dvpdemo3.ui.components.BottomBar
import com.example.dvpdemo3.ui.theme.DvpDemo3Theme
import com.example.dvpdemo3.ui.viewmodel.ScanViewModel
import com.example.dvpdemo3.ui.viewmodel.ScanViewModelFactory

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

    val scanViewModel: ScanViewModel = viewModel(
    factory = ScanViewModelFactory(LocalContext.current.applicationContext as Application)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        scanViewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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