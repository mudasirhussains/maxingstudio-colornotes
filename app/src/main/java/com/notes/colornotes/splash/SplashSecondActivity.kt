package com.notes.colornotes.splash

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.notes.colornotes.MainActivity
import com.notes.colornotes.R
import com.notes.colornotes.databinding.ActivityFavoriteBinding
import com.notes.colornotes.databinding.ActivitySplashBinding
import com.notes.colornotes.databinding.ActivitySplashSecondBinding

class SplashSecondActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashSecondBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolBar()
        setBinding()
        callBacks()

        val nativeAdView = findViewById<NativeAdView>(R.id.nativeAdView)

        val adLoader = AdLoader.Builder(this@SplashSecondActivity, "ca-app-pub-4820125560371856/2018057669") //testing unit id
            //val adLoader = AdLoader.Builder(mCtx, "ca-app-pub-6915317566334175/9698722354")
            .forNativeAd { ad : NativeAd ->
                // Show the ad.
                populateNativeAdView(ad, nativeAdView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (adError.code == 0){
                        Log.d("adError", "onAdFailedToLoad: $adError")
                    }
                    else{
                        Log.d("adError", "onAdFailedToLoad: $adError")
//                        itemBinding.nativeAdView[position].layoutParams.height = 0
//                        itemBinding.nativeAdView.visibility = View.GONE
//                        itemBinding.nativeAdView.layoutParams.height = 0
//                        itemBinding.nativeAdView.layoutParams.width = 0
                    }

                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Methods to specify individual options settings.
                    .build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun callBacks() {
        binding.btnGetStarted.setOnClickListener {
            goToMain()
        }
    }

    private fun setToolBar() {
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            val decorView: View = window.decorView
            val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.setSystemUiVisibility(uiOptions)
        }
    }

    private fun setBinding() {
        binding = ActivitySplashSecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    private fun goToMain() {
        startActivity(Intent(this@SplashSecondActivity, MainActivity::class.java))
        finish()
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        // Set the media view.
        adView.mediaView =
            adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every NativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView?.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)


    }
}