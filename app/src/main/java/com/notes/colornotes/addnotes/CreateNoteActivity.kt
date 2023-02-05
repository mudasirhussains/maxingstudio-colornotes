package com.notes.colornotes.addnotes

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.notes.colornotes.R
import com.notes.colornotes.databinding.ActivityCreateNoteBinding
import com.notes.colornotes.fragments.allnotes.MainFragment
import com.notes.colornotes.room.database.DatabaseBuilder
import com.notes.colornotes.room.entity.NoteModel
import com.notes.colornotes.util.ICreateNoteListener
import com.notes.colornotes.util.TextViewUndoRedo
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteActivity : AppCompatActivity(),
    ICreateNoteListener {
    private var selectedNoteColor: String? = null
    private var selectedImagePath: String? = null
    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2
    private var dialogAddURL: AlertDialog? = null
    private var dialogDeleteNote: AlertDialog? = null
    private var alreadyAvailableNote: NoteModel? = null
    lateinit var binding: ActivityCreateNoteBinding
    private var mInterstitialAd: InterstitialAd? = null
    var cal = Calendar.getInstance()
    lateinit var helper: TextViewUndoRedo
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()

        binding.imageBack.setOnClickListener {
            onBackPressed()
            hideKeyboard()
        }

        // load ad
        loadAd();

        helper = TextViewUndoRedo(binding.inputNoteText)


        //format the date as in example: Friday, 19 June 2020 08:35 PM
        binding.txtSelectedDate.text =
//            SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
//                .format(Date())
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                .format(Date())

//        binding.imageSave.isEnabled = false
        binding.imageSave.setOnClickListener {
            loadDialog()
        }

        // undo/redo
        binding.imgUndo.setOnClickListener {
            helper.undo()
        }

        binding.imgRedo.setOnClickListener {
            helper.redo()
        }



        selectedNoteColor = "#333333"
        selectedImagePath = ""

        if (intent.getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = intent.getSerializableExtra("note") as NoteModel?
            setViewOrUpdateNote()
        }

        binding.imageRemoveWebURL.setOnClickListener {
            binding.textWebURL.text = null
            binding.layoutWebURL.visibility = View.GONE
        }

        binding.imageRemoveImage.setOnClickListener {
            binding.imageNote.setImageBitmap(null)
            binding.imageNote.visibility = View.GONE
            binding.imageRemoveImage.visibility = View.GONE
            selectedImagePath = ""
        }

        if (intent.getBooleanExtra("isFromQuickActions", false)) {
            val type = intent.getStringExtra("quickActionType")
            if (type != null) {
                if (type == "image") {
                    selectedImagePath = intent.getStringExtra("imagePath")
                    binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath))
                    binding.imageNote.visibility = View.VISIBLE
                    binding.imageRemoveImage.visibility = View.VISIBLE
                } else if (type == "URL") {
                    binding.textWebURL.text = intent.getStringExtra("URL")
                    binding.layoutWebURL.visibility = View.VISIBLE
                }
            }
        }

        initMiscellaneous()
        setSubtitleIndicatorColor()

        callBacks()
        if (MainFragment.isFromOptionMenu){
            MainFragment.isFromOptionMenu = false
            callCategoryPopup()
        }
    }

    private fun callBacks() {
        binding.cardSelectCategory.setOnClickListener {
            callCategoryPopup()
        }
        binding.txtSelectedDate.setOnClickListener {
            callDatePicker()
        }

        binding.imgShare.setOnClickListener {
            sharePlaneText()
        }
    }

    private fun sharePlaneText() {
        val finalText: String = "Color Notes \n\n Title: "+binding.inputNoteTitle.text.toString()+
                "\n Subtitle: "+binding.inputNoteSubtitle.text.toString()+
                "\n Notes: "+binding.inputNoteText.text.toString()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, finalText)
        startActivity(Intent.createChooser(intent, "Share Via"))
    }

    private fun callDatePicker() {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                binding.txtSelectedDate.text = sdf.format(cal.time)
            }
        DatePickerDialog(
            this@CreateNoteActivity,
            dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun callCategoryPopup() {
        val factory = LayoutInflater.from(this@CreateNoteActivity)
        val layoutView: View = factory.inflate(R.layout.category_selection_dialog, null)
        val dialog = AlertDialog.Builder(this@CreateNoteActivity).create()
        dialog.setView(layoutView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val etCategory = layoutView.findViewById<EditText>(R.id.etSelectedCategory)
        if (binding.txtSelectedCategory.text.toString() != ("Select category")){
            etCategory.setText(binding.txtSelectedCategory.text.toString())
        }
        etCategory.setOnClickListener {
            etCategory.clearFocus()
            etCategory.error = null
        }

        val catHome = layoutView.findViewById<TextView>(R.id.chipItemHome)
        catHome.setOnClickListener {
            etCategory.clearFocus()
            etCategory.error = null
            etCategory.setText(catHome.text.toString())
        }
        val catWork = layoutView.findViewById<TextView>(R.id.chipItemWork)
        catWork.setOnClickListener {
            etCategory.clearFocus()
            etCategory.error = null
            etCategory.setText(catWork.text.toString())
        }
        val catShopping = layoutView.findViewById<TextView>(R.id.chipItemShopping)
        catShopping.setOnClickListener {
            etCategory.clearFocus()
            etCategory.error = null
            etCategory.setText(catShopping.text.toString())
        }
        val catSchool = layoutView.findViewById<TextView>(R.id.chipItemSchool)
        catSchool.setOnClickListener {
            etCategory.clearFocus()
            etCategory.error = null
            etCategory.setText(catSchool.text.toString())
        }
        val catStudy = layoutView.findViewById<TextView>(R.id.chipItemStudy)
        catStudy.setOnClickListener {
            etCategory.clearFocus()
            etCategory.setText(catStudy.text.toString())
        }


        layoutView.findViewById<TextView>(R.id.categoryOk).setOnClickListener {
            if (etCategory.text.toString().isNullOrEmpty()){
                etCategory.requestFocus()
                etCategory.error = "Must Not Be Empty"
            }else{
                binding.txtSelectedCategory.text = etCategory.text.toString()
                dialog.dismiss()
            }
        }

        layoutView.findViewById<TextView>(R.id.categoryCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun setBinding() {
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun hideKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            try {
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    /*//Hide keyboard in fragment
    //hide soft keyboard
    public static void hideKeyboard(Context context, View view){
        InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }*/
    /*@Override
    public void onBackPressed() {
        //super.onBackPressed();
        hideKeyboard();
    }*/
    private fun setViewOrUpdateNote() {
        binding.inputNoteTitle.setText(alreadyAvailableNote!!.title)
        binding.inputNoteSubtitle.setText(alreadyAvailableNote!!.subtitle)
        binding.inputNoteText.setText(alreadyAvailableNote!!.noteText)
        binding.textDateTime.text = alreadyAvailableNote!!.dateTime
        binding.txtSelectedDate.text = alreadyAvailableNote!!.dateTime
        binding.txtSelectedCategory.text = alreadyAvailableNote!!.category
        if (alreadyAvailableNote!!.imagePath != null && !alreadyAvailableNote!!.imagePath.trim()
                .isEmpty()
        ) {
            binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote!!.imagePath))
            binding.imageNote.visibility = View.VISIBLE
            binding.imageRemoveImage.visibility = View.VISIBLE
            selectedImagePath = alreadyAvailableNote!!.imagePath
        }
        if (alreadyAvailableNote!!.webLink != null && !alreadyAvailableNote!!.webLink.trim()
                .isEmpty()
        ) {
            binding.textWebURL.text = alreadyAvailableNote!!.webLink
            binding.layoutWebURL.visibility = View.VISIBLE
        }
    }

    private fun saveNote() {
        if (binding.inputNoteTitle.text.toString().trim().isEmpty()) {
            Toast.makeText(applicationContext, "Note title can't be empty!", Toast.LENGTH_SHORT)
                .show()
            return
        } else if (binding.inputNoteSubtitle.text.toString().trim()
                .isEmpty() && binding.inputNoteText.text.toString().trim().isEmpty()
        ) {
            Toast.makeText(applicationContext, "Note can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        val note = NoteModel()
        note.title = binding.inputNoteTitle.text.toString()
        note.subtitle = binding.inputNoteSubtitle.text.toString()
        note.noteText = binding.inputNoteText.text.toString()
        note.dateTime = binding.txtSelectedDate.text.toString()
        note.color = selectedNoteColor
        note.category = binding.txtSelectedCategory.text.toString()
        note.imagePath = selectedImagePath
        note.trash = false
        note.favorite = false
        if (binding.layoutWebURL.visibility === View.VISIBLE) {
            note.webLink = binding.textWebURL.text.toString()
        }
        if (alreadyAvailableNote != null) {
            note.id = alreadyAvailableNote!!.id
        }
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask :
            AsyncTask<Void?, Void?, Void?>() {

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                val intent = Intent()
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun doInBackground(vararg params: Void?): Void? {
                DatabaseBuilder.getInstance(applicationContext).dao().insertNote(note)
                return null
            }
        }
        SaveNoteTask().execute()
    }

    private fun initMiscellaneous() {
        val layoutMiscellaneous = binding.includedLayoutMisc.layoutMiscellaneous
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)
        //TODO Replace find view by id with View Binding
        layoutMiscellaneous.findViewById<View>(R.id.textMiscellaneous).setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        //TODO Replace find view by id with View Binding
        val imageColor1 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor1)
        val imageColor2 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor2)
        val imageColor3 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor3)
        val imageColor4 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor4)
        val imageColor5 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor5)
        val imageColor6 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor6)
        val imageColor7 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor7)
        layoutMiscellaneous.findViewById<View>(R.id.viewColor1).setOnClickListener {
            selectedNoteColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            imageColor7.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor2).setOnClickListener {
            selectedNoteColor = "#F7DC6F"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            imageColor7.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor3).setOnClickListener {
            selectedNoteColor = "#E16E6E"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            imageColor7.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor4).setOnClickListener {
            selectedNoteColor = "#AED6F1"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            imageColor7.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor5).setOnClickListener {
            selectedNoteColor = "#95A5A6"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            imageColor6.setImageResource(0)
            imageColor7.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor6).setOnClickListener {
            selectedNoteColor = "#48C9B0"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(R.drawable.ic_done)
            imageColor7.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor7).setOnClickListener {
            selectedNoteColor = "#AF7AC5"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            imageColor7.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }
        if (alreadyAvailableNote != null && alreadyAvailableNote!!.color != null && !alreadyAvailableNote!!.color.trim()
                .isEmpty()
        ) {
            when (alreadyAvailableNote!!.color) {
                "#F7DC6F" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor2).performClick()
                "#E16E6E" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor3).performClick()
                "#AED6F1" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor4).performClick()
                "#95A5A6" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor5).performClick()
                "#48C9B0" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor6).performClick()
                "#AF7AC5" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor7).performClick()
            }
        }
        layoutMiscellaneous.findViewById<View>(R.id.textAddImage).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@CreateNoteActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                selectImage()
            }
        }
        layoutMiscellaneous.findViewById<View>(R.id.textAddUrl).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }
        if (alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById<View>(R.id.textDeleteNote).visibility =
                View.VISIBLE
            layoutMiscellaneous.findViewById<View>(R.id.textDeleteNote).setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                showDeleteNoteDialog()
            }
        }
    }

    private fun showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            val builder = AlertDialog.Builder(this@CreateNoteActivity)
            val view: View = layoutInflater.inflate(R.layout.layout_delete_note, null)
            builder.setView(view)
            dialogDeleteNote = builder.create()
            if (dialogDeleteNote!!.window != null) {
                dialogDeleteNote!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            view.findViewById<View>(R.id.textDeleteNote).setOnClickListener {
                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask :
                    AsyncTask<Void?, Void?, Void?>() {

                    override fun onPostExecute(aVoid: Void?) {
                        super.onPostExecute(aVoid)
                        val intent = Intent()
                        intent.putExtra("isNoteDeleted", true)
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    override fun doInBackground(vararg params: Void?): Void? {
//                        NotesDatabase.getNotesDatabase(applicationContext).noteDao()
//                            .deleteNote(alreadyAvailableNote)
                        //DatabaseBuilder.getInstance(applicationContext).dao().deleteNote(alreadyAvailableNote!!)
                        DatabaseBuilder.getInstance(applicationContext).dao()
                            .updateForTrash(true, alreadyAvailableNote!!.id)
                        return null
                    }
                }
                DeleteNoteTask().execute()
            }
            view.findViewById<View>(R.id.textCancel)
                .setOnClickListener { dialogDeleteNote!!.dismiss() }
        }
        dialogDeleteNote!!.show()
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable = binding.viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            val selectedImageUri = data!!.data
            if (data != null) {
                try {
                    val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageNote.setImageBitmap(bitmap)
                    binding.imageNote.visibility = View.VISIBLE
                    binding.imageRemoveImage.visibility = View.VISIBLE
                    selectedImagePath = getPathFromUri(selectedImageUri)
                } catch (exception: Exception) {
                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri?): String {
        val filePath: String?
        val cursor = contentResolver.query(contentUri!!, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath!!
    }

    private fun showAddURLDialog() {
        if (dialogAddURL == null) {
            val builder = AlertDialog.Builder(this@CreateNoteActivity)
            val view: View = layoutInflater.inflate(R.layout.layout_add_url, null)
            builder.setView(view)
            dialogAddURL = builder.create()
            if (dialogAddURL!!.window != null) {
                dialogAddURL!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            val inputURL = view.findViewById<EditText>(R.id.inputURL)
            inputURL.requestFocus()
            view.findViewById<View>(R.id.textAdd).setOnClickListener {
                if (inputURL.text.toString().trim { it <= ' ' }.isEmpty()) {
                    Toast.makeText(applicationContext, "Enter Web Link", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()) {
                    Toast.makeText(applicationContext, "Enter a valid Web Link", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    binding.textWebURL.text = inputURL.text.toString()
                    binding.layoutWebURL.visibility = View.VISIBLE
                    dialogAddURL!!.dismiss()
                }
            }
            view.findViewById<View>(R.id.textCancel).setOnClickListener { dialogAddURL!!.dismiss() }
        }
        dialogAddURL!!.show()
    }

    //ads start

//    private fun newInterstitialAd(): InterstitialAd {
//        val interstitialAd = InterstitialAd(this)
//        interstitialAd.adUnitId = getString(R.string.interstitial_ad_unit_id)
//        interstitialAd.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                binding.imageSave.isEnabled = true
//                //Toast.makeText(applicationContext, "Ad Loaded", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onAdFailedToLoad(errorCode: Int) {
//                binding.imageSave.isEnabled = true
//                // Toast.makeText(applicationContext, "Ad Failed To Load", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onAdClosed() {
//                // Proceed to the next level.
//                // goToNextLevel()
//                saveNote()
////                Toast.makeText(applicationContext, "Ad Closed", Toast.LENGTH_SHORT).show()
////                tryToLoadAdOnceAgain()
//            }
//        }
//        return interstitialAd
//    }
//
//    private fun loadInterstitial() {
//        // Disable the load ad button and load the ad.
//        binding.imageSave.isEnabled = false
//        val adRequest = AdRequest.Builder().build()
//        mInterstitialAd!!.loadAd(adRequest)
//    }
//
//    private fun showInterstitial() {
//        // Show the ad if it is ready. Otherwise toast and reload the ad.
//        if (mInterstitialAd != null && mInterstitialAd!!.isLoaded) {
//            mInterstitialAd!!.show()
//        } else {
//            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
//            tryToLoadAdOnceAgain()
//        }
//    }
//
//    private fun tryToLoadAdOnceAgain() {
//        mInterstitialAd = newInterstitialAd()
//        loadInterstitial()
//    }


    // load ad
    private fun loadAd() {
        InterstitialAd.load(
            this@CreateNoteActivity,
            "ca-app-pub-4820125560371856/4644221002", //real id
//            "ca-app-pub-3940256099942544/1033173712", // testing id
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                mInterstitialAd = null
                                saveNote()

                                //// perform your code that you wants to do after ad dismissed or closed
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                mInterstitialAd = null

                                /// perform your action here when ad will not load
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                this@CreateNoteActivity.mInterstitialAd = null
                            }
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    mInterstitialAd = null
                }
            })
    }

    private fun toNextLevel() {
        // Show the interstitial if it is ready. Otherwise, proceed to the next level
        // without ever showing it
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this@CreateNoteActivity)
        } else {
            saveNote()
            // in case you want to load a new ad
            requestNewInterstitial()
        }
    }

    private fun requestNewInterstitial() {
        if (mInterstitialAd == null) {
            loadAd()
        }
    }

    //ads end

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
        return true
    }
    fun loadDialog(){
        progressDialog = ProgressDialog(this@CreateNoteActivity)
        progressDialog!!.setMessage("Loading...") // Setting Message
        progressDialog!!.setTitle("Saving Your Note") // Setting Title
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER) // Progress Dialog Style Spinner
        progressDialog!!.show() // Display Progress Dialog
        progressDialog!!.setCancelable(false)
        Thread {
            try {
                Thread.sleep(1500)
                if (isNetworkAvailable(this)) {
                toNextLevel()
            } else {
                saveNote()
            }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            progressDialog!!.dismiss()
        }.start()
    }
}