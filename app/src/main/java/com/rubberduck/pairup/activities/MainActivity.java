package com.rubberduck.pairup.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
//import com.melnykov.fab.FloatingActionButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.rubberduck.pairup.adapters.ShirtsRVAdapter;
import com.rubberduck.pairup.database.DBHelper;
import com.rubberduck.pairup.fragments.FavoritesFragment;
import com.rubberduck.pairup.fragments.LoginFragment;
import com.rubberduck.pairup.fragments.MainFragment;
import com.rubberduck.pairup.R;
import com.rubberduck.pairup.fragments.ShirtsFragment;
import com.rubberduck.pairup.fragments.TrousersFragment;
import com.rubberduck.pairup.fragments.WardrobeFragment;
import com.rubberduck.pairup.model.Pair;
import com.rubberduck.pairup.model.Shirt;
import com.rubberduck.pairup.receivers.PickRandomPairReceiver;

import java.util.Calendar;
import java.util.List;

// The primary activity which hosts different fragments

public class MainActivity extends ActionBarActivity implements
        MainFragment.OnMainFragmentInteractionListener,
        LoginFragment.OnLoginFragmentInteractionListener,
        WardrobeFragment.OnWardrobeFragmentInteractionListener,
        FavoritesFragment.OnFavoritesFragmentInteractionListener,
        ShirtsFragment.OnShirtsFragmentInteractionListener,
        TrousersFragment.OnTrousersFragmentInteractionListener {

    public static final String TAG = "Akshay/MainActivity";

    // Items which are part of the navigation drawer
    public enum DrawerItem {
        MAIN, FAVORITES, WARDROBE, DIVIDER, SETTINGS, LOGOUT
    }

    // Different fragments
    private MainFragment mainFragment;
    private LoginFragment loginFragment;
    private WardrobeFragment wardrobeFragment;
    private FavoritesFragment favoritesFragment;

    private Toolbar toolbar;

    // Navigation drawer and account header section
    private AccountHeader.Result accountHeader;
    private Drawer.Result drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle the toolbar
        toolbar = (Toolbar) findViewById(R.id.drawer_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(12);

        // Handle the drawer
        setDrawer(savedInstanceState);

        // Set a recurring alarm to shuffle the pair everyday
        setRecurringAlarm();

        FacebookSdk.sdkInitialize(getApplicationContext());

        /*
         * This activity is being created for the first time, create a new fragment and add it to
         * the activity
         */
        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            loginFragment = LoginFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, loginFragment).commit();
            hideActionBarDrawer();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle menu item actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the drawer is open, then back button should just close the drawer
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        }

        // Get current active fragment
        Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);

        // Current fragment is Login fragment, close the app
        if (currentFrag instanceof LoginFragment)
            finish();

        // Current fragment is not the main fragment, show the main fragment
        if (!(currentFrag instanceof MainFragment)) {
            drawer.setSelection(0);
            showMainFragment();
        }

        // Main fragment is the active fragment, just call parent function
        else {
            super.onBackPressed();
            //moveTaskToBack(true);
        }
    }

    @Override
    public void onMainFragmentInteraction(String nameEntered) {

    }

    @Override
    public void onLoginFragmentInteraction(boolean loginSuccess) {
        updateUI(loginSuccess);
    }

    @Override
    public void onWardrobeFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFavoritesFragmentInteraction(Uri uri) {

    }

    @Override
    public void onShirtsFragmentInteraction(Uri uri) {

    }

    @Override
    public void onTrousersFragmentInteraction(Uri uri) {

    }

    // Sets a repeating alarm to shuffle the pair of clothes everyday
    public void setRecurringAlarm() {
        // Set the alarm to start at approximately 4:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        if (calendar.before(Calendar.getInstance()))
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Create intent & pending intent
        Intent intent = new Intent(this, PickRandomPairReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to repeat everyday
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    // Update UI based on whether the login was successful or not
    public void updateUI(boolean loggedIn) {
        Log.d(TAG, "updateUI(), loggedIn = " + loggedIn);

        // User is logged in, show the main fragment
        if (loggedIn) {
            mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, mainFragment).commit();
            showActionBarDrawer();
        }
        // User is logged out, show the login fragment
        else {
            loginFragment = LoginFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, loginFragment).commit();
            hideActionBarDrawer();
        }
    }

    // Hide the action bar when login fragment is active
    public void hideActionBarDrawer() {
        if (toolbar != null)
            toolbar.setVisibility(View.GONE);

        if (drawer != null)
            drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    // Show the action bar with all fragments except the login fragment
    public void showActionBarDrawer() {
        if (toolbar != null)
            toolbar.setVisibility(View.VISIBLE);

        if (drawer != null)
            drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    // Set the drawer properties and options
    public void setDrawer(Bundle savedInstanceState) {
        // Create AccountHeader
        accountHeader = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.navbar_header)
                .withSavedInstance(savedInstanceState)
                .build();

        // Create the drawer, add items and click listeners
        drawer = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withSelectedItem(0)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_today).withIcon(R.drawable.ic_action_today_48dp).
                                withTextColor(Color.BLACK).withSelectedTextColor(Color.parseColor("#00796B")),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_favorites).withIcon(R.drawable.ic_star_yellow_96dp).
                                withTextColor(Color.BLACK).withSelectedTextColor(Color.parseColor("#00796B")),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_wardrobe).withIcon(R.drawable.ic_shirt_blue).
                                withTextColor(Color.BLACK).withSelectedTextColor(Color.parseColor("#00796B")),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(R.drawable.ic_settings_grey600_48dp).
                                withTextColor(Color.BLACK).withSelectedTextColor(Color.parseColor("#00796B")),
                        //withTintSelectedIcon(true).withSelectedIcon(R.drawable.ic_settings_green_48dp),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_logout).withIcon(R.drawable.ic_settings_power_grey600_48dp).
                                withTextColor(Color.BLACK).withSelectedTextColor(Color.parseColor("#00796B"))
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long id, IDrawerItem iDrawerItem) {
                        if (iDrawerItem != null && iDrawerItem instanceof Nameable) {
                            //getSupportActionBar().setTitle(((Nameable) iDrawerItem).getNameRes());

                            DrawerItem drawerItem = DrawerItem.values()[i];
                            switch (drawerItem) {
                                case MAIN:
                                    showMainFragment();
                                    break;
                                case FAVORITES:
                                    showFavoritesFragment();
                                    break;
                                case WARDROBE:
                                    showWardrobeFragment();
                                    break;
                                case SETTINGS:
                                    Toast.makeText(getApplicationContext(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
                                    getSupportActionBar().setElevation(12);
                                    break;
                                case LOGOUT:
                                    LoginManager.getInstance().logOut();
                                    drawer.setSelection(0);
                                    updateUI(false);
                                    Toast.makeText(getApplicationContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
                                default:
                                    break;
                            }
                        }
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //drawer.setSelectionByIdentifier(1, false);
        drawer.keyboardSupportEnabled(this, true);
    }

    public void showMainFragment() {
        mainFragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, mainFragment)
                .commit();
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setElevation(12);
        //drawer.setSelection(0);
    }

    public void showFavoritesFragment() {
        favoritesFragment = FavoritesFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, favoritesFragment)
                .commit();
        getSupportActionBar().setTitle(getString(R.string.drawer_item_favorites));
        getSupportActionBar().setElevation(12);
    }

    public void showWardrobeFragment() {
        wardrobeFragment = WardrobeFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_container, wardrobeFragment)
                .commit();
        getSupportActionBar().setTitle(getString(R.string.drawer_item_wardrobe));
        getSupportActionBar().setElevation(0);

    }
}
