package com.github.shadowjonathan.automatiaapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.github.shadowjonathan.automatiaapp.background.Comms;
import com.github.shadowjonathan.automatiaapp.background.Modules;
import com.github.shadowjonathan.automatiaapp.background.Operator;
import com.github.shadowjonathan.automatiaapp.ffnet.select.FFNetCategorySelectActivity;
import com.github.shadowjonathan.automatiaapp.global.DirListener;
import com.github.shadowjonathan.automatiaapp.global.HomeScreenHelp;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback
        , DirectoryChooserFragment.OnFragmentInteractionListener {
    private static final String TAG = "ActivityActions";
    private static int selected = 0;
    private Operator OPS;
    private Comms C;
    private Modules M;
    private ServiceConnection mConnection = new ServiceConnection() {
        private String TAG = "OPS_CONN";

        public void onServiceConnected(ComponentName className, IBinder service) {
            OPS = ((Operator.LocalBinder) service).getService();
            Log.d(TAG, "SERVICE CONNECTED");
            M = OPS.getModules();
            C = OPS.getComms();
        }

        public void onServiceDisconnected(ComponentName className) {
            OPS = null;
            Log.d(TAG, "SERVICE DISCONNECTED");
            M = null;
            C = null;
        }
    };
    private boolean mIsBound = false;
    private DirectoryChooserFragment chooserDialog;
    private DirListener dirListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*)
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Paste URL");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        M.onPaste(input.getText().toString());
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                //builder.show();

                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        android.support.v7.app.ActionBarDrawerToggle toggle = new android.support.v7.app.ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        Log.d(TAG, "+++ ON CREATE +++");
    }

    @Override
    public void onStart() {
        super.onStart();
        doBindService();
        Log.d(TAG, "++ ON START ++");
        select();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "+ ON RESUME +");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        doUnbindService();
        Log.d(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "--- ON DESTROY ---");
        super.onDestroy();
    }

    public void addToTitle(String s) {
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " - " + s);
    }

    public void addToTitle() {
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void select(int i) {
        selected = i;
        select();
    }

    private void select() {
        RecyclerView rview = (RecyclerView) findViewById(R.id.home_screen);
        Log.d(TAG, "select: " + selected);
        switch (selected) {
            case 0:
                rview.setAdapter(new HomeScreen());
                addToTitle();
                break;
            case 1:
                com.github.shadowjonathan.automatiaapp.ffnet.HomeScreen hv = new com.github.shadowjonathan.automatiaapp.ffnet.HomeScreen();
                rview.setAdapter(hv);
                dirListener = hv.dl;
                addToTitle("Fanfiction");
                break;
        }
        ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(selected).setChecked(true);
    }

    void doBindService() {
        startService(
                new Intent(this, Operator.class));
        bindService(
                new Intent(this, Operator.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d(TAG, "Binding service...");
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            Log.d(TAG, "Unbinding service...");
        } else Log.w(TAG, "doUnbindService called but no service bound");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        } else if (id == R.id.action_debug) {
            chooserDialog = DirectoryChooserFragment.newInstance(DirectoryChooserConfig.builder()
                    .newDirectoryName("Stories")
                    .allowReadOnlyDirectory(false)
                    .allowNewDirectoryNameModification(true)
                    .initialDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .build());

            Log.d(TAG, "onOptionsItemSelected: " + Environment.getExternalStorageDirectory());

            chooserDialog.show(getFragmentManager(), null);

        } else if (id == R.id.action_ffnet) {
            openFFNET();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1337) if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Granted permission, re-tap the button to proceed to saving the file", Toast.LENGTH_SHORT).show();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            select(0);
        } else if (id == R.id.nav_ffnet) {
            select(1);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openFFNET() {
        Intent ffnetI = new Intent(MainActivity.this, FFNetCategorySelectActivity.class);
        startActivity(ffnetI);
    }

    @Override
    public void onSelectDirectory(@NonNull String path) {
        Log.d(TAG, "onSelectDirectory: " + path);
        if (dirListener != null)
            dirListener.select(path);
        if (chooserDialog != null)
            chooserDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        if (dirListener != null)
            dirListener.dismiss();
        if (chooserDialog != null)
            chooserDialog.dismiss();
    }

    public static class HomeScreen extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HomeScreenHelp.HasPalette {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public HomeScreenHelp.Palette getPalette() {
            return null;
        }
    }
}