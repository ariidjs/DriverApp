package app.proyekakhir.core.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import app.proyekakhir.core.BuildConfig
import app.proyekakhir.core.util.Constants.FILE_AUTHORITY
import com.google.android.material.snackbar.Snackbar
import com.shashank.sony.fancytoastlib.FancyToast
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random


fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun Context.getTmpFileUri(): Uri {
    val tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".png", cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(applicationContext, FILE_AUTHORITY, tmpFile)
}

fun Fragment.showPermissionSnackBar(
    snackStrId: Int,
    actionStrId: Int = 0,
    listener: View.OnClickListener? = null
) {
    val snackBar = Snackbar.make(
        requireView(),
        getString(snackStrId),
        Snackbar.LENGTH_INDEFINITE
    )
    if (actionStrId != 0 && listener != null) {
        snackBar.setAction(getString(actionStrId), listener)
    }
    snackBar.show()
}

fun Fragment.tempFileImage(
    bitmap: Bitmap,
    name: String
): String? {
    val outputDir = context?.cacheDir
    val imageFile = File(outputDir, "$name.png")
    val os: OutputStream
    try {
        os = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()
    } catch (e: Exception) {
        Log.e(this.javaClass.simpleName, "Error writing file", e)
    }
    return imageFile.absolutePath
}

fun decodeUriToBitmap(mContext: Context, sendUri: Uri): Bitmap {
    var getBitmap: Bitmap? = null
    try {
        try {
            val imageStream = mContext.contentResolver.openInputStream(sendUri)
            getBitmap = BitmapFactory.decodeStream(imageStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return getBitmap!!
}

fun tempFileImage(
    context: Context,
    bitmap: Bitmap,
    name: String
): String? {
    val outputDir = context.cacheDir
    val imageFile = File(outputDir, "$name.png")
    val os: OutputStream
    try {
        os = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        os.flush()
        os.close()
    } catch (e: Exception) {
        Log.e(context.javaClass.simpleName, "Error writing file", e)
    }
    return imageFile.absolutePath
}

fun Fragment.showToast(message: String, type: Int) {
    FancyToast.makeText(requireContext(), message, FancyToast.LENGTH_LONG, type, false).show()
}

fun Activity.showToast(message: String, type: Int) {
    FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, type, false).show()
}

fun Fragment.showSoftKeyboard() {
    val imm: InputMethodManager =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Fragment.hideSoftKeyboard() {
    val imm: InputMethodManager =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
}

fun convertToIDR(number: Int): String {
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    return StringBuilder().append(numberFormat.format(number)).toString()
}

fun convertDate(dateString: String): String {
    val dateTimeInUtc = DateTime(dateString, DateTimeZone.UTC)
    val secondsSinceUnixEpoch: Long = dateTimeInUtc.millis / 1000
    val date = Date(secondsSinceUnixEpoch * 1000L)
    val format: DateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    return format.format(date)
}

fun convertDateTime(dateString: String, pattern: String): String {
    val dateTimeInUtc = DateTime(dateString, DateTimeZone.UTC)
    val secondsSinceUnixEpoch: Long = dateTimeInUtc.millis / 1000
    val date = Date(secondsSinceUnixEpoch * 1000L)
    val format: DateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    return format.format(date)
}


fun expand(viewBeingAnimated: View) {
    //Measure full height of the view. In our case, it's the taskDescription text view on our CardView.
    viewBeingAnimated.measure(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    //This is the final height that the expand animation is aiming for.
    val targetHeight: Int = viewBeingAnimated.measuredHeight

    //Setup the view to expand.
    viewBeingAnimated.layoutParams.height = 1
    viewBeingAnimated.visibility = View.VISIBLE

    val expandAnimation: Animation = object : Animation() {
        //Called on every frame of the animation
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            //Grow the view's height over time. And if the animation is over, make sure the view is at full height.
            viewBeingAnimated.layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
            //Redraw layout
            viewBeingAnimated.requestLayout()
        }

        //This tells Android that this animation will change the boundaries of its view.
        override fun willChangeBounds() = true
    }

    //This ensures the duration of the animation is appropriate for the size of the view being expanded.
    //In other words, you'll have a longer expand animation for a tall view and vice versa.
    expandAnimation.duration = (targetHeight / getViewDensity(viewBeingAnimated)).toLong()
    viewBeingAnimated.startAnimation(expandAnimation)
}

fun collapse(viewBeingAnimated: View) {
    //The initial height is the starting point for our animation.
    val initialHeight: Int = viewBeingAnimated.measuredHeight

    val collapseAnimation: Animation = object : Animation() {
        //Called on every frame of the animation
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            //If the animation is over, this cleans up the view from the layout completely
            if (interpolatedTime == 1f)
                viewBeingAnimated.visibility = View.GONE
            else {
                //Shrink the view's height over time.
                viewBeingAnimated.layoutParams.height =
                    initialHeight - (initialHeight * interpolatedTime).toInt()

                //Redraw layout
                viewBeingAnimated.requestLayout()
            }
        }

        //This animation will change the boundaries of its view.
        override fun willChangeBounds() = true
    }

    //Once again, the duration is relative to the initial height of the view.
    //It's also inversely proportional to the density of the view.
    collapseAnimation.duration = (initialHeight / getViewDensity(viewBeingAnimated)).toLong()
    viewBeingAnimated.startAnimation(collapseAnimation)
}

//This function returns the relative density of the view.
//Screens with more pixels will make this value larger.
//See https://developer.android.com/training/multiscreen/screendensities for reference.
private fun getViewDensity(view: View) = view.context.resources.displayMetrics.density