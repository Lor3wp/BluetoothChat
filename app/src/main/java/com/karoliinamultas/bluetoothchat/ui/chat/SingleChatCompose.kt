package com.karoliinamultas.bluetoothchat.ui.chat


//import com.karoliinamultas.bluetoothchat.service.ChatForegroundService
import android.bluetooth.BluetoothAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.karoliinamultas.bluetoothchat.*
import com.karoliinamultas.bluetoothchat.R
import com.karoliinamultas.bluetoothchat.data.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


private const val TAG = "ChatCompose"


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ChatWindow(
    navController: NavController,
    notificationManagerWrapper: NotificationManagerWrapper,
    mBluetoothAdapter: BluetoothAdapter,
    model: MyViewModel
) {
    //Statusbar
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)

    // Create a boolean variable
    // to store the display menu state
    var mDisplayMenu by remember { mutableStateOf(false) }

    // Colors on off
    var colorsOnOff = remember { mutableStateOf(false) }


    // fetching local context
    val mContext = LocalContext.current

    //Joined chatname
    val chatName = model.beaconFilter.observeAsState()

    //Topbar
    Scaffold(containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                elevation = 5.dp,
                backgroundColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        chatName.value ?: "Unknown Chat",
                        modifier = Modifier.padding(30.dp, 0.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.ShowChats.route) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack, contentDescription = "Back button"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                        Icon(
                            imageVector = Icons.Filled.Menu, contentDescription = "Menu button"
                        )
                    }
                    androidx.compose.material3.DropdownMenu(expanded = mDisplayMenu,
                        onDismissRequest = { mDisplayMenu = false }) {
                        // Creating dropdown menu item, on click
                        // would create a Toast message
                        DropdownMenuItem(onClick = {
                            Toast.makeText(
                                mContext, "Settings", Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Text(text = "Settings")
                        }
                        DropdownMenuItem(onClick = { colorsOnOff.value = !colorsOnOff.value }) {
                            val chatColorText =
                                if (colorsOnOff.value) "Colorful mode" else "Colorblind mode"
                            Text(text = chatColorText)
                        }
                    }
                },
            )
        },
        content = { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Chats(
                    Modifier,
                    notificationManagerWrapper,
                    navController,
                    mBluetoothAdapter,
                    model,
                    colorsOnOff
                )
            }
        })
}

