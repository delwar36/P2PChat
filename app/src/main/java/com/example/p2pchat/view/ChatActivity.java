package com.example.p2pchat.view;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.p2pchat.Constants;
import com.example.p2pchat.R;
import com.example.p2pchat.model.MessageEntity;
import com.example.p2pchat.viewmodel.ChatPageViewModel;
import com.example.p2pchat.viewmodel.MainViewModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ChatActivity extends AppCompatActivity {
    private View chatBox, checkOn;
    private EditText newMessage;
    private MessageListAdapter adapter;
    private String addressee;
    private String startDate;
    private boolean isOffline;
    private ChatPageViewModel model;
    private ConstraintLayout loadingScreen;
    private ConstraintLayout messengerLayout;
    private CoordinatorLayout backgroundLayout;
    public static final int PICKFILE_RESULT_CODE = 1;

    public static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    int myDefaultColor;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initChatPage();
        setupToolbar();
        varifyStoragePermission();


        final AlertDialog.Builder adb = new AlertDialog.Builder(ChatActivity.this);
        final Boolean[] dialogActive = {Boolean.FALSE};
        final AlertDialog[] dialogs = {null};

        if (isOffline) {
            chatBox.setVisibility(View.INVISIBLE);
            checkOn.setVisibility(View.VISIBLE);
            model.setAddressee(addressee);
            model.getMessageList().observe(this, new Observer<List<MessageEntity>>() {
                @Override
                public void onChanged(@Nullable List<MessageEntity> messageEntities) {
                    adapter.updateData(messageEntities);
                }
            });
        } else {
            chatBox.setVisibility(View.VISIBLE);
            loadingScreen.setVisibility(View.VISIBLE);
            messengerLayout.setVisibility(View.GONE);
            Objects.requireNonNull(getSupportActionBar()).hide();

            model.startSearch();
            model.chatIsReady().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean != null && aBoolean) {
                        loadingScreen.setVisibility(View.GONE);
                        messengerLayout.setVisibility(View.VISIBLE);
                        Objects.requireNonNull(getSupportActionBar()).show();
                        addressee = model.getAddressee();
                        getSupportActionBar().setTitle(addressee);
                        if (dialogActive[0]) {
                            dialogs[0].dismiss();
                        }
                        model.getMessageList().observe(ChatActivity.this, new Observer<List<MessageEntity>>() {
                            @Override
                            public void onChanged(@Nullable List<MessageEntity> messageEntities) {
                                adapter.updateData(messageEntities);
                            }
                        });
                    }
                }
            });


            model.getPeerList().observe(this, new Observer<List<WifiP2pDevice>>() {
                @Override
                public void onChanged(@Nullable final List<WifiP2pDevice> peers) {

                    assert peers != null;
                    Log.d("", peers.toString());
                    if (peers.size() == 0)
                        return;
                    CharSequence[] items = new CharSequence[peers.size()];
                    int i = 0;
                    for (WifiP2pDevice wifiP2pDevice : peers) {
                        items[i] = wifiP2pDevice.deviceName;
                        i++;
                    }
                    adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int n) {
                            model.connectToPeer(peers.get(n));
                            d.cancel();
                        }

                    });
                    adb.setNegativeButton("Cancel", null);
                    adb.setTitle("Which one?");
                    if (!dialogActive[0]) {
                        dialogs[0] = adb.show();
                        dialogActive[0] = true;
                    } else {
                        dialogs[0].dismiss();
                        dialogs[0] = adb.show();
                    }
                }
            });

            model.getChatClosed().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null || aBoolean) {
                        Toast.makeText(ChatActivity.this, addressee + " left the chat", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }

    }

    private void varifyStoragePermission() {

        // Check if we have write permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    private void initChatPage() {
        backgroundLayout = findViewById(R.id.change_bg_layout);
        myDefaultColor = ContextCompat.getColor(ChatActivity.this, R.color.bgDefaultColor);

        messengerLayout = findViewById(R.id.messengerLayout);
        chatBox = findViewById(R.id.layout_chatbox);
        loadingScreen = findViewById(R.id.loading_screen);
        checkOn = findViewById(R.id.checkOnline);
        loadingScreen.setVisibility(View.GONE);
        findViewById(R.id.stopSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOffline) {
                    model.closeChat();
                }
                finish();
            }
        });
        isOffline = getIntent().getBooleanExtra(Constants.IS_OFFLINE, false);
        model = ViewModelProviders.of(this).get(ChatPageViewModel.class);
        addressee = getIntent().getStringExtra(Constants.ADDRESAT_NAME);
        startDate = getIntent().getStringExtra(Constants.DATE);
        newMessage = findViewById(R.id.edittext_chatbox);
        ImageButton sendMessage = findViewById(R.id.button_chatbox_send);
        ImageView fileChooser = findViewById(R.id.file_chooser);
        ImageView voiceText = findViewById(R.id.voice);

        voiceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }

            private void speak() {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi, speak something");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                } catch (Exception e) {
                    Toast.makeText(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        fileChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();

            }

        });


        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newMessage.getText().toString().trim().length() == 0) {
                    newMessage.setText("");
                    return;
                }

                model.sendMessage(newMessage.getText().toString().replaceAll("\\s+", " "), true);
                newMessage.setText("");
            }
        });

        RecyclerView messages = findViewById(R.id.reyclerview_message_list);
        messages.setLayoutManager(new LinearLayoutManager(this, 1, true));
        adapter = new MessageListAdapter(new ArrayList<MessageEntity>(), this);
        messages.setAdapter(adapter);

    }

    private void showChooser() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        try {
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    try {
                        assert uri != null;

                        String textFile = "~\n" +uriToString(uri);
                        model.sendMessage(textFile, false);
                        Toast.makeText(this, "File sent successfully!", Toast.LENGTH_SHORT).show();


                    } catch (Exception e) {
                        Toast.makeText(this, "File selection failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                model.sendMessage(result.get(0), true);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String uriToString(Uri uri) {

        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append("\n" + line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(addressee);
        if (isOffline) {
            getSupportActionBar().setSubtitle("Last seen " + startDate);
        } else {
            getSupportActionBar().setSubtitle("•Active now");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOffline) {
                    model.closeChat();
                }
                finish();
            }
        });

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.connect_button) {

            MainViewModel model2 = ViewModelProviders.of(this).get(MainViewModel.class);
            model2.startSearch();
            return true;
        }

        if (id == R.id.color_button) {

            openColorPicker();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openColorPicker() {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, myDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                myDefaultColor = color;
                backgroundLayout.setBackgroundColor(myDefaultColor);
            }
        });
        ambilWarnaDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.unregisterBroadcast();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (!isOffline) {
            model.closeChat();
            finish();
        }
    }
}
