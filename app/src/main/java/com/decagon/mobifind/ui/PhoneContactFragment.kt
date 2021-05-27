package com.decagon.mobifind.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.adapter.OnclickPhoneContact
import com.decagon.mobifind.adapter.PhoneContactAdapter
import com.decagon.mobifind.databinding.FragmentPhoneContactBinding
import com.decagon.mobifind.utils.initPhoneAdapter
import com.decagon.mobifind.utils.showSnackBar

class PhoneContactFragment : Fragment(),OnclickPhoneContact {

    private  var _binding : FragmentPhoneContactBinding? = null
    private val binding
     get() = _binding!!
    private lateinit var adapter : PhoneContactAdapter
    private lateinit var recyclerView : RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneContactBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PhoneContactAdapter(this)
        recyclerView = binding.recyclerviewPhoneFragment
        initPhoneAdapter(adapter,recyclerView)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClickStatus(status: String, number: String) {
        if (status == "ADD") view?.showSnackBar("give permission to track you") else {
            view?.showSnackBar("Send invite SMS")
        }
    }
}