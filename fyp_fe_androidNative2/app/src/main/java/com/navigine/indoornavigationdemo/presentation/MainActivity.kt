package com.navigine.indoornavigationdemo.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.navigine.indoornavigationdemo.navigation.AppNavigator
import com.navigine.indoornavigationdemo.R
import com.navigine.indoornavigationdemo.presentation.locations.LocationsScreen
import com.navigine.indoornavigationdemo.presentation.locations.LocationsViewModel
import com.navigine.indoornavigationdemo.presentation.ui.theme.IndoorNavigationDemoTheme
import com.navigine.indoornavigationdemo.utils.PermissionUtils
import com.navigine.indoornavigationdemo.utils.PermissionUtils.REQUIRED_PERMISSIONS
import com.navigine.indoornavigationdemo.presentation.ui.screens.profile.ProfileScreen

class MainActivity : ComponentActivity() {

    private val locationsViewModel : LocationsViewModel by viewModels ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IndoorNavigationDemoTheme {
//                PermissionHandler(viewModel = locationsViewModel)
                AppNavigator()
            }
        }
    }
}

//@Composable
//private fun PermissionHandler(
//    viewModel: LocationsViewModel
//) {
//    val context = LocalContext.current
//
//    var permissionsGranted by remember {
//        mutableStateOf(
//            PermissionUtils.arePermissionsGranted(
//                context
//            )
//        )
//    }
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            permissionsGranted = PermissionUtils.arePermissionsGranted(context)
//        }
//
//    )
//
//    LaunchedEffect(permissionsGranted) {
//        if (!permissionsGranted) {
//            permissionLauncher.launch(REQUIRED_PERMISSIONS)
//        }
//    }
//
//
//    if (permissionsGranted) {
//
//        LocationsScreen(
//            viewModel = viewModel
//        )
//    } else {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(stringResource(id = R.string.grant_permissions_to_app))
//            Button(onClick = {
//                permissionLauncher.launch(REQUIRED_PERMISSIONS)
//            }) {
//                Text(stringResource(id = R.string.grant_permissions))
//            }
//        }
//    }
//}