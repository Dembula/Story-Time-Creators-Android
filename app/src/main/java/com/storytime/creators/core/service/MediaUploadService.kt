package com.storytime.creators.core.service

import com.storytime.creators.core.network.ApiClient
import com.storytime.creators.core.network.ApiException
import com.storytime.creators.core.network.post
import com.storytime.creators.core.model.PresignRequest
import com.storytime.creators.core.model.PresignResponse
import com.storytime.creators.core.model.UploadCompleteRequest
import com.storytime.creators.core.model.UploadCompleteResponse

/** Mirrors iOS MediaUploadService: presign -> PUT to storage -> complete. */
object MediaUploadService {

    suspend fun upload(
        client: ApiClient,
        bytes: ByteArray,
        fileName: String,
        contentType: String,
    ): UploadCompleteResponse {
        val presign: PresignResponse = client.post(
            "/api/upload/content-media/presign",
            PresignRequest(fileName = fileName, size = bytes.size, contentType = contentType),
        )

        val mime = presign.headers?.contentType ?: presign.contentType
        val ok = client.putBinary(presign.uploadUrl, bytes, mime)
        if (!ok) throw ApiException.Network("Upload to storage failed.")

        return client.post(
            "/api/upload/content-media/complete",
            UploadCompleteRequest(key = presign.key, contentType = mime, fileName = fileName),
        )
    }
}
