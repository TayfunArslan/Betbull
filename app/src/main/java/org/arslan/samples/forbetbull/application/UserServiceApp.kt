package org.arslan.samples.forbetbull.application

import android.app.Application
import okhttp3.OkHttpClient
import org.csystem.samples.forbetbull.R
import org.arslan.samples.forbetbull.global.g_retrofit
import org.arslan.samples.forbetbull.global.g_userInfoService
import org.arslan.samples.forbetbull.service.IUserInfoService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserServiceApp: Application() {
    override fun onCreate() {
        super.onCreate()
        createRetrofit()
        createUserInfoService()
    }

    private fun createRetrofit() {
        var httpClient = OkHttpClient.Builder()
        g_retrofit = Retrofit.Builder()
            .baseUrl(applicationContext.resources.getString(R.string.BASE_URL))
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
    }

    private fun createUserInfoService() {
        g_userInfoService = g_retrofit.create(IUserInfoService::class.java)
    }
}