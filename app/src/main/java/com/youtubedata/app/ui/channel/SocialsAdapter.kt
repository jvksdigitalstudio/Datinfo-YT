package com.youtubedata.app.ui.channel

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youtubedata.app.data.model.SocialLink
import com.youtubedata.app.databinding.ItemSocialBinding

class SocialsAdapter(
    private val links: List<SocialLink>,
    private val onClick: (SocialLink) -> Unit
) : RecyclerView.Adapter<SocialsAdapter.SocialViewHolder>() {

    inner class SocialViewHolder(private val binding: ItemSocialBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(link: SocialLink) {
            binding.tvIcon.text   = link.icon
            binding.tvName.text   = link.name
            binding.tvHandle.text = link.handle

            try {
                val color = Color.parseColor(link.colorHex)
                binding.tvIcon.setTextColor(color)
                binding.tvName.setTextColor(color)
                binding.cardSocial.strokeColor = color
            } catch (_: Exception) {}

            binding.root.setOnClickListener { onClick(link) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialViewHolder {
        val binding = ItemSocialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SocialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialViewHolder, position: Int) = holder.bind(links[position])
    override fun getItemCount() = links.size
}
