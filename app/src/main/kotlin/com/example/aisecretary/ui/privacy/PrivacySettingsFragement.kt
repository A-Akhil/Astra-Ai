package com.example.aisecretary.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aisecretary.R
import com.example.aisecretary.databinding.FragmentPrivacySettingsBinding
import kotlinx.coroutines.launch

/**
 * Fragment for privacy and security settings
 */
class PrivacySettingsFragment : Fragment() {
    
    private var _binding: FragmentPrivacySettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: PrivacySettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[PrivacySettingsViewModel::class.java]
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.privacySettings.collect { settings ->
                binding.apply {
                    switchAllowAnalytics.isChecked = settings.allowAnalytics
                    switchAllowCrashReports.isChecked = settings.allowCrashReports
                    switchAnonymizeData.isChecked = settings.anonymizeData
                    switchEnableBiometric.isChecked = settings.enableBiometric
                    switchAutoDeleteData.isChecked = settings.autoDeleteOldData
                    textDataRetention.text = "${settings.dataRetentionDays} days"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is PrivacyUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is PrivacyUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is PrivacyUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is PrivacyUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Privacy toggles
            switchAllowAnalytics.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAnalyticsPermission(isChecked)
            }

            switchAllowCrashReports.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateCrashReportsPermission(isChecked)
            }

            switchAnonymizeData.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateDataAnonymization(isChecked)
            }

            switchEnableBiometric.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateBiometricAuthentication(isChecked)
            }

            switchAutoDeleteData.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAutoDataDeletion(isChecked)
            }

            // Data retention
            buttonChangeRetention.setOnClickListener {
                showDataRetentionDialog()
            }

            // Export data
            buttonExportData.setOnClickListener {
                viewModel.exportUserData()
            }

            // Generate privacy report
            buttonPrivacyReport.setOnClickListener {
                viewModel.generatePrivacyReport()
            }

            // Delete account
            buttonDeleteAccount.setOnClickListener {
                showDeleteAccountDialog()
            }

            // Back button
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun showDataRetentionDialog() {
        val options = arrayOf("7 days", "30 days", "90 days", "1 year", "Never")
        val values = arrayOf(7, 30, 90, 365, -1)
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Data Retention Period")
        builder.setItems(options) { _, which ->
            viewModel.updateDataRetentionDays(values[which])
        }
        builder.show()
    }

    private fun showDeleteAccountDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Account")
        builder.setMessage("This will permanently delete all your data including conversations, memory facts, and settings. This action cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ ->
            viewModel.deleteAccount()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
