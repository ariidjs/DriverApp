package app.proyekakhir.core.util

import android.content.Context
import android.net.Uri
import com.iceteck.silicompressorr.FileUtils
import com.iceteck.silicompressorr.SiliCompressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun createPartFromString(descriptionString: String): RequestBody {
    return descriptionString.toRequestBody(MultipartBody.FORM)
}

fun Context.prepareFilePart(
    name: String,
    file: Uri?
): MultipartBody.Part {
    var originalFile = File(file?.path!!)
    val con = Uri.fromFile(originalFile)
    if (originalFile.length() > 2000000) {

        originalFile = File(
            SiliCompressor.with(this)
                .compress(FileUtils.getPath(this, con), File(this.cacheDir, "temp"))
        )
    }
    val filePart = originalFile
        .asRequestBody(
            this.contentResolver.getType(con)?.toMediaTypeOrNull()
        )

    return MultipartBody.Part.createFormData(name, originalFile.name, filePart)

}

fun Context.prepareFile(
    file: Uri
): File {
    var originalFile = File(file.path!!)
    val con = Uri.fromFile(originalFile)
    if (originalFile.length() > 2000000) {

        originalFile = File(
            SiliCompressor.with(this)
                .compress(FileUtils.getPath(this, con), File(this.cacheDir, "temp"))
        )
    }

    return originalFile

}

fun createPart(name: String, filename: String, requestBody: RequestBody): MultipartBody.Part {
    return MultipartBody.Part.createFormData(name, filename, requestBody)
}