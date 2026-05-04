package com.lonx.lyrics.source.am

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Url

interface AppleApi {
    @GET
    suspend fun get(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): Response<ResponseBody>
}
