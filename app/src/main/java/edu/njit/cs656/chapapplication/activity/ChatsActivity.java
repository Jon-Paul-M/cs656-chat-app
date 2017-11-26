package edu.njit.cs656.chapapplication.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.adapter.MessageAdapter;
import edu.njit.cs656.chapapplication.model.Message;
import edu.njit.cs656.chapapplication.tools.ConcurrentDateFormater;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;
import edu.njit.cs656.chapapplication.tools.StringUtils;

/**
 * This class handle the chat activity
 * - send/receive text message
 * - send/receive images from Camera roll or Gallery
 * - capture & send images via the camera
 * <p>
 * Chat inside a chat room
 */
public class ChatsActivity extends AppCompatActivity {

    private static final String DB_NAME_MESSAGES = "messages";
    private static final String DB_ORDER_BY_FIELD = "time";
    private static final int DB_QUERY_LIMIT = 50;
    private static final int GALLERY_PICK = 1;
    private static final int REQUEST_SEND_CAMERA_IMAGE_CODE = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 3;
  private static final ConcurrentDateFormater formater = new ConcurrentDateFormater("yyyyMMdd_hhmmss");

    public static String currentChatRoomId = "General";

    // These components help displaying a MESSAGE item
    private RecyclerView messageListView;
    private List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private EditText chatMessageView;

    private Button addImageButton;    // the ADD image button
    private Button sendMessageButton;    // the SEND button

    private Dialog mediaDialog;
    private Button galleryButton;
    private Button cameraButton;
    private Button voiceButton;
    private Uri photoUri;

