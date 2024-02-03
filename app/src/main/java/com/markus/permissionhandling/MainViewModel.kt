package com.markus.permissionhandling

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    // [CAMERA, RECORD_AUDIO]
    val visiblePermissionDialogQueue = mutableStateListOf<String>() //simulate a queue data structure. Create a queue

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst() //remove entry of the item that was put to the queue first
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if(!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission) //inserts an element into the list at the specified index.
            //Queue in our permission at the given index

        }
    }
}