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
import edu.njit.cs656.chapapplication.model.Message;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;
import edu.njit.cs656.chapapplication.tools.StringUtils;

/**
 * Chat inside a chat room
 */
public class ChatsActivity extends AppCompatActivity {

  public static final String DB_NAME_MESSAGES = "messages";
  public static final String DB_ORDER_BY_FIELD = "time";
  public static final int DB_QUERY_LIMIT = 50;
  private FirebaseListAdapter<Message> adapter;
  private String currentChatRoomId = "General";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chats);
    extractChatRoomIdFromIntent();
    displayChatMessages();
    buildSendButton();
  }

  @Override
  protected void onResume() {
    super.onResume();
    String intentChatRoomId = getIntent().getStringExtra("chatRoomId");
    if (StringUtils.isNotEmpty(intentChatRoomId)
        && !intentChatRoomId.equals(currentChatRoomId)) {
      currentChatRoomId = intentChatRoomId;
      displayChatMessages();
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


  private void displayChatMessages() {
    ListView listOfMessages = findViewById(R.id.list_of_messages);

    FirebaseListOptions.Builder<Message> builder = new FirebaseListOptions.Builder<>();
    builder.setLayout(R.layout.message);
    Query query = FirebaseDatabase
        .getInstance()
        .getReference()
        .child(DB_NAME_MESSAGES)
        .child(currentChatRoomId)
        .orderByChild(DB_ORDER_BY_FIELD)
        .limitToLast(DB_QUERY_LIMIT);
    builder.setQuery(query, Message.class);
    builder.setLifecycleOwner(this);

    FirebaseListOptions<Message> options = builder.build();

    adapter = new FirebaseListAdapter<Message>(options) {
      @Override
      protected void populateView(View view, Message model, int position) {
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
  }

  private void buildSendButton() {
    FloatingActionButton fab = findViewById(R.id.fab);  // SEND button
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        EditText input = findViewById(R.id.input);

        // Get the message text and push it to FirebaseDatabase
        FirebaseDatabase.getInstance().getReference().child(DB_NAME_MESSAGES).child(currentChatRoomId)
            .push()
            .setValue(new Message(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                input.getText().toString(),
                (new Date()).getTime()));
        input.setText("");
      }
    });
  }

  private void extractChatRoomIdFromIntent() {
    String intentChatRoomId = getIntent().getStringExtra("chatRoomId");
    if (StringUtils.isNotEmpty(intentChatRoomId)) {
      currentChatRoomId = intentChatRoomId;
    }
  }
}
