package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.ChatRoom;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class MainActivity extends AppCompatActivity {

  private static final String DB_NAME_CHAT_ROOM = "chatrooms";
  private static final String MESSAGE_SIGNIN_ERROR = "We couldn't sign you in. Please try again later.";
  private static final String MESSAGE_SIGNIN_SUCCESSFUL = "Successfully signed in. Welcome!";
  private static final int SIGN_IN_REQUEST_CODE = 100;

  private ListView chatRoomList;
  private TextView noChatRoom;
  private FirebaseListAdapter<ChatRoom> adapter;

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
      displayAllChatRooms();
    }
  }

  /**
   * Display a list of chat rooms
   */
  private void displayAllChatRooms() {
    chatRoomList = findViewById(R.id.chatRoomList);
    noChatRoom = findViewById(R.id.noChatRoomText);

    FirebaseListOptions.Builder<ChatRoom> builder =
        new FirebaseListOptions.Builder<>();
    builder.setLayout(R.layout.chat_room);
    Query query = FirebaseDatabase.getInstance()
        .getReference()
        .child(DB_NAME_CHAT_ROOM)
        .orderByChild("open")
        .equalTo(true);

    builder.setQuery(query, ChatRoom.class);
    builder.setLifecycleOwner(this);

    FirebaseListOptions<ChatRoom> options = builder.build();

    adapter = new FirebaseListAdapter<ChatRoom>(options) {
      @Override
      protected void populateView(View view, ChatRoom model, int position) {
        TextView text = view.findViewById(R.id.chat_room_name);
        text.setText(model.getTitle());
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
        String chatRoomId = adapter.getRef(position).getKey();
        OptionsMenuHelper.switchToChatsActivity(MainActivity.this, chatRoomId);
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
