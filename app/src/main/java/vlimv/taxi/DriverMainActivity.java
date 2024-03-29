package vlimv.taxi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.json.JSONObject;

import java.util.ArrayList;


public class DriverMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SettingsFragment.OnFragmentInteractionListener, CarOptionsFragment.OnFragmentInteractionListener,
        CabinetFragment.OnFragmentInteractionListener, SupportFragment.OnFragmentInteractionListener,
        DriverTakeOrderFragment.OnFragmentInteractionListener, View.OnClickListener, ServerRequest.UpdateCarInfo,
        ServerRequest.NextActivity {
    private DrawerLayout mDrawerLayout;
    public static Toolbar toolbar;
    private static NavigationView nvDrawer;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private static Activity mActivity;
    public static TextView free, busy;
    public static String status;
//    private Socket mSocket;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO sockets connection
//        ServerSocket.getInstance(getBaseContext()).getOnline();
//        ServerSocket.getInstance(getBaseContext()).getActiveTrips();
//        TaxiApplication app = (TaxiApplication) this.getApplication();
//        mSocket = app.getSocket();
//        mSocket.on("hello", onHello);
//        mSocket.connect();
        mActivity = this;
        setContentView(R.layout.activity_driver_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ServerRequest.getInstance(this).getUser(SharedPref.loadToken(this), this, 0);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mActionBarDrawerToggle.syncState();
        mActionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorPrimary));
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        nvDrawer = findViewById(R.id.nav_view);
        nvDrawer.setNavigationItemSelectedListener(this);
        View navHeader = nvDrawer.getHeaderView(0);

        TextView name = navHeader.findViewById(R.id.name);
        String nameText = SharedPref.loadUserName(this) + " " + SharedPref.loadUserSurname(this);
        name.setText(nameText);

        free = findViewById(R.id.free);
        free.setVisibility(View.VISIBLE);
        busy = findViewById(R.id.busy);
        busy.setVisibility(View.VISIBLE);
        free.setOnClickListener(this);
        busy.setOnClickListener(this);

        displaySelectedScreen(R.id.nav_city);
    }
    public void getActiveTrips() {
//        Log.d("getActiveTrips", "Emitting active_trips");
//        mSocket.emit("active_trips");
        ServerSocket.getInstance(getApplicationContext()).getActiveTrips();
    }

    public void lockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    public void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public static void setName(final String name, final String lname) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View navHeader = nvDrawer.getHeaderView(0);
                    TextView fullName = navHeader.findViewById(R.id.name);
                    fullName.setText(name + " " + lname);
                }
            });
        }
    }

//    public void getOnline() {
//        Log.d("getOnline", "Emitting online_user");
//        mSocket.emit("online_user", SharedPref.loadUserId(this.getBaseContext()));
//
//        getActiveTrips();
//    }
//    private Emitter.Listener onHello = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            for (Object arg: args) {
//                Log.d("onHello", arg.toString());
//            }
//            getOnline();
//        }
//    };

    @Override
    protected void onDestroy() {
//        mSocket.off("hello", onHello);
//        mSocket.disconnect();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            DialogQuitApp d = new DialogQuitApp(this);
            d.showDialog(this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OnResume DriverMain", "here");
        if (fragment instanceof CabinetFragment) {
            ServerRequest.getInstance(this).getUser(SharedPref.loadToken(this), this, 0);
            Log.d("Cabinet Fragment", "here too");
        }
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        fragment = null;
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_city:
                fragment = new DriverTakeOrderFragment();
                break;
            case R.id.nav_cabinet:
                fragment = new CabinetFragment();
                break;
            case R.id.nav_car_options:
                fragment = new CarOptionsFragment();
                break;
            case R.id.nav_support:
                fragment = new SupportFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContent, fragment);
            ft.commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.free:
                status = "free";
                free.setBackground(getResources().getDrawable(R.drawable.ripple_effect_square));
                free.setTextColor(Color.parseColor("#ffffff"));
                free.setElevation(5.0f);
                busy.setBackground(getResources().getDrawable(R.drawable.ripple_effect_square_white_stroke));
                busy.setTextColor(Color.parseColor("#000000"));
                busy.setElevation(0.0f);
                Toast.makeText(this, "Вам будут приходить все поездки", Toast.LENGTH_LONG).show();
                getActiveTrips();
                break;
            case R.id.busy:
                status = "busy";
                free.setBackground(getResources().getDrawable(R.drawable.ripple_effect_square_white_stroke));
                free.setTextColor(Color.parseColor("#000000"));
                free.setElevation(0.0f);
                busy.setBackground(getResources().getDrawable(R.drawable.ripple_effect_square_red));
                busy.setTextColor(Color.parseColor("#ffffff"));
                busy.setElevation(5.0f);
                final ArrayList<JSONObject> listCur = new ArrayList<>();
                for (JSONObject obj: NewOrdersFragment.getTrips()) {
                    try {
                        String cost = obj.getString("cost");
                        boolean costVol = cost.equals("Волонтерская поездка");
                        if (costVol) {
                            listCur.add(obj);
                        }
                    } catch (Exception e) {
                        Log.e("busy filter", e.getMessage());
                    }
                }
                NewOrdersFragment.initDataset(listCur);
                Toast.makeText(this, "Вам будут приходить только Волонтерские поездки", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void updateCarInfo(String name, String model, String type, String gosNumber, int year) {
        CarOptionsFragment fr = (CarOptionsFragment) fragment;
        fr.spinner_car.setText(name);
        fr.spinner_car_model.setText(model);
        fr.spinner_car_type.setText(type);
        fr.numberEditText.setText(gosNumber);
        fr.yearEditText.setText(year + "");
//        fr.spinner_car.setEnabled(true);
//        fr.spinner_car_model.setEnabled(true);
//        fr.spinner_car_type.setEnabled(true);
    }

    @Override
    public void goNext() {
        Toast.makeText(this, "Информация успешно обновлена.", Toast.LENGTH_LONG).show();
        try {
            CarOptionsFragment fr = (CarOptionsFragment) fragment;
            fr.progressBar.hideNow();
            fr.next_btn.setVisibility(View.VISIBLE);
        } catch (ClassCastException e) {
            Log.e("goNext", "Oops");
        }
    }

    @Override
    public void tryAgain() {
        Toast.makeText(this, "Произошла ошибка. Повторите попытку.", Toast.LENGTH_LONG).show();
        try {
            CarOptionsFragment fr = (CarOptionsFragment) fragment;
            fr.progressBar.hideNow();
            fr.next_btn.setVisibility(View.VISIBLE);
        } catch (ClassCastException e) {
            Log.e("goNext", "Oops");
        }
    }

}