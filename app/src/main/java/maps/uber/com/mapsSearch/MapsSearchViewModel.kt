package maps.uber.com.mapsSearch

import android.app.Application
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Geometry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import maps.uber.com.api.ApiService
import javax.inject.Inject


@HiltViewModel
class MapsSearchViewModel @Inject constructor(
    private val application: Application,
    private val repository: ApiService,
) : AndroidViewModel(application) {

    private val _imageState = MutableStateFlow<ApiState>(ApiState.NotStarted)
    val imageState: StateFlow<ApiState> = _imageState.asStateFlow()

    private val _query = MutableStateFlow(TextFieldValue())
    val query: StateFlow<TextFieldValue> = _query.asStateFlow()

    private val _addresses = MutableStateFlow<List<Address>>(listOf())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    private val _address = MutableStateFlow<Address?>(null)
    val address: StateFlow<Address?> = _address.asStateFlow()

    private val _placeId = MutableStateFlow("")
    val placeId: StateFlow<String> = _placeId.asStateFlow()

    private val _photoId = MutableStateFlow<MutableList<ByteArray?>>(mutableListOf())
    val photoId: StateFlow<List<ByteArray?>> = _photoId.asStateFlow()


    private val _isChecking = MutableStateFlow<Boolean>(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    var isClicked = mutableStateOf(false)
    var latitude =  mutableDoubleStateOf(20.5937)
    var longitude =mutableDoubleStateOf(78.9629)


    fun setImageState(state: ApiState) {
        _imageState.value = state
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun setQuery(query: TextFieldValue) {
        _query.value = query
        viewModelScope.launch {
            _query.debounce(2400)
                .filter { query ->
                    if (query.text.isEmpty() && !_isChecking.value) {
                        _query.value = TextFieldValue("")
                        return@filter false
                    } else {
                        return@filter true
                    }
                }
                .filter {
                    return@filter _imageState.value !is ApiState.ReceivedPhoto
                }
                .onStart {
                    _isChecking.emit(true) // Set API request status to true
                }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    dataFromNetwork(query.text)
                        .catch {
                            emitAll(flowOf(""))
                        }
                        .onCompletion {
                            _isChecking.emit(false) // Set API request status to false on completion
                        }
                }
                .flowOn(Dispatchers.Default)
                .collect { result ->
                    if (result.isNotEmpty()) {
                        getAutoComplete(result)
                    }
                }


//                _query.debounce(800).collectLatest {
//                    if (_imageState.value !is ApiState.ReceivedPhoto) {
//                    getAutoComplete(it.text)
//                }
//            }
        }
    }

    fun searchPlace(index: Int) {
        try {
            _address.value = _addresses.value[index]
            latitude.value = _addresses.value[index].latitude
            longitude.value = _addresses.value[index].longitude
            _addresses.value = listOf()
        } catch (e: Exception) {
            _query.value = TextFieldValue("")
            _addresses.value = listOf()
            e.printStackTrace()
        }
    }

    fun getAutoComplete(query: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val apiData =
                        repository.getGeocodingData(
                            query = query,
                        )
                    val latitude = apiData.items?.get(0)?.position?.lat ?: 0.0
                    val longitude = apiData.items?.get(0)?.position?.lng ?: 0.0
                    val autoComplete = repository.hereSearch(
                        latitude = latitude,
                        longitude = longitude,
                        query = query,
                    )
                    _addresses.value = autoComplete.items?.map {
                        Address(
                            name = it.title ?: "",
                            formattedAddress = it.address?.label ?: "",
                            latitude = latitude,
                            longitude = longitude,
                        )
                    } ?: listOf()
                    println("_addresses.value: ${_addresses.value}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
//                _addresses.value =  mapsSearch(application.applicationContext, query)
            }
        }
    }

    private fun dataFromNetwork(query: String): Flow<String> {
        return flow {
            delay(500)
            emit(query)
        }
    }



}


sealed class ApiState {
    object Loading : ApiState()

    data class Error(val exception: Exception) : ApiState()

    object NotStarted : ApiState()
    object ReceivedGeoCodes : ApiState()
    object ReceivedPlaceId : ApiState()
    object ReceivedPhotoId : ApiState()
    object CalculatedDistance : ApiState()

    object ReceivedPhoto : ApiState()
}

data class Address(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val houseNumber: String? = null,
    val street: String? = null,
    val neighborhood: String? = null,
    val locality: String? = null,
    val postcode: String? = null,
    val place: String? = null,
    val district: String? = null,
    val region: String? = null,
    val country: String? = null,
    val formattedAddress: String? = null,
    val countryIso1: String? = null,
)

