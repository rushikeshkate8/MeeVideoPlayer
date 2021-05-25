package com.mee.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.jiajunhui.xapp.medialoader.MediaLoader
import com.jiajunhui.xapp.medialoader.bean.VideoResult
import com.jiajunhui.xapp.medialoader.callback.OnVideoLoaderCallBack
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mee.player.R
import com.mee.player.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    var viewModel: MainActivityViewModel? = null
    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        setUpBottomNavigationBar()
    }

    private fun setUpBottomNavigationBar() {
        binding!!.bottomNavBar.setItemSelected(R.id.videos_menu_item, true)
        binding!!.bottomNavBar.setOnItemSelectedListener {
            when (it) {
                R.id.folders_menu_item -> Navigation.findNavController(
                    this@MainActivity,
                    R.id.nav_host_fragment
                ).navigate(R.id.folders_fragment)
                R.id.videos_menu_item -> Navigation.findNavController(
                    this@MainActivity,
                    R.id.nav_host_fragment
                ).navigate(R.id.videos_Fragment)
            }
        }
//        binding!!.bottomNavBar.setOnItemSelectedListener {
//            override fun onItemSelected(i: Int) {
//
//            }
//        })
    }

    fun updateMediaDatabase() {
        MediaLoader.getLoader().loadVideos(this, object : OnVideoLoaderCallBack() {
            override fun onResult(result: VideoResult) {
                MainActivityViewModel._videoResult.value = result
            }
        })
    }

    private fun requestPermissions() {

        val multiplePermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                report.grantedPermissionResponses.forEach {
                    if (it.permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        updateMediaDatabase()
                        return
                    }
                }

                report.deniedPermissionResponses.forEach {
                    if (it.permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        if (it.isPermanentlyDenied) {
                            simpleAlert(
                                resources.getString(R.string.permission_dialog_message_permanant_deny),
                                resources.getString(R.string.permission_dialog_positive_button_text_permanant_deny),
                                false
                            )
                        } else {
                            simpleAlert(
                                resources.getString(R.string.permission_dialog_message_deny),
                                resources.getString(R.string.permission_dialog_positive_button_text_deny),
                                true
                            )
                        }
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        }


        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(multiplePermissionsListener).check()


    }

    fun simpleAlert(message: String, positiveButtonText: String, requestPermission: Boolean) {

        val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        builder.setTitle(R.string.perimission_dialog_title)
        builder.setMessage(message)
        builder.setPositiveButton(
            positiveButtonText
        ) { dialog, which ->

            dialog.cancel()
            // below is the intent from which we
            // are redirecting our user.
            if (requestPermission) {
                requestPermissions()
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, 101)
            }
        }
        builder.setNegativeButton(
            R.string.permission_dialog_negative_button
        ) { dialog, which ->
            finishAndRemoveTask()
        }
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        alertDialog.show()
    }


    override fun onStart() {
        super.onStart()
        requestPermissions()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}