    // RECORDING dialog
    private Dialog audioDialog;
    private Button audioRecordButton;
    private Button audioStopButton;
    private Button playbackStartButton;
    private Button playbackStopButton;
    private Button sendAudioButton;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private File audioFile;
    private Uri audioUri;
    private String audioOutputPath = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        extractChatRoomIdFromIntent();
        displayMessages();
        setupSendMessageButton();
        setupMediaSelectButton();
    }

    private void setupMediaSelectButton() {
        // Media button listner
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaSelection();
            }
        });
    }

    private void setupSendMessageButton() {
        // send message
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTextMessage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String intentChatRoomId = getIntent().getStringExtra("chatRoomId");
        if (StringUtils.isNotEmpty(intentChatRoomId) && !intentChatRoomId.equals(currentChatRoomId)) {
            currentChatRoomId = intentChatRoomId;
            displayMessages();
        }
    }

    /**
     * Sending image to Firebase
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {    // Sending image from gallery
            uri = data.getData();
        } else if (requestCode == REQUEST_SEND_CAMERA_IMAGE_CODE && resultCode == RESULT_OK) {
            uri = photoUri;
        }
        if (uri != null) {
            sendMediaMessage(uri, "image");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return OptionsMenuHelper.createMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return OptionsMenuHelper.itemSelected(this, item);
    }

    private void extractChatRoomIdFromIntent() {
        String intentChatRoomId = getIntent().getStringExtra("chatRoomId");
        if (StringUtils.isNotEmpty(intentChatRoomId)) {
            currentChatRoomId = intentChatRoomId;
        }
    }

    /**
     * Show the dialog and select an option
     */
    private void startMediaSelection() {

        // Set up the dialog
        mediaDialog = new Dialog(ChatsActivity.this);
        mediaDialog.setContentView(R.layout.media_selection);
        galleryButton = mediaDialog.findViewById(R.id.img_gallery_bttn);
        cameraButton = mediaDialog.findViewById(R.id.img_camera_bttn);
        voiceButton = mediaDialog.findViewById(R.id.voice_bttn);

        mediaDialog.show();    // display pop up

        // Send images from gallery
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), GALLERY_PICK);

                mediaDialog.dismiss();
            }
        });

        // capture a picture and then send it
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                mediaDialog.dismiss();
            }
        });

        // Record voice and then send it
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ChatsActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

                recordAudio();
                mediaDialog.dismiss();
            }
        });
    }

    /**
     * Send text message
     */
    private void sendTextMessage() {
        String message = chatMessageView.getText().toString(); // get the message from the input area

        if (StringUtils.isNotEmpty(message)) {
            String currentChatReference = DB_NAME_MESSAGES + "/" + currentChatRoomId;
            DatabaseReference userMessagePush = FirebaseDatabase.getInstance().getReference().child(DB_NAME_MESSAGES).child(currentChatRoomId).push();
            String pushId = userMessagePush.getKey();

            // new Map to be pushed to database
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("fromDisplay", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            messageMap.put("time", (new Date()).getTime());
            messageMap.put("fromId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            messageMap.put("type", "text");

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentChatReference + "/" + pushId, messageMap);

            chatMessageView.setText("");

            // Scroll down to the new message
            messageListView.smoothScrollToPosition(messageAdapter.getItemCount());
            linearLayoutManager.setStackFromEnd(true);


            FirebaseDatabase.getInstance().getReference().updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    /**
     * Send an image
     *
     * @param uri
     */
    private void sendMediaMessage(Uri uri, String fileType) {
        String extension = null;
        final String type = fileType;
        if (type.equalsIgnoreCase("image"))
            extension = ".jpg";
        else if (type.equalsIgnoreCase("audio"))
            extension = ".mp3";
        else {
        }

        String storageBaseName = FirebaseAuth.getInstance().getCurrentUser().getUid() +
                "_" + System.currentTimeMillis() +
                extension;
        final StorageReference filePath = FirebaseStorage.getInstance().getReference()
                .child("media")
                .child(type)
                .child(currentChatRoomId)
                .child(storageBaseName);

        Toast.makeText(ChatsActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
        filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task2) {
                if (task2.isSuccessful()) {
                    String downloadUrl = task2.getResult().getMetadata().getDownloadUrl().toString();

                    Map messageMap = new HashMap();
                    messageMap.put("message", downloadUrl);
                    messageMap.put("fromDisplay", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    messageMap.put("time", (new Date()).getTime());
                    messageMap.put("fromId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    messageMap.put("type", type);

                    chatMessageView.setText("");

                    String currentChatRef = DB_NAME_MESSAGES + "/" + currentChatRoomId;
                    DatabaseReference userMessagePush = FirebaseDatabase.getInstance().getReference().child(DB_NAME_MESSAGES).child(currentChatRoomId).push();
                    String pushId = userMessagePush.getKey();

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(currentChatRef + "/" + pushId, messageMap);

                    chatMessageView.setText("");

                    messageListView.smoothScrollToPosition(messageAdapter.getItemCount());
                    linearLayoutManager.setStackFromEnd(true);

                    FirebaseDatabase.getInstance().getReference().updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                    Toast.makeText(ChatsActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatsActivity.this, "Upload Failed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Display all messages including text and images
     */
    private void displayMessages() {
        setTitle(currentChatRoomId);
        messageAdapter = new MessageAdapter(messagesList);

        // help displaying the messages
        linearLayoutManager = new LinearLayoutManager(this);
        messageListView = findViewById(R.id.reyclerview_message_list);
        messageListView.setHasFixedSize(true);
        messageListView.setLayoutManager(linearLayoutManager);
        messageListView.setAdapter(messageAdapter);


        addImageButton = findViewById(R.id.image_bttn);
        sendMessageButton = findViewById(R.id.send_bttn);
        chatMessageView = findViewById(R.id.chat_message_view);
        messagesList.clear();
        FirebaseDatabase.getInstance().getReference()
                .child(DB_NAME_MESSAGES)
                .child(currentChatRoomId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // if a new child is detected, store it to the dataSnapshot
                        // get data from dataSnapshot
                        final Message message = dataSnapshot.getValue(Message.class);
                        messagesList.add(message);  // add the message to the array
                        messageAdapter.notifyDataSetChanged();    // notify data set
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Open camera, capture image, and store image
     * 1. create empty file
     * 2. capture image
     * 3. replace the captured image with the empty file
     */
    private void dispatchTakePictureIntent() {
        // Create an empty image file
      String timeStamp = formater.format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, // file name
                    ".jpg",     // file extension
                    storageDir);        // file path

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // ensure that there's a camera activity to hangle the intenet
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = image;

                // continue if the file was successfully created
                if (photoFile != null) {
                    photoUri = Uri.fromFile(photoFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    // open CAMERA app and capture image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); // store
                    startActivityForResult(intent, REQUEST_SEND_CAMERA_IMAGE_CODE);    // call for next activity
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Record voice audio
     */
    private void recordAudio() {
        audioDialog = new Dialog(ChatsActivity.this);
        audioDialog.setContentView(R.layout.audio_recording_layout);
        audioRecordButton = audioDialog.findViewById(R.id.audio_record_bttn);
        audioStopButton = audioDialog.findViewById(R.id.audio_stop_bttn);
        playbackStartButton = audioDialog.findViewById(R.id.playback_play_bttn);
        playbackStopButton = audioDialog.findViewById(R.id.playback_stop_bttn);
        sendAudioButton = audioDialog.findViewById(R.id.audio_send_bttn);

        // default settings
        audioRecordButton.setEnabled(true);
        audioStopButton.setEnabled(false);
        playbackStartButton.setEnabled(false);
        playbackStopButton.setEnabled(false);
        sendAudioButton.setEnabled(false);

        audioDialog.show();

        audioRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Create an empty image file
                  String timeStamp = formater.format(new Date());
                    String audioFileName = "Recorded_" + timeStamp;
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);

                    audioFile = File.createTempFile(audioFileName, // file name
                            ".mp3",     // file extension
                            storageDir);        // file path

                    audioOutputPath = audioFile.getAbsolutePath();

                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    mediaRecorder.setOutputFile(audioOutputPath);

                    System.out.println(mediaRecorder);

                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                audioRecordButton.setEnabled(false);
                audioStopButton.setEnabled(true);

                Toast.makeText(ChatsActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
            }
        });

        audioStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                audioRecordButton.setEnabled(true);
                audioStopButton.setEnabled(false);
                playbackStartButton.setEnabled(true);
                playbackStopButton.setEnabled(false);
                sendAudioButton.setEnabled(true);

                Toast.makeText(ChatsActivity.this, "Recording Completed!", Toast.LENGTH_LONG);
            }
        });

        playbackStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws RuntimeException {
                audioStopButton.setEnabled(false);
                audioRecordButton.setEnabled(false);
                playbackStartButton.setEnabled(false);
                playbackStopButton.setEnabled(true);
                sendAudioButton.setEnabled(true);

                mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(audioOutputPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(ChatsActivity.this, "Recording playing", Toast.LENGTH_LONG);

            }
        });

        playbackStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecordButton.setEnabled(true);
                audioStopButton.setEnabled(false);
                playbackStartButton.setEnabled(true);
                playbackStopButton.setEnabled(false);
                sendAudioButton.setEnabled(true);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
        });

        sendAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioUri = Uri.fromFile(audioFile);
                sendMediaMessage(audioUri, "audio");
                audioDialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

}

