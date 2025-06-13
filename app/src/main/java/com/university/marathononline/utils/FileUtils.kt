package com.university.marathononline.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object FileUtils {
    fun prepareFilePart(partName: String, fileUri: Uri, context: Context): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(fileUri)
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        val outputStream = file.outputStream()
        inputStream?.copyTo(outputStream)
        outputStream.close()

        val mimeType = contentResolver.getType(fileUri) ?: "image/jpeg"
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}
