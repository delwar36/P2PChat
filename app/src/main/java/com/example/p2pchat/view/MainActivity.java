package com.example.p2pchat.view;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.p2pchat.R;
import com.example.p2pchat.model.ChatHistoryEntity;
import com.example.p2pchat.viewmodel.MainViewModel;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_PERMISSION_CODE = 1;
    private TextView emptyPageMessage;
    private Button reConnect;

    private RecyclerView chatHistoryView;
    private ChatListAdapter historyAdapter;

    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpGPS();
        setUpHistoryPage();
        setUpViewModel();
        reConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.startSearch();
            }
        });

    }

    private void setUpViewModel() {
        model = ViewModelProviders.of(this).get(MainViewModel.class);

        model.startSearch();
        model.getHistory().observe(this, new Observer<List<ChatHistoryEntity>>() {
            @Override
            public void onChanged(@Nullable List<ChatHistoryEntity> chats) {
                assert chats != null;
                assert getSupportActionBar() != null;

                if (chats.size() == 0) {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.historyPageTitle));
                    emptyPageMessage.setVisibility(View.VISIBLE);
                    reConnect.setVisibility(View.VISIBLE);
                    chatHistoryView.setVisibility(View.GONE);
                } else {
                    reConnect.setVisibility(View.GONE);
                    emptyPageMessage.setVisibility(View.GONE);
                    chatHistoryView.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle(getString(R.string.historyPageTitle) + "(" + chats.size() + ")");
                    Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.historyPageTitle) + "(" + chats.size() + ")");
                }
                historyAdapter.updateData(chats);

            }
        });
    }

    public void setUpGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_REQUEST_PERMISSION_CODE);
        }

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(true);
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Please enable GPS");

        adb.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    dialog.dismiss();
                } else {
                    adb.show();
                }
            }
        });
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            adb.show();
        }
    }

    private void setUpHistoryPage() {
        chatHistoryView = findViewById(R.id.chatHistory);
        chatHistoryView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new ChatListAdapter(this);
        chatHistoryView.setAdapter(historyAdapter);
        emptyPageMessage = findViewById(R.id.chat_list_empty_message);
        reConnect = findViewById(R.id.reConnect);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.connect2_button) {
            model.startSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}

