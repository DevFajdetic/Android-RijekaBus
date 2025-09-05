package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.models.StationImage
import com.example.rijekabusapp.network.response.BusLocationsResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MyApiService {
    @POST("upload")
    suspend fun addImageForStation(
        @Body requestBody: RequestBody,
    ): Response<ResponseBody>

    @GET("download")
    suspend fun getStationImages(
        @Query("stationId") stationId: Int,
        @Query("userId") userId: Int,
    ): List<StationImage>

    @DELETE("images/{imageId}")
    suspend fun deleteImage(
        @Path("imageId") imageId: String,
    ): Response<ResponseBody>
    
    @GET("bus-locations")
    suspend fun getBusLocations(): BusLocationsResponse
}
