package edu.njit.cs656.chapapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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

    // These components help displaying a MESSAGE item
    private RecyclerView mMessageList;
    private List<MessageModel> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;
    private String mChatUser;
    private EditText mChatMessageView;

    private Button mChatAddBtn;    // the ADD image button
    private Button mChatSendBtn;    // the SEND button
    private ImageView messageImage;     // image viewer

    private FirebaseStorage storage = FirebaseStorage.getInstance(); // Storage Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        extractChatRoomIdFromIntent();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mAdapter = new MessageAdapter(messagesList);

        // help displaying the messages
        mLinearLayout = new LinearLayoutManager(this);
        mMessageList = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatAddBtn = (Button) findViewById(R.id.image_bttn);
        mChatSendBtn = (Button) findViewById(R.id.send_bttn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        displayMessages();

        // send message
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // add image button listner
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), GALLERY_PICK);
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

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            StorageReference filePath = storage.getReference().child("message_images").child(currentChatRoomId)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()
                                                + "_" + System.currentTimeMillis() + ".jpg"); // added  time milis to ensure unique file

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {

                        //Toast.makeText(ChatsActivity.this, "Image uploaded", Toast.LENGTH_LONG).show();
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("fromDisplay", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        messageMap.put("time", (new Date()).getTime());
                        messageMap.put("fromId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        messageMap.put("type", "image");

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
                                if(databaseError != null){
                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                }
                            }
                        });
                    }
                }
            });
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

    /**
     * Send text message
     */
    private void sendMessage() {
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
     * Display all messages including text and images
     */
    private void displayMessages() {
        mRootRef.child("messages").child(currentChatRoomId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // if a new child is detected, store it to the dataSnapshot
                // get data from dataSnapshot
                MessageModel message = dataSnapshot.getValue(MessageModel.class);

                messagesList.add(message);  // add the message to the array
                mAdapter.notifyDataSetChanged();    // notify dataset
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

    private void extractChatRoomIdFromIntent() {
        String intentChatRoomId = getIntent().getStringExtra("chatRoomId");
        if (StringUtils.isNotEmpty(intentChatRoomId)) {
            currentChatRoomId = intentChatRoomId;
        }
    }

    /*
    private void displayChatMessages() {
        listOfMessages = findViewById(R.id.list_of_messages);

        FirebaseListOptions.Builder<MessageModel> builder = new FirebaseListOptions.Builder<>();
        builder.setLayout(R.layout.message);
        Query query = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(DB_NAME_MESSAGES)
                .child(currentChatRoomId)
                .orderByChild(DB_ORDER_BY_FIELD)
                .limitToLast(DB_QUERY_LIMIT);
        builder.setQuery(query, MessageModel.class);
        builder.setLifecycleOwner(this);

        FirebaseListOptions<MessageModel> options = builder.build();

        adapter = new FirebaseListAdapter<MessageModel>(options) {
            @Override
            protected void populateView(View view, MessageModel model, int position) {
                Log.d(this.getClass().getSimpleName(), "model: " + model.toString());

                TextView messageText = view.findViewById(R.id.message_text);
                TextView messageUser = view.findViewById(R.id.message_user);
                TextView messageTime = view.findViewById(R.id.message_time);

                messageText.setText(model.getMessage());
                messageUser.setText(model.getFromDisplay());
                if (model.getTime() != null)
                    messageTime.setText(DateFormat.format("MM-dd-yyyy (hh:mm:ss aa)", model.getTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    } */
}
