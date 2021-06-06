package com.mee.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.callback.StorageAccessCallback
import com.anggrayudi.storage.file.StorageType
import com.anggrayudi.storage.file.storageId
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mee.player.R
import com.mee.player.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

    private lateinit var mActivity: MainActivity

    var viewModel: MainActivityViewModel? = null
    var binding: ActivityMainBinding? = null
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        mActivity = this

        setUpBottomNavigationBar()

        //setUpObservers()
    }


    private fun setUpBottomNavigationBar() {
        binding!!.bottomNavBar.setItemSelected(R.id.videos_menu_item, true)
        binding!!.bottomNavBar.setOnItemSelectedListener {
            when (it) {
                R.id.folders_menu_item -> Navigation.findNavController(
                    this@MainActivity,
                    R.id.nav_host_fragment
                ).apply { popBackStack(R.id.videos_Fragment, true) }.navigate(R.id.folders_fragment)
                R.id.videos_menu_item -> {
                    Navigation.findNavController(
                        this@MainActivity,
                        R.id.nav_host_fragment
                    ).apply { popBackStack(R.id.folders_fragment, true) }.navigate(R.id.videos_Fragment)
                }
            }

        }

    }

//    fun updateVideoDatabase() {
//        launch {
//            MainActivityViewModel.videos.value = async(Dispatchers.IO) {
//                MediaFacer
//                    .withVideoContex(mActivity)
//                    .getAllVideoContent(VideoGet.externalContentUri)
//            }.await()
//        }

//        launch {
//            withContext(Dispatchers.IO) {
//                MediaLoader.getLoader().loadVideos(mActivity, object : OnVideoLoaderCallBack() {
//                    override fun onResult(result: VideoResult) {
//                        MainActivityViewModel.videoResult.postValue(result)
//                        MainActivityViewModel.databaseUpdateHandled()
//                    }
//                })
//            }
//        }


    private fun requestPermissions() {

        val multiplePermissionsListener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                report.grantedPermissionResponses.forEach {
                    if (it.permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {

                        MainActivityViewModel.isReadPermissionGranted.value = true

                        alertDialog?.dismiss()
                        alertDialog = null

                        //updateVideoDatabase()
                        return
                    }
                }

                report.deniedPermissionResponses.forEach {
                    if (it.permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {

                        MainActivityViewModel.isReadPermissionGranted.value = false

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

//    fun setUpObservers() {
//        MainActivityViewModel.updateDatabase.observe(this, {
//            if (it) {
//                updateVideoDatabase()
//                MainActivityViewModel.databaseUpdateHandled()
//            }
//        })
//    }

    override fun onBackPressed() {
        if(supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.backStackEntryCount != 0)
            super.onBackPressed()
        else {
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
    }

    override fun onStart() {
        super.onStart()
        if (MainActivityViewModel.videos.value!!.size == 0)
            requestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}