package com.nnine.pickmymeal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nnine.pickmymeal.data.model.Restaurant
import com.nnine.pickmymeal.databinding.ItemRestaurantBinding

class RestaurantAdapter(
    private val onItemClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    private var items: List<Restaurant> = emptyList()

    fun submitList(list: List<Restaurant>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(restaurant: Restaurant) {
            binding.tvName.text = restaurant.name
            binding.tvCategoryItem.text = restaurant.category.substringAfterLast(" > ")
            binding.tvAddressItem.text = restaurant.roadAddress.ifEmpty { restaurant.address }
            binding.tvDistanceItem.text = if (restaurant.distance.isNotEmpty()) "${restaurant.distance}m" else ""
            binding.root.setOnClickListener { onItemClick(restaurant) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRestaurantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
