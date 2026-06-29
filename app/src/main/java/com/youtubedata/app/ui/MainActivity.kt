package com.youtubedata.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.youtubedata.app.R
import com.youtubedata.app.databinding.ActivityMainBinding
import com.youtubedata.app.ui.channel.ChannelActivity
import com.youtubedata.app.utils.PrefsManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()

        // Deep link: si la app se abrió desde una URL de YouTube
        intent?.data?.toString()?.let { url ->
            if (url.contains("youtube.com")) {
                binding.etSearch.setText(url)
                triggerAnalysis()
            }
        }
    }

    private fun setupUI() {
        // Botón analizar
        binding.btnAnalyze.setOnClickListener { triggerAnalysis() }

        // Enter en el teclado
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { triggerAnalysis(); true } else false
        }

        // Limpiar
        binding.btnClear.setOnClickListener {
            binding.etSearch.text?.clear()
            binding.btnClear.visibility = View.GONE
            viewModel.reset()
        }

        // Mostrar botón X cuando hay texto
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.btnClear.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
        })

        // Demo
        binding.btnDemo.setOnClickListener {
            binding.etSearch.setText("@MrBeast")
            triggerAnalysis()
        }

        // API Key hint chips
        binding.chipHandle.setOnClickListener { binding.etSearch.hint = "@usuario"; binding.etSearch.requestFocus() }
        binding.chipUrl.setOnClickListener    { binding.etSearch.hint = "youtube.com/@canal" }
        binding.chipId.setOnClickListener     { binding.etSearch.hint = "UCxxxxxxxxxxxxxxxx" }
    }

    private fun triggerAnalysis() {
        val input = binding.etSearch.text?.toString()?.trim() ?: ""
        if (input.isBlank()) {
            binding.etSearch.error = "Ingresa un canal"
            return
        }

        // Verificar API key
        val apiKey = PrefsManager.getApiKey(this)
        if (apiKey.isBlank()) {
            showApiKeyDialog(input)
            return
        }

        hideKeyboard()
        viewModel.analyzeChannel(input, apiKey)
    }

    private fun showApiKeyDialog(pendingInput: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_api_key, null)
        val etKey = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etApiKey)

        AlertDialog.Builder(this, R.style.AlertDialogDark)
            .setTitle("🔑 Configura tu API Key")
            .setMessage("Obtén una gratis en Google Cloud Console → habilita \"YouTube Data API v3\" → Credenciales → API Key")
            .setView(dialogView)
            .setPositiveButton("Guardar y analizar") { _, _ ->
                val key = etKey?.text?.toString()?.trim() ?: ""
                if (key.length < 10) {
                    binding.etSearch.error = "API Key inválida"
                    return@setPositiveButton
                }
                PrefsManager.saveApiKey(this, key)
                viewModel.analyzeChannel(pendingInput, key)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Idle    -> showEmptyState()
                is UiState.Loading -> showLoading()
                is UiState.Error   -> showError(state.message)
                is UiState.Success -> openChannelScreen(state)
            }
        }
    }

    private fun showEmptyState() {
        binding.layoutEmpty.visibility   = View.VISIBLE
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
        binding.layoutError.visibility   = View.GONE
    }

    private fun showLoading() {
        binding.layoutEmpty.visibility   = View.GONE
        binding.layoutError.visibility   = View.GONE
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
    }

    private fun showError(message: String) {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.layoutEmpty.visibility   = View.GONE
        binding.layoutError.visibility   = View.VISIBLE
        binding.tvError.text             = message
        binding.btnDismissError.setOnClickListener { showEmptyState() }

        // Si es error de API key, ofrecer reconfigurar
        if (message.contains("API Key") || message.contains("inválida")) {
            PrefsManager.clearApiKey(this)
        }
    }

    private fun openChannelScreen(state: UiState.Success) {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.layoutEmpty.visibility   = View.VISIBLE

        val intent = Intent(this, ChannelActivity::class.java).apply {
            putExtra(ChannelActivity.EXTRA_CHANNEL, state.channel)
        }
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
