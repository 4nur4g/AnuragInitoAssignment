package com.example.anuraginitoassignment

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.example.anuraginitoassignment.databinding.ActivityScreen2Binding
import com.example.anuraginitoassignment.util.CameraPreview
import com.github.lzyzsd.circleprogress.CircleProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.timer

class Screen2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen2Binding

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mParameters: Camera.Parameters? = null

    // Create a file for the image
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getOutputMediaFile(): File {
        // Get the directory for storing images
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), "MyCameraApp"
        )

        // Create the directory if it doesn't exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(ContentValues.TAG, "failed to create directory")
                return mediaStorageDir
            }
        }

        // Create a unique file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(
            mediaStorageDir.path + File.separator +
                    "IMG_" + timeStamp + ".jpg"
        )
    }


    /** A safe way to get an instance of the Camera object. */
    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // capture image with custom camera using Camera1 library
        // set ISO to 100 and focus to 1
        // save image to gallery

        binding.circularTimer.visibility = View.GONE


        // Create an instance of Camera
        mCamera = getCameraInstance()

        // Set the camera parameters
        mParameters = mCamera!!.parameters
        mParameters!!.set("iso", "100")
        setCameraDisplayOrientation()

        mCamera!!.parameters = mParameters
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

//            mCamera?.takePicture(null, null, mPicture)
            timer(5000, 1000, mPicture)
        }
    }

    private fun timer(i: Int, i1: Int, mPicture: Camera.PictureCallback) {
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                mCamera?.takePicture(null, null, mPicture)
                binding.testStatusField.visibility = View.GONE
                binding.circularTimer.visibility = View.VISIBLE
                timer2(5000, 1000, mPicture)
            }
        }.start()
    }

    private fun timer2(i: Int, i1: Int, mPicture: Camera.PictureCallback) {
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.circularTimer.text = (seconds).toString() + 's'
                binding.circularTimer.setDonut_progress(
                    (100 - ((seconds.toDouble()) / 5) * 100).toInt().toString()
                )
                Log.d("TIMER", ((seconds.toDouble() / 5) * 100).toString())
            }

            override fun onFinish() {
                binding.circularTimer.progress = 0f
                binding.circularTimer.text = "0s"
                binding.circularTimer.setDonut_progress("100")
                // move to Screen 3
                val intent = Intent(this@Screen2Activity, Screen3Activity::class.java)
                startActivity(intent)
                finish()
            }
        }.start()
    }


    // Method to set the camera preview orientation
    private fun setCameraDisplayOrientation() {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(0, info)
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate for the mirror image
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        mCamera!!.setDisplayOrientation(result)
    }
}
