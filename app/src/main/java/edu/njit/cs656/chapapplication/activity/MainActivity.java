package edu.njit.cs656.chapapplication.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.ChatRoom;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class MainActivity extends AppCompatActivity {

    private static final String DB_NAME_CHAT_ROOM = "chatrooms";
    private static final String WRONG_CHAT_ROOM_PASSWORD = "Wrong password. Please try again";
    private static final String MESSAGE_SIGNIN_ERROR = "We couldn't sign you in. Please try again later.";
    private static final String MESSAGE_SIGNIN_SUCCESSFUL = "Successfully signed in. Welcome!";
    private static final int SIGN_IN_REQUEST_CODE = 100;
    private Dialog dialog;
    private ChatRoom aChatRoom;

    private ListView chatRoomList;
    private TextView noChatRoom;
    private FirebaseListAdapter<ChatRoom> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new Dialog(this);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        } else {
            // User is already signed in. Therefore, display a welcome Toast
            Toast.makeText(this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
            // Load chat room contents
            displayAllChatRooms();
        }
    }

    /**
     * Display a list of chat rooms
     */
    private void displayAllChatRooms() {
        chatRoomList = findViewById(R.id.chatRoomList);
        noChatRoom = findViewById(R.id.noChatRoomText);

        FirebaseListOptions.Builder<ChatRoom> builder = new FirebaseListOptions.Builder<>();
        builder.setLayout(R.layout.chat_room);

        Query query = FirebaseDatabase.getInstance().getReference().child(DB_NAME_CHAT_ROOM).orderByChild("open").equalTo(true);

        builder.setQuery(query, ChatRoom.class);
        builder.setLifecycleOwner(this);

        FirebaseListOptions<ChatRoom> options = builder.build();

        // count the chat room. if less than 1, then show "no chat room available'
        adapter = new FirebaseListAdapter<ChatRoom>(options) {
            @Override
            protected void populateView(View view, ChatRoom model, int position) {

                ImageView img = view.findViewById(R.id.icon);
                TextView text = view.findViewById(R.id.chat_room_name);
                text.setText(model.getTitle());

                if (model.getTitle().equalsIgnoreCase("Comedy"))
                    img.setImageResource(R.drawable.room_comedy);
                else if (model.getTitle().equalsIgnoreCase("Gaming"))
                    img.setImageResource(R.drawable.room_gaming);
                else if (model.getTitle().equalsIgnoreCase("General"))
                    img.setImageResource(R.drawable.room_general);
                else if (model.getTitle().equalsIgnoreCase("Music"))
                    img.setImageResource(R.drawable.room_music);
                else if (model.getTitle().equalsIgnoreCase("Sports"))
                    img.setImageResource(R.drawable.room_sports);
                else if (model.getTitle().equalsIgnoreCase("Technology"))
                    img.setImageResource(R.drawable.room_tech);
                else if (model.getTitle().equalsIgnoreCase("Private"))
                    img.setImageResource(R.drawable.room_private);
                else {
                    System.out.println(model.getTitle());
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (adapter.getCount() < 1) {
                    noChatRoom.setVisibility(View.VISIBLE);
                    chatRoomList.setVisibility(View.GONE);
                } else {
                    noChatRoom.setVisibility(View.GONE);
                    chatRoomList.setVisibility(View.VISIBLE);
                }
            }
        };
        chatRoomList.setAdapter(adapter);

        chatRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String chatRoomId = adapter.getRef(position).getKey();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DB_NAME_CHAT_ROOM).child(chatRoomId);

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap aChatRoom = (HashMap) dataSnapshot.getValue();
                        if(aChatRoom.get("type").toString().equalsIgnoreCase("private")) {
                            enterPrivateChatRoom(aChatRoom, chatRoomId);

                        } else {
                            OptionsMenuHelper.switchToChatsActivity(MainActivity.this, chatRoomId);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    protected void enterPrivateChatRoom(HashMap aChatRoom, String chatRoomId){
        // Set up the dialog
        dialog.setContentView(R.layout.chatroom_password_dialog);


        // Set the custom dialog components
        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText("Enter password for " + chatRoomId + " chat room:");
        final TextView wrong_password = (TextView) dialog.findViewById(R.id.wrong_password_text);
        final EditText user_password = (EditText) dialog.findViewById(R.id.user_password);
        Button submitBttn = (Button) dialog.findViewById(R.id.submit_bttn);
        Button cancelBttn = (Button) dialog.findViewById(R.id.cancel_bttn);

        wrong_password.setText("");
        dialog.show();

        // attach a listener on the cancel button
        cancelBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        final HashMap tmpRoom = aChatRoom;
        final String chatId = chatRoomId;

        // attach a listern on the submit button
        submitBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_password.getText().toString().equals(tmpRoom.get("password").toString())) {
                    dialog.dismiss();
                    // go to room
                    OptionsMenuHelper.switchToChatsActivity(MainActivity.this, chatId);
                }
                else {  // wrong chat room password
                    wrong_password.setText("Wrong password. Please try again.");
                    user_password.setText("");
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, MESSAGE_SIGNIN_SUCCESSFUL, Toast.LENGTH_LONG).show();
                // Load chat room contents
                displayAllChatRooms();
            } else {
                Toast.makeText(this, MESSAGE_SIGNIN_ERROR, Toast.LENGTH_LONG).show();
            }
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

}
