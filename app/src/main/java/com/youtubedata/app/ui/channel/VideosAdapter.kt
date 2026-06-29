package com.youtubedata.app.ui.channel

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.youtubedata.app.R
import com.youtubedata.app.data.model.VideoData
import com.youtubedata.app.databinding.ItemVideoBinding
import com.youtubedata.app.utils.FormatUtils

class VideosAdapter(
    private val videos: List<VideoData>,
    private val onClick: (VideoData) -> Unit
) : RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(video: VideoData) {
            binding.tvTitle.text    = video.title
            binding.tvViews.text    = "${FormatUtils.formatNumber(video.viewsRaw)} vistas"
            binding.tvDuration.text = video.duration
            binding.tvDate.text     = FormatUtils.timeAgo(video.publishedAt)
            binding.tvLikes.text    = FormatUtils.formatNumber(video.likesRaw)

            // Thumbnail
            Glide.with(binding.root.context)
                .load(video.thumbUrl)
                .placeholder(R.drawable.ic_video_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgThumb)

            // Badges
            binding.badgeLatest.visibility  = if (video.isLatest)   View.VISIBLE else View.GONE
            binding.badgePopular.visibility = if (video.isPopular)  View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onClick(video) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) = holder.bind(videos[position])
    override fun getItemCount() = videos.size
}
