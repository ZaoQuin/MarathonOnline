package com.university.marathononline.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

object FileUtils {
    private const val TAG = "FileUtils"
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    private const val MAX_WIDTH = 1080
    private const val MAX_HEIGHT = 1080
    private const val JPEG_QUALITY = 85

    // Danh sách các loại file ảnh được phép
    private val ALLOWED_IMAGE_TYPES = listOf(
        "image/jpeg",
        "image/png",
        "image/jpg",
        "image/gif"
    )

    fun prepareFilePart(partName: String, fileUri: Uri, context: Context): MultipartBody.Part? {
        return try {
            // Kiểm tra loại file trước khi xử lý
            if (!isValidImageType(fileUri, context)) {
                Log.e(TAG, "Invalid image type")
                return null
            }

            val compressedFile = compressImage(fileUri, context)
            if (compressedFile == null) {
                Log.e(TAG, "Failed to compress image")
                return null
            }

            val mimeType = context.contentResolver.getType(fileUri) ?: "image/jpeg"
            val requestFile = compressedFile.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, compressedFile.name, requestFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing file part", e)
            null
        }
    }

    fun isValidImageType(fileUri: Uri, context: Context): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(fileUri)
            Log.d(TAG, "File MIME type: $mimeType")
            mimeType != null && ALLOWED_IMAGE_TYPES.contains(mimeType.lowercase())
        } catch (e: Exception) {
            Log.e(TAG, "Error checking file type", e)
            false
        }
    }

    fun getAllowedImageTypes(): List<String> {
        return ALLOWED_IMAGE_TYPES
    }

    fun getInvalidTypeErrorMessage(): String {
        return "Chỉ chấp nhận file ảnh với định dạng: JPEG, PNG, JPG, GIF"
    }

    private fun compressImage(fileUri: Uri, context: Context): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val sampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

            val newInputStream = contentResolver.openInputStream(fileUri)
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inJustDecodeBounds = false
            }

            var bitmap = BitmapFactory.decodeStream(newInputStream, null, finalOptions)
            newInputStream?.close()

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap")
                return null
            }

            bitmap = handleImageRotation(fileUri, bitmap, context)

            bitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)

            // Tạo file tạm thời
            val fileName = "compressed_avatar_${System.currentTimeMillis()}.jpg"
            val compressedFile = File(context.cacheDir, fileName)

            // Nén và lưu với chất lượng phù hợp
            var quality = JPEG_QUALITY
            var outputStream: FileOutputStream

            do {
                outputStream = FileOutputStream(compressedFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()

                // Giảm chất lượng nếu file vẫn còn quá lớn
                if (compressedFile.length() > MAX_FILE_SIZE && quality > 10) {
                    quality -= 10
                    Log.d(TAG, "File still too large, reducing quality to $quality")
                } else {
                    break
                }
            } while (quality > 10)

            bitmap.recycle()

            if (compressedFile.length() > MAX_FILE_SIZE) {
                Log.w(TAG, "File size still exceeds limit after compression: ${compressedFile.length()} bytes")
            } else {
                Log.d(TAG, "Compressed file size: ${compressedFile.length()} bytes")
            }

            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (resizedBitmap != bitmap) {
            bitmap.recycle()
        }
        return resizedBitmap
    }

    private fun handleImageRotation(fileUri: Uri, bitmap: Bitmap, context: Context): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap
        } catch (e: IOException) {
            Log.e(TAG, "Error handling image rotation", e)
            bitmap
        }
    }

    // Utility method để kiểm tra kích thước file
    fun getFileSize(fileUri: Uri, context: Context): Long {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val size = inputStream?.available()?.toLong() ?: 0L
            inputStream?.close()
            size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file size", e)
            0L
        }
    }

    // Utility method để format kích thước file
    fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$sizeInBytes bytes"
        }
    }
}