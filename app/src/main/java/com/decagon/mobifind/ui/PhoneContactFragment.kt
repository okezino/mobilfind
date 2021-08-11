package com.decagon.mobifind.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.decagon.mobifind.R
import com.decagon.mobifind.adapter.OnclickPhoneContact
import com.decagon.mobifind.adapter.PhoneContactAdapter
import com.decagon.mobifind.databinding.FragmentPhoneContactBinding
import com.decagon.mobifind.utils.*
import com.decagon.mobifind.viewModel.MobifindViewModel
import com.shreyaspatil.MaterialDialog.MaterialDialog


class PhoneContactFragment : Fragment(), OnclickPhoneContact {

    private var _binding: FragmentPhoneContactBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var adapter: PhoneContactAdapter
    private var contactList = arrayListOf<String>()
    private lateinit var recyclerView: RecyclerView
    private var photo: String? = null
    private val viewModel by activityViewModels<MobifindViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhoneContactBinding.inflate(layoutInflater)
        return binding.root
    }

    // Checks if the permission to read the phone's contact has been granted to the app. If the
    // permission has been granted, read the contact else, request for the appropriate permission.

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                1
            )
        } else {
            handlePermission()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.phoneContactBackBtn.setOnClickListener {
            findNavController().popBackStack()

        }

        recyclerView = binding.recyclerviewPhoneFragment

        viewModel.getPhotoInPhotos()
        viewModel.photoUri.observe(viewLifecycleOwner, Observer {
            photo = it
        })

        binding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               handleTextChange(searchContact(s.toString(),contactList))
            }

            override fun afterTextChanged(s: Editable?) {
                handleTextChange(searchContact(s.toString(),contactList))
            }
        })
    }

    // This is used to read phone contacts and save them in a mutableList
    private fun getPhoneContact() {
        val cursor: Cursor? = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null)

        while (cursor!!.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contactList.add("$name:$number")
        }


        cursor.close()
    }


    override fun onClickStatus(name: String, number: String) {
        val userNumber = filterNumber(number)

        viewModel.mobifindUser.observe(viewLifecycleOwner, Observer {
            if (it.contains(userNumber)) {
                viewModel.myTrackers.observe(viewLifecycleOwner, Observer {
                if(validateUser(userNumber,it)){
                    showOldUserAlert(name)
                }else {
                    showAddAlert(name,userNumber)
                }
                })
            } else {
                sendMessage(number, name)
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handlePermission()
            } else {
                view?.showSnackBar(denyMessage())
            }
        }
    }


    private fun sendMessage(number: String, name: String) {
        val uri = Uri.parse("smsto:$number")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", inviteMessage(name))
        startActivity(intent)
    }

    private fun handlePermission() {
        getPhoneContact()
        recyclerView = binding.recyclerviewPhoneFragment
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PhoneContactAdapter(this,contactList.toSet().toList())
    }

    private fun handleTextChange( contact : List<String>) {
        recyclerView = binding.recyclerviewPhoneFragment
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PhoneContactAdapter(this,contact)
    }

    private fun addToTrackerList(name : String, userNumber : String){
        viewModel.setUpUserFirebase(userNumber)

        if (viewModel.getTrackerPhotoInPhotos(userNumber, name)) {
            view?.showSnackBar(sendSuccessMessage(name))
            findNavController().popBackStack()
        } else {
            view?.showSnackBar(failedMessage())
        }

        viewModel.pushToTracking(photo)
    }
    private  fun showAddAlert(name: String,userNumber: String){
        MaterialDialog.Builder(requireActivity())
            .setTitle(ALERT_TITLE)
            .setMessage(affirmationMessage(name))
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                addToTrackerList(name,userNumber)

            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false)
            .build()
            .show()
    }

    private  fun showOldUserAlert(name: String){
        MaterialDialog.Builder(requireActivity())
            .setTitle(ALERT_TITLE)
            .setMessage(existingUserMessage(name))
            .setPositiveButton(OK) { dialog, _ ->
                dialog.dismiss()

            }.setCancelable(true)
            .build()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}