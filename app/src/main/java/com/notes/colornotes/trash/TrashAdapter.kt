package com.notes.colornotes.trash

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.notes.colornotes.R
import com.notes.colornotes.room.entity.NoteModel
import java.util.*

class TrashAdapter(private var notes: List<NoteModel>, private val notesListener: TrashListeners) :
    RecyclerView.Adapter<TrashAdapter.NotesViewHolder>() {
    private var timer: Timer? = null
    var onItemLongClick: ((NoteModel) -> Unit)?= null
    private val notesSource: List<NoteModel>
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
//        holder.layoutNote.setOnClickListener {
//            notesListener.onNoteClicked(
//                notes[position],
//                position
//            )
//        }

//        holder.layoutNote.setOnLongClickListener {
//            notesListener.onLongClick(notes[holder.adapterPosition].id, holder.adapterPosition)
//            return@setOnLongClickListener true
//        }


        if (notes[holder.adapterPosition].favorite) {
            holder.imageFav.setImageResource(R.drawable.ic_filled_fav)
        } else {
            holder.imageFav.setImageResource(R.drawable.ic_unfilled_fav)
        }

        holder.layoutNote.setOnClickListener {
            notesListener.onSingleClicked(notes[holder.adapterPosition].id, holder.adapterPosition)
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
}
