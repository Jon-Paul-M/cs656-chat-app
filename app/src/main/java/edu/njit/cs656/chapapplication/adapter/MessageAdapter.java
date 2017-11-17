package edu.njit.cs656.chapapplication.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import edu.njit.cs656.chapapplication.R;
import edu.njit.cs656.chapapplication.model.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageModel> mMessageList; // list that holds the messages
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

    public MessageAdapter(List<MessageModel> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Add the layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        MessageModel aMessage = mMessageList.get(i);
        String from_user = aMessage.getFromDisplay();

        if(aMessage.getType().equals("image")) {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.messageImage.getContext()).load(aMessage.getMessage()).into(viewHolder.messageImage);
        }
        else if(aMessage.getType().equals("text")) {
            viewHolder.messageText.setText(aMessage.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
        }
        else if(!aMessage.getType().equals("image")) {
            viewHolder.messageImage.setImageDrawable(null);
        }
        else {
            //Do Nothing
        }

        viewHolder.displayName.setText(aMessage.getFromDisplay());
        viewHolder.messageTime.setText(simpleDateFormat.format(aMessage.getTime()));
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView displayName;
        public TextView messageTime;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            messageTime = view.findViewById(R.id.time_text_layout);
            messageImage = view.findViewById(R.id.message_image_layout);
        }
    }
}
