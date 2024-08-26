package com.yeudaby.yedidim.tires

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.storage.FirebaseStorage
import com.yeudaby.yedidim.tires.ui.theme.YedidimTiresTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {

    lateinit var viewModel: MainActivityViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainActivityViewModel(this)

        enableEdgeToEdge()
        setContent {
            YedidimTiresTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier.padding(innerPadding)
                        ) {

                            var num by remember { mutableStateOf<Long?>(null) }
                            val results by viewModel.carDetails.collectAsState()
                            val loading by viewModel.loading.collectAsState()
                            val error by viewModel.error.collectAsState()
                            val coroutineScope = rememberCoroutineScope()
                            var chosenRecord by remember { mutableStateOf<Record?>(null) }

                            var disclaimerShown by remember { mutableStateOf(false) }

                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current

                            fun openWebsite() {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("https://yeudaby.com")
                                startActivity(intent)
                            }

                            fun openMail() {
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("mailto:aviad@yedidim-il.org")
                                startActivity(intent)
                            }

                            fun search() = coroutineScope.launch {
                                viewModel.fetchCarDetails(num!!)
                            }

                            LaunchedEffect(key1 = num) {
                                viewModel.clearResults()
                                if (num != null && num!! > 99_999) search()
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Image(
                                    painterResource(id = R.drawable.yedidim_logo_ho),
                                    contentDescription = "Yedidim Logo",
                                    modifier = Modifier
                                        .height(40.dp)
                                )

                                IconButton(
                                    onClick = { disclaimerShown = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = num?.toString() ?: "",
                                    onValueChange = {
                                        num = it.toLongOrNull()
                                    },
                                    placeholder = { Text(stringResource(id = R.string.carNumber)) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 3.sp,
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center,
                                    ),
                                    modifier = Modifier
                                        .background(Color.Yellow, MaterialTheme.shapes.medium)
                                        .focusRequester(focusRequester)
                                        .weight(1f),
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.screenshot_2024_06_23_at_14_09_56),
                                            contentDescription = null,
                                            modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight)
                                        )
                                    },
                                    shape = MaterialTheme.shapes.medium,
                                    singleLine = true,
                                    trailingIcon = {
                                        if (loading) CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        else
                                            IconButton(
                                                onClick = {
                                                    search()
                                                },
                                                enabled = num != null
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search"
                                                )
                                            }
                                    },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            search()
                                            focusManager.clearFocus()
                                        },
                                        onDone = {
                                            search()
                                            focusManager.clearFocus()
                                        }
                                    )
                                )

                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                if (error != null) {
                                    ElevatedCard(onClick = { /*TODO*/ }) {

                                    }
                                }
                                if (!loading)
                                    if (results.isNullOrEmpty()) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .fillMaxWidth(),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = null
                                                    )
                                                    Text(
                                                        text = stringResource(id = R.string.noResults),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        textAlign = TextAlign.Start
                                                    )
                                                }
                                            }
                                            if (num != null && num!! > 100_000) {
                                                Button(
                                                    onClick = {
                                                        chosenRecord = Record.empty(num!!)
                                                    },
                                                    Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = stringResource(
                                                            id = R.string.useAnyway,
                                                            num!!
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn {
                                            item {
                                                Text(
                                                    text = stringResource(id = R.string.chooseRecord),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                            items(results ?: emptyList()) {
                                                ElevatedCard(
                                                    onClick = { chosenRecord = it },
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(8.dp)

                                                            .fillMaxWidth(),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "${it.tozeret_nm} ${it.kinuy_mishari} ${it.tzeva_rechev}",
                                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Text(
                                                            text = "${it.ramat_gimur} • ${it.shnat_yitzur}",
                                                            style = MaterialTheme.typography.headlineSmall,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Divider(
                                                            modifier = Modifier
                                                                .fillMaxWidth(0.5f)
                                                                .padding(8.dp)
                                                        )
                                                        Text(
                                                            text = "${stringResource(id = R.string.zmigKidmi)} ${it.zmig_kidmi}",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Text(
                                                            text = "${stringResource(id = R.string.zmigAhori)} ${it.zmig_ahori}",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }

                            if (disclaimerShown) {
                                Dialog(
                                    onDismissRequest = { disclaimerShown = false }
                                ) {
                                    ElevatedCard {
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.disclaimer),
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.background,
                                                        MaterialTheme.shapes.medium
                                                    )
                                                    .padding(8.dp),
                                            )
                                            Divider()
                                            Text(
                                                text = stringResource(
                                                    id = R.string.version,
                                                    BuildConfig.VERSION_NAME
                                                )
                                            )

                                            Button(
                                                onClick = { openWebsite() },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(stringResource(id = R.string.website))
                                            }
                                            Button(
                                                onClick = { openMail() },
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(stringResource(id = R.string.contact))
                                            }
                                            OutlinedButton(
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = {
                                                    disclaimerShown = false
                                                }) {
                                                Text(stringResource(id = R.string.closeDialog))
                                            }
                                        }
                                    }
                                }
                            }


                            if (chosenRecord != null) {
                                UploadImageDialog(
                                    chosenRecord!!,
                                    exit = {
                                        this@MainActivity.finish()
                                    },
                                    restart = {
                                        chosenRecord = null
                                        viewModel.clearResults()
                                    },
                                ) {
                                    chosenRecord = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class Status {
    Static,
    Processing,
    Uploading,
    Done,
    Error
}

@Composable
fun UploadImageDialog(
    record: Record,
    exit: () -> Unit,
    restart: () -> Unit,
    dismiss: () -> Unit
) {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val context = LocalContext.current
    var status by remember { mutableStateOf(Status.Static) }
    var totalBytes by remember { mutableLongStateOf(0L) }
    var progressBytes by remember { mutableLongStateOf(0L) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.let {
            imageUris = it
            imageBitmaps = it.map { uri ->
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            imageBitmaps = imageBitmaps + it
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Log.e("Permission", "Camera permission denied")
            context.toast("Camera permission denied")
        }
    }

    fun uploadImage() {
        status = Status.Processing

        imageBitmaps.forEach { bitmap ->
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()
            val nowTimestamp = System.currentTimeMillis()
            val storageRef =
                FirebaseStorage.getInstance().reference.child("tires/${record.mispar_rechev}-${nowTimestamp}.jpg")
            status = Status.Uploading
            storageRef.putBytes(imageData)
                .addOnProgressListener { taskSnapshot ->
                    totalBytes += taskSnapshot.totalByteCount
                    progressBytes += taskSnapshot.bytesTransferred
                }
                .addOnSuccessListener {
                    Log.d("Upload", "Image uploaded successfully")
                    if (status != Status.Error) status = Status.Done
                }
                .addOnFailureListener { exception ->
                    Log.e("Upload", "Image upload failed", exception)
                    status = Status.Error
                }
        }
    }

    LaunchedEffect(key1 = progressBytes, key2 = totalBytes) {
        Log.d("Upload", "Progress: $progressBytes / $totalBytes")
    }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
//                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (imageBitmaps.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.instructions),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.shapes.medium
                            )
                            .padding(8.dp),
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            galleryLauncher.launch("image/*")
                        }) {
                        Text(stringResource(id = R.string.fromGallery))
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),

                        onClick = {
                            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }) {
                        Text(stringResource(id = R.string.fromCamera))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        items(imageBitmaps) { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = imageBitmaps.isNotEmpty() && status != Status.Uploading && status != Status.Processing,
                        onClick = {
                            if (status === Status.Done || status === Status.Error) exit()
                            else uploadImage()
                        }) {
                        Text(
                            when (status) {
                                Status.Static -> stringResource(id = R.string.upload)
                                Status.Processing -> stringResource(id = R.string.processing)
                                Status.Uploading -> stringResource(id = R.string.uploading)
                                Status.Done -> stringResource(id = R.string.uploaded)
                                Status.Error -> stringResource(id = R.string.error)
                            }
                        )
                    }
                    if (status === Status.Done) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                restart()
                            }) {
                            Text(stringResource(id = R.string.restart))
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                status = Status.Static
                                imageBitmaps = emptyList()
                                imageUris = emptyList()
                                totalBytes = 0
                                progressBytes = 0
                            }) {
                            Text(stringResource(id = R.string.upload_another))
                        }
                    }
                    if (status === Status.Processing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (status === Status.Uploading) {
                        CircularProgressIndicator(
                            progress = (progressBytes.toFloat() / totalBytes).takeUnless { it.isNaN() }
                                ?: 0f,
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(text = "${progressBytes / 1024} KB / ${totalBytes / 1024} KB")
                        }
                    }
                    Text(
                        text = when (status) {
                            Status.Done -> stringResource(id = R.string.close)
                            Status.Uploading -> stringResource(id = R.string.dontClose)
                            Status.Processing -> stringResource(id = R.string.dontClose)
                            Status.Error -> stringResource(id = R.string.tryAgain)
                            else -> ""
                        },
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (status === Status.Done) {
                        Text(
                            text = stringResource(id = R.string.thankYou),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                        )
                    }
                }
            }
        }
    }
}


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}