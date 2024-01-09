package maps.uber.com.mapsSearch.ui


import maps.uber.com.mapsSearch.MapsSearchViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import maps.uber.com.maps.PreviewMap
import maps.uber.com.mapsSearch.ApiState
import maps.uber.com.mapsSearch.ui.MapsSearchBar
import maps.uber.com.ui.theme.lightText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsSearchScreen(viewModel: MapsSearchViewModel, navController: NavController) {
    val query = viewModel.query.collectAsState()
    val imageState = viewModel.imageState.collectAsState()
    println("Image State: ${imageState.value}")
    Box(modifier = Modifier.fillMaxSize()) {
        PreviewMap(
            modifier = Modifier.fillMaxSize(),
            latitude = viewModel.latitude,
            longitude = viewModel.longitude,
            isClicked = viewModel.isClicked,
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            Column {
                MapsSearchBar(
                    mutableText = query.value,
                    onValueChange = {
                        viewModel.setQuery(it)
                    },
                    viewModel = viewModel,
                    onTrailingClick = {
                        viewModel.setQuery(TextFieldValue(""))
                    },
                    navController = navController
                )
                Spacer(modifier = Modifier.height(10.dp))
                AnimatedVisibility(
                    visible = imageState.value !is ApiState.NotStarted && imageState.value !is ApiState.ReceivedPhoto,
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        color = lightText
                    )
                }

            }
        }

        AnimatedVisibility(
            visible = imageState.value is ApiState.ReceivedPhoto,
            enter = slideInVertically(initialOffsetY = {
                it
            }),
            exit = slideOutVertically(targetOffsetY = {
                it
            })
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

            }
        }
    }
}




