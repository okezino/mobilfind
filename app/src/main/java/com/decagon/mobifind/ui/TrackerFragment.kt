package com.decagon.mobifind.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.UserAdapter
import com.decagon.mobifind.databinding.FragmentTrackerBinding
import com.decagon.mobifind.model.data.Track
import com.decagon.mobifind.model.data.TrackState
import com.decagon.mobifind.utils.initAdapter
import com.decagon.mobifind.utils.showSnackBar
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

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemPosition = viewHolder.adapterPosition
                val track: Track = adapter.getTrack(itemPosition)
                AlertDialog.Builder(viewHolder.itemView.context, R.style.MyDialogTheme)
                    .setTitle("Alert")
                    .setMessage("Are you sure you want to delete ${track.name} from your trackers list?")
                    .setPositiveButton("Yes") { _, _ ->
                       deleteTracker(track)
                    }.setNegativeButton("Cancel") { _, _ ->
                        adapter.notifyDataSetChanged()
                    }.setCancelable(false)
                    .create()
                    .show()
            }
        }
        ).attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun deleteTracker(track: Track) {
        viewModel.deleteFromTrackers(track.phoneNumber!!)
        viewModel.isTrackerDeleted.observe(viewLifecycleOwner){
            if (it == true) {
                view?.showSnackBar("${track.name} deleted from trackers successfully")
            } else if (it == false) {
                view?.showSnackBar("Unable to delete ${track.name} from trackers. Please try again")
            }
        }
    }
}