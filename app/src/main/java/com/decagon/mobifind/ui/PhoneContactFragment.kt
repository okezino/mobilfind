package com.decagon.mobifind.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class PhoneContactFragment : Fragment(), OnclickPhoneContact,EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentPhoneContactBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var adapter: PhoneContactAdapter
    private var contactList = arrayListOf<Contact>()
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

    fun hasContactPermission() = EasyPermissions.hasPermissions(requireContext(),Manifest.permission.READ_CONTACTS)
    fun requestPermission(){
        EasyPermissions.requestPermissions(this,
            "You can not update your Tracker List without Contact Permission",
        REQUEST_READ_CONTACT,Manifest.permission.READ_CONTACTS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.phoneContactBackBtn.setOnClickListener {
            findNavController().navigate(R.id.dashBoardFragment)
        }

        recyclerView = binding.recyclerviewPhoneFragment
//
//        setUpPermissions(
//            android.Manifest.permission.READ_CONTACTS,
//            "contacts",
//            REQUEST_READ_CONTACT
//        )
        if (!hasContactPermission()){
            requestPermission()
        }else{
            getContact()
        }


        viewModel.getPhotoInPhotos()

        viewModel.photoUri.observe(viewLifecycleOwner, Observer {
            photo = it
        })
    }

    private fun setUpPermissions(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                   getContact()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )

                else -> requireActivity().requestPermissions(arrayOf(permission), requestCode)
            }
        }
    }

    fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(requireContext())

        builder.apply {
            setTitle("Permission required")
            setMessage("Permission to access your $name is required to use this app")
            setPositiveButton("Allow") { dialog, which ->
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        builder.create().show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
   if(EasyPermissions.somePermissionDenied(this,perms.first())){
       SettingsDialog.Builder(requireContext()).build().show()
   }else requestPermission()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if(hasContactPermission()) getContact() else {
            view?.showSnackBar("Denield")
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        fun innerCheck(name: String) {
//            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                getContact()
//            } else {
//                Toast.makeText(requireContext(), "$name permission granted", Toast.LENGTH_LONG)
//                    .show()
//            }
//        }
//
//        when (requestCode) {
//            REQUEST_READ_CONTACT -> innerCheck("contacts")
//        }
//    }


    override fun onClickStatus(name: String, number: String) {
        val userNumber = filterNumber(number)

        viewModel.mobifindUser.observe(viewLifecycleOwner, Observer {
            if (it.contains(userNumber)) {
                viewModel.setUpUserFirebase(userNumber)
                // viewModel.getTrackerPhotoInPhotos(userNumber)
//                viewModel.trackerPhotoUri.observe(viewLifecycleOwner, Observer { photo ->
//                    val newTrack = Track(name, userNumber,photo)
//                    viewModel.pushToTrackers(newTrack)
//                })
                val newTrack = Track(name, userNumber)
                viewModel.pushToTrackers(newTrack)
                viewModel.pushToTracking(photo)
                viewModel.response.observe(viewLifecycleOwner, Observer { response ->
                    view?.showSnackBar(response)

                })

            } else {
                sendMessage(number, name)
            }
        })
    }


    private fun getContact() {

        if(hasContactPermission()){

            val contact = requireActivity().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
            )
            while (contact!!.moveToNext()) {
                val name =
                    contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val newNumber = Contact(name, number)
                contactList.add(newNumber)

            }

            adapter = PhoneContactAdapter(this, contactList)
            recyclerView.adapter = adapter
            initPhoneAdapter(adapter, recyclerView)
        }
        }

//    private fun getContact() {
//
//        val contact = requireActivity().contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
//        )
//        while (contact!!.moveToNext()) {
//            val name =
//                contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//            val number =
//                contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//
//            val newNumber = Contact(name, number)
//            contactList.add(newNumber)
//
//        }
//
//        adapter = PhoneContactAdapter(this, contactList)
//        recyclerView.adapter = adapter
//        initPhoneAdapter(adapter, recyclerView)
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun sendMessage(number: String, name: String) {
        val uri = Uri.parse("smsto:$number")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", inviteMessage(name))
        startActivity(intent)
    }


}