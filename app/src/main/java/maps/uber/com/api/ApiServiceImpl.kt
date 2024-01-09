package maps.uber.com.api


import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import maps.uber.com.dto.geocoding.GeoCodes
import maps.uber.com.dto.hereSearch.HereSearchResponse
import java.net.URLEncoder

class ApiServiceImpl(
    private val client: HttpClient,
) : ApiService {


    override suspend fun getGeocodingData(query: String): GeoCodes {
        return try {
            client.get {
            val encodedLocation = URLEncoder.encode(query, "UTF-8")
            url("${ApiRoutes.Geocoding_URL}?q=$encodedLocation&apiKey=${ApiRoutes.HereKey}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            headers {
                append("Accept", "*/*")
                append("Content-Type", "application/json")
            }
        }.body()
        } catch (e: Exception) {
            Log.i("ApiException", e.message.toString())
            return GeoCodes(
                items = null
            )
        }
    }


    override suspend fun hereSearch(
        query: String,
        latitude: Double,
        longitude: Double,
        limit: Int,
    ): HereSearchResponse {
        return try {
            val a = client.get {
                val encodedLocation = URLEncoder.encode(query, "UTF-8")
                url("${ApiRoutes.hereSearch}?at=$latitude,$longitude&q=$encodedLocation" +
                        "&lang=en&apiKey=${ApiRoutes.HereKey}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                headers {
                    append("Accept", "*/*")
                    append("Content-Type", "application/json")
                }
            }.body<HereSearchResponse>()
            println("photooooo: $a")
            return a
        } catch (e: Exception) {
            Log.i("ApiException", e.message.toString())
            return HereSearchResponse(
                items = null,
            )
        }
    }
}

