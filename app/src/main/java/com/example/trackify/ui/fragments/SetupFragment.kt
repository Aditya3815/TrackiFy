package com.example.trackify.ui.fragments

import android.R.attr.text
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.trackify.R
import androidx.navigation.fragment.findNavController
import com.example.trackify.databinding.FragmentSetupBinding
import com.example.trackify.util.Constants.KEY_NAME
import com.example.trackify.util.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.content.edit
import androidx.navigation.NavOptions
import com.example.trackify.util.Constants.KEY_FIRST_TIME_TOGGLE
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences


    @set:Inject
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        _binding?.btnContinue?.setOnClickListener {
            findNavController().navigate(R.id.action_setupFragment_to_runFragment2)
        }

        if (!sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)) {
            findNavController().navigate(R.id.action_setupFragment_to_runFragment2)
        }

        binding.btnContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment2)
            } else {
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        if(!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment2,
                savedInstanceState,
                navOptions
            )
        }



        super.onViewCreated(view, savedInstanceState)
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit() {
            putString(KEY_NAME, name)
                .putFloat(KEY_WEIGHT, weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
        }
        val toolbarText = "Let's go, $name!"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }


}
