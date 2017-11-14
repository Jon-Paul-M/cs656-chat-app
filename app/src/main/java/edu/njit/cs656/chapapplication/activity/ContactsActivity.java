package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import edu.njit.cs656.chapapplication.model.Contact;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class ContactsActivity extends AppCompatActivity {


  public static final String DB_NAME_MESSAGES = "contacts";
  public static final String DB_ORDER_BY_FIELD = "time";

  private FirebaseListAdapter<Contact> adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);
    displayContacts();
  }

  private void displayContacts() {
    ListView listOfContacts = findViewById(R.id.list_of_contacts);

    FirebaseListOptions.Builder<Contact> builder = new FirebaseListOptions.Builder<>();
    builder.setLayout(R.layout.contact);
    Query query = FirebaseDatabase.getInstance()
        .getReference()
        .child(DB_NAME_MESSAGES)
        .child(FirebaseAuth
            .getInstance()
            .getCurrentUser()
            .getUid());
    builder.setQuery(query, Contact.class);
    builder.setLifecycleOwner(this);

    FirebaseListOptions<Contact> options = builder.build();

    adapter = new FirebaseListAdapter<Contact>(options) {
      @Override
      protected void populateView(View view, Contact model, int position) {
        TextView contactDisplay = view.findViewById(R.id.contact_displayname);
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
