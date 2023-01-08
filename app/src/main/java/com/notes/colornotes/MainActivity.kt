package com.notes.colornotes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.navigation.NavigationView
import com.notes.colornotes.databinding.ActivityMainBinding
import com.notes.colornotes.favorite.FavoriteActivity
import com.notes.colornotes.fragments.allnotes.MainFragment
import com.notes.colornotes.fragments.allnotes.bottom.calender.CalenderActivity
import com.notes.colornotes.home.HomeViewModel
import com.notes.colornotes.trash.TrashActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var mViewModel: HomeViewModel
    private var exitDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        setDrawer()
        setDefaultFragment(savedInstanceState)
        setDrawerToggle()



        binding.clearText.setOnClickListener {
            if (binding.etSearch.text!!.isNotEmpty()) {
                binding.etSearch.text = null
            }
        }


        binding.bSheetHome.setOnClickListener {
            Toast.makeText(applicationContext, "Home", Toast.LENGTH_SHORT).show()
        }
        binding.bSheetCalender.setOnClickListener {
            startActivity(Intent(applicationContext, CalenderActivity::class.java))
        }
    }


    private fun setDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.post(Runnable {
            val d = ResourcesCompat.getDrawable(resources, R.drawable.ic_navigationicon, null)
            binding.toolbar.navigationIcon = d
        })
        binding.toolbar.overflowIcon!!.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun setDefaultFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {

            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(
                R.id.flContent,
                MainFragment()
            )
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    private fun setDrawerToggle() {
        drawerToggle = setupDrawerToggle()
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        binding.drawerLayout.addDrawerListener(drawerToggle)
        setupDrawerContent(binding.navView)
    }

    // to use three dots
    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                if (binding.showSearchBar.isVisible) {
                    hideKeyboard(applicationContext)
                    binding.showSearchBar.visibility = View.GONE
                } else {
                    binding.showSearchBar.visibility = View.VISIBLE
                    binding.etSearch.requestFocus()
                    binding.toolbarTitle.visibility = View.GONE
                    showKeyboard(applicationContext)
                }
            }
            R.id.action_grid -> {
                if (item.title.equals("Grid View")) {
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_outline_list)
                    item.title = "List View"
                } else {
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_dots_one)
                    item.title = "Grid View"
                }

            }
//            R.id.action_select -> {
//                Toast.makeText(applicationContext, "Select", Toast.LENGTH_SHORT).show()
//            }
            R.id.action_sort -> {


            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            false
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_all_notes ->
                closeDrawer()
            R.id.nav_calender -> {
                openCalender()
            }
            R.id.nav_fav -> {
                openFavorite()
            }
            R.id.nav_trash -> {
                openTrashScreen()
            }
//            R.id.nav_setting -> {
//                closeDrawer()
//            }
            R.id.nav_rate -> {
                rateUs()
            }
            R.id.nav_share -> {
                shareUs()
            }
            R.id.nav_policy -> {
                policyIntent()
            }
            else ->
                closeDrawer()
        }
        closeDrawer()
    }

    private fun policyIntent() {
        val url = "https://maxingstudio.blogspot.com/2022/09/privacy-policy.html"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun openFavorite() {
        startActivity(Intent(applicationContext, FavoriteActivity::class.java))
    }

    private fun openCalender() {
        startActivity(Intent(applicationContext, CalenderActivity::class.java))
    }

    private fun openTrashScreen() {
        startActivity(Intent(applicationContext, TrashActivity::class.java))
    }

    private fun rateUs() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun shareUs() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawers()
    }


    private fun setBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private fun hideKeyboard(context: Context) {
        try {
            (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if ((context).currentFocus != null && (context).currentFocus!!
                    .windowToken != null
            ) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    (context).currentFocus!!.windowToken, 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showKeyboard(context: Context) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    private fun showExitDialog() {
        if (exitDialog == null) {
            val builder = AlertDialog.Builder(this@MainActivity)
            val view: View = layoutInflater.inflate(R.layout.exit_dialog, null)
            builder.setView(view)
            exitDialog = builder.create()

            val nativeAdView = view.findViewById<NativeAdView>(R.id.nativeAdView)

            val adLoader = AdLoader.Builder(
                this@MainActivity,
                "ca-app-pub-4820125560371856/2018057669"
            ) //testing unit id
                //val adLoader = AdLoader.Builder(mCtx, "ca-app-pub-6915317566334175/9698722354")
                .forNativeAd { ad: NativeAd ->
                    // Show the ad.
                    populateNativeAdView(ad, nativeAdView)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        if (adError.code == 0) {
                            Log.d("adError", "onAdFailedToLoad: $adError")
                        } else {
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
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())


            if (exitDialog!!.window != null) {
                exitDialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }

            view.findViewById<View>(R.id.textExitYes).setOnClickListener {
                finish()
            }

            view.findViewById<View>(R.id.textExitNo)
                .setOnClickListener { exitDialog!!.dismiss() }
        }
        exitDialog!!.show()
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