package com.decagon.mobifind.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.OnclickPhoneContact
import com.decagon.mobifind.adapter.PhoneContactAdapter
import com.decagon.mobifind.databinding.FragmentPhoneContactBinding
import com.decagon.mobifind.model.data.Contact
import com.decagon.mobifind.model.data.Track
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.viewModel.MobifindViewModel

class PhoneContactFragment : Fragment(), OnclickPhoneContact {

    private var _binding: FragmentPhoneContactBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var adapter: PhoneContactAdapter
    private lateinit var recyclerView: RecyclerView
    private var contactList = arrayListOf<Contact>()
    private val viewModel by activityViewModels<MobifindViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneContactBinding.inflate(layoutInflater)
        getContact()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.phoneContactBackBtn.setOnClickListener {
            findNavController().navigate(R.id.dashBoardFragment)
        }


        recyclerView = binding.recyclerviewPhoneFragment

    }

    override fun onClickStatus(name: String, number: String) {
        val userNumber = filterNumber(number)
        val newTrack = Track(name, userNumber)

        viewModel.mobifindUser.observe(viewLifecycleOwner, Observer {
            if (it.contains(userNumber)) {
                viewModel.setUpUserFirebase(userNumber)
                viewModel.pushToTrackers(newTrack)
                viewModel.pushToTracking()

            } else {
                sendMessage(number, name)
            }

        })


    }

    private fun sendMessage(number: String, name: String) {
        val uri = Uri.parse("smsto:$number")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", inviteMessage(name))
        startActivity(intent)
    }

    fun getContact() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACT
            )
        } else {

            val contact = requireActivity().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (contact!!.moveToNext()) {

                val name =
                    contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val newNumber = Contact(name, number)

                contactList.add(newNumber)
            }


        }
    }

    override fun onResume() {
        super.onResume()

        adapter = PhoneContactAdapter(this, contactList)
        initPhoneAdapter(adapter, recyclerView)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}