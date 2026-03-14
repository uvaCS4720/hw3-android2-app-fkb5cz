package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import edu.nd.pmcburne.hwapp.one.ui.BasketballScoresScreen
import edu.nd.pmcburne.hwapp.one.ui.BasketballViewModel


class MainActivity : ComponentActivity() {
    // viewmodel instance to survive config changes
    private val viewModel : BasketballViewModel by viewModels {
        BasketballViewModel.Factory((application as BasketballApp).repository)
    }

    // initialize ui
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                BasketballScoresScreen(viewModel = viewModel)
            }
        }
    }
}