package com.notes.colornotes.fragments.allnotes.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.notes.colornotes.R
import com.notes.colornotes.room.entity.NoteModel

class CategoriesAdapter(private var categoriesList: List<String>, private val categoriesListener: CategoriesListeners) :
    RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.category_list_item,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.setCategory(categoriesList[position])


        holder.cardCategory.setOnClickListener {
            categoriesListener.onCategoriesClicked(holder.adapterPosition, categoriesList[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryTitle: TextView
        var cardCategory: CardView
        fun setCategory(note: String) {
            if (!note.equals("select category", true)){
                categoryTitle.text = note
            }

        }

        init {
            //TODO Replace findViewByID with View Binding
            categoryTitle = itemView.findViewById(R.id.txtCategory)
            cardCategory = itemView.findViewById(R.id.cardCategory)
        }
    }
}
