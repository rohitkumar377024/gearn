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

class AnswerAndEarnFragment : Fragment() {

    private lateinit var adView: AdView
    private lateinit var goButton: Button
    private lateinit var textEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var radioGroup: RadioGroup

    var radioBtnIdStorer = -1

    /* Singleton Instance of OkHTTPClient */
    object OKHTTPSingleton {
        val client = OkHttpClient()
    }

    /* Needed for Request Body of POST Request */
    private val MEDIA_TYPE = MediaType.parse("application/json")

    /* URLs */
    private val fetchQuestionnaireUrl = "http://gearn.online/api/fetch"
    private val submitResponseAndFetchNextUrl = "http://gearn.online/api/submit"
    private val fetchSkippedQuestionsUrl = "http://localhost:8888/gearn/public/api/fetchskipped"
    private val skipQuestionnaireUrl = "http://gearn.online/api/skip"

    /* POST Data */
    private val fetchQuestionnairePostData = JSONObject().apply { put("appid", 3) }
    private val submitResponseAndFetchNextPostData = JSONObject().apply { put("appid", 3); put("optionid", ""); put("option", "UP"); put("userquestionaireanswerid", 8) }
    private val fetchSkippedQuestionsPostData = JSONObject().apply { put("appid", 3) }
    private val skipQuestionnairePostData = JSONObject().apply { put("appid", 3); put("userquestionaireanswerid", 9) }

    /* Helper Static Variables */
    companion object {
        const val FETCH_QUESTION = 111
        const val SUBMIT_RESPONSE_AND_FETCH_NEXT = 222
        const val FETCH_SKIPPED_QUESTIONS = 333
        const val SKIP_QUESTIONNAIRE = 444
    }

