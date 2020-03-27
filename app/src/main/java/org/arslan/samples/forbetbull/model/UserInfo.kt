package org.arslan.samples.forbetbull.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName(value = "id")
    var userId: Int,
    @SerializedName(value = "name")
    var username: String
) {
    override fun toString() = username
}
