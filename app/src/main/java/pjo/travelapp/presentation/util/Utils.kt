package pjo.travelapp.presentation.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import pjo.travelapp.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale
import java.util.UUID

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

class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}

class BitmapUtil(context: Context) {
    private val bitmapDirectory: File = context.getDir("bitmaps", Context.MODE_PRIVATE)

    fun saveBitmap(bitmap: Bitmap, filename: String): String {
        val file = File(bitmapDirectory, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    fun loadBitmap(path: String): Bitmap {
        return BitmapFactory.decodeFile(path)
    }
}

@SuppressLint("RestrictedApi")
fun showCustomSnackbar(view: View, message: String, context: Context) {
    val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)

    // 커스텀 레이아웃 인플레이션
    val customSnackbarView = LayoutInflater.from(context).inflate(R.layout.snackbar_show_my_application, null)
    val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

    // 기존 텍스트 제거
    snackbarLayout.removeAllViews()

    // 커스텀 뷰 추가
    snackbarLayout.addView(customSnackbarView)

    // 커스텀 뷰에 메시지 설정
    val snackbarTextView = customSnackbarView.findViewById<TextView>(R.id.snackbar_text)
    snackbarTextView.text = message

    // 앱 아이콘 설정 (필요시)
    val snackbarIconView = customSnackbarView.findViewById<ImageView>(R.id.snackbar_icon)
    snackbarIconView.setImageResource(R.drawable.ic_launcher_foreground)

    snackbar.show()
}

fun makeItemId(): String {
    return UUID.randomUUID().toString()
}