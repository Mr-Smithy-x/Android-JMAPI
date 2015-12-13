package com.mrsmyx.jmapi;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.mrsmyx.JMAPI;
import com.mrsmyx.exceptions.JMAPIException;
import com.mrsmyx.jmapi.adapters.JMAPIPagerAdapter;
import com.mrsmyx.jmapi.adapters.SystemAdapter;
import com.mrsmyx.jmapi.fragments.CoreDialogFragment;
import com.mrsmyx.jmapi.fragments.MemoryFragment;
import com.mrsmyx.jmapi.fragments.SystemFragment;
import com.mrsmyx.ps3util.PS3Process;
import com.mrsmyx.ps3util.Temperature;

import java.util.List;

public class MainActivity extends AppCompatActivity implements JMAPI.JMAPIListener, Runnable, SearchView.OnQueryTextListener, CoreDialogFragment.OnCoreSetListener, View.OnClickListener, SystemFragment.OnSystemListener {

    private FloatingActionButton mMainFab;
    private CoordinatorLayout mCoord;
    private Toolbar mToolbar;
    private AppBarLayout mAppBar;
    private ViewPager mViewPager;
    private BottomSheetLayout bottomSheet;
    private SearchView mSearchView;
    private JMAPI jmapi;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNav;
    private Handler handler = new Handler();
    private List<PS3Process> ps3ProcessList;
    private ActionBarDrawerToggle mToggle;
    private TabLayout mTabs;
    private JMAPIPagerAdapter jmapiPagerAdapter;
    private View savedView;
    private NavigationView mNavProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initCont();
        new Thread(this).start();
    }

    private void init() {
        mAppBar = (AppBarLayout) findViewById(R.id.main_appbar);
        mCoord = (CoordinatorLayout) findViewById(R.id.main_coord);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mNav = (NavigationView) findViewById(R.id.main_nav_view);
        mNav.setNavigationItemSelectedListener(mNavListener);
        mMainFab = (FloatingActionButton) findViewById(R.id.main_fab);
        mTabs = (TabLayout) findViewById(R.id.main_tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
    }

    private void initCont() {
        mMainFab.setOnClickListener(this);
        mAppBar.setExpanded(true, true);
        setSupportActionBar(mToolbar);
        mViewPager.setAdapter(jmapiPagerAdapter = new JMAPIPagerAdapter(getSupportFragmentManager()));
        jmapiPagerAdapter.append(JMAPIPagerAdapter.JPAGE.Builder().setTitle("System").setFragment(new SystemFragment()));
        jmapiPagerAdapter.append(JMAPIPagerAdapter.JPAGE.Builder().setTitle("Memory").setFragment(new MemoryFragment()));
        mTabs.setupWithViewPager(mViewPager);
        bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        bottomSheet.bottomSheetOwnsTouch = true;
        mDrawerLayout.setDrawerListener(mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name));
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mToggle.syncState();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act_main_menu, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.act_main_notify));
        mSearchView.setQueryHint("Notify");
        mSearchView.setOnQueryTextListener(this);
        MenuItem favoriteItem = menu.findItem(R.id.act_main_ps3op);
        Drawable newIcon = favoriteItem.getIcon();
        newIcon.mutate().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN);
        favoriteItem.setIcon(newIcon);
        return true;
    }

    public void launchUrl(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void launchInsta(String user){
        Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("http://instagram.com/_u/" + user));
        i.setPackage("com.instagram.android");
        try {
            startActivity(i);
        }catch (Exception ex){
           ex.printStackTrace();
            launchUrl("http://instagram.com/" +user);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.act_main_ps3op:
                if (savedView == null)
                    savedView = LayoutInflater.from(this).inflate(R.layout.my_sheet_layout, bottomSheet, false);
                if (mNavProcess == null)
                    mNavProcess = (NavigationView) savedView.findViewById(R.id.sheet_nav_view);
                mNavProcess.setNavigationItemSelectedListener(mNavProcessListener);
                bottomSheet.showWithSheetView(savedView);
                break;
            case R.id.act_main_credits:
                Snackbar.make(getWindow().getDecorView(), "PS3MAPI by NvZ (Special Thanks for open sourcing), Android App Developed By Mr Smithy x", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.act_main_igfollow:
                launchInsta("ocjsmith");
                break;
            case R.id.act_main_youtube:
                launchUrl("http://youtube.com/xDudek13lx");
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheet.isSheetShowing()) {
            bottomSheet.dismissSheet();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onJMAPIError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onJMAPIResponse(PS3OP ps3Op, JMAPI.PS3MAPI_RESPONSECODE responseCode, String message) {
        switch (ps3Op) {
            case DELHISTORY:
                Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_FOUND:
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case DISCONNECTED:
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case IDPS:
                if (mNav != null && mNav.getMenu() != null) {
                    mNav.getMenu().findItem(R.id.act_sheet_idps).setTitle("IDPS: " + message.substring(0, 16));
                }
                break;
            case PSID:
                if (mNav != null && mNav.getMenu() != null) {
                    mNav.getMenu().findItem(R.id.act_sheet_psid).setTitle("PSID: " + message.substring(0, 16));
                }
                break;
            case BUZZ:
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case FWVERSION:
                if (mNav != null && mNav.getMenu() != null) {
                    mNav.getMenu().findItem(R.id.act_sheet_fw).setTitle("FW: " + message);
                }
                break;
            case FWTYPE:
                if (mNav != null && mNav.getMenu() != null) {
                    mNav.getMenu().findItem(R.id.act_sheet_fw).setTitle(mNav.getMenu().findItem(R.id.act_sheet_fw).getTitle() + " " + message);
                }
                break;
        }
    }

    @Override
    public void onJMAPIPS3Process(JMAPI.PS3MAPI_RESPONSECODE responseCode, List<PS3Process> processes) {
        if (processes != null && processes.size() > 0) {
            this.ps3ProcessList = processes;
        }
        mNavProcess.getMenu().clear();
        mNavProcess.inflateMenu(R.menu.sheet_process_menu);
        for (PS3Process p : processes) {
            mNavProcess.getMenu().add(p.getTitle());
        }
        if (bottomSheet.isSheetShowing()) {
            bottomSheet.dismissSheet();
        }
        bottomSheet.showWithSheetView(savedView);
    }

    @Override
    public void onJMAPITemperature(JMAPI.PS3MAPI_RESPONSECODE responseCode, Temperature temperature) {
        if (mNav != null && mNav.getMenu() != null) {
            mNav.getMenu().findItem(R.id.act_sheet_temp).setTitle("CPU: " + temperature.getCPU() + " - RSX: " + temperature.getRSX());
        }
    }


    @Override
    public void run() {
        jmapi = new JMAPI(true, handlerListener);
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jmapi.notify(query);
                } catch (JMAPIException e) {
                    e.printStackTrace();
                    handlerListener.onJMAPIError(e.getMessage());
                }
            }
        }).start();
        mSearchView.onActionViewCollapsed();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private JMAPI.JMAPIListener handlerListener = new JMAPI.JMAPIListener() {
        @Override
        public void onJMAPIError(final String error) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.onJMAPIError(error);
                }
            });
        }

        @Override
        public void onJMAPIResponse(final PS3OP ps3Op, final JMAPI.PS3MAPI_RESPONSECODE responseCode, final String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.onJMAPIResponse(ps3Op, responseCode, message);
                }
            });
        }

        @Override
        public void onJMAPIPS3Process(final JMAPI.PS3MAPI_RESPONSECODE responseCode, final List<PS3Process> processes) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.onJMAPIPS3Process(responseCode, processes);
                }
            });
        }

        @Override
        public void onJMAPITemperature(final JMAPI.PS3MAPI_RESPONSECODE responseCode, final Temperature temperature) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.onJMAPITemperature(responseCode, temperature);
                }
            });
        }
    };
    private NavigationView.OnNavigationItemSelectedListener mNavListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.act_sheet_refresh:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                jmapi.getIDPS();
                                jmapi.getPSID();
                                jmapi.getTemp();
                                jmapi.getFwVersion();
                                jmapi.getFwType();
                            } catch (JMAPIException e) {
                                handlerListener.onJMAPIError(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case R.id.act_main_connect:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!jmapi.isConnected()) {
                                jmapi.scanNetwork();
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onJMAPIError("Already connected.");
                                    }
                                });
                            }
                        }
                    }).start();
                    break;
                case R.id.act_sheet_buzz:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                jmapi.buzzer(JMAPI.BUZZER.DOUBLE);
                            } catch (JMAPIException e) {
                                handlerListener.onJMAPIError(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case R.id.act_sheet_disconnect:
                    jmapi.disconnect();
            }
            return false;
        }
    };

    private PS3Process findProcess(String processTitle, List<PS3Process> processList) {
        for (PS3Process p : processList) {
            if (p.getTitle().equals(processTitle)) return p;
        }
        return null;
    }

    private NavigationView.OnNavigationItemSelectedListener mNavProcessListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.sheet_proc_refresh) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        jmapi.getAllProcesses();
                    }
                }).start();
            } else {
                PS3Process process = findProcess(menuItem.getTitle().toString(), ps3ProcessList);
                Toast.makeText(MainActivity.this, process.getTitle() + ":" + String.valueOf(process.getProcess()), Toast.LENGTH_LONG).show();
            }
            return false;
        }
    };

    @Override
    public boolean onCoreSet(CORETYPE coretype, final String str) {
        switch (coretype) {
            case IDPS:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jmapi.setIDPS(str);
                        } catch (JMAPIException e) {
                            e.printStackTrace();
                            handlerListener.onJMAPIError(e.getMessage());
                        }
                    }
                }).start();
                break;
            case PSID:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jmapi.setPSID(str);
                        } catch (JMAPIException e) {
                            handlerListener.onJMAPIError(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_fab:
                   new CoreDialogFragment().show(getSupportFragmentManager(), "");
                break;
        }
    }

    public void sendBootCMD(final JMAPI.PS3BOOT ps3BOOT){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jmapi.boot(ps3BOOT);
                } catch (JMAPIException e) {
                    handlerListener.onJMAPIError(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onSystemOptionSelected(SystemAdapter.SystemStruct.PS3OPTIONS ps3OPTIONS) {
        switch (ps3OPTIONS){
            case REBOOT:
                sendBootCMD(JMAPI.PS3BOOT.REBOOT);
                break;
            case SOFTBOOT:
                sendBootCMD(JMAPI.PS3BOOT.SOFTREBOOT);
                break;
            case HARDBOOT:
                sendBootCMD(JMAPI.PS3BOOT.HARDREBOOT);
                break;
            case DELHIST:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        jmapi.deleteHistory(JMAPI.DELHISTORY.EXCLUDE_DIR);
                    }
                }).start();
                break;
            case DELHISTD:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        jmapi.deleteHistory(JMAPI.DELHISTORY.INCLUDE_DIR);
                    }
                }).start();
                break;
            case SHUTDOWN:
                sendBootCMD(JMAPI.PS3BOOT.SHUTDOWN);
                break;
        }
    }
}
