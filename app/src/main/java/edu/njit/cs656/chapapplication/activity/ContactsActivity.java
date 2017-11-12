package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.ContactDetails;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class ContactsActivity extends AppCompatActivity {

  private static final String MESSAGE_SIGNOUT = "You have been signed out.";

  private String currentChatId = "c000001";

    private FirebaseListAdapter<ContactDetails> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);
    TextView messageText = findViewById(R.id.contacts_header);
    messageText.setText("This is the contacts view (onCreate)");
    displayContacts();
  }

  private void displayContacts() {
    ListView listOfContacts = findViewById(R.id.list_of_contacts);

      FirebaseListOptions.Builder<ContactDetails> builder = new FirebaseListOptions.Builder<>();
    builder.setLayout(R.layout.contact);
    Query query = FirebaseDatabase.getInstance()
        .getReference()
        .child("contactList")
        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
      builder.setQuery(query, ContactDetails.class);
    builder.setLifecycleOwner(this);

      FirebaseListOptions<ContactDetails> options = builder.build();
    Log.d(this.getClass().getSimpleName(), "JPM2");
    Log.d(this.getClass().getSimpleName(), query.toString());
    Log.d(this.getClass().getSimpleName(), options.toString());

      adapter = new FirebaseListAdapter<ContactDetails>(options) {
      @Override
      protected void populateView(View view, ContactDetails model, int position) {
        Log.d(this.getClass().getSimpleName(), "model: " + model.toString());

        TextView contactId = view.findViewById(R.id.contact_id);
        TextView contactDisplay = view.findViewById(R.id.contact_displayname);

        contactId.setText(model.getId());
        contactDisplay.setText(model.getDisplay());

      }
    };
    listOfContacts.setAdapter(adapter);
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
