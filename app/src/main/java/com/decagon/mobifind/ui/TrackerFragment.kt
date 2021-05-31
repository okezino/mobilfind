package com.decagon.mobifind.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.UserAdapter
import com.decagon.mobifind.databinding.FragmentTrackerBinding
import com.decagon.mobifind.model.data.TrackState
import com.decagon.mobifind.utils.initAdapter
import com.decagon.mobifind.viewModel.MobifindViewModel


class TrackerFragment : Fragment() {
    private var _binding: FragmentTrackerBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var adapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel by activityViewModels<MobifindViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getTrackList(TrackState.TRACKERS)
        _binding = FragmentTrackerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UserAdapter(UserAdapter.ClickListener{})
        recyclerView = binding.recyclerview
        initAdapter(adapter, recyclerView)

        viewModel.myTrackers.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            if (it.isEmpty()) {
                binding.recyclerview.visibility = View.GONE
                binding.emptyList.visibility = View.VISIBLE
            } else {
                adapter.loadUsers(it)
                binding.emptyList.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE
            }
        }

        binding.fab.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.phoneContactFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}