package com.mee.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.mee.player.R;
import com.mee.player.databinding.ActivityMainBinding;
import com.mee.player.databinding.VideoItemBinding;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        binding = ActivityMainBinding.inflate( getLayoutInflater() );
        setContentView( binding.getRoot() );
        setUpBottomNavigationBar();
    }

    void setUpBottomNavigationBar() {
        binding.bottomNavBar.setOnItemSelectedListener( new ChipNavigationBar.OnItemSelectedListener() {

            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.folders_menu_item:
                        Toast.makeText( MainActivity.this , "Folders Item clicked" , Toast.LENGTH_SHORT ).show();
                        break;
                    case R.id.videos_menu_item:
                        Toast.makeText( MainActivity.this , "Videos Item clicked" , Toast.LENGTH_SHORT ).show();
                        break;
                }
            }
        });
    }
}