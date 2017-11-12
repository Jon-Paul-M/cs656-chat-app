package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.MessageDetails;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class ChatsActivity extends AppCompatActivity {

    private FirebaseListAdapter<MessageDetails> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        displayChatMessages();

        // Input message area. GOOD
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.input);

                FirebaseDatabase.getInstance().getReference().child("messages").child(MainActivity.yourChatRoom.chatRoomName)
                        .push()
                        .setValue(new MessageDetails(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                input.getText().toString(),
                                (new Date()).getTime()));
                input.setText("");
            }
        });
    }

    /**
     * Display all chat messages in a particular chat room
     */
    private void displayChatMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);

        FirebaseListOptions.Builder<MessageDetails> builder = new FirebaseListOptions.Builder<>();
        builder.setLayout(R.layout.message);
        Query query = FirebaseDatabase.getInstance().getReference().child("messages").child(MainActivity.yourChatRoom.chatRoomName);
        builder.setQuery(query, MessageDetails.class);
        builder.setLifecycleOwner(this);

        FirebaseListOptions<MessageDetails> options = builder.build();
        Log.d(this.getClass().getSimpleName(), "JPM2");
        Log.d(this.getClass().getSimpleName(), query.toString());
        Log.d(this.getClass().getSimpleName(), options.toString());

        adapter = new FirebaseListAdapter<MessageDetails>(options) {
            @Override
            protected void populateView(View view, MessageDetails model, int position) {
                Log.d(this.getClass().getSimpleName(), "model: " + model.toString());

                TextView messageText = view.findViewById(R.id.message_text);
                TextView messageUser = view.findViewById(R.id.message_user);
                TextView messageTime = view.findViewById(R.id.message_time);

                messageText.setText(model.getMessage());
                messageUser.setText(model.getFromDisplay());

                messageTime.setText(DateFormat.format("MM-dd-yyyy (hh:mm:ss aa)", model.getTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