private suspend fun getImage(url: URL): Bitmap = withContext(Dispatchers.IO) {
    val myConn = url.openStream()
    return@withContext BitmapFactory.decodeStream(myConn)
}
fun getBitmapFromURL(src: String?): Bitmap? {
    return try {


        val url = URL(src)
        val connection: HttpURLConnection = url
            .openConnection() as HttpURLConnection
        connection.setDoInput(true)
        connection.connect()
        val input: InputStream = connection.getInputStream()
        BitmapFactory.decodeStream(input)
    } catch (e: Exception) {
        Log.d("vk21", e.toString())
        null
    }
}
@Composable
fun ShowImage(urlText: String) {

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(urlText)
            .size(coil.size.Size.ORIGINAL) // Set the target size to load the image at.
            .build()
    )


    Image(
        painter = painter ,
        contentDescription = "image",
        contentScale = ContentScale.Crop,

        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
            .clip(RoundedCornerShape(8.dp))

//            .width(240.dp)
//            .height(300.dp)
            .size(406.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowChat(message: Message, modifier: Modifier = Modifier, colorsOnOff: MutableState<Boolean>) {
    val regex = message.message_content.split(":")
    val isImage = regex[0].equals("https")
    val messageHorizontalArrangement =
        if (message.local_message) Arrangement.End else Arrangement.Start

    val textColors_random = listOf(
        Color(0xFF00FDDC),
        Color(0xFFFFFFFF),
        Color(0xFF04E762),
        Color(0xFFFDE74C),
        Color(0xFFFF4365)
    )

    val randomTexts =
        if (colorsOnOff.value) MaterialTheme.colorScheme.background else textColors_random.random()


    val backgroundColors_random = listOf(
        Color(0xFF111D4A),
        Color(0xFF43AA8B),
        Color(0xFF8B635C),
        Color(0xFF60594D),
        Color(0xFF93A29B)
    )

    val randomBack =
        if (colorsOnOff.value) MaterialTheme.colorScheme.onBackground else backgroundColors_random.random()

    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .fillMaxWidth(), horizontalArrangement = messageHorizontalArrangement
    ) {
        Card(
            modifier = Modifier
                .width(256.dp)
                .padding(5.dp),

            colors = CardDefaults.cardColors(containerColor = randomBack),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            if (isImage){
                ShowImage(urlText = message.message_content)
            } else {
            Text(
                text = message.message_content,
                color = randomTexts,
                modifier = Modifier.padding(10.dp)
            )
            }
        }
    }
}


@Composable
fun Chats(
    modifier: Modifier = Modifier,
    notificationManagerWrapper: NotificationManagerWrapper,
    navController: NavController,
    mBluetoothAdapter: BluetoothAdapter,
    model: MyViewModel,
    colorsOnOff: MutableState<Boolean>
) {


    val inputvalue = remember { mutableStateOf(TextFieldValue()) }


    Column(modifier = Modifier.fillMaxSize()) {

        Surface(
            modifier = Modifier
                .padding(all = Dp(0f))
                .fillMaxHeight(0.88f)
        ) {
            ChatsList(
                model,
                colorsOnOff = colorsOnOff,
                notificationManagerWrapper = notificationManagerWrapper
            )
        }
        InputField(modifier, navController, mBluetoothAdapter, model)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun InputField(
    modifier: Modifier = Modifier,
    navController: NavController,
    mBluetoothAdapter: BluetoothAdapter,
    model: MyViewModel
) {
    val context = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    //BotMenu
    // Declaring a Boolean value to
    // store bottom sheet collapsed state
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val isVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState, sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.End
                ) {

                    IconButton(onClick = {
                        coroutineScope.launch {
                            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            } else {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                    },
                        modifier = Modifier
                            .height(60.dp)
                            .width(60.dp)
                            .padding(0.dp, 6.dp, 0.dp, 0.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.background),
                        content = {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Localized description"
                            )
                        })
                }
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        IconButton(onClick = {
                            navController.navigate(Screen.DrawingPad.route)
                            model.uploadingImage = true
                        },
                            modifier = Modifier
                                .height(60.dp)
                                .width(60.dp)
                                .padding(0.dp, 6.dp, 0.dp, 0.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.background),
                            content = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.draw),
                                    contentDescription = "Localized description"
                                )
                            })
                        CameraButton(context)
                        GalleryButton(context)
                    }
                }
            }
        }, sheetPeekHeight = 0.dp
    ) {
//        TextField(
//            value = text,
//            onValueChange = {text = it},
//            modifier = Modifier
//                .requiredHeightIn(80.dp, 80.dp)
//                .fillMaxWidth(0.8f)
//                .fillMaxHeight(0.8f)
//                .focusRequester(focusRequester),
//            placeholder = { Text(text = "Enter your message", color = Color(0xFF242124).copy(0.5f)) },
//            shape = RoundedCornerShape(100.dp),
//            trailingIcon = {
//                Row() {
//                    androidx.compose.material.Divider(
//                        color = Color(0xFF242124).copy(0.3f), //MaterialTheme.colorScheme.background.copy(0.2f),
//                        modifier = Modifier
//                            .padding(0.dp, 8.dp, 0.dp, 0.dp)
//                            .fillMaxHeight(0.6f)  //fill the max height
//                            .width(1.dp)
//                    )
//                    IconButton(
//                        onClick = { coroutineScope.launch {
//                            if (bottomSheetScaffoldState.bottomSheetState.isCollapsed){
//                                bottomSheetScaffoldState.bottomSheetState.expand()
//                            }else{
//                                bottomSheetScaffoldState.bottomSheetState.collapse()
//                            }
//                        } },
//                        modifier = Modifier
//                            .height(60.dp)
//                            .width(60.dp),
//                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF242124)),
//                        content = {
//                            Icon(
//                                imageVector = Icons.Filled.KeyboardArrowUp,
//                                contentDescription = "Localized description"
//                            )
//                        }
//                    )
//                }
//
//            },
//            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
//            keyboardOptions = KeyboardOptions(
//                capitalization = KeyboardCapitalization.Sentences,
//                keyboardType = KeyboardType.Text,
//                imeAction = ImeAction.Done,
//            ),
//            textStyle = TextStyle(
//                color = Color(0xFF242124),
//                fontSize = TextUnit.Unspecified,
//                fontFamily = FontFamily.SansSerif
//            ),
//            maxLines = 1,
//            singleLine = true,
//            colors = TextFieldDefaults.textFieldColors(
//                containerColor = Color(0xFFF5FEFD),
//                textColor = Color(0xFF242124),
//                disabledTextColor = Color.Transparent,
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                disabledIndicatorColor = Color.Transparent)
//            )
        Box(
            Modifier
                .drawWithCache {
                    val offsetY = (-5).dp.toPx()
                    val shadowColor = Color.Black
                    val shadowAlpha = 0.3f
                    val shadowBlur = 6.dp.toPx()
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            shadowColor.copy(alpha = shadowAlpha),
                            Offset(0f, offsetY),
                            size = Size(size.width, shadowBlur),
                            alpha = shadowAlpha,
                            style = Fill
                        )
                    }
                }
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .fillMaxHeight(1f)) {
            Row(
                Modifier
                    .requiredHeightIn(80.dp, 80.dp)
                    .padding(5.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    Modifier
                        .weight(8f)
                        .padding(10.dp, 5.dp, 5.dp, 5.dp)
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(5.dp),
                    placeholder = {
                        Text(
                            text = "Enter your message", color = Color(0xFF242124).copy(0.5f)
                        )
                    },
                    trailingIcon = {
                        Row() {
                            androidx.compose.material.Divider(
                                color = Color(0xFF242124).copy(0.3f), //MaterialTheme.colorScheme.background.copy(0.2f),
                                modifier = Modifier
                                    .padding(0.dp, 8.dp, 0.dp, 0.dp)
                                    .fillMaxHeight(0.8f)  //fill the max height
                                    .width(1.dp)
                            )
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                    } else {
                                        bottomSheetScaffoldState.bottomSheetState.collapse()
                                    }
                                }
                            },
                                modifier = Modifier
                                    .height(60.dp)
                                    .width(60.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color(
                                        0xFF242124
                                    )
                                ),
                                content = {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowUp,
                                        contentDescription = "Localized description"
                                    )
                                })
                        }

                    },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    textStyle = TextStyle(
                        color = Color(0xFF242124),
                        fontSize = TextUnit.Unspecified,
                        fontFamily = FontFamily.SansSerif
                    ),
                    maxLines = 1,
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF5FEFD),
                        textColor = Color(0xFF242124),
                        disabledTextColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                IconButton(onClick = {
                    if (!model.mSending.value!!) {
                        model.sendMessage(
                            mBluetoothAdapter,
                            mBluetoothAdapter.bluetoothLeScanner,
                            text,
                            "",
                            "0"
                        )
                        text = ""
                    } else {
                        Toast.makeText(context, "sending message", Toast.LENGTH_SHORT).show()
                    }
                },
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp)
                        .padding(0.dp, 6.dp, 0.dp, 0.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            modifier = Modifier
                                .height(28.dp)
                                .width(28.dp),
                            contentDescription = "Localized description"
                        )
                    })


            }
        }
    }
}


@Composable
fun ChatsList(
    model: MyViewModel/*messagesList: List<Message>*/,
    notificationManagerWrapper: NotificationManagerWrapper,
    modifier: Modifier = Modifier,
    colorsOnOff: MutableState<Boolean>
) {
    val valueList by model.messages.collectAsState()
    val listState = rememberLazyListState()
// Show notification when message is sent (NOW SENDS NOTIFICATION WHEN YOU SEND A MESSAGE AS WELL)
    LaunchedEffect(valueList) {
        if (!valueList.messagesDatabaseList.isNullOrEmpty()) {
            // Value list has changed, show a notification
            notificationManagerWrapper.showNotification(
                "tossa on kissa", "kissakoira"
            )
            listState.scrollToItem(valueList.messagesDatabaseList?.lastIndex ?: 0)

        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(valueList.messagesDatabaseList?.size ?: 0) { index ->
            ShowChat(
                valueList.messagesDatabaseList?.get(index) ?: Message(
                    "", "viesti tuli perille ilman dataa", "", false
                ), colorsOnOff = colorsOnOff
            )
        }
    }
}


