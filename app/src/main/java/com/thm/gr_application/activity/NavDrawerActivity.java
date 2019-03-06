package com.thm.gr_application.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.thm.gr_application.R;

public class NavDrawerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        PrimaryDrawerItem bookmarkItem = new PrimaryDrawerItem().withIdentifier(0).withName("Bookmark").withIcon(R.drawable.ic_favorite_on);
        PrimaryDrawerItem carItem = new PrimaryDrawerItem().withIdentifier(1).withName("Car").withIcon(R.drawable.ic_car);
        SecondaryDrawerItem helpItem = new SecondaryDrawerItem().withIdentifier(2).withName("Help");
        SecondaryDrawerItem managerItem = new SecondaryDrawerItem().withIdentifier(3).withName("Manager");

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ic_user)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com").withIcon(getResources().getDrawable(R.drawable.ic_car))
                )
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(
                        bookmarkItem,
                        carItem,
                        new DividerDrawerItem(),
                        helpItem,
                        managerItem
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 0){
                            Toast.makeText(NavDrawerActivity.this, "Kay", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                })
                .build();
        findViewById(R.id.bt_draw).setOnClickListener((v) -> {
            if (!result.isDrawerOpen()) {
                result.openDrawer();
            }
        });


    }
}
