package pjo.travelapp.presentation.util.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.View
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.random.Random

fun saveImageIntoFileFromUri(bitmap: Bitmap, fileName: String, path: String): File {
    val directory = File(path)
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val file = File(directory, fileName)

    try {
        val fileOutputStream = FileOutputStream(file)
        when (file.extension.lowercase(Locale.getDefault())) {
            "jpeg", "jpg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            "png" -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            else -> throw IllegalArgumentException("Unsupported file extension")
        }
        fileOutputStream.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        Log.e("Utils", "saveImageIntoFileFromUri FileNotFoundException : $e")
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e("Utils", "saveImageIntoFileFromUri IOException : $e")
    }
    return file
}

fun getExternalFilePath(): String {
    val filePath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
    return filePath
}

fun setRandomGradientBackground(view: View) {
    val startColor = getRandomColor()
    val centerColor = getRandomColor()
    val endColor = getRandomColor()

    val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(startColor, centerColor, endColor)
    ).apply {
        cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            7f,
            view.resources.displayMetrics
        )
    }

    view.background = gradientDrawable
}

fun getRandomColor(): Int {
    val random = java.util.Random()
    return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
}