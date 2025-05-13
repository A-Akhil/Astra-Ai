package com.example.aisecretary.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aisecretary.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var switchVoiceInput: Switch
    private lateinit var switchMemoryStorage: Switch
    private lateinit var buttonSaveSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        // Initialize UI components
        switchVoiceInput = view.findViewById(R.id.switch_voice_input)
        switchMemoryStorage = view.findViewById(R.id.switch_memory_storage)
        buttonSaveSettings = view.findViewById(R.id.button_save_settings)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        switchVoiceInput.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVoiceInputEnabled(isChecked)
            viewModel.setVoiceOutputEnabled(isChecked) // Link voice input and output for simplicity
        }

        switchMemoryStorage.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMemoryEnabled(isChecked)
        }

        buttonSaveSettings.setOnClickListener {
            viewModel.saveSettings()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.voiceInputEnabled.collectLatest { enabled ->
                if (switchVoiceInput.isChecked != enabled) {
                    switchVoiceInput.isChecked = enabled
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.memoryEnabled.collectLatest { enabled ->
                if (switchMemoryStorage.isChecked != enabled) {
                    switchMemoryStorage.isChecked = enabled
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settingsUpdated.collectLatest { updated ->
                if (updated) {
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                    viewModel.resetSettingsUpdatedFlag()
                    
                    // Navigate back to chat fragment
                    findNavController().navigate(R.id.action_settingsFragment_to_chatFragment)
                }
            }
        }
    }
}