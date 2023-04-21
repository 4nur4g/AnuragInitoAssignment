package com.example.anuraginitoassignment

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.example.anuraginitoassignment.databinding.ActivityCameraTestBinding
import com.example.anuraginitoassignment.databinding.ActivityScreen2Binding
import com.example.anuraginitoassignment.util.CameraPreview
import com.github.lzyzsd.circleprogress.CircleProgress
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CameraTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraTestBinding
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    // Create a file for the image
    private fun getOutputMediaFile(): File {
        // Get the directory for storing images
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp")

        // Create the directory if it doesn't exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory")
                return mediaStorageDir
            }
        }

        // Create a unique file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(mediaStorageDir.path + File.separator +
                "IMG_" + timeStamp + ".jpg")
    }

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create an instance of Camera
        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            // Create our Preview view
            CameraPreview(this, it)
        }

// Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)

            // Create a PictureCallback to handle image capture
            val mPicture = Camera.PictureCallback { data, camera ->
                try {
                    // Save the image to the gallery
                    val imageFile = getOutputMediaFile()
                    val fos = FileOutputStream(imageFile)
                    fos.write(data)
                    fos.close()

                    // Notify the gallery of the new image
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(imageFile)
                    mediaScanIntent.data = contentUri
                    this.sendBroadcast(mediaScanIntent)

                    // Restart the camera preview
                    camera.startPreview()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving image: " + e.message)
                }
            }

            // Add a button to capture the image
            val captureButton: Button = findViewById(R.id.button_capture)
            captureButton.setOnClickListener {
                // Take the picture
                mCamera?.takePicture(null, null, mPicture)
            }
        }
    }
}
