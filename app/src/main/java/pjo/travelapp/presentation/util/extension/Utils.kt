package pjo.travelapp.presentation.util.extension

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

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
    val filePath: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
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

fun getKeyHash(context: Context): String? {
    try {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNATURES
        )
        for (signature in info.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return null
}

fun backPressed(activity: FragmentActivity, viewLifecycleOwner: LifecycleOwner, callback: () -> Unit) {
    activity.onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callback()
            }
        })
}

fun hideKeyboard(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}