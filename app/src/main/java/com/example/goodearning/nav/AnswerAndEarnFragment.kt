package com.example.goodearning.nav

import android.Manifest
import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.*
import com.example.goodearning.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.GsonBuilder
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.fragment_answer_and_earn.*
import org.json.JSONObject
import java.io.IOException
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.appcompat.app.AppCompatActivity
import com.rizlee.handler.PermissionHandler
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream

class AnswerAndEarnFragment : Fragment() {

    private lateinit var adView: AdView
    private lateinit var skipButton: Button
    private lateinit var textEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var optionsContainerLL: LinearLayout
    private lateinit var imageContainerLL: LinearLayout
    private lateinit var imageTakeAPhotoBtn: Button
    private lateinit var imageSelectFromGalleryBtn: Button
    private lateinit var imagePreviewImageView: ImageView
    private lateinit var mainFrameLayout: FrameLayout
    private lateinit var noMoreQuestionsTextView: TextView

    /* Singleton Instance of OkHTTPClient */
    object OKHTTPSingleton {
        val client = OkHttpClient()
    }

    private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    /* Helper Static Variables */
    companion object {
        /* Needed for Request Body of POST Request */
        val MEDIA_TYPE = MediaType.parse("application/json")

        /* URLs */
        const val fetchQuestionnaireUrl = "http://gearn.online/api/fetch"
        const val submitResponseAndFetchNextUrl = "http://gearn.online/api/submit"
        const val skipQuestionnaireUrl = "http://gearn.online/api/skip"

        /* TAGs for Image Operation Results */
        const val PICK_GALLERY = 13
        const val PICK_CAMERA = 19
    }

    private lateinit var v: View

    /* IMPORTANT Variables */
    private val appId = 3 //TODO -> Replace with UID Later

    private var userQuestionaireAnswerId = -1

    private var clickedOptionButtonTag = -1 //this is used for optionid
    private var clickedOptionText: Any = ""

    //VERY MUCH NEEDED
    private var questionType = -1

    private var message = "" //Stores GSON Response 'message' parameter result -> "Success" or "No Other Questions Available" probably...

