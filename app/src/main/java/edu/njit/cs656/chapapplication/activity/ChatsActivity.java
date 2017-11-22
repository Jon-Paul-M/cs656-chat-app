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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.MessageAdapter;
import edu.njit.cs656.chapapplication.model.MessageModel;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;
import edu.njit.cs656.chapapplication.tools.StringUtils;

/**
 *  This class handle the chat activity
 *  - send/receive text message
 *  - send/receive images from Camera roll or Gallery
 *  - capture & send images via the camera
 *
 * Chat inside a chat room
 */
public class ChatsActivity extends AppCompatActivity {

    public static final String DB_NAME_MESSAGES = "messages";
    public static final String DB_ORDER_BY_FIELD = "time";
    public static final int DB_QUERY_LIMIT = 50;

    public static String currentChatRoomId = "General";
    private static final int GALLERY_PICK = 1;
    private static final int REQUEST_SEND_CAMERA_IMAGE_CODE = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 3;


    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

    // These components help displaying a MESSAGE item
    private RecyclerView mMessageList;
    private List<MessageModel> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;
    private EditText mChatMessageView;

    private Button mChatAddBtn;    // the ADD image button
    private Button mChatSendBtn;    // the SEND button
    private ImageView messageImage;     // image viewer

    private Dialog media_dialog;
    private Button galleryBttn;
    private Button cameraBttn;
    private Button voiceBttn;
    private Uri photoURI;

    // RECORDING dialog
    private Dialog audio_dialog;
    private Button audioRecordBttn;
    private Button audioStopBttn;
    private Button playbackStartBttn;
    private Button playbackStopBttn;
    private Button sendAudioBttn;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private File audioFile;
    private Uri audioURI;
    private String audioOutputPath = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private String mCurrentPhotoPath;

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        extractChatRoomIdFromIntent();
        storageReference = FirebaseStorage.getInstance().getReference();

        mAdapter = new MessageAdapter(messagesList);

        // help displaying the messages
        mLinearLayout = new LinearLayoutManager(this);
        mMessageList = findViewById(R.id.reyclerview_message_list);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatAddBtn = findViewById(R.id.image_bttn);
        mChatSendBtn = findViewById(R.id.send_bttn);
        mChatMessageView = findViewById(R.id.chat_message_view);

        displayMessages();

