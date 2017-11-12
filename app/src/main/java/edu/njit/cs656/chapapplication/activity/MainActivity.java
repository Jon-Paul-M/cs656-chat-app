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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.Message;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class MainActivity extends AppCompatActivity {

  private static final int SIGN_IN_REQUEST_CODE = 100;

  private static final String MESSAGE_SIGNIN_ERROR = "We couldn't sign you in. Please try again later.";
  private static final String MESSAGE_SIGNIN_SUCCESSFUL = "Successfully signed in. Welcome!";

  private String currentChatId = "c000001";

  private FirebaseListAdapter<Message> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
      // Start sign in/sign up activity
      startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
    } else {
      // User is already signed in. Therefore, display a welcome Toast
      Toast.makeText(this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
      // Load chat room contents
      displayChatMessages();
    }

    FloatingActionButton fab = findViewById(R.id.fab);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        EditText input = findViewById(R.id.input);

        FirebaseDatabase.getInstance()
            .getReference()
            .child("messages")
            .child(currentChatId)
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
        input.setText("");
      }
    });
  }

  private void displayChatMessages() {
    ListView listOfMessages = findViewById(R.id.list_of_messages);

    FirebaseListOptions.Builder<Message> builder = new FirebaseListOptions.Builder<>();
    builder.setLayout(R.layout.message);
    Query query = FirebaseDatabase.getInstance()
        .getReference()
        .child("messages")
        .child(currentChatId);
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
    if (requestCode == SIGN_IN_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        Toast.makeText(this, MESSAGE_SIGNIN_SUCCESSFUL, Toast.LENGTH_LONG).show();
        displayChatMessages();
      } else {
        Toast.makeText(this, MESSAGE_SIGNIN_ERROR, Toast.LENGTH_LONG).show();
      }
    }
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
