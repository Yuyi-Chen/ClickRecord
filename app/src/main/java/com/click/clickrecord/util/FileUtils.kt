package com.click.clickrecord.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

object FileUtils {

    /**
     * 在安卓Q上拷贝文件到公用目录
     */
    fun copyMediaFileInQ(context: Context, originalFile: File): Uri? {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, originalFile.name)
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ClickRecord")
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

        val contentResolver: ContentResolver = context.contentResolver
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        if (uri != null) {
            var `is`: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                `is` = FileInputStream(originalFile)
                outputStream = contentResolver.openOutputStream(uri)

                if (outputStream != null) {
                    val buffer = ByteArray(1024)
                    var length: Int
                    while ((`is`.read(buffer).also { length = it }) > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }

                return uri
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                originalFile.delete()
            }
        }

        return null
    }


    const val FILE_FRONT: String = "file://"

    fun getFullFileUri(path: String): String {
        if (TextUtils.isEmpty(path)) {
            return ""
        }
        return FILE_FRONT + path
    }
}