package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.Message;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class ChatsActivity extends AppCompatActivity {

  private static final String MESSAGE_SIGNOUT = "You have been signed out.";

  private String currentChatId = "c000001";

  private FirebaseListAdapter<Message> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chats);
    TextView messageText = findViewById(R.id.header);
    messageText.setText("This is the chats view (onCreate)");
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
