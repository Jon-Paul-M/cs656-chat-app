package edu.njit.cs656.chapapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.ChatRoomDetails;
import edu.njit.cs656.chapapplication.model.MessageDetails;
import edu.njit.cs656.chapapplication.tools.OptionsMenuHelper;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 100;

    private static final String MESSAGE_SIGNIN_ERROR = "We couldn't sign you in. Please try again later.";
    private static final String MESSAGE_SIGNIN_SUCCESSFUL = "Successfully signed in. Welcome!";

    public static ChatRoomDetails yourChatRoom = new ChatRoomDetails();
    ListView chatRoomList;
    TextView noChatRoom;
    public static int totalChatRoom = 0;
    public static ArrayList<String> myArrayList = new ArrayList<>();
    ProgressDialog progressDialog;

    private FirebaseListAdapter<MessageDetails> adapter;

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

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        String url = "https://chapapplication-ed1cd.firebaseio.com/messages.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(request);

        chatRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRoomDetails.chatRoomName = myArrayList.get(position);
                startActivity(new Intent(MainActivity.this, ChatsActivity.class));
            }
        });
    }

    public void doOnSuccess(String s) {
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();
            String key = "";

            while (i.hasNext()) {
                key = i.next().toString();

                if (!key.equals(ChatRoomDetails.chatRoomName)) {
                    myArrayList.add(key);
                }
                totalChatRoom++;
            }

            if (totalChatRoom <= 1) {
                noChatRoom.setVisibility(View.VISIBLE);
                chatRoomList.setVisibility(View.GONE);
            } else {
                noChatRoom.setVisibility(View.GONE);
                chatRoomList.setVisibility(View.VISIBLE);
                chatRoomList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myArrayList));
            }

            progressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return OptionsMenuHelper.createMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return OptionsMenuHelper.itemSelected(this, item);
    }

}
