package maps.uber.com.api

import maps.uber.com.dto.geocoding.GeoCodes
import maps.uber.com.dto.hereSearch.HereSearchResponse

interface ApiService {
    suspend fun getGeocodingData(query: String): GeoCodes

    suspend fun hereSearch(
        query: String,
        latitude: Double,
        longitude: Double,
        limit: Int = 6,
    ): HereSearchResponse

}