    private lateinit var galleryImageUri: Uri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_answer_and_earn, container, false)
        /* Perform setup at-start */
        setup(v)
        /* Hide All 3 Initially */
        hide(optionsContainerLL); hide(textEditText); hide(numberEditText)

        /* Hide Image Preview At Start */
        hideImagePreview()

        /* POST Data */
        val fetchQuestionnairePostData = JSONObject().apply {
            put("appid", appId)
        }
        /* Fetch the First Question */
        vanillaPOST(fetchQuestionnaireUrl, fetchQuestionnairePostData)

        /* Submit Response and Fetch Next Question */
        activity?.findViewById<Button>(R.id.toolbar_submit_btn)?.setOnClickListener {
            //Hide Keyboard Whenever Next Question Is Fetched [Safety Code]
            hideKeyboard()

            //Mutating 'option' and 'optionid'
            when (questionType) {
                1 -> { //option
                    //no mutations needed
                    hide(optionsContainerLL)
                }
                2 -> { //text
                    clickedOptionButtonTag = 0 //used for optionid
                    clickedOptionText = textEditText.text.toString()

                    hide(textEditText)
                }
                3 -> { //number
                    clickedOptionButtonTag = 0 //used for optionid
                    clickedOptionText = numberEditText.text.toString()

                    hide(numberEditText)
                }
                4 -> { //image
                    clickedOptionButtonTag = 0 //used for optionid

                    //val imagePreviewBitmap = getBitmapFromImageView(imagePreviewImageView)
                    //val byteArrayFinal = convertBitmapToByteArray(imagePreviewBitmap)

                    //clickedOptionText = byteArrayFinal //Passing the ByteArray of Bitmap as 'option' parameter
                    galleryImageUri.let { clickedOptionText = galleryImageUri }

                    hide(imageContainerLL)
                }
            }

            val submitResponseAndFetchNextPostData = JSONObject().apply {
                put("appid", appId)
                put("optionid", clickedOptionButtonTag) //used for optionid
                put("option", clickedOptionText)
                put("userquestionaireanswerid", userQuestionaireAnswerId)
            }

            Log.d("api_testing -> submit", submitResponseAndFetchNextPostData.toString())
            vanillaPOST(submitResponseAndFetchNextUrl, submitResponseAndFetchNextPostData)

            cleanAll()
        }

        /* Post Response and Fetch Next Question */
        skipButton.setOnClickListener {
            //Hide Keyboard Whenever Question Is Skipped [Safety Code]
            hideKeyboard()

            val skipQuestionnairePostData = JSONObject().apply {
                put("appid", appId)
                put("userquestionaireanswerid", userQuestionaireAnswerId)
            }

            Log.d("api_testing -> skip", skipQuestionnairePostData.toString())
            vanillaPOST(skipQuestionnaireUrl, skipQuestionnairePostData)

            cleanAll()
        }

        //todo -> requesting at start in this fragment here for now
        requestPermissions()

        //Image Related Click Operations
        imageTakeAPhotoBtn.setOnClickListener { takeAPhoto() }
        imageSelectFromGalleryBtn.setOnClickListener { selectFromGallery() }

        showSubmitBtn()

        return v // Inflate the layout for this fragment
    }

    /* Shows 'Submit' Button of the MainActivity */
    private fun showSubmitBtn() { activity?.findViewById<Button>(R.id.toolbar_submit_btn)?.visibility = View.VISIBLE }
    /* Exists -> Just In-Case If Needed */
    private fun hideSubmitBtn() { activity?.findViewById<Button>(R.id.toolbar_submit_btn)?.visibility = View.GONE }

    /* Enqueues POST Request */
    private fun vanillaPOST(url: String, postData: JSONObject) {
        val body = RequestBody.create(MEDIA_TYPE, postData.toString())
        val request = Request.Builder().url(url).post(body).build()

        OKHTTPSingleton.client.newCall(request).enqueue(object: Callback {
            override fun onResponse(response: com.squareup.okhttp.Response?) {
                val responseBody = response?.body()?.string()
                Log.e("post_success", responseBody!!)

                /* Make Changes with Response */
                receiveAndMutateResponse(responseBody)
            }
            override fun onFailure(request: Request?, e: IOException?) {
                val message = e?.message.toString()
                Log.w("post_failure", message)
            }
        })
    }

    private fun receiveAndMutateResponse(responseBody: String) {
        val gson = GsonBuilder().create()
        val gsonResponse = gson.fromJson(responseBody, Response::class.java)

        val question = gsonResponse.question
        questionType = gsonResponse.questiontype
        userQuestionaireAnswerId = gsonResponse.userquestionaireanswerid
        message = gsonResponse.message

        /* Checking Whether If More Questions Are Available */
        if (message == "No More Question available") {
            //NOT AVAILABLE
            Log.d("post_no_more_questions", "true")
            activity?.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                /* Show No More Questions */
                show(noMoreQuestionsTextView)
            }
        } else {
            //AVAILABLE
            /* Handle Answer Layout */
            activity?.runOnUiThread {
                when (questionType) {
                    1 -> showFirstQuestionTypeLayout(gsonResponse.options)
                    2 -> showSecondQuestionTypeLayout()
                    3 -> showThirdQuestionTypeLayout()
                    4 -> showFourthQuestionTypeLayout()
                }
            }

            activity?.runOnUiThread {
                /* Question Text Goes Here */
                question_actual_question_textview.text = question
            }
        }
    }

    /* IGNORE BELOW CODE FOR NOW */
    ///////////////////////////////
    /* Handles Setting Up of Ads */
    private fun setup(v: View) {
        /* Initializing Mobile Ads SDK */
//        MobileAds.initialize(context, getString(R.string.admob_app_id))

        adView = v.findViewById(R.id.adView)

        skipButton = v.findViewById(R.id.question_skip_button)

        textEditText = v.findViewById(R.id.text_edittext)
        numberEditText = v.findViewById(R.id.number_edittext)
        optionsContainerLL = v.findViewById(R.id.options_container_ll)
        imageContainerLL = v.findViewById(R.id.image_container_ll)
        imageTakeAPhotoBtn = v.findViewById(R.id.image_take_a_photo_btn)
        imageSelectFromGalleryBtn = v.findViewById(R.id.image_select_from_gallery_btn)
        imagePreviewImageView = v.findViewById(R.id.image_preview_imageview)
        mainFrameLayout = v.findViewById(R.id.answer_and_earn_main_frame_layout)
        noMoreQuestionsTextView = v.findViewById(R.id.no_more_questions_textview)

        MobileAds.initialize(context, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun addOptionButtons(number: Int, options: List<Options>) {
        for (i in 1..number) {
            val button = Button(context)
            button.id = View.generateViewId()
            button.text = options[i - 1].option
            button.setTextColor(Color.WHITE)
            button.setBackgroundResource(R.drawable.auth_field) //irony is field one looks more button-y, button one looks more empty-eyeyey
            button.tag = options[i - 1].optionid
            button.isAllCaps = false
            button.textSize = 21f
            optionsContainerLL.addView(button)

            val params = button.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
            params.leftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
            params.rightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
            button.layoutParams = params

            //Whenever clicked, we want to stored clicked button option tag -> it has optionid
            button.setOnClickListener {
                clickedOptionButtonTag = (it as Button).tag.toString().toInt()
                clickedOptionText = (it as Button).text.toString()

                skipButton.isSoundEffectsEnabled = false /* Disabling Before Performing Artificial Click */
                skipButton.performClick()
                skipButton.isSoundEffectsEnabled = true /* Once our artificial click is done, we make the Button Normal */
            }

            Log.d("option_buttons", button.tag.toString())
        }
    }

    /* Shows First Type of Question Layout */
    private fun showFirstQuestionTypeLayout(options: List<Options>) {
        hideSubmitBtn() /* Hide When Options Type Question */

        options.forEach { Log.d("option_x", "option -> ${it.option}, optionid -> ${it.optionid}, sequence -> ${it.sequence}") }
        activity?.runOnUiThread { addOptionButtons(options.size, options) }

        show(optionsContainerLL); hide(textEditText); hide(numberEditText);  hide(imageContainerLL)
    }

    /* Shows Second Type of Question Layout */ /* Show Keyboard Pop Up Automatically */
    private fun showSecondQuestionTypeLayout() {
        showSubmitBtn()

        hide(optionsContainerLL); show(textEditText); hide(numberEditText); hide(imageContainerLL); textEditText.showKeyboard()
    }

    /* Shows Third Type of Question Layout */ /* Show Keyboard Pop Up Automatically */
    private fun showThirdQuestionTypeLayout() {
        showSubmitBtn()

        hide(optionsContainerLL); hide(textEditText); show(numberEditText); hide(imageContainerLL); numberEditText.showKeyboard()
    }

    /* Shows Fourth Type of Question Layout */
    private fun showFourthQuestionTypeLayout() {
        hideSubmitBtn() /* Hide When Image Type Question [At First] */

        hide(optionsContainerLL); hide(textEditText); hide(numberEditText); show(imageContainerLL)
    }

    /* Visibility Helper Methods */
    private fun hide(v: View) { v.visibility = View.GONE }
    private fun show(v: View) { v.visibility = View.VISIBLE }

    /* Master Function for Cleaning Process */
    private fun cleanAll() {
        optionsContainerLL.removeAllViews()
        textEditText.text.clear()
        numberEditText.text.clear()
    }

    /* Helper Functions for Soft Keyboard */
    private fun EditText.showKeyboard() {
        if (requestFocus()) {
            (getActivity()?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(this, SHOW_IMPLICIT)
            setSelection(text.length)
        }
    }
    private fun View.getActivity(): AppCompatActivity?{
        var context = this.context
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    /* Image Operations Related Code */
    /* Handles Gallery Selection Operation */
    private fun selectFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select From Gallery"), PICK_GALLERY)
    }
    /* Handles Camera Photo Taking Operation */
    private fun takeAPhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(Intent.createChooser(intent, "Take A Photo"), PICK_CAMERA)
    }
    /* Handles OnActivityResult Data */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) { /* First Check If Result Code is OK */
            when (requestCode) { /* Check Which Request Code */
                PICK_GALLERY -> { /* Gallery Operation */
                    galleryImageUri = data?.data!!
                    imagePreviewImageView.setImageURI(galleryImageUri)
                    showImagePreview() /* Show Image Preview Now */
                    hide(imageSelectFromGalleryBtn)
                    hide(imageTakeAPhotoBtn)
                    showSubmitBtn()
                }
                PICK_CAMERA -> { /* Camera Operation */
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imagePreviewImageView.setImageBitmap(bitmap)
                    showImagePreview() /* Show Image Preview Now */
                    hide(imageSelectFromGalleryBtn)
                    hide(imageTakeAPhotoBtn)
                    showSubmitBtn()
                }
            }
        }
    }
    /* Helper Methods for Image Preview Visibility */
    private fun hideImagePreview() = hide(imagePreviewImageView)
    private fun showImagePreview() = show(imagePreviewImageView)
    /* Helper Method for Getting Bitmap from an ImageView */
    private fun getBitmapFromImageView(imageView: ImageView): Bitmap {
        val drawable = imageView.drawable as BitmapDrawable
        return drawable.bitmap
    }
    /* Helper Method to Convert Bitmap to ByteArray */
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /* todo -> currently exists because register/login isn't integrated yet */
    private fun requestPermissions() = PermissionHandler.requestPermission(this, { permissionsGranted() }, PERMISSIONS)
    private fun permissionsGranted() = Toast.makeText(context, "Permissions Granted Successfully.", Toast.LENGTH_SHORT).show()
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionHandler.permissionsResult(activity!!, PERMISSIONS, requestCode, grantResults, { permissionsGranted() },
            { Toast.makeText(context, "Permission Not Successful.", Toast.LENGTH_SHORT).show() },
            { Toast.makeText(context, "Permission Error.", Toast.LENGTH_SHORT).show() }
        )
    }
}

/* Data Classes */
data class Response(val question: String, val questiontype: Int, val userquestionaireanswerid: Int, val message: String, val options: List<Options>)
data class Options(val option: String, val sequence: Int, val optionid: Int)