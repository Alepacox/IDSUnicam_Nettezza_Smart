package unicam.nettezzasmart;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import unicam.nettezzasmart.Report.ReportCollection;
import unicam.nettezzasmart.Report.ReportMaking;
import unicam.nettezzasmart.Request.RequestCollection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static FragmentManager manager;
    public static NavigationView navigationView;
    public static boolean firstSyncRequest=true;
    public static boolean firstSyncReport=true;
    public static boolean logged;
    public static int selected;
    public static Executor tpe = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        RWStoredData.readLocalData(this);
        navigationView.getMenu().getItem(3).setChecked(true);
        selected=3;
        try {
            startFragment(InfoFragment.newInstance("param1","param2" ));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        ReportMaking fragment;
        int id = item.getItemId();
        clearBackstack();
        if (id == R.id.nav_report) {
            selected=0;
            try {
            if (logged) {
                startFragment(ReportMaking.newInstance("", ""));
            } else {
                startFragment(Login.newInstance("", ""));
            }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        } else if (id == R.id.nav_event) {
            selected=1;
            try {
                if (logged) {
                    startFragment(RequestCollection.newInstance("", ""));
                } else {
                    startFragment(Login.newInstance("", ""));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        } else if (id == R.id.nav_data){
            selected=2;
            try {
            if (logged) {
                startFragment(MyData.newInstance("param1", "param2"));
            } else {
                startFragment(Login.newInstance("param1", "param2"));
            }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        } else if (id == R.id.nav_info) {
            selected=3;
            try {
                startFragment(InfoFragment.newInstance("param1","param2" ));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void startFragment(Fragment myclass) throws IllegalAccessException, InstantiationException {
        manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.content_frame, myclass);
        transaction.commit();
    }

    public void clearBackstack() {
        if(manager.getBackStackEntryCount()>0) {
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(
                    0);
            manager.popBackStack(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            manager.executePendingTransactions();
        }
    }

}
