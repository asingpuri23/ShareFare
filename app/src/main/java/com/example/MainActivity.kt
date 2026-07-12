package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ShareFareRepository
import com.example.ui.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = ShareFareRepository(
            userDao = database.userDao(),
            disputeDao = database.disputeDao(),
            messageDao = database.messageDao(),
            rideDao = database.rideDao()
        )
        
        // Setup ViewModel
        val factory = ShareFareViewModelFactory(application, repository)
        val viewModel: ShareFareViewModel by viewModels { factory }
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Full bleed navigation with custom safe inset padding handled inside components
                    AppNavigation(viewModel)
                }
            }
        }
    }
}
