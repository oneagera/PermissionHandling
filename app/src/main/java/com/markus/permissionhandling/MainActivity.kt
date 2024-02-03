package com.markus.permissionhandling

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.markus.permissionhandling.ui.theme.PermissionHandlingTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU) //FOR READ MEDIA IMAGES permission
    private val permissionsToRequest = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionHandlingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = viewModel<MainViewModel>()
                    val dialogueQueue = viewModel.visiblePermissionDialogQueue
                    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
                        //Contract defines which activity gets launched for what kind of the result.
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted ->
                            viewModel.onPermissionResult(
                                permission = Manifest.permission.CAMERA,
                                isGranted = isGranted
                            )
                        }
                    )

                    val multiplePermissionsResultLauncher = rememberLauncherForActivityResult(
                        //Contract defines which activity gets launched for what kind of the result.
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { perms -> //map of strings that match to the corresponding boolean
                            perms.keys.forEach { permission ->
                                viewModel.onPermissionResult(
                                    permission = permission,
                                    isGranted = perms[permission] == true
                                )
                            }
                        }
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                cameraPermissionResultLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
                        ) {
                            Text(text = "Request one permission")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                multiplePermissionsResultLauncher.launch(
                                    permissionsToRequest
                                )
                            }
                        ) {
                            Text(text = "Request multiple permissions")
                        }
                    }

                    dialogueQueue
                        .reversed()
                        .forEach { permission ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                PermissionDialog(
                                    permissionTextProvider = when (permission) {
                                        Manifest.permission.CAMERA -> {
                                            CameraPermissionTextProvider()
                                        }

                                        Manifest.permission.RECORD_AUDIO -> {
                                            AudioPermissionTextProvider()
                                        }

                                        Manifest.permission.CALL_PHONE -> {
                                            PhonePermissionTextProvider()
                                        }

                                        Manifest.permission.READ_CONTACTS -> {
                                            ContactsPermissionTextProvider()
                                        }

                                        Manifest.permission.READ_MEDIA_IMAGES -> {
                                            MediaPermissionTextProvider()
                                        }

                                        else -> return@forEach

                                    },
                                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                        permission
                                    ),
                                    onDismiss = viewModel::dismissDialog,
                                    onOkClick = {
                                        viewModel.dismissDialog()
                                        multiplePermissionsResultLauncher.launch(
                                            arrayOf(permission)
                                        )
                                    },
                                    onGoToAppSettingsClick = ::openAppSettings//call to openAppSettings Activity
                                )
                            }
                        }
                }
            }
        }
    }
}

fun Activity.openAppSettings() { //extension fun for onGoToAppSettings click
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity) //start an activity with the intent we created
}



