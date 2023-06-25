package com.example.rijekabusapp.network

import com.example.rijekabusapp.network.models.StationImage
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface MyApiService {
    @POST("upload")
    suspend fun addImageForStation(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("download")
    suspend fun getStationImages(@Query("stationId") stationId: Int): List<StationImage>

    @DELETE("images/{imageId}")
    suspend fun deleteImage(@Path("imageId") imageId: String): Response<ResponseBody>
}
