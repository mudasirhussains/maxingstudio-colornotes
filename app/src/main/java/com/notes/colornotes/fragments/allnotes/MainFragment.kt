package com.notes.colornotes.fragments.allnotes

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.notes.colornotes.R
import com.notes.colornotes.addnotes.CreateNoteActivity
import com.notes.colornotes.addnotes.NotesListener
import com.notes.colornotes.addnotes.adapter.NotesAdapter
import com.notes.colornotes.databinding.FragmentMainBinding
import com.notes.colornotes.fragments.allnotes.categories.CategoriesAdapter
import com.notes.colornotes.fragments.allnotes.categories.CategoriesListeners
import com.notes.colornotes.room.database.DatabaseBuilder
import com.notes.colornotes.room.entity.NoteModel
import java.io.File


class MainFragment : Fragment(), NotesListener, CategoriesListeners {


    lateinit var binding: FragmentMainBinding

    val REQUEST_CODE_ADD_NOTE = 1
    val REQUEST_CODE_UPDATE_NOTE = 2
    val REQUEST_CODE_SHOW_NOTES = 3
    val REQUEST_CODE_SELECT_IMAGE = 4
    val REQUEST_CODE_STORAGE_PERMISSION = 5
    lateinit var noteList: ArrayList<NoteModel>
    private var notesAdapter: NotesAdapter? = null
    private var categoriesAdapter: CategoriesAdapter? = null
    private var noteClickedPosition = -1
    private var isLinear = false
    private var positionRadio = 1
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(layoutInflater)

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        val addNoteButton =
            requireActivity().findViewById<LinearLayoutCompat>(R.id.btn_plus) as LinearLayoutCompat

        addNoteButton.setOnClickListener(View.OnClickListener {
            startActivityForResult(
                Intent(requireContext(), CreateNoteActivity::class.java),
                REQUEST_CODE_ADD_NOTE
            )
        })

        loadAd()
        Handler().postDelayed({
            toNextLevel()
        }, 3000)

        //binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setGridRecycler()
        noteList = ArrayList()
        notesAdapter = NotesAdapter(noteList as ArrayList<NoteModel>, this)
        binding.notesRecyclerView.adapter = notesAdapter

        getNotes(REQUEST_CODE_SHOW_NOTES, false)

        binding.srNotes.setOnRefreshListener {
            getNotes(REQUEST_CODE_SHOW_NOTES, false)
            binding.srNotes.isRefreshing = false
        }

        setCategoriesAdapter()

