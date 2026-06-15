package com.carrepair.app.domain.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.carrepair.app.data.apis.AuthApi
import com.carrepair.app.data.dto.auth.PresignedUrlRequestDto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class PresignedUrlResponse(
    val uploadUrl: String,
    val objectKey: String
)

class S3UploadService(
    private val authApi: AuthApi,
    private val okHttpClient: OkHttpClient
) {

    suspend fun getPresignedUrl(
        token: String,
        folder: String,
        fileName: String,
        contentType: String
    ): PresignedUrlResponse = withContext(Dispatchers.IO) {
        val response = authApi.getPresignedUrl(
            token = "Bearer $token",
            body = PresignedUrlRequestDto(
                folder = folder,
                fileName = fileName,
                contentType = contentType
            )
        )

        if (!response.isSuccessful || response.body() == null) {
            throw RuntimeException("Failed to get presigned URL: ${response.code()}")
        }

        val body = response.body()!!
        PresignedUrlResponse(
            uploadUrl = body.uploadUrl,
            objectKey = body.objectKey
        )
    }

    suspend fun uploadToS3(
        uploadUrl: String,
        fileBytes: ByteArray,
        contentType: String
    ): Boolean = withContext(Dispatchers.IO) {
        val requestBody = fileBytes.toRequestBody(contentType.toMediaTypeOrNull())

        val request = Request.Builder()
            .url(uploadUrl)
            .put(requestBody)
            .header("Content-Type", contentType)
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.close()
        response.isSuccessful
    }

    suspend fun uploadFileAndGetKey(
        context: Context,
        uri: Uri,
        folder: String,
        token: String
    ): String = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        val mimeType = contentResolver.getType(uri)
            ?: throw RuntimeException("Could not determine file MIME type")

        val fileName = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        } ?: throw RuntimeException("Could not determine file name")

        val fileBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw RuntimeException("Could not read file bytes")

        val presignedUrlResponse = getPresignedUrl(
            token = token,
            folder = folder,
            fileName = fileName,
            contentType = mimeType
        )

        val uploaded = uploadToS3(
            uploadUrl = presignedUrlResponse.uploadUrl,
            fileBytes = fileBytes,
            contentType = mimeType
        )

        if (!uploaded) {
            throw RuntimeException("Failed to upload file to S3")
        }

        presignedUrlResponse.objectKey
    }
}