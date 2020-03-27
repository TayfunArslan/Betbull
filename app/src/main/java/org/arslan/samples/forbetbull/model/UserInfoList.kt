package org.arslan.samples.forbetbull.model

import com.google.gson.annotations.SerializedName

data class UserInfoList(
    @SerializedName(value = "data")
    val userInfoList: List<UserInfo>
)