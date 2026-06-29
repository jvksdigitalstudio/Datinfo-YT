package com.youtubedata.app.ui.channel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.youtubedata.app.R
import com.youtubedata.app.data.model.ChannelData
import com.youtubedata.app.databinding.ActivityChannelBinding
import com.youtubedata.app.ui.ImageViewerActivity
import com.youtubedata.app.utils.ExportUtils
import com.youtubedata.app.utils.FormatUtils

class ChannelActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHANNEL = "extra_channel"
    }

    private lateinit var binding: ActivityChannelBinding
    private lateinit var channel: ChannelData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        channel = intent.getParcelableExtra(EXTRA_CHANNEL) ?: run { finish(); return }

        setupToolbar()
        renderChannel()
        renderStats()
        renderSocials()
        renderVideos()
        renderDescription()
        renderKeywords()
        setupExport()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun renderChannel() {
        // Banner
        if (channel.bannerUrl.isNotBlank()) {
            Glide.with(this).load(channel.bannerUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgBanner)
            // Click en banner → pantalla completa
            binding.imgBanner.setOnClickListener { openImage(channel.bannerUrl) }
        } else {
            binding.imgBanner.setBackgroundColor(Color.parseColor("#1a1a1a"))
        }

        // Avatar
        Glide.with(this).load(channel.avatarUrl)
            .placeholder(R.drawable.ic_avatar_placeholder)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imgAvatar)
        // Click en avatar → pantalla completa
        if (channel.avatarUrl.isNotBlank()) {
            binding.imgAvatar.setOnClickListener { openImage(channel.avatarUrl) }
        }

        binding.tvChannelName.text = channel.name
        binding.tvHandle.text      = channel.handle.ifBlank { channel.url }

        if (channel.country.isNotBlank() && channel.country != "—") {
            binding.tvCountry.text       = "${channel.countryFlag} ${channel.country}"
            binding.tvCountry.visibility = View.VISIBLE
        }

        binding.tvCreatedAt.text = "Canal creado el ${FormatUtils.formatDate(channel.createdAt)}"
        binding.tvChannelId.text = "ID: ${channel.id}"
        binding.tvChannelId.setOnClickListener { copyToClipboard("Channel ID", channel.id) }

        binding.btnOpenYoutube.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(channel.url)))
        }
        binding.btnCopyId.setOnClickListener { copyToClipboard("ID del canal", channel.id) }

        if (channel.topicCategories.isNotEmpty()) {
            binding.tvTopics.text         = channel.topicCategories.joinToString(" · ")
            binding.cardTopics.visibility = View.VISIBLE
        }
    }

    private fun renderStats() {
        val subsText = if (channel.hiddenSubscribers) "Ocultos"
                       else FormatUtils.formatNumber(channel.subscribersRaw)
        val subsFull = if (channel.hiddenSubscribers) ""
                       else FormatUtils.formatFull(channel.subscribersRaw)

        binding.tvSubsDisplay.text       = subsText
        binding.tvSubsFullDisplay.text   = subsFull
        binding.tvVideosDisplay.text     = FormatUtils.formatNumber(channel.videosRaw)
        binding.tvVideosFullDisplay.text = FormatUtils.formatFull(channel.videosRaw)
        binding.tvViewsDisplay.text      = FormatUtils.formatNumber(channel.totalViewsRaw)
        binding.tvViewsFullDisplay.text  = FormatUtils.formatFull(channel.totalViewsRaw)

        if (!channel.hiddenSubscribers && channel.subscribersRaw > 0) {
            val ratio = channel.totalViewsRaw.toDouble() / channel.subscribersRaw
            binding.tvRatio.text       = "%.1fx vistas/suscriptor".format(ratio)
            binding.tvRatio.visibility = View.VISIBLE
        }
    }

    private fun renderSocials() {
        if (channel.socialLinks.isEmpty()) { binding.cardSocials.visibility = View.GONE; return }
        binding.cardSocials.visibility = View.VISIBLE
        val adapter = SocialsAdapter(channel.socialLinks) { link ->
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.href))) }
            catch (_: Exception) { copyToClipboard(link.name, link.href) }
        }
        binding.rvSocials.apply {
            layoutManager = GridLayoutManager(this@ChannelActivity, 2)
            this.adapter   = adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun renderVideos() {
        if (channel.videos.isEmpty()) { binding.cardVideos.visibility = View.GONE; return }
        binding.cardVideos.visibility = View.VISIBLE
        val adapter = VideosAdapter(channel.videos) { video ->
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=${video.id}")))
        }
        binding.rvVideos.apply {
            layoutManager = LinearLayoutManager(this@ChannelActivity)
            this.adapter   = adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun renderDescription() {
        if (channel.description.isBlank()) { binding.cardDescription.visibility = View.GONE; return }
        binding.cardDescription.visibility = View.VISIBLE
        binding.tvDescription.text         = channel.description
        binding.tvDescription.maxLines     = 5
        binding.btnExpandDesc.setOnClickListener {
            if (binding.tvDescription.maxLines == 5) {
                binding.tvDescription.maxLines = Int.MAX_VALUE
                binding.btnExpandDesc.text     = "Ver menos"
            } else {
                binding.tvDescription.maxLines = 5
                binding.btnExpandDesc.text     = "Ver más"
            }
        }
        binding.btnCopyDesc.setOnClickListener { copyToClipboard("Descripción", channel.description) }
    }

    private fun renderKeywords() {
        if (channel.keywords.isEmpty()) { binding.cardKeywords.visibility = View.GONE; return }
        binding.cardKeywords.visibility = View.VISIBLE
        binding.chipGroupKeywords.removeAllViews()
        channel.keywords.take(30).forEach { keyword ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text        = keyword
                isClickable = false
                setChipBackgroundColorResource(R.color.chip_bg)
                setTextColor(Color.parseColor("#cccccc"))
            }
            binding.chipGroupKeywords.addView(chip)
        }
    }

    private fun setupExport() {
        binding.btnExportJson.setOnClickListener { ExportUtils.exportJson(this, channel) }
        binding.btnExportTxt.setOnClickListener  { ExportUtils.exportTxt(this, channel) }
        binding.btnShare.setOnClickListener {
            val text = buildString {
                appendLine("📊 ${channel.name} (${channel.handle})")
                appendLine("👥 ${FormatUtils.formatNumber(channel.subscribersRaw)} suscriptores")
                appendLine("🎬 ${FormatUtils.formatNumber(channel.videosRaw)} videos")
                appendLine("👁 ${FormatUtils.formatNumber(channel.totalViewsRaw)} vistas")
                appendLine("🌍 ${channel.countryFlag} ${channel.country}")
                appendLine("🔗 ${channel.url}")
                append("\nAnalizado con Datinfo YT")
            }
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) },
                "Compartir canal"
            ))
        }
    }

    private fun openImage(url: String) {
        val intent = Intent(this, ImageViewerActivity::class.java).apply {
            putExtra(ImageViewerActivity.EXTRA_URL, url)
        }
        startActivity(intent)
    }

    private fun copyToClipboard(label: String, text: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(this, "✅ $label copiado", Toast.LENGTH_SHORT).show()
    }
}
