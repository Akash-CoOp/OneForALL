//package com.example.oneforall
//
//class temp {
//    import android.content.Context
//    import android.content.Intent
//    import android.net.Uri
//    import android.os.Bundle
//    import android.provider.MediaStore
//    import androidx.activity.compose.rememberLauncherForActivityResult
//    import androidx.activity.result.contract.ActivityResultContracts
//    import androidx.compose.foundation.Image
//    import androidx.compose.foundation.layout.*
//    import androidx.compose.material3.*
//    import androidx.compose.runtime.*
//    import androidx.compose.ui.Alignment
//    import androidx.compose.ui.Modifier
//    import androidx.compose.ui.graphics.asImageBitmap
//    import androidx.compose.ui.layout.ContentScale
//    import androidx.compose.ui.platform.LocalContext
//    import androidx.compose.ui.unit.dp
//    import androidx.core.content.FileProvider
//    import coil.compose.rememberImagePainter
//    import kotlinx.coroutines.launch
//    import java.io.File
//    import java.text.SimpleDateFormat
//    import java.util.*
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun CameraScreen() {
//        val context = LocalContext.current
//        var imageUri by remember { mutableStateOf<Uri?>(null) }
//        var photoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
//        var showDialog by remember { mutableStateOf(false) }
//        val scope = rememberCoroutineScope()
//
//        // Create a file to store the photo
//        fun createImageFile(context: Context): File {
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//            val storageDir = context.getExternalFilesDir(null)
//            return File.createTempFile(
//                "JPEG_${timeStamp}_", /* prefix */
//                ".jpg", /* suffix */
//                storageDir /* directory */
//            )
//        }
//
//        // Launcher for taking a photo
//        val cameraLauncher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.TakePicture(),
//            onResult = { success ->
//                if (success) {
//                    imageUri?.let { uri ->
//                        // Load the captured image into a Bitmap
//                        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
//                        photoBitmap = bitmap
//                    }
//                }
//            }
//        )
//
//        // Launcher for picking an image from the gallery
//        val galleryLauncher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == android.app.Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                data?.data?.let { uri ->
//                    imageUri = uri
//                    // Load the selected image into a Bitmap
//                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
//                    photoBitmap = bitmap
//                }
//            }
//        }
//
//        // Show dialog to choose between camera and gallery
//        if (showDialog) {
//            AlertDialog(
//                onDismissRequest = { showDialog = false },
//                title = { Text("Upload Image") },
//                text = { Text("Choose an option") },
//                confirmButton = {
//                    TextButton(onClick = {
//                        showDialog = false
//                        // Create a file for the photo
//                        val file = createImageFile(context)
//                        imageUri = FileProvider.getUriForFile(
//                            context,
//                            "${context.packageName}.provider",
//                            file
//                        )
//                        // Launch the camera
//                        cameraLauncher.launch(imageUri)
//                    }) {
//                        Text("Take Photo")
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = {
//                        showDialog = false
//                        // Launch the gallery
//                        galleryLauncher.launch(
//                            Intent(Intent.ACTION_PICK).apply {
//                                type = "image/*"
//                            }
//                        )
//                    }) {
//                        Text("Choose from Gallery")
//                    }
//                }
//            )
//        }
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("Upload Photo") }
//                )
//            }
//        ) { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Image preview
//                photoBitmap?.let { bitmap ->
//                    Image(
//                        bitmap = bitmap.asImageBitmap(),
//                        contentDescription = "Captured Photo",
//                        modifier = Modifier.size(200.dp),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//                Spacer(modifier = Modifier.height(20.dp))
//
//                // Upload Photo Button
//                Button(onClick = { showDialog = true }) {
//                    Text("Upload Photo")
//                }
//
//                // Compress and Save Button
//                if (photoBitmap != null) {
//                    Spacer(modifier = Modifier.height(20.dp))
//                    Button(onClick = {
//                        scope.launch {
//                            // Compress the photo (add your compression logic here)
//                            val compressedBitmap = compressBitmap(photoBitmap!!)
//                            // Save or use the compressed bitmap
//                            saveCompressedBitmap(context, compressedBitmap)
//                        }
//                    }) {
//                        Text("Compress and Save")
//                    }
//                }
//            }
//        }
//    }
//
//    // Example compression function
//    fun compressBitmap(bitmap: android.graphics.Bitmap): android.graphics.Bitmap {
//        // Add your compression logic here
//        return bitmap // Return the compressed bitmap
//    }
//
//    // Example function to save the compressed bitmap
//    fun saveCompressedBitmap(context: Context, bitmap: android.graphics.Bitmap) {
//        // Save the compressed bitmap to storage
//        val file = File(context.getExternalFilesDir(null), "compressed_photo.jpg")
//        file.outputStream().use { outputStream ->
//            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
//        }
//    }
//}
//
//// Permission dialog
//if (showPermissionDialog) {
//    AlertDialog(
//        onDismissRequest = { showPermissionDialog = false },
//        title = { Text("Permission Required") },
//        text = { Text("Camera and storage permissions are required to use this feature") },
//        confirmButton = {
//            TextButton(onClick = {
//                showPermissionDialog = false
//                permissionState.launchMultiplePermissionRequest()
//            }) {
//                Text("Grant Permissions")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = { showPermissionDialog = false }) {
//                Text("Cancel")
//            }
//        }
//    )
//}