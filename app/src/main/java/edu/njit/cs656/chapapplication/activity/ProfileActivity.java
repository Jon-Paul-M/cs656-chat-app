package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.Message;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class ProfileActivity extends AppCompatActivity {

  private static final String MESSAGE_SIGNOUT = "You have been signed out.";

  private String currentChatId = "c000001";

  private FirebaseListAdapter<Message> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    TextView messageText = findViewById(R.id.profile_header);
    messageText.setText("This is the profile view (onCreate)");
    UserInfo user = FirebaseAuth.getInstance().getCurrentUser();
    ((TextView) findViewById(R.id.profile_id)).setText(user.getUid());
    ((TextView) findViewById(R.id.profile_email)).setText(user.getEmail());
    ((TextView) findViewById(R.id.profile_displayname)).setText(user.getDisplayName());
    ((TextView) findViewById(R.id.profile_photo)).setText(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

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
