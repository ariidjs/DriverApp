package app.proyekakhir.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.media.Image
import android.util.Size
import androidx.annotation.CheckResult
import androidx.camera.core.AspectRatio
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun aspectRatio(width: Int, height: Int): Int {
    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - 4.0 / 3.0) <= abs(previewRatio - 16.0 / 9.0)) {
        return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
}

fun rotateBitmap(src: String, bitmap: Bitmap): Bitmap {
    try {
        val exif = ExifInterface(src)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        if (orientation == 1) {
            return bitmap
        }
        val matrix = Matrix()
        when (orientation) {
            2 -> matrix.setScale(-1f, 1f)
            3 -> matrix.setRotate(180f)
            4 -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            5 -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            6 -> matrix.setRotate(90f)
            7 -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            8 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return try {
            val oriented =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            oriented
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            bitmap
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}

fun Image.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

/**
 * crop bitmap based on given Rect
 */
fun Bitmap.crop(crop: Rect): Bitmap {
    require(crop.left < crop.right && crop.top < crop.bottom) { "Cannot use negative crop" }
    require(crop.left >= 0 && crop.top >= 0 && crop.bottom <= this.height && crop.right <= this.width) {
        "Crop is outside the bounds of the image"
    }
    return Bitmap.createBitmap(this, crop.left, crop.top, crop.width(), crop.height())
}

/**
 * Get the size of a bitmap.
 */
fun Bitmap.size(): Size = Size(this.width, this.height)

/**
 * Calculate the position of the [Size] within the [containingSize]. This makes a few
 * assumptions:
 * 1. the [Size] and the [containingSize] are centered relative to each other.
 * 2. the [Size] and the [containingSize] have the same orientation
 * 3. the [containingSize] and the [Size] share either a horizontal or vertical field of view
 * 4. the non-shared field of view must be smaller on the [Size] than the [containingSize]
 *
 * Note that the [Size] and the [containingSize] are allowed to have completely independent
 * resolutions.
 */
@CheckResult
fun Size.scaleAndCenterWithin(containingSize: Size): Rect {
    val aspectRatio = width.toFloat() / height

    // Since the preview image may be at a different resolution than the full image, scale the
    // preview image to be circumscribed by the fullImage.
    val scaledSize = maxAspectRatioInSize(containingSize, aspectRatio)
    val left = (containingSize.width - scaledSize.width) / 2
    val top = (containingSize.height - scaledSize.height) / 2
    return Rect(
        /* left */ left,
        /* top */ top,
        /* right */ left + scaledSize.width,
        /* bottom */ top + scaledSize.height
    )
}

/**
 * Determine the maximum size of rectangle with a given aspect ratio (X/Y) that can fit inside the
 * specified area.
 *
 * For example, if the aspect ratio is 1/2 and the area is 2x2, the resulting rectangle would be
 * size 1x2 and look like this:
 * ```
 *  ________
 * | |    | |
 * | |    | |
 * | |    | |
 * |_|____|_|
 * ```
 */
@CheckResult
fun maxAspectRatioInSize(area: Size, aspectRatio: Float): Size {
    var width = area.width
    var height = (width / aspectRatio).roundToInt()

    return if (height <= area.height) {
        Size(area.width, height)
    } else {
        height = area.height
        width = (height * aspectRatio).roundToInt()
        Size(min(width, area.width), height)
    }
}

fun cropImage(fullImage: Bitmap, previewSize: Size, cardFinder: Rect): Bitmap {
    require(
        cardFinder.left >= 0 &&
                cardFinder.right <= previewSize.width &&
                cardFinder.top >= 0 &&
                cardFinder.bottom <= previewSize.height
    ) { "Card finder is outside preview image bounds" }

    // Scale the previewImage to match the fullImage
    val scaledPreviewImage = previewSize.scaleAndCenterWithin(fullImage.size())
    val previewScale = scaledPreviewImage.width().toFloat() / previewSize.width

    // Scale the cardFinder to match the scaledPreviewImage
    val scaledCardFinder = Rect(
        (cardFinder.left * previewScale).roundToInt(),
        (cardFinder.top * previewScale).roundToInt(),
        (cardFinder.right * previewScale).roundToInt(),
        (cardFinder.bottom * previewScale).roundToInt()
    )

    // Position the scaledCardFinder on the fullImage
    val cropRect = Rect(
        max(0, scaledCardFinder.left + scaledPreviewImage.left),
        max(0, scaledCardFinder.top + scaledPreviewImage.top),
        min(fullImage.width, scaledCardFinder.right + scaledPreviewImage.left),
        min(fullImage.height, scaledCardFinder.bottom + scaledPreviewImage.top)
    )

    return fullImage.crop(cropRect)
}