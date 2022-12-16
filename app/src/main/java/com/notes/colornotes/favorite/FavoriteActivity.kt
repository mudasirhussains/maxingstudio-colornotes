package com.notes.colornotes.favorite

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.notes.colornotes.R
import com.notes.colornotes.databinding.ActivityFavoriteBinding
import com.notes.colornotes.room.database.DatabaseBuilder
import com.notes.colornotes.room.entity.NoteModel
import com.notes.colornotes.trash.TrashAdapter
import com.notes.colornotes.trash.TrashViewModel

class FavoriteActivity : AppCompatActivity(), FavoriteListeners {
    lateinit var binding:ActivityFavoriteBinding
    private lateinit var mViewModel: FavoriteViewModel
    private var notesAdapter: FavoriteAdapter? = null
    private var noteList: ArrayList<NoteModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBindings()
        setCallBacks()
        getData()
        setRecyclerView()

        binding.srNotes.setOnRefreshListener {
            binding.srNotes.isRefreshing = false
        }
    }

    private fun getData() {
        noteList = DatabaseBuilder.getInstance(applicationContext).dao().getAllFavoriteNotes() as ArrayList<NoteModel>
    }

    private fun setCallBacks() {
        findViewById<ImageView>(R.id.imgGoBackWeb).setOnClickListener {
            finish()
        }
    }

    private fun setRecyclerView() {
        if (noteList.isEmpty()) {
            binding.layoutNoNotes.visibility = View.VISIBLE
        } else {
            binding.layoutNoNotes.visibility = View.GONE
            binding.notesRecyclerViewFav.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            notesAdapter = FavoriteAdapter(noteList as ArrayList<NoteModel>, this)
            binding.notesRecyclerViewFav.adapter = notesAdapter
        }

    }

    private fun setBindings() {
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mViewModel = ViewModelProvider(this).get(FavoriteViewModel::class.java)
    }

    override fun onFavoriteClickedForUnFav(id: Int, position: Int) {
        mViewModel.updateDBForFavorite(false, id)
        if (position != 0){
            if (noteList.isNotEmpty()) {
                noteList.removeAt(position)
                notesAdapter!!.notifyItemRemoved(position)
            } else {
                finish()
            }
        }else{
            finish()
        }

    }
}