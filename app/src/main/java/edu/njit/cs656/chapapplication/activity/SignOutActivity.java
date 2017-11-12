package edu.njit.cs656.chapapplication.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.njit.cs656.chapapplication.R;

public class SignOutActivity extends AppCompatActivity {
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signout);


        loginButton = findViewById(R.id.signinBttn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.yourChatRoom.setChatRoomName("");
                startActivity(new Intent(SignOutActivity.this, MainActivity.class));
            }
        });
    }
}