    private lateinit var v: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_answer_and_earn, container, false)

        setup(v)

        /* Hide All 3 Initially */
        hide(radioGroup)
        hide(textEditText)
        hide(numberEditText)

        /* Performing POST Requests */
        /* Fetch the First Question */
        vanillaPOST(fetchQuestionnaireUrl, fetchQuestionnairePostData, FETCH_QUESTION)

        /* Fetch Next Question */
        goButton.setOnClickListener {
            vanillaPOST(submitResponseAndFetchNextUrl, submitResponseAndFetchNextPostData, SUBMIT_RESPONSE_AND_FETCH_NEXT)
        }



        //vanillaPOST(fetchSkippedQuestionsUrl, fetchSkippedQuestionsPostData, FETCH_SKIPPED_QUESTIONS)
        //vanillaPOST(skipQuestionnaireUrl, skipQuestionnairePostData, SKIP_QUESTIONNAIRE)

        return v // Inflate the layout for this fragment
    }

    /* Enqueues POST Request */
    private fun vanillaPOST(url: String, postData: JSONObject, typeOfRequest: Int) {
        val body = RequestBody.create(MEDIA_TYPE, postData.toString())
        val request = Request.Builder().url(url).post(body).build()

        OKHTTPSingleton.client.newCall(request).enqueue(object: Callback {
            override fun onResponse(response: Response?) {
                val responseBody = response?.body()?.string()
                Log.e("post_success", responseBody!!)

                when (typeOfRequest) { /* Type of Request Helps us to Differentiate POST Call Requirements */
                    FETCH_QUESTION -> fetchQuestions(responseBody)
                    SUBMIT_RESPONSE_AND_FETCH_NEXT -> submitResponseAndFetchNext(responseBody)
                    FETCH_SKIPPED_QUESTIONS -> fetchSkippedQuestions(responseBody)
                    SKIP_QUESTIONNAIRE -> skipQuestionnaire(responseBody)
                }
            }
            override fun onFailure(request: Request?, e: IOException?) {
                val message = e?.message.toString()
                Log.w("failure Response", message)
            }
        })
    }

    private fun fetchQuestions(responseBody: String) {
        val gson = GsonBuilder().create()
        val fetchedQuestionnaireGSON = gson.fromJson(responseBody, FetchQuestionnaireObj::class.java)

        val question = fetchedQuestionnaireGSON.question
        val questionType = fetchedQuestionnaireGSON.questiontype
//        val options = fetchedQuestionnaireGSON.options

        activity?.runOnUiThread {
            when (questionType) {
                1 -> showFirstQuestionTypeLayout(fetchedQuestionnaireGSON.options)
                2 -> showSecondQuestionTypeLayout()
                3 -> showThirdQuestionTypeLayout()
            }
        }

        /* Question Text Goes Here */
        question_actual_question_textview.text = question
//        options.forEach { Log.d("option_x", "option -> ${it.option}, optionid -> ${it.optionid}, sequence -> ${it.sequence}") }
//        activity?.runOnUiThread { addRadioButtons(3, options) }
    }
    private fun submitResponseAndFetchNext(responseBody: String) {
        val gson = GsonBuilder().create()
        val submitResponseAndFetchNextGSON = gson.fromJson(responseBody, SubmitResponseAndFetchNextObj::class.java)

        val question = submitResponseAndFetchNextGSON.question
        val questionType = submitResponseAndFetchNextGSON.questiontype
//        val options = submitResponseAndFetchNextGSON.options

        activity?.runOnUiThread {
            when (questionType) {
                1 -> showFirstQuestionTypeLayout(submitResponseAndFetchNextGSON.options)
                2 -> showSecondQuestionTypeLayout()
                3 -> showThirdQuestionTypeLayout()
            }
        }

        /* Question Text Goes Here */
        question_actual_question_textview.text = question
//        options.forEach { Log.d("option_x", "option -> ${it.option}, optionid -> ${it.optionid}, sequence -> ${it.sequence}") }
//        activity?.runOnUiThread { addRadioButtons(3, options) }

//        /* Removes the RadioGroup */
//        activity?.runOnUiThread {
//            val parent = v.findViewById<RadioGroup>(R.id.radiogroup).parent as ViewGroup
//            parent.removeView(v.findViewById<RadioGroup>(R.id.radiogroup))
//        }
    }
    private fun fetchSkippedQuestions(responseBody: String) {}
    private fun skipQuestionnaire(responseBody: String) {}

    fun addRadioButtons(number: Int, options: List<Options>) {
        for (row in 0..0) {
            val ll = RadioGroup(context)
            ll.orientation = LinearLayout.VERTICAL

            for (i in 1..number) {
                val rdbtn = RadioButton(context)
                rdbtn.id = View.generateViewId()
                rdbtn.text = options[i - 1].option
                radioBtnIdStorer = rdbtn.id
                rdbtn.setTextColor(Color.WHITE)
                ll.addView(rdbtn)
            }
            (activity?.findViewById(R.id.radiogroup) as ViewGroup).addView(ll)
        }
    }

    /* Handles Setting Up of Ads */
    private fun setup(v: View) {
        /* Initializing Mobile Ads SDK */
//        MobileAds.initialize(context, getString(R.string.admob_app_id))

        adView = v.findViewById(R.id.adView)

        goButton = v.findViewById(R.id.question_go_button)

        textEditText = v.findViewById(R.id.text_edittext)
        numberEditText = v.findViewById(R.id.number_edittext)
        radioGroup = v.findViewById(R.id.radiogroup)

        MobileAds.initialize(context, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    /* Shows First Type of Question Layout */
    private fun showFirstQuestionTypeLayout(options: List<Options>) {
        options.forEach { Log.d("option_x", "option -> ${it.option}, optionid -> ${it.optionid}, sequence -> ${it.sequence}") }

        activity?.runOnUiThread { addRadioButtons(options.size, options) }

        show(radioGroup)
        hide(textEditText)
        hide(numberEditText)
    }

    /* Shows Second Type of Question Layout */
    private fun showSecondQuestionTypeLayout() {
        hide(radioGroup)
        show(textEditText)
        hide(numberEditText)
    }

    /* Shows Third Type of Question Layout */
    private fun showThirdQuestionTypeLayout() {
        hide(radioGroup)
        hide(textEditText)
        show(numberEditText)
    }

    /* Visibility Helper Methods */
    private fun hide(v: View) { v.visibility = View.GONE }
    private fun show(v: View) { v.visibility = View.VISIBLE }
}

data class FetchQuestionnaireObj(val question: String, val questiontype: Int, val options: List<Options>)
data class Options(val option: String, val sequence: Int, val optionid: Int)

data class SubmitResponseAndFetchNextObj(val question: String, val questiontype: Int, val options: List<Options>)
