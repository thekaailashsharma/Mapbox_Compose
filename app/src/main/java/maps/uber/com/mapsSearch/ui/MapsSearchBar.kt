package maps.uber.com.mapsSearch.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maps.uber.com.mapsSearch.ApiState
import maps.uber.com.mapsSearch.MapsSearchViewModel
import maps.uber.com.ui.theme.CardBackground
import maps.uber.com.ui.theme.lightText
import maps.uber.com.ui.theme.textColor

@Composable
fun MapsSearchBar(
    text: String,
    mutableText: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onTrailingClick: () -> Unit = {},
    viewModel: MapsSearchViewModel,
    navController: NavController,
) {
    val isChecking by viewModel.isChecking.collectAsState()
    var isCheckingJob: Job? = null // Initialize isCheckingJob
    val addresses = viewModel.addresses.collectAsState()
    val imageState = viewModel.imageState.collectAsState()
    val sourceLoading = viewModel.searchingSource.collectAsState()
    val destinationLoading = viewModel.searchingDestination.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (addresses.value.isEmpty()) Color.Transparent else Color(0xFF221F1F)
            ),
        horizontalAlignment = Alignment.Start
    ) {
        AnimatedVisibility(
            visible = if (text != "Enter Destination Location")
                (viewModel.destination.value == "") else (viewModel.source.value == ""),
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextFieldWithIcons(
                    textValue = "Search",
                    placeholder = text,
                    icon = Icons.Filled.Search,
                    mutableText = mutableText,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                    onValueChanged = {
                        viewModel.setImageState(
                            ApiState.NotStarted
                        )
                        onValueChange(it)
                        if (isCheckingJob?.isActive == true) {
                            isCheckingJob?.cancel()
                        }
                        isCheckingJob = CoroutineScope(Dispatchers.Main).launch {
                            delay(1000) // Adjust the delay time as needed
                        }
                    },
                    onSearch = {
                        viewModel.getAutoComplete(mutableText.text)
                    },
                    contentColor = textColor,
                    containerColor = Color.Black,
                    trailingIcon = Icons.Filled.Close,
                    isTrailingVisible = true,
                    onTrailingClick = {
                        onTrailingClick()
                    },
                    modifier = Modifier,
                )
            }
        }
        AnimatedVisibility(
            visible = if (text != "Enter Destination Location")
                (viewModel.destination.value != "") else (viewModel.source.value != ""),
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (text == "Enter Destination Location") viewModel.destination
                        .value else viewModel.source.value,
                    color = textColor,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)
                )
            }
        }
        AnimatedVisibility(
            visible = addresses.value.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(contentPadding = PaddingValues(5.dp)) {
                itemsIndexed(addresses.value) { index, address ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null
                            ) {
                                if (text == "Enter Destination Location") {
                                    viewModel.searchDestination(index)
                                } else {
                                    viewModel.searchPlace(index)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = address.formattedAddress ?: "",
                                color = textColor,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                            Icon(
                                imageVector = Icons.Filled.NorthEast,
                                contentDescription = "",
                                tint = lightText
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithIcons(
    modifier: Modifier = Modifier,
    textValue: String,
    placeholder: String,
    icon: ImageVector,
    mutableText: TextFieldValue,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isTrailingVisible: Boolean = false,
    trailingIcon: ImageVector? = null,
    onTrailingClick: () -> Unit = {},
    ifIsOtp: Boolean = false,
    isEnabled: Boolean = true,
    onValueChanged: (TextFieldValue) -> Unit,
    onSearch: () -> Unit = {},
    contentColor: Color = textColor,
    containerColor: Color = CardBackground,
) {
    TextField(
        value = mutableText,
        leadingIcon = {
            Icon(
                imageVector = icon,
                tint = textColor,
                contentDescription = "Icon"
            )
        },
        trailingIcon = {
            if (isTrailingVisible && trailingIcon != null) {
                if (!ifIsOtp) {
                    IconButton(onClick = {
                        onTrailingClick()
                    }) {
                        Icon(
                            imageVector = trailingIcon,
                            tint = textColor,
                            contentDescription = "Icon"
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onTrailingClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBackground,
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(35.dp),
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Text(
                            text = "Get OTP",
                            color = textColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp),
                            maxLines = 1,
                            softWrap = true
                        )
                    }
                }
            }
        },
        onValueChange = onValueChanged,
        label = { Text(text = textValue, color = textColor) },
        placeholder = { Text(text = placeholder, color = textColor) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
            }
        ),
        modifier = modifier
            .padding(start = 15.dp, top = 5.dp, bottom = 15.dp, end = 15.dp)
            .fillMaxWidth(),
        colors = TextFieldDefaults.textFieldColors(
            textColor = contentColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = textColor,
            placeholderColor = textColor,
            containerColor = containerColor
        ),
        enabled = isEnabled,
        shape = RoundedCornerShape(20.dp),
    )
}