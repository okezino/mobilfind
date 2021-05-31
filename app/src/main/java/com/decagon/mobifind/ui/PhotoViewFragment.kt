package com.decagon.mobifind.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.decagon.mobifind.databinding.PhotoViewFragmentBinding
import com.decagon.mobifind.utils.load


class PhotoViewFragment : Fragment() {
    private lateinit var photoUri: String
    private var _binding: PhotoViewFragmentBinding? = null
    private val binding
        get() = _binding!!
    private val args by navArgs<PhotoViewFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PhotoViewFragmentBinding.inflate(layoutInflater, container, false)
        photoUri = args.photoUri
        binding.fullImage.load(photoUri)
        binding.root.setOnClickListener { findNavController().popBackStack() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

