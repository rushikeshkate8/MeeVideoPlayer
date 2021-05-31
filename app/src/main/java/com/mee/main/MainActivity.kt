package com.mee.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setUpBottomNavigationBar()

        setUpObservers()
    }


    private fun setUpBottomNavigationBar() {
        binding!!.bottomNavBar.setItemSelected(R.id.videos_menu_item, true)
        binding!!.bottomNavBar.setOnItemSelectedListener {
            when (it) {
                R.id.folders_menu_item -> Navigation.findNavController(
                    this@MainActivity,
                    R.id.nav_host_fragment
                ).apply { popBackStack() }.navigate(R.id.folders_fragment)
                R.id.videos_menu_item -> {
                    Navigation.findNavController(
                        this@MainActivity,
                        R.id.nav_host_fragment
                    ).apply { popBackStack() }.navigate(R.id.videos_Fragment)
                }
            }

        }

    }

    fun updateMediaDatabase() {
        MediaLoader.getLoader().loadVideos(this, object : OnVideoLoaderCallBack() {
            override fun onResult(result: VideoResult) {
                MainActivityViewModel.videoResult.value = result
                MainActivityViewModel.databaseUpdateHandled()
            }
        })
    }

    private fun requestPermissions() {

        val multiplePermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                report.grantedPermissionResponses.forEach {
                    if (it.permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        alertDialog?.dismiss()
                        alertDialog = null
                        updateMediaDatabase()
                        Toast.makeText(
                            this@MainActivity,
                            "Made by Rushikesh Kate",
                            Toast.LENGTH_SHORT
                        ).show()
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

        if (alertDialog != null) return

        val requestDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        requestDialogBuilder.setTitle(
            Html.fromHtml(
                "<font color='${resources.getColor(R.color.color_primary)}'>${
                    resources.getString(
                        R.string.permission_dialog_title
                    )
                }</font>"
            )
        )

        requestDialogBuilder.setMessage(message)
        requestDialogBuilder.setPositiveButton(
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
            dialog.cancel()
            alertDialog = null
        }
        requestDialogBuilder.setNegativeButton(
            R.string.permission_dialog_negative_button
        ) { dialog, which ->
            finishAndRemoveTask()
        }

        alertDialog = requestDialogBuilder.create()

        alertDialog!!.setCanceledOnTouchOutside(false)

        alertDialog!!.show()
    }

    fun setUpObservers() {
        MainActivityViewModel.updateDatabase.observe(this, {
            if(it) {
                updateMediaDatabase()
            }
        })
    }

    override fun onBackPressed() {
        if (viewModel?.isBackPressed!!)
            super.onBackPressed()
        else {
            Toast.makeText(this, R.string.tap_again_to_exit_app, Toast.LENGTH_SHORT).show()
            viewModel!!.isBackPressed = true
            Handler(Looper.getMainLooper()).postDelayed(
                { viewModel!!.isBackPressed = false }, 2000
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if(MainActivityViewModel.videoResult.value?.items  == null)
            requestPermissions()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}