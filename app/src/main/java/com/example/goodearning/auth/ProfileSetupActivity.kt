package com.example.goodearning.auth

import android.Manifest
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.goodearning.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile_setup.*
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.annotation.SuppressLint
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.os.StrictMode
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.rizlee.handler.PermissionHandler
import java.io.ByteArrayOutputStream

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth /* Shared Instance of FirebaseAuth */
    private lateinit var UID: String /* UID of the User */

    companion object {
        /* TAGs for Activity Results */
        const val PICK_GALLERY = 13
        const val PICK_CAMERA = 19

        /* State of Profile Image (1 -> Gallery, 2 -> Camera, 3 -> Default) */
        var PROFILE_IMAGE_STATE = 3
    }

    /* Global Variable for Gallery Photo Operation */
    private lateinit var galleryImageUri: Uri

    /* Global Variables for Camera Photo Operation */
    private lateinit var cameraImageBitmap: Bitmap
    private lateinit var cameraCurrentPhotoPath: String

    private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        requestPermissions()

        auth = FirebaseAuth.getInstance() /* Getting the Instance of FirebaseAuth Just Once in OnCreate() */
        UID = auth.currentUser?.uid.toString() /* Initializing the UID variable with its value */

        profile_setup_photo_imageview.setOnClickListener { handleProfilePhoto() } /* Profile Photo Clicked */
        profile_setup_proceed_btn.setOnClickListener { completeProfileSetupProcessing() } /* Proceed Button Clicked */
    }

    private fun requestPermissions() = PermissionHandler.requestPermission(this, { permissionsGranted() }, PERMISSIONS)
    private fun permissionsGranted() = Toast.makeText(applicationContext, "Permissions Granted Successfully.", Toast.LENGTH_SHORT).show()
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionHandler.permissionsResult(this, PERMISSIONS, requestCode, grantResults, { permissionsGranted() },
            { Toast.makeText(applicationContext, "Permission Not Successful.", Toast.LENGTH_SHORT).show() },
            { Toast.makeText(applicationContext, "Permission Error.", Toast.LENGTH_SHORT).show() }
        )
    }

    /* Handles Processing of Profile Setup -> Uploads Profile Photo to Storage + Stores Necessary Information to Database */
    private fun completeProfileSetupProcessing() {
        /* Database Stuff */
        val name = profile_setup_name_edittext.text.toString() /* Get Name Input From User*/
        val database = FirebaseDatabase.getInstance().reference /* Get Database Root Reference */
        val nameRef = database.child("users").child(UID).child("name") /* Get Name Reference - root -> users -> uid -> name */
        nameRef.setValue(name) /* Set Name Value in the Specified Reference */

        //todo -> revise path name
        val userProfilePhotosRef = FirebaseStorage.getInstance().reference.child("user_profile_photos").child(auth.currentUser?.uid!!)

        //Stores the UploadTask
        var uploadTask: UploadTask? = null

        /* Storage Stuff */
        when (PROFILE_IMAGE_STATE) {
            1 -> uploadTask = userProfilePhotosRef.putFile(galleryImageUri) //GALLERY
            2 -> uploadTask = userProfilePhotosRef.putBytes(getImageDataAsByteArray()) //CAMERA
            3 -> uploadTask = userProfilePhotosRef.putFile(Uri.parse("android.resource://" + R::class.java.getPackage()?.name + "/" + R.drawable.ic_profile)) //DEFAULT PIC
        }

        uploadTask?.addOnFailureListener { // Handle unsuccessful uploads
        }?.addOnSuccessListener { }// taskSnapshot.metadata contains file metadata such as size, content-type, etc. // ...
    }

    // Get the Data from Profile Photo ImageView as Bytes
    private fun getImageDataAsByteArray(): ByteArray {
        profile_setup_photo_imageview.isDrawingCacheEnabled = true
        profile_setup_photo_imageview.buildDrawingCache()
        val bitmap = (profile_setup_photo_imageview.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return data
    }

    /* Handles Profile Photo Setting Up Operations */
    private fun handleProfilePhoto() {
        /* Show up an Alert Dialog at Start */
        val profilePhotoOperationsArr = arrayOf("Select from Gallery", "Take Photo from Camera", "Set Default One")
        AlertDialog.Builder(this).apply {
            setTitle("Profile Photo")
            setItems(profilePhotoOperationsArr) { _, which ->
                when (which) {
                    0 -> selectFromGallery()
                    1 -> takePhotoFromCamera()
                    2 -> setDefaultProfilePic()
                }
            }
            show()
        }
    }

    /* Handles Activity Result */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) { /* First Check If Result Code is OK */
            when (requestCode) { /* Check Which Request Code */
                PICK_GALLERY -> { /* Gallery Operation */
                    galleryImageUri = data?.data!!
                    profile_setup_photo_imageview.setImageURI(galleryImageUri)
                    PROFILE_IMAGE_STATE = 1
                    addBorder()
                }
                PICK_CAMERA -> { /* Camera Operation */
                    if (::cameraCurrentPhotoPath.isInitialized) {
                        cameraImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(cameraCurrentPhotoPath))
                        profile_setup_photo_imageview.setImageBitmap(cameraImageBitmap)
                        PROFILE_IMAGE_STATE = 2
                        addBorder()
                    }
                }
            }
        }
    }

    /* Handles Gallery Selection Operation */
    private fun selectFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_GALLERY)
    }

    /* Handles Camera Photo Taking Operation */
    private fun takePhotoFromCamera() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.i("profile_setup_activity", "IOException")
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                startActivityForResult(cameraIntent, PICK_CAMERA)
            }
        }
    }

    /* Helper Method for Creating an Image File */
    @SuppressLint("SimpleDateFormat") @Throws(IOException::class) private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        // Save a file: path for use with ACTION_VIEW intents
        cameraCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

    /* Handles Default Profile Pic Setting Operation */
    private fun setDefaultProfilePic() {
        profile_setup_photo_imageview.setImageResource(R.drawable.ic_profile)
        removeBorder() /* No Border Needed as Default Image Already Has A White Border */
    }

    /* Profile Photo Border Manipulation */
    private fun addBorder() = profile_setup_photo_imageview.apply { borderColor = Color.WHITE; borderWidth = 32 }
    private fun removeBorder() =  profile_setup_photo_imageview.apply { borderColor = Color.WHITE; borderWidth = 0 }
}
