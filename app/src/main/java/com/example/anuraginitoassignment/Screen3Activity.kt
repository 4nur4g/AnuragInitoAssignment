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
import com.example.anuraginitoassignment.databinding.ActivityScreen3Binding
import com.example.anuraginitoassignment.util.CameraPreview
import com.github.lzyzsd.circleprogress.CircleProgress
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.timer

class Screen3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen3Binding

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var mParameters: Camera.Parameters? = null

    var isFirstSecond = true // initialize the flag as true
    var initialExposure = -12

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
        binding = ActivityScreen3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // capture image with custom camera using Camera1 library
        // set ISO to 100 and focus to 1
        // save image to gallery

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
            timer(27000, 1000, mPicture)
        }
    }

    private fun timer(i: Long, i1: Long, mPicture: Camera.PictureCallback) {
        object : CountDownTimer(i, i1) {
            override fun onTick(millisUntilFinished: Long) {

                if (isFirstSecond) {
                    binding.testStatusField.text = "Capturing Multiple Images"
                    isFirstSecond = false
                } else {

                    if (initialExposure <= 12) {
                        binding.testStatusField.text = "Capturing Image with exposure ${initialExposure}"
                        Log.d("EXPOSURE", "Capturing Image with exposure ${initialExposure}")
                        mCamera?.let { captureImageWithExposure(it, initialExposure, mPicture) }
                        initialExposure += 1
                    }
                }
            }

            override fun onFinish() {
                binding.testStatusField.text = "Uploading Images"
                resetAndReleaseCamera()
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

    fun captureImageWithExposure(camera: Camera, exposure: Int, callback: Camera.PictureCallback) {
        // Get the camera parameters
        val params = camera.parameters
        // Set the exposure compensation
        params.exposureCompensation = exposure
        // Set the parameters back to the camera
        camera.parameters = params
        // Take a picture and pass the callback
        camera.takePicture(null, null, callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        resetAndReleaseCamera()
    }

    private fun resetAndReleaseCamera() {
        // Stop the preview
        mCamera?.stopPreview()
        // Clear the preview callback
        mCamera?.setPreviewCallback(null)
        // Release the camera resources
        mCamera?.release()
        // Set the camera instance to null
        mCamera = null
    }

}
