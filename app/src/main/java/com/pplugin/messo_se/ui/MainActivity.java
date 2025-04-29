package com.pplugin.messo_se.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.pplugin.messo_se.R;


public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);
        navView = findViewById(R.id.nav_view);
        ViewpagerAdapter viewpagerAdapter = new ViewpagerAdapter(getSupportFragmentManager()
                , FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewpagerAdapter);

        viewPager.setCurrentItem(0);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Messages");
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        navView.getMenu().findItem(R.id.navigation_messages).setChecked(true);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Messages");
                        }
                        break;
                    case 1:
                        navView.getMenu().findItem(R.id.navigation_contacts).setChecked(true);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Contacts");
                        }
                        break;
                    case 2:
                        navView.getMenu().findItem(R.id.navigation_profile).setChecked(true);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Profile");
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_messages:
                        viewPager.setCurrentItem(0);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Messages");
                        }
                        break;
                    case R.id.navigation_contacts:
                        viewPager.setCurrentItem(1);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Contacts");
                        }
                        break;
                    case R.id.navigation_profile:
                        viewPager.setCurrentItem(2);
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Profile");
                        }
                        break;
                }
                return true;
            }
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}