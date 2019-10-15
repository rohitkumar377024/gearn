package com.example.goodearning.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.goodearning.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import kotlinx.android.synthetic.main.fragment_answer_and_earn.*

class AnswerAndEarnFragment : Fragment() {

    private lateinit var adView: AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_answer_and_earn, container, false)

        adView = v.findViewById(R.id.adView)
        setup()

        return v // Inflate the layout for this fragment
    }

    private fun setup() {
        /* Initializing Mobile Ads SDK */
//        MobileAds.initialize(context, getString(R.string.admob_app_id))

        MobileAds.initialize(context, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

}
