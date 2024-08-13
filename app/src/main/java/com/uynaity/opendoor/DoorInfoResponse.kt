package com.uynaity.opendoor

import android.annotation.SuppressLint
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DoorInfoResponse(
    val code: Int,
    val msg: String,
    val data: List<@Contextual DoorInfo>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DoorInfo(
    val equipmentName: String,
    val advertising: Int,
    val source: Int,
    val uniqueNumber: String,
    val equipmentId: Int,
    val roomId: String?,
    val equipmentStatus: Int
)