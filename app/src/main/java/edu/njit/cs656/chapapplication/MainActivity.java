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

import java.util.Date;

import edu.njit.cs656.chapapplication.model.Message;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 100;

    private static final String MESSAGE_SIGNIN_ERROR = "We couldn't sign you in. Please try again later.";
    private static final String MESSGE_SIGNIN_SUCCESSFUL = "Successfully signed in. Welcome!";
    private static final String MESSAGE_SIGNOUT = "You have been signed out.";

    private FirebaseListAdapter<Message> adapter;

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
                // of Message to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                    .child("message")
                    .child("c000001")
                        .push()
                    .setValue(new Message(FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                            FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getUid(),
                            input.getText().toString(),
                            (new Date()).getTime()
                        )
                        );
                // Clear the input
                input.setText("");
            }
        });
    }

    private void displayChatMessages() {
        loadConversations();
    }

    private void loadConversations() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);

        FirebaseListOptions.Builder<Message> builder = new FirebaseListOptions.Builder<>();
        builder.setLayout(R.layout.message);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
            .child("messages")
            .child("c000001");
        builder.setQuery(query, Message.class);
        builder.setLifecycleOwner(this);


        FirebaseListOptions<Message> options = builder.build();
        Log.d(this.getClass().getSimpleName(), "JPM2");
        Log.d(this.getClass().getSimpleName(), query.toString());
        Log.d(this.getClass().getSimpleName(), options.toString());


        adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(View view, Message model, int position) {
                Log.d(this.getClass().getSimpleName(), "model: " + model.toString());

                // Get references to the views of message.xml
                TextView messageText = view.findViewById(R.id.message_text);
                TextView messageUser = view.findViewById(R.id.message_user);
                TextView messageTime = view.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessage());
                messageUser.setText(model.getFromDisplay());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("MM-dd-yyyy (hh:mm:ss aa)",
                    model.getTime()));


            }
        };
        listOfMessages.setAdapter(adapter);

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
