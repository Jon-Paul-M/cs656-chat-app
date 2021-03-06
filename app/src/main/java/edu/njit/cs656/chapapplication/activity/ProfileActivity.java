package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.Message;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseListAdapter<Message> adapter;

    private ImageView profilePic;
    private TextView textName;
    private TextView textEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserInfo user = FirebaseAuth.getInstance().getCurrentUser();


        profilePic = findViewById(R.id.profile_picture);
        textName = findViewById(R.id.profile_name);
        textEmail = findViewById(R.id.profile_email);

        textName.setText(user.getDisplayName());
        textEmail.setText(user.getEmail());

        Uri photoURI = user.getPhotoUrl();
        if(photoURI != null) {
            Picasso.with(this).load(photoURI).into(profilePic);
        }
        else {
            Picasso.with(this).load(R.drawable.default_avatar).into(profilePic);
        }
        //Log.d(this.getClass().getSimpleName(), "THIS IS THE URI:  " + photoURI.toString());
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
