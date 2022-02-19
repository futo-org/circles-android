package com.futo.circles.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseRecyclerViewHolder<T, VB : ViewBinding> : RecyclerView.ViewHolder {

    protected val binding: VB

    protected constructor(
        parent: ViewGroup,
        inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
    ) : this(inflate.invoke(LayoutInflater.from(parent.context), parent, false))

    private constructor(viewBinding: VB) : super(viewBinding.root) {
        this.binding = viewBinding
    }

    abstract fun bind(data: T)
}

val RecyclerView.ViewHolder.context: Context get() = this.itemView.context

abstract class BaseRvAdapter<T, VH : RecyclerView.ViewHolder>(
    itemCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(itemCallback) {

    @Suppress("UNCHECKED_CAST")
    protected fun <D : T> getItemAs(position: Int): D = getItem(position) as D

    companion object {
        @Suppress("FunctionName")
        @SuppressLint("DiffUtilEquals")
        fun <T> DefaultDiffUtilCallback() = object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
        }
    }

}
