package com.mee.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jiajunhui.xapp.medialoader.MediaLoader;
import com.jiajunhui.xapp.medialoader.bean.VideoResult;
import com.jiajunhui.xapp.medialoader.callback.OnVideoLoaderCallBack;
import com.mee.main.videos.VideosViewModel;
import com.mee.player.PlayerActivity;
import com.mee.player.R;
import com.mee.player.databinding.ActivityMainBinding;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    MainActivityViewModel viewModel;
    private static final String TAG = "MainActivity";
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityMainBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        setUpBottomNavigationBar();
        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();

//        MediaLoader.getLoader().loadVideos( this , new OnVideoLoaderCallBack() {
//            @Override
//            public void onResult(VideoResult result) {
//
//                Toast.makeText( MainActivity.this , result.getItems().get( 0 ).getPath() , Toast.LENGTH_SHORT ).show();
//                Intent intent = new Intent( MainActivity.this , PlayerActivity.class );
//                Uri uri = Uri.fromFile( new File( result.getItems().get( 6 ).getPath() ) );
//                intent.setDataAndType( uri , "video/*" );
//                startActivity( intent );
//
//
//            }
//        } );
    }


    public void isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE )
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v( TAG , "Permission is granted1" );
                MediaLoader.getLoader().loadVideos( this , new OnVideoLoaderCallBack() {
                    @Override
                    public void onResult(VideoResult result) {
                        MainActivityViewModel._videoResult.setValue( result );
                    }
                } );
            } else {

                Log.v( TAG , "Permission is revoked1" );
                ActivityCompat.requestPermissions( this , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , 3 );
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v( TAG , "Permission is granted1" );
        }
    }

    public void isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission( android.Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v( TAG , "Permission is granted2" );
            } else {

                Log.v( TAG , "Permission is revoked2" );
                ActivityCompat.requestPermissions( this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} , 2 );
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v( TAG , "Permission is granted2" );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode , String[] permissions , int[] grantResults) {
        super.onRequestPermissionsResult( requestCode , permissions , grantResults );
        switch (requestCode) {
            case 2:
                Log.d( TAG , "External storage2" );
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v( TAG , "Permission: " + permissions[0] + "was " + grantResults[0] );
                    //resume tasks needing this permission
//                    downloadPdfFile();


                } else {
//                    progress.dismiss();
                }
                break;

            case 3:
                Log.d( TAG , "External storage1" );
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v( TAG , "Permission: " + permissions[0] + "was " + grantResults[0] );
                    //resume tasks needing this permission
//                    SharePdfFile();
                    MediaLoader.getLoader().loadVideos( this , new OnVideoLoaderCallBack() {
                        @Override
                        public void onResult(VideoResult result) {
                            MainActivityViewModel._videoResult.setValue( result );
                        }
                    } );
                } else {
//                    progress.dismiss();
                }
                break;
        }
    }


    void setUpBottomNavigationBar() {
        binding.bottomNavBar.setItemSelected( R.id.videos_menu_item , true );
        binding.bottomNavBar.setOnItemSelectedListener( new ChipNavigationBar.OnItemSelectedListener() {

            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.folders_menu_item:
                        Navigation.findNavController( MainActivity.this , R.id.nav_host_fragment ).navigate( R.id.folders_fragment );
                        break;
                    case R.id.videos_menu_item:
                        Navigation.findNavController( MainActivity.this , R.id.nav_host_fragment ).navigate( R.id.videos_Fragment );
                        break;
                }
            }
        } );
    }
}


