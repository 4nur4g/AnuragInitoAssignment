package com.example.anuraginitoassignment

import android.content.ContentValues
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
import android.widget.FrameLayout
import com.example.anuraginitoassignment.databinding.ActivityScreen2Binding
import com.example.anuraginitoassignment.util.CameraPreview
import com.github.lzyzsd.circleprogress.CircleProgress
import java.io.File
import java.io.FileOutputStream
import java.util.*

class Screen2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen2Binding

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
                Log.d(ContentValues.TAG, "failed to create directory")
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
        binding = ActivityScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // start timer
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.circularTimer.text = (seconds).toString() + 's'
//                binding.circularTimer.progress = ((millisUntilFinished/5000))*1000.toFloat()
                binding.circularTimer.setDonut_progress((100 - ((seconds.toDouble())/5)*100).toInt().toString())
                Log.d("TIMER", ((seconds.toDouble()/5)*100).toString())
            }

            override fun onFinish() {
                binding.circularTimer.progress = 0f
                binding.circularTimer.text = "0s"
                binding.circularTimer.setDonut_progress("100")
                // move to Screen 3
            }
        }.start()

        // capture image with custom camera using Camera1 library
        // set ISO to 100 and focus to 1
        // save image to gallery

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
                    Log.e(ContentValues.TAG, "Error saving image: " + e.message)
                }
            }
    }
}}
