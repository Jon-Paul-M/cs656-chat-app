package edu.njit.cs656.chapapplication.tools;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.activity.ChatsActivity;
import edu.njit.cs656.chapapplication.activity.ContactsActivity;
import edu.njit.cs656.chapapplication.activity.MainActivity;
import edu.njit.cs656.chapapplication.activity.ProfileActivity;
import edu.njit.cs656.chapapplication.activity.SignOutActivity;

/**
 * Created by jon-paul on 11/10/17.
 */

public class OptionsMenuHelper {

    private static final String MESSAGE_SIGNOUT = "You have been signed out.";

    public static boolean itemSelected(final AppCompatActivity activity, MenuItem item) {
        if (item.getItemId() == R.id.menu_home) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        } else if (item.getItemId() == R.id.menu_contacts) {
            Intent intent = new Intent(activity, ContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        } else if (item.getItemId() == R.id.menu_chats) {

            if (MainActivity.yourChatRoom.getChatRoomName() == null)
                MainActivity.yourChatRoom.setChatRoomName(MainActivity.myArrayList.get(0));

            Intent intent = new Intent(activity, ChatsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        } else if (item.getItemId() == R.id.menu_profile) {
            Intent intent = new Intent(activity, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
        } else if (item.getItemId() == R.id.menu_sign_out) {
            /*
            AuthUI.getInstance().signOut(activity)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(activity, MESSAGE_SIGNOUT, Toast.LENGTH_LONG).show();
                            activity.finish();
                        }
                    });
                    */

            Intent intent = new Intent(activity, SignOutActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);

            AuthUI.getInstance().signOut(activity);
            Toast.makeText(activity, MESSAGE_SIGNOUT, Toast.LENGTH_LONG).show();

        }
        return true;
    }

    @SuppressWarnings("ResourceType")
    public static boolean createMenu(final AppCompatActivity activity, Menu menu) {
        activity.getMenuInflater().inflate(R.layout.main_menu, menu);
        return true;
    }
}
