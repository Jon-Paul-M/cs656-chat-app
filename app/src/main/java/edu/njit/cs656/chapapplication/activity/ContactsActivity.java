package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.Message;

public class ContactsActivity extends AppCompatActivity {

  private static final String MESSAGE_SIGNOUT = "You have been signed out.";

  private String currentChatId = "c000001";

  private FirebaseListAdapter<Message> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);
    TextView messageText = findViewById(R.id.header);
    messageText.setText("This is the contacts view (onCreate)");
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  @SuppressWarnings("ResourceType")
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.layout.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_sign_out) {
      System.out.println("Sign out");
      AuthUI.getInstance().signOut(this)
          .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              Toast.makeText(ContactsActivity.this, MESSAGE_SIGNOUT, Toast.LENGTH_LONG).show();
              // Close activity
              finish();
            }
          });
    }
    return true;
  }
}
