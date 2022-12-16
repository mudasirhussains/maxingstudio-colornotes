package com.notes.colornotes.trash

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.notes.colornotes.R
import com.notes.colornotes.databinding.ActivityTrashBinding
import com.notes.colornotes.room.database.DatabaseBuilder
import com.notes.colornotes.room.entity.NoteModel

class TrashActivity : AppCompatActivity(), TrashListeners {
    lateinit var binding: ActivityTrashBinding
    private lateinit var mViewModel: TrashViewModel
    private var noteList: ArrayList<NoteModel> = ArrayList()
    private var notesAdapter: TrashAdapter? = null
    private var dialogTrashNote: AlertDialog? = null
    var customId: Int = -1
    var customPosition: Int = -1
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
        noteList = DatabaseBuilder.getInstance(applicationContext).dao().getAllTrashNotes() as ArrayList<NoteModel>
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
            binding.notesRecyclerViewTrash.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            notesAdapter = TrashAdapter(noteList as ArrayList<NoteModel>, this)
            binding.notesRecyclerViewTrash.adapter = notesAdapter
        }

    }

    private fun setBindings() {
        binding = ActivityTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mViewModel = ViewModelProvider(this).get(TrashViewModel::class.java)
    }

    private fun showOnlyTrashNotes(notes: List<NoteModel>) {
        noteList.clear()
        for (i in notes.indices) {
            if (notes[i].trash == true) {
                noteList.add(notes[i])
            }
        }
    }

    private fun showDeleteNoteDialog() {
        if (dialogTrashNote == null) {
            val builder = AlertDialog.Builder(this@TrashActivity)
            val view: View = layoutInflater.inflate(R.layout.layout_trash_options, null)
            builder.setView(view)
            dialogTrashNote = builder.create()
            if (dialogTrashNote!!.window != null) {
                dialogTrashNote!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }

            view.findViewById<View>(R.id.textUndo).setOnClickListener {
                if (customId > -1 && customPosition > -1) {
                    mViewModel.updateDBForTrash(false, customId)
                    if (noteList.isNotEmpty()) {
                        noteList.removeAt(customPosition)
                        notesAdapter!!.notifyItemRemoved(customPosition)
                        dialogTrashNote!!.dismiss()
                    } else {
                        finish()
                        dialogTrashNote!!.dismiss()
                    }
                } else {
                    Toast.makeText(applicationContext, "Unable to delete note", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            view.findViewById<View>(R.id.textDeleteNoteTrash).setOnClickListener {
                if (customId > -1 && customPosition > -1) {
                    mViewModel.deleteNoteForTrash(customId)
                    if (noteList.isNotEmpty()) {
                        noteList.removeAt(customPosition)
                        notesAdapter!!.notifyItemRemoved(customPosition)
                        dialogTrashNote!!.dismiss()
                    } else {
                        binding.srNotes.visibility = View.VISIBLE
                        dialogTrashNote!!.dismiss()
                    }
                } else {
                    Toast.makeText(applicationContext, "Unable to delete note", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            view.findViewById<View>(R.id.textCancelTrash)
                .setOnClickListener { dialogTrashNote!!.dismiss() }
        }
        dialogTrashNote!!.show()
    }

    override fun onSingleClicked(id: Int, position: Int) {
        customId = id
        customPosition = position
        showDeleteNoteDialog()
    }
}