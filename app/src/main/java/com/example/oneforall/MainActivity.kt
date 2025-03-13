package com.example.oneforall


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.Manifest
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.oneforall.ui.theme.OneForAllTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            OneForAllTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // handeling permissions
    val permissionsArray = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )
    var permissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted = permissions.entries.all { it.value }
        if (permissionGranted) {
            Toast.makeText(context, "permissions Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }


    LaunchedEffect(Unit) {

        permissionLauncher.launch(permissionsArray)

    }


    //variable declarations

    var imageUri by remember { mutableStateOf<Uri?>(Uri.EMPTY) }

    var photoBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }
    var showDialog by remember { mutableStateOf(false) }

    var qualityFactor by remember { mutableIntStateOf(80) }
    var currentFileSize by remember { mutableLongStateOf(0L) }
    var estimatedFileSize by remember {
        mutableDoubleStateOf(0.0)
    }
    val scope = rememberCoroutineScope()
//image file to store image

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile(
            "Jpeg_${timeStamp}_original",
            ".jpg", storageDir
        )


    }
    // camera for taking images

    val cameraLauncher = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            try {
                if (success) imageUri?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    photoBitmap = bitmap
                    currentFileSize = getFileSize(context, uri)
                    estimatedFileSize = estimateCompressedFileSize(bitmap, qualityFactor)


                    //delete this later
                    Toast.makeText(context, "camera image captured", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("camera launcher", "error in camera launcher${e.message}")


            }


        }
    )
    //selecting from gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    )
    {
        try {
            if (it.resultCode == Activity.RESULT_OK) {
                val data: Intent? = it.data
                data?.data?.let { uri ->
                    imageUri = uri
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    photoBitmap = bitmap
                    currentFileSize = getFileSize(context, uri)
                    estimatedFileSize = estimateCompressedFileSize(bitmap, qualityFactor)
                    Toast.makeText(context, "gallery image fetched", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("gallery launcher", "error in gallery launcher${e.message}")

        }

    }

    //function for image compression
    fun compressBitmap(bitmap: Bitmap, quality: Int): Uri? {
        val file = "compressed_${System.currentTimeMillis()}.jpg"

        val contentValues= ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,file)
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING,1)
        }
val resolver= context.contentResolver
        val uri:Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)
        uri?.let {
            try{
                resolver.openOutputStream(it)?.use { outputStream ->

                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING,0  )
                resolver.update(it,contentValues,null,null)

            }catch (e :Exception  ){
               Toast.makeText(context,"error in compressing image",Toast.LENGTH_SHORT).show()

            }
        }
        return uri
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Upload Image") },
            text = { Text(text = "choose an option") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val file = createImageFile(context)
                    imageUri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider",
                        file
                    )
                    imageUri?.let { uri -> cameraLauncher.launch(uri) }
                        ?: run { Log.e("cameraScreen", "failed to create uri for the file") }


                }) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    galleryLauncher.launch(Intent(Intent.ACTION_PICK).apply {
                        type = "image/*"
                    })
                }) {
                    Text("choose from gallery")
                }

            })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Upload Photo") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            photoBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Crop
                )

            }
            Spacer(modifier = Modifier.height(20.dp))

            //slider to choose quality factor
            Slider(
                value = qualityFactor.toFloat(),
                onValueChange = { newValue ->
                    qualityFactor = newValue.toInt()
                    photoBitmap?.let { bitmap ->
                        estimatedFileSize = estimateCompressedFileSize(bitmap, qualityFactor)
                    }
                },
                valueRange = 0f..100f,
                steps = 99
            )
            //file sizes
            Text("current size:${currentFileSize / 1024} KB")
            Text("compressed size:${estimatedFileSize / 1024} KB")

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                scope.launch {
                    photoBitmap?.let { bitmap: Bitmap ->
                        // here image is compressed accordingly

                        val compressedUri = compressBitmap(bitmap, qualityFactor)
                        imageUri = compressedUri
                        compressedUri?.let { uri ->
                            Toast.makeText(context, "image compressed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }) {
                Text(text = "Save to Downloads")
            }
            if (imageUri != Uri.EMPTY) {
                Button(onClick = {                        //here is the share image intent
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        type = "image/jpeg"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent,"Share image to"))

                }) {
                    Text(text = "Share Image")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Upload Photo")
            }


        }


    }


}

fun getFileSize(context: Context, uri: Uri): Long {
    return context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0

}


fun estimateCompressedFileSize(bitmap: Bitmap, qualityFactor: Int): Double {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, qualityFactor, outputStream)
    val byteArray = outputStream.toByteArray()
    return byteArray.size.toDouble()

}