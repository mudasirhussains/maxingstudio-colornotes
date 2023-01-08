package com.notes.colornotes.addnotes.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.notes.colornotes.R
import com.notes.colornotes.addnotes.NotesListener
import com.notes.colornotes.room.entity.NoteModel
import java.util.*


class NotesAdapter(
    private var notes: ArrayList<NoteModel>,
    private val notesListener: NotesListener
) :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    private var timer: Timer? = null
    var isChecked = false
    val selectedPos: ArrayList<Int> = ArrayList()
    private val notesSource: ArrayList<NoteModel>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_container_note,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.setNote(notes[position])
        holder.layoutNote.setOnClickListener {
            notesListener.onNoteClicked(
                notes[position],
                position
            )
        }

        holder.imageCheckBox.visibility =
            if (notes[holder.adapterPosition].isSelected) View.VISIBLE else View.GONE

        holder.layoutNote.setOnLongClickListener {
            notes[holder.adapterPosition].isSelected = !notes[holder.adapterPosition].isSelected
            if (notes[holder.adapterPosition].isSelected) {
                notes[holder.adapterPosition].isSelected = true
                selectedPos.add(holder.adapterPosition)
            }else{
                notes[holder.adapterPosition].isSelected = false
                selectedPos.remove(holder.adapterPosition)
            }
            holder.imageCheckBox.visibility =
                if (notes[holder.adapterPosition].isSelected) View.VISIBLE else View.GONE
            true
        }

        if (notes[holder.adapterPosition].favorite) {
            holder.imageFav.setImageResource(R.drawable.ic_filled_fav)
        } else {
            holder.imageFav.setImageResource(R.drawable.ic_unfilled_fav)
        }

        holder.imageFav.setOnClickListener {
            if (notes[position].favorite) {
                notes[position].favorite = false
                holder.imageFav.setImageResource(R.drawable.ic_unfilled_fav)
                notesListener.onNoteFavorite(notes[holder.adapterPosition], holder.adapterPosition)
            } else {
                notes[position].favorite = true
                holder.imageFav.setImageResource(R.drawable.ic_filled_fav)
                notesListener.onNoteFavorite(notes[holder.adapterPosition], holder.adapterPosition)
            }
        }

        holder.optionMenu.setOnClickListener {
            notesListener.onNoteOptionMenu(notes[holder.adapterPosition], holder.adapterPosition)
        }


    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView
        var textSubtitle: TextView
        var textDateTime: TextView
        var layoutNote: LinearLayout
        var imageNote: ImageView
        var imageFav: ImageView
        var imageCheckBox: ImageView
        var optionMenu: ImageView
        fun setNote(note: NoteModel) {
            textTitle.text = note.title
            if (note.subtitle.trim().isEmpty()) {
                textSubtitle.visibility = View.GONE
            } else {
                textSubtitle.text = note.subtitle
            }
            textDateTime.text = note.dateTime
            val gradientDrawable = layoutNote.background as GradientDrawable
            if (note.color != null) {
                gradientDrawable.setColor(Color.parseColor(note.color))
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"))
            }
            if (note.imagePath != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.imagePath))
                imageNote.visibility = View.VISIBLE
            } else {
                imageNote.visibility = View.GONE
            }
        }

        init {
            //TODO Replace findViewByID with View Binding
            textTitle = itemView.findViewById(R.id.textTitle)
            textSubtitle = itemView.findViewById(R.id.textSubtitle)
            textDateTime = itemView.findViewById(R.id.textDateTime)
            layoutNote = itemView.findViewById(R.id.layoutNote)
            imageNote = itemView.findViewById(R.id.imageNote)
            imageFav = itemView.findViewById(R.id.imageFav)
            imageCheckBox = itemView.findViewById(R.id.imgCheckBox)
            optionMenu = itemView.findViewById(R.id.imageMenuCard)
        }
    }

    fun searchNotes(searchKeyword: String) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                notes = if (searchKeyword.trim { it <= ' ' }.isEmpty()) {
                    notesSource
                } else {
                    val temp = ArrayList<NoteModel>()
                    for (note in notesSource) {
                        if (note.title.toLowerCase()
                                .contains(searchKeyword.lowercase(Locale.getDefault()))
                            || note.subtitle.toLowerCase()
                                .contains(searchKeyword.lowercase(Locale.getDefault()))
                            || note.noteText.toLowerCase()
                                .contains(searchKeyword.lowercase(Locale.getDefault()))
                        ) {
                            temp.add(note)
                        }
                    }
                    temp
                }
                Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
            }
        }, 500)
    }

    fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
        }
    }

    init {
        notesSource = notes
    }

    fun getAll(): ArrayList<NoteModel> {
        return notes
    }

    fun getSelected(): ArrayList<NoteModel>? {
        val selected: ArrayList<NoteModel> = ArrayList()
        for (i in 0 until notes.size) {
            if (notes[i].isSelected) {
                selected.add(notes[i])
            }
        }
        return selected
    }

    fun getSelectedPositions(): ArrayList<Int> {
        //notesListener.onMultipleNote(selectedPos)
        return selectedPos
    }
}
