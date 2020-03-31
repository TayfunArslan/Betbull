package org.arslan.samples.forbetbull.service

import org.arslan.samples.forbetbull.model.UserInfoList
import retrofit2.Call
import retrofit2.http.GET

interface IUserInfoService {
    @GET(value = "/emredirican/mock-api/db")
    fun getUsers(): Call<UserInfoList>
}