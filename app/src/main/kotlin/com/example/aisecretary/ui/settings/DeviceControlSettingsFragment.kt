package com.example.aisecretary.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.aisecretary.ai.device.DevicePermissionManager
import com.example.aisecretary.ai.device.PermissionInfo
import com.example.aisecretary.databinding.FragmentDeviceControlSettingsBinding
import com.example.aisecretary.di.AppModule
import com.example.aisecretary.ui.chat.ChatViewModel
import kotlinx.coroutines.launch

class DeviceControlSettingsFragment : Fragment() {
    
    private var _binding: FragmentDeviceControlSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var devicePermissionManager: DevicePermissionManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceControlSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        devicePermissionManager = AppModule.provideDevicePermissionManager(requireContext())
        
        setupUI()
        observeSettings()
        checkPermissions()
    }
    
    private fun setupUI() {
        // Device control toggle
        binding.switchDeviceControl.setOnCheckedChangeListener { _, isChecked ->
            chatViewModel.settingsManager.setDeviceControlEnabled(isChecked)
        }
        
        // Confirmation required toggle
        binding.switchConfirmationRequired.setOnCheckedChangeListener { _, isChecked ->
            chatViewModel.settingsManager.setDeviceControlConfirmationRequired(isChecked)
        }
        
        // Permission buttons
        binding.btnGrantWriteSettingsPermission.setOnClickListener {
            requestWriteSettingsPermission()
        }
        
        binding.btnGrantBluetoothPermission.setOnClickListener {
            requestBluetoothPermission()
        }
        
        binding.btnGrantWifiPermission.setOnClickListener {
            requestWifiPermission()
        }
        
        binding.btnGrantAudioPermission.setOnClickListener {
            requestAudioPermission()
        }
        
        // Status buttons
        binding.btnRefreshStatus.setOnClickListener {
            refreshDeviceStatus()
        }
        
        binding.btnViewAuditLog.setOnClickListener {
            viewAuditLog()
        }
        
        binding.btnClearAuditLog.setOnClickListener {
            clearAuditLog()
        }
    }
    
    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            chatViewModel.settingsManager.deviceControlEnabled.collect { enabled ->
                binding.switchDeviceControl.isChecked = enabled
                updatePermissionUI()
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            chatViewModel.settingsManager.deviceControlConfirmationRequired.collect { required ->
                binding.switchConfirmationRequired.isChecked = required
            }
        }
    }
    
    private fun checkPermissions() {
        updatePermissionUI()
    }
    
    private fun updatePermissionUI() {
        val hasWriteSettings = devicePermissionManager.hasWriteSettingsPermission()
        val hasBluetooth = devicePermissionManager.hasBluetoothPermissions()
        val hasWifi = devicePermissionManager.hasWifiPermissions()
        val hasAudio = devicePermissionManager.hasAudioPermissions()
        
        // Update permission status
        binding.tvWriteSettingsStatus.text = if (hasWriteSettings) "✓ Granted" else "✗ Not Granted"
        binding.tvBluetoothStatus.text = if (hasBluetooth) "✓ Granted" else "✗ Not Granted"
        binding.tvWifiStatus.text = if (hasWifi) "✓ Granted" else "✗ Not Granted"
        binding.tvAudioStatus.text = if (hasAudio) "✓ Granted" else "✗ Not Granted"
        
        // Update button visibility
        binding.btnGrantWriteSettingsPermission.visibility = if (hasWriteSettings) View.GONE else View.VISIBLE
        binding.btnGrantBluetoothPermission.visibility = if (hasBluetooth) View.GONE else View.VISIBLE
        binding.btnGrantWifiPermission.visibility = if (hasWifi) View.GONE else View.VISIBLE
        binding.btnGrantAudioPermission.visibility = if (hasAudio) View.GONE else View.VISIBLE
        
        // Update overall status
        val allGranted = hasWriteSettings && hasBluetooth && hasWifi && hasAudio
        binding.tvOverallStatus.text = if (allGranted) "✓ All Permissions Granted" else "✗ Some Permissions Missing"
        binding.tvOverallStatus.setTextColor(
            if (allGranted) 
                resources.getColor(android.R.color.holo_green_dark, null)
            else 
                resources.getColor(android.R.color.holo_red_dark, null)
        )
    }
    
    private fun requestWriteSettingsPermission() {
        val intent = devicePermissionManager.getWriteSettingsPermissionIntent()
        startActivity(intent)
        showPermissionInstructions(PermissionInfo.WRITE_SETTINGS)
    }
    
    private fun requestBluetoothPermission() {
        val intent = devicePermissionManager.getBluetoothPermissionIntent()
        startActivity(intent)
        showPermissionInstructions(PermissionInfo.BLUETOOTH)
    }
    
    private fun requestWifiPermission() {
        val intent = devicePermissionManager.getWifiPermissionIntent()
        startActivity(intent)
        showPermissionInstructions(PermissionInfo.WIFI)
    }
    
    private fun requestAudioPermission() {
        val intent = devicePermissionManager.getAudioPermissionIntent()
        startActivity(intent)
        showPermissionInstructions(PermissionInfo.AUDIO)
    }
    
    private fun showPermissionInstructions(permission: PermissionInfo) {
        val explanation = devicePermissionManager.getPermissionExplanation(permission)
        val instructions = devicePermissionManager.getPermissionRequestInstructions(permission)
        
        val message = "$explanation\n\n$instructions"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun refreshDeviceStatus() {
        chatViewModel.refreshDeviceStatus()
        val statusReport = chatViewModel.getDeviceStatusReport()
        binding.tvDeviceStatus.text = statusReport
        
        val recommendations = chatViewModel.getOptimalSettingsRecommendations()
        if (recommendations.isNotEmpty()) {
            binding.tvRecommendations.text = "Recommendations:\n" + recommendations.joinToString("\n")
        } else {
            binding.tvRecommendations.text = "No recommendations at this time."
        }
    }
    
    private fun viewAuditLog() {
        val deviceControlManager = AppModule.provideDeviceControlManager(requireContext())
        val auditLog = deviceControlManager.getAuditLog()
        
        // Show audit log in a dialog or new activity
        val message = if (auditLog.isNotEmpty()) auditLog else "No audit log entries found."
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun clearAuditLog() {
        val deviceControlManager = AppModule.provideDeviceControlManager(requireContext())
        deviceControlManager.clearAuditLog()
        Toast.makeText(requireContext(), "Audit log cleared", Toast.LENGTH_SHORT).show()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh permissions when returning from settings
        checkPermissions()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 