        // send message
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTextMessage();
            }
        });

        // Media button listner
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaSelection();
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
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {    // Sending image from gallery
            Uri uri1 = data.getData();
            sendMediaMessage(uri1, "image");
        } else if (requestCode == REQUEST_SEND_CAMERA_IMAGE_CODE && resultCode == RESULT_OK) {
            Uri uri2 = photoURI;
            sendMediaMessage(uri2, "image");
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
        media_dialog = new Dialog(ChatsActivity.this);
        media_dialog.setContentView(R.layout.media_selection);
        galleryBttn = media_dialog.findViewById(R.id.img_gallery_bttn);
        cameraBttn = media_dialog.findViewById(R.id.img_camera_bttn);
        voiceBttn = media_dialog.findViewById(R.id.voice_bttn);

        media_dialog.show();    // display pop up

        // Send images from gallery
        galleryBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), GALLERY_PICK);

                media_dialog.dismiss();
            }
        });

        // capture a picture and then send it
        cameraBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                media_dialog.dismiss();
            }
        });

        // Record voice and then send it
        voiceBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ChatsActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

                recordAudio();
                media_dialog.dismiss();
            }
        });
    }

    /**
     * Send text message
     */
    private void sendTextMessage() {
        String message = mChatMessageView.getText().toString(); // get the message from the input area

        if(!TextUtils.isEmpty(message)){
            String current_chat_ref = "messages/" + currentChatRoomId;
            DatabaseReference user_message_push = mRootRef.child("messages").child(currentChatRoomId).push();
            String push_id = user_message_push.getKey();

            // new Map to be pushed to database
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("fromDisplay", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            messageMap.put("time", (new Date()).getTime());
            messageMap.put("fromId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            messageMap.put("type", "text");

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_chat_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            // Scroll down to the new message
            mMessageList.smoothScrollToPosition(mAdapter.getItemCount());
            mLinearLayout.setStackFromEnd(true);


            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
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

        final StorageReference filePath = firebaseStorage.getReference().child("media").child(type).child(currentChatRoomId)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "_" + System.currentTimeMillis() + extension);

        Toast.makeText(ChatsActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
        filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task2) {
                if (task2.isSuccessful()) {
                    String download_url = task2.getResult().getMetadata().getDownloadUrl().toString();

                    Map messageMap = new HashMap();
                    messageMap.put("message", download_url);
                    messageMap.put("fromDisplay", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    messageMap.put("time", (new Date()).getTime());
                    messageMap.put("fromId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    messageMap.put("type", type);

                    mChatMessageView.setText("");

                    String current_chat_ref = "messages/" + currentChatRoomId;
                    DatabaseReference user_message_push = mRootRef.child("messages").child(currentChatRoomId).push();
                    String push_id = user_message_push.getKey();

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_chat_ref + "/" + push_id, messageMap);

                    mChatMessageView.setText("");

                    mMessageList.smoothScrollToPosition(mAdapter.getItemCount());
                    mLinearLayout.setStackFromEnd(true);

                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
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
        mRootRef.child("messages").child(currentChatRoomId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // if a new child is detected, store it to the dataSnapshot
                // get data from dataSnapshot
                final MessageModel message = dataSnapshot.getValue(MessageModel.class);
                messagesList.add(message);  // add the message to the array
                mAdapter.notifyDataSetChanged();    // notify data set
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
        String timeStamp = sdf.format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, // file name
                    ".jpg",     // file extension
                    storageDir);        // file path

            mCurrentPhotoPath = image.getAbsolutePath();

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // ensure that there's a camera activity to hangle the intenet
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = image;

                // continue if the file was successfully created
                if (photoFile != null) {
                    photoURI = Uri.fromFile(photoFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    // open CAMERA app and capture image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); // store
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
        audio_dialog = new Dialog(ChatsActivity.this);
        audio_dialog.setContentView(R.layout.audio_recording_layout);
        audioRecordBttn = audio_dialog.findViewById(R.id.audio_record_bttn);
        audioStopBttn = audio_dialog.findViewById(R.id.audio_stop_bttn);
        playbackStartBttn = audio_dialog.findViewById(R.id.playback_play_bttn);
        playbackStopBttn = audio_dialog.findViewById(R.id.playback_stop_bttn);
        sendAudioBttn = audio_dialog.findViewById(R.id.audio_send_bttn);

        // default settings
        audioRecordBttn.setEnabled(true);
        audioStopBttn.setEnabled(false);
        playbackStartBttn.setEnabled(false);
        playbackStopBttn.setEnabled(false);
        sendAudioBttn.setEnabled(false);

        audio_dialog.show();

        audioRecordBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    // Create an empty image file
                    String timeStamp = sdf.format(new Date());
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

                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                audioRecordBttn.setEnabled(false);
                audioStopBttn.setEnabled(true);

                Toast.makeText(ChatsActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
            }
        });

        audioStopBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                audioRecordBttn.setEnabled(true);
                audioStopBttn.setEnabled(false);
                playbackStartBttn.setEnabled(true);
                playbackStopBttn.setEnabled(false);
                sendAudioBttn.setEnabled(true);

                Toast.makeText(ChatsActivity.this, "Recording Completed!", Toast.LENGTH_LONG);
            }
        });

        playbackStartBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws RuntimeException {
                audioStopBttn.setEnabled(false);
                audioRecordBttn.setEnabled(false);
                playbackStartBttn.setEnabled(false);
                playbackStopBttn.setEnabled(true);
                sendAudioBttn.setEnabled(true);

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

        playbackStopBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecordBttn.setEnabled(true);
                audioStopBttn.setEnabled(false);
                playbackStartBttn.setEnabled(true);
                playbackStopBttn.setEnabled(false);
                sendAudioBttn.setEnabled(true);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
        });

        sendAudioBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioURI = Uri.fromFile(audioFile);
                sendMediaMessage(audioURI, "audio");
                audio_dialog.dismiss();
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
