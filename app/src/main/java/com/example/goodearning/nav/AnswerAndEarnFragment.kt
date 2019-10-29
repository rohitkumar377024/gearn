package com.example.goodearning.nav

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class AnswerAndEarnFragment : Fragment() {

    private lateinit var adView: AdView
    private lateinit var goButton: Button
    private lateinit var skipButton: Button
    private lateinit var textEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var optionsContainerLL: LinearLayout

    /* Singleton Instance of OkHTTPClient */
    object OKHTTPSingleton {
        val client = OkHttpClient()
    }

    /* Helper Static Variables */
    companion object {
        /* Needed for Request Body of POST Request */
        val MEDIA_TYPE = MediaType.parse("application/json")

        /* URLs */
        const val fetchQuestionnaireUrl = "http://gearn.online/api/fetch"
        const val submitResponseAndFetchNextUrl = "http://gearn.online/api/submit"
        const val skipQuestionnaireUrl = "http://gearn.online/api/skip"

        /* Constants for Identification */
        const val FETCH_QUESTION = 111
        const val SUBMIT_RESPONSE_AND_FETCH_NEXT = 222
        const val SKIP_QUESTIONNAIRE = 444
    }

    private lateinit var v: View

    /* IMPORTANT Variables */
    private val appId = 3 //TODO -> Replace with UID Later

    private var userQuestionaireAnswerId = -1

    private var clickedOptionButtonTag = -1
    private var clickedOptionText = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_answer_and_earn, container, false)
        /* Perform setup at-start */
        setup(v)
        /* Hide All 3 Initially */
        hide(optionsContainerLL); hide(textEditText); hide(numberEditText)

        /* POST Data */
        val fetchQuestionnairePostData = JSONObject().apply {
            put("appid", appId)
        }
        /* Fetch the First Question */
        vanillaPOST(fetchQuestionnaireUrl, fetchQuestionnairePostData, FETCH_QUESTION)

        /* Post Response and Fetch Next Question */
        goButton.setOnClickListener {
            val submitResponseAndFetchNextPostData = JSONObject().apply {
                put("appid", appId)
                put("optionid", clickedOptionButtonTag)
                put("option", clickedOptionText)
                put("userquestionaireanswerid", userQuestionaireAnswerId)
            }

            Log.d("api_testing", submitResponseAndFetchNextPostData.toString())
            vanillaPOST(submitResponseAndFetchNextUrl, submitResponseAndFetchNextPostData, SUBMIT_RESPONSE_AND_FETCH_NEXT)
        }

        /* Skip Question and Fetch Next Question */
        skipButton.setOnClickListener {
            val skipQuestionnairePostData = JSONObject().apply {
                put("appid", appId)
                put("userquestionaireanswerid", 9)
            }
            vanillaPOST(skipQuestionnaireUrl, skipQuestionnairePostData, SKIP_QUESTIONNAIRE)
        }

        return v // Inflate the layout for this fragment
    }

    /* Enqueues POST Request */
    private fun vanillaPOST(url: String, postData: JSONObject, typeOfRequest: Int) {
        val body = RequestBody.create(MEDIA_TYPE, postData.toString())
        val request = Request.Builder().url(url).post(body).build()

        OKHTTPSingleton.client.newCall(request).enqueue(object: Callback {
            override fun onResponse(response: com.squareup.okhttp.Response?) {
                val responseBody = response?.body()?.string()
                Log.e("post_success", responseBody!!)
                /* Make Changes with Response */
                mutateResponse(responseBody)
            }
            override fun onFailure(request: Request?, e: IOException?) {
                val message = e?.message.toString()
                Log.w("post_failure", message)
            }
        })
    }

    private fun mutateResponse(responseBody: String) {
        val gson = GsonBuilder().create()
        val gsonResponse = gson.fromJson(responseBody, Response::class.java)

        val question = gsonResponse.question
        val questionType = gsonResponse.questiontype
        userQuestionaireAnswerId = gsonResponse.userquestionaireanswerid

        activity?.runOnUiThread {
            when (questionType) {
                1 -> showFirstQuestionTypeLayout(gsonResponse.options)
                2 -> showSecondQuestionTypeLayout()
                3 -> showThirdQuestionTypeLayout()
            }
        }

        /* Question Text Goes Here */
        question_actual_question_textview.text = question
    }
































































































    /* IGNORE BELOW CODE FOR NOW */
    ///////////////////////////////
    /* Handles Setting Up of Ads */
    private fun setup(v: View) {
        /* Initializing Mobile Ads SDK */
//        MobileAds.initialize(context, getString(R.string.admob_app_id))

        adView = v.findViewById(R.id.adView)

        goButton = v.findViewById(R.id.question_go_button)
        skipButton = v.findViewById(R.id.question_skip_button)

        textEditText = v.findViewById(R.id.text_edittext)
        numberEditText = v.findViewById(R.id.number_edittext)
        optionsContainerLL = v.findViewById(R.id.options_container_ll)

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
            optionsContainerLL.addView(button)

            val params = button.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt() //16dp of Margin Bottom
            button.layoutParams = params

            //Whenever clicked, we want to stored clicked button option tag -> it has optionid
            button.setOnClickListener {
                clickedOptionButtonTag = (it as Button).tag.toString().toInt()
                clickedOptionText = (it as Button).text.toString()
            }

            Log.d("option_buttons", button.tag.toString())
        }
    }

    /* Shows First Type of Question Layout */
    private fun showFirstQuestionTypeLayout(options: List<Options>) {
        options.forEach { Log.d("option_x", "option -> ${it.option}, optionid -> ${it.optionid}, sequence -> ${it.sequence}") }
        activity?.runOnUiThread { addOptionButtons(options.size, options) }

        show(optionsContainerLL); hide(textEditText); hide(numberEditText)
    }

    /* Shows Second Type of Question Layout */
    private fun showSecondQuestionTypeLayout() { hide(optionsContainerLL); show(textEditText); hide(numberEditText) }

    /* Shows Third Type of Question Layout */
    private fun showThirdQuestionTypeLayout() { hide(optionsContainerLL); hide(textEditText); show(numberEditText) }

    /* Visibility Helper Methods */
    private fun hide(v: View) { v.visibility = View.GONE }
    private fun show(v: View) { v.visibility = View.VISIBLE }
}

/* Data Classes */
data class Response(val question: String, val questiontype: Int, val options: List<Options>, val userquestionaireanswerid: Int)
data class Options(val option: String, val sequence: Int, val optionid: Int)