        val toolbarSearch =
            requireActivity().findViewById<AppCompatEditText>(R.id.etSearch) as AppCompatEditText
        toolbarSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                notesAdapter!!.cancelTimer()
            }

            override fun afterTextChanged(s: Editable) {
                if ((noteList).size != 0) {
                    notesAdapter!!.searchNotes(s.toString())
                }
            }
        })

        setHasOptionsMenu(true)

        return binding.root

    }

    private fun setCategoriesAdapter() {
        val mList = ArrayList<String>()
        var filteredCategories: ArrayList<NoteModel>? = null
        val categoryList: ArrayList<NoteModel> = DatabaseBuilder.getInstance(requireContext()).dao()
            .getAllNotes() as ArrayList<NoteModel>
        if (categoryList.isNotEmpty()) {
            binding.txtCategoryHome.visibility = View.VISIBLE
            filteredCategories?.clear()
            filteredCategories = categoryList.filter { s ->
                !s.category.equals(
                    "select category",
                    true
                )
            } as ArrayList<NoteModel>
        }
        filteredCategories?.forEach { res ->
            mList.add(res.category.toString())
        }
        val distinct: List<String> = mList.toSet().toList()
        categoriesAdapter = CategoriesAdapter(distinct, this)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerHomeCategories.layoutManager = layoutManager
        binding.recyclerHomeCategories.adapter = categoriesAdapter
    }

    private fun loadAds() {
        binding.adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                val toastMessage: String = "ad fail to load"
                //Toast.makeText(requireContext(), toastMessage.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                val toastMessage: String = "ad loaded"
                // Toast.makeText(applicationContext, toastMessage.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onAdOpened() {
                super.onAdOpened()
                val toastMessage: String = "ad is open"
//                Toast.makeText(applicationContext, toastMessage.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                val toastMessage: String = "ad is clicked"
//                Toast.makeText(applicationContext, toastMessage.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onAdClosed() {
                super.onAdClosed()
                val toastMessage: String = "ad is closed"
//                Toast.makeText(applicationContext, toastMessage.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                val toastMessage: String = "ad impression"
//                Toast.makeText(applicationContext, toastMessage.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setGridRecycler() {
        isLinear = false
        binding.notesRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun setLinearRecycler() {
        isLinear = true
        binding.notesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_grid -> {
                if (isLinear) {
                    setGridRecycler()
                } else {
                    setLinearRecycler()
                }
            }
            R.id.action_sort -> {
                callSortingDialog()
            }

            R.id.action_delete -> {
                actionDelete()
            }
        }
        return super.onOptionsItemSelected(item)
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
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onNoteClicked(note: NoteModel?, position: Int) {
        noteClickedPosition = position
        val intent = Intent(requireContext(), CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }

    override fun onNoteFavorite(noteModel: NoteModel?, position: Int) {
        if (noteModel!!.favorite == true) {
            DatabaseBuilder.getInstance(requireContext()).dao()
                .updateForFavorite(true, noteModel.id)
        } else {
            DatabaseBuilder.getInstance(requireContext()).dao()
                .updateForFavorite(false, noteModel.id)
        }
    }


    private fun getNotes(requestCode: Int, isNoteDeleted: Boolean) {
        class GetNoteTask :
            AsyncTask<Void?, Void?, List<NoteModel>>() {
            override fun onPreExecute() {
                super.onPreExecute()
                binding.srNotes.isRefreshing = true
            }

            override fun onPostExecute(notes: List<NoteModel>) {
                super.onPostExecute(notes)
                binding.srNotes.isRefreshing = false
                if (notes.size == 0) {
                    binding.layoutNoNotes.visibility = View.VISIBLE
                    binding.notesRecyclerView.visibility = View.GONE
                } else {
                    binding.layoutNoNotes.visibility = View.GONE
                    binding.notesRecyclerView.visibility = View.VISIBLE
                    if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                        //noteList.addAll(notes)
                        showWithoutTrashNotes(notes)
                        //notesAdapter!!.notifyDataSetChanged()
                    } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                        noteList.add(0, notes[0])
                        notesAdapter!!.notifyItemInserted(0)
                        binding.notesRecyclerView.smoothScrollToPosition(0)
                    } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                        noteList.removeAt(noteClickedPosition)
                        if (isNoteDeleted) {
                            notesAdapter!!.notifyItemRemoved(noteClickedPosition)
                        } else {
                            noteList.add(noteClickedPosition, notes[noteClickedPosition])
                            notesAdapter!!.notifyItemChanged(noteClickedPosition)
                        }
                    }
                }
            }

            override fun doInBackground(vararg params: Void?): List<NoteModel> {
                return DatabaseBuilder.getInstance(requireContext()).dao().getAllNotes()
//                NotesDatabase.getNotesDatabase(requireContext()).noteDao().allNotes

            }
        }
        GetNoteTask().execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == Activity.RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false)
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                getNotes(
                    REQUEST_CODE_UPDATE_NOTE,
                    data.getBooleanExtra("isNoteDeleted", false)
                )
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImageUri = data.data
                if (selectedImageUri != null) {
                    try {
                        val selectedImagePath: String = getPathFromUri(selectedImageUri)
                        val intent = Intent(requireContext(), CreateNoteActivity::class.java)
                        intent.putExtra("isFromQuickActions", true)
                        intent.putExtra("quickActionType", "image")
                        intent.putExtra("imagePath", selectedImagePath)
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                    } catch (exception: Exception) {
                        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri): String {
        val filePath: String?
        val cursor: Cursor? =
            requireActivity().contentResolver.query(contentUri, null, null, null, null)
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

    private fun showWithoutTrashNotes(notes: List<NoteModel>) {
        noteList.clear()
        for (i in notes.indices) {
            if (notes[i].trash == false) {
                noteList.add(notes[i])
            }
        }
        notesAdapter!!.notifyDataSetChanged()
    }

    private fun callSortingDialog() {
        val factory = LayoutInflater.from(requireContext())
        val layoutView: View = factory.inflate(R.layout.dialog_arrangment, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).create()
        dialog.setView(layoutView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val radioGroup = layoutView.findViewById<RadioGroup>(R.id.arrange_radio_group)
        val radioOne = layoutView.findViewById<RadioButton>(R.id.arrange_radio_btn1)
        val radioTwo = layoutView.findViewById<RadioButton>(R.id.arrange_radio_btn2)
        val radioThree = layoutView.findViewById<RadioButton>(R.id.arrange_radio_btn3)
        val radioFour = layoutView.findViewById<RadioButton>(R.id.arrange_radio_btn4)

        when (positionRadio) {
            1 -> {
                radioOne.isChecked = true
            }
            2 -> {
                radioTwo.isChecked = true
            }
            3 -> {
                radioThree.isChecked = true
            }
            4 -> {
                radioFour.isChecked = true
            }
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, checkId ->
            when (checkId) {
                R.id.arrange_radio_btn1 -> {
                    sortAscending()
                    radioOne.isChecked = true
                    positionRadio = 1
                }
                R.id.arrange_radio_btn2 -> {
                    sortDescending()
                    radioTwo.isChecked = true
                    positionRadio = 1
                }
                R.id.arrange_radio_btn3 -> {
                    radioThree.isChecked = true
                    positionRadio = 3
                    sortAtoZ()
                }
                R.id.arrange_radio_btn4 -> {
                    radioFour.isChecked = true
                    positionRadio = 4
                    sortZtoA()
                }
            }
        }

//        val radioId: Int = radioGroup.checkedRadioButtonId
//        val radio: RadioButton = layoutView.findViewById(radioId)
//        radio.text

        layoutView.findViewById<TextView>(R.id.arrange_ok).setOnClickListener {
            dialog.dismiss()
        }

        layoutView.findViewById<TextView>(R.id.arrange_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sortAscending() {
        noteList.clear()
        noteList = DatabaseBuilder.getInstance(requireContext()).dao()
            .getAllNotes() as ArrayList<NoteModel>
        notesAdapter = NotesAdapter(noteList, this)
        binding.notesRecyclerView.adapter = notesAdapter
    }

    private fun sortDescending() {
        noteList.clear()
        noteList = DatabaseBuilder.getInstance(requireContext()).dao()
            .getAllSortByOldest() as ArrayList<NoteModel>
        notesAdapter = NotesAdapter(noteList, this)
        binding.notesRecyclerView.adapter = notesAdapter
    }

    private fun sortAtoZ() {
        noteList.clear()
        noteList = DatabaseBuilder.getInstance(requireContext()).dao()
            .getAllSortByAZ() as ArrayList<NoteModel>
        notesAdapter = NotesAdapter(noteList, this)
        binding.notesRecyclerView.adapter = notesAdapter
    }

    private fun sortZtoA() {
        noteList.clear()
        noteList = DatabaseBuilder.getInstance(requireContext()).dao()
            .getAllSortByZA() as ArrayList<NoteModel>
        notesAdapter = NotesAdapter(noteList, this)
        binding.notesRecyclerView.adapter = notesAdapter
    }

    private fun actionDelete() {
        val deleteList: ArrayList<Int> = ArrayList()
        if (notesAdapter?.getSelected()?.size!! > 0) {
            val stringBuilder = StringBuilder()
            for (i in 0 until notesAdapter?.getSelected()!!.size) {
                deleteList.add(notesAdapter?.getSelected()!![i].id)
//                noteList.removeAt(notesAdapter?.getSelected()!![i].id)
//                notesAdapter!!.notifyItemRemoved(notesAdapter?.getSelected()!![i].id)
                stringBuilder.append(notesAdapter?.getSelected()!![i].id)
                stringBuilder.append("\n")
            }
//            showToast(stringBuilder.toString().trim { it <= ' ' })
            if (noteList.size === 0) {
                showToast("Nothing")
            }
            //database delete
            deleteMultipleItems(deleteList)

        } else {
            showToast("No Selected Item Found")
        }
    }

    private fun deleteMultipleItems(deleteList: ArrayList<Int>) {
        DatabaseBuilder.getInstance(requireContext()).dao().updateForTrashMultiple(true, deleteList)
        getNotes(REQUEST_CODE_SHOW_NOTES, false)
    }


    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        if (binding.adView != null) {
            binding.adView.pause();
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (binding.adView != null) {
            binding.adView.resume();
        }
        setCategoriesAdapter()
    }

    override fun onDestroy() {
        if (binding.adView != null) {
            binding.adView.resume();
        }
        super.onDestroy()
    }

    // load ad
    private fun loadAd() {
        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-4820125560371856/4644221002",
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
                                //// perform your code that you wants to do after ad dismissed or closed
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                mInterstitialAd = null

                                /// perform your action here when ad will not load
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                mInterstitialAd = null
                            }
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    mInterstitialAd = null
                    Log.d("kkki==", "onAdFailedToLoad: " + loadAdError.message)
                }
            })
    }

    private fun toNextLevel() {
        // Show the interstitial if it is ready. Otherwise, proceed to the next level
        // without ever showing it
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(requireActivity())
        } else {
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

    private fun loadAdForPhone() {
        class AsyncLoadData :
            AsyncTask<Void?, Void?, ArrayList<File>?>() {
            override fun doInBackground(vararg p0: Void?): ArrayList<File>? {
                loadAd()
                return null
            }

            override fun onPostExecute(result: ArrayList<File>?) {
                toNextLevel()
            }

            override fun onPreExecute() {

            }

        }
        AsyncLoadData().execute()
    }

    override fun onCategoriesClicked(position: Int, category: String?) {
        noteList.clear()
        noteList = DatabaseBuilder.getInstance(requireContext()).dao()
            .getAllNotes() as ArrayList<NoteModel>
        if (noteList.isNotEmpty()) {
            noteList = noteList.filter { s -> s.category.equals(category) } as ArrayList<NoteModel>
        }
        notesAdapter = NotesAdapter(noteList, this)
        binding.notesRecyclerView.adapter = notesAdapter
    }

}