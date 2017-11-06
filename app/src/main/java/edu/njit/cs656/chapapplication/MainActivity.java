package edu.njit.cs656.chapapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Map;

import edu.njit.cs656.chapapplication.model.ChatMessage;
import edu.njit.cs656.chapapplication.model.Conversation;
import edu.njit.cs656.chapapplication.model.Message;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 100;

    private static final String MESSAGE_SIGNIN_ERROR = "We couldn't sign you in. Please try again later.";
    private static final String MESSGE_SIGNIN_SUCCESSFUL = "Successfully signed in. Welcome!";
    private static final String MESSAGE_SIGNOUT = "You have been signed out.";

    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseListAdapter<Conversation> adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
        //if (true) {
            // Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        } else {
            // User is already signed in. Therefore, display a welcome Toast
            Toast.makeText(this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
            // Load chat room contents
            displayChatMessages();
        }

        FloatingActionButton fab =
                findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("chatMessage")
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );
                // Clear the input
                input.setText("");
            }
        });
    }

    private void displayChatMessages() {
        loadConversations();
        //loadConversationsGood();
    }

    private void loadConversationsGood() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);



        FirebaseListOptions.Builder<ChatMessage> builder = new FirebaseListOptions.Builder<>();
        builder.setLayout(R.layout.message);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("chatMessage")
                .limitToLast(50);
        builder.setQuery(query, ChatMessage.class);
        builder.setLifecycleOwner(this);



        FirebaseListOptions<ChatMessage> options = builder.build();
        Log.d(this.getClass().getSimpleName(), "JPM2");
        Log.d(this.getClass().getSimpleName(), query.toString());
        Log.d(this.getClass().getSimpleName(), options.toString());


        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("MM-dd-yyyy (hh:mm:ss aa)",
                        model.getMessageTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    private void loadConversations() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);

        FirebaseListOptions.Builder<Conversation> builder = new FirebaseListOptions.Builder<>();
        builder.setLayout(R.layout.message);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("conversation")
                .limitToLast(50);
        builder.setQuery(query, Conversation.class);
        builder.setLifecycleOwner(this);


        FirebaseListOptions<Conversation> options = builder.build();
        Log.d(this.getClass().getSimpleName(), "JPM2");
        Log.d(this.getClass().getSimpleName(), query.toString());
        Log.d(this.getClass().getSimpleName(), options.toString());


        adapter2 = new FirebaseListAdapter<Conversation>(options) {
            @Override
            protected void populateView(View v, Conversation model, int position) {
                Log.d(this.getClass().getSimpleName(), "position: " + position);
                for (Map.Entry<String, Message> entry : model.getMessages().entrySet()) {
                    String key = entry.getKey();
                    Message value = entry.getValue();
                    Log.d(this.getClass().getSimpleName(), "key: " + key);
                    Log.d(this.getClass().getSimpleName(), "value: " + value.toString());
                }
            }
        };
        listOfMessages.setAdapter(adapter2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, MESSGE_SIGNIN_SUCCESSFUL, Toast.LENGTH_LONG).show();
                displayChatMessages();
            } else {
                Toast.makeText(this, MESSAGE_SIGNIN_ERROR, Toast.LENGTH_LONG).show();
                // Close the app
                //finish();
            }
        }
    }

    @SuppressWarnings("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.main_menu, menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            System.out.println("Sign out");
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, MESSAGE_SIGNOUT, Toast.LENGTH_LONG).show();
                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }
}
