package com.example.projectmxh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.request.PendingFollowRequest;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AcceptFollowAdapter extends RecyclerView.Adapter<AcceptFollowAdapter.ViewHolder> {
    private final List<PendingFollowRequest> requests;
    private final RequestActionListener listener;

    public interface RequestActionListener {
        void onAccept(PendingFollowRequest request);
        void onDelete(PendingFollowRequest request);
    }

    public AcceptFollowAdapter(List<PendingFollowRequest> requests, RequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accept, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingFollowRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void resetButtonState(PendingFollowRequest request) {
        int position = requests.indexOf(request);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView avatar;
        private final TextView nameText;
        private final Button confirmButton;
        private final Button deleteButton;

        ViewHolder(View view) {
            super(view);
            avatar = view.findViewById(R.id.imageView);
            nameText = view.findViewById(R.id.tvName);
            confirmButton = view.findViewById(R.id.btnConfirm);
            deleteButton = view.findViewById(R.id.btnDelete);
        }

        void bind(PendingFollowRequest request) {
            nameText.setText(request.getFullName());

            if (request.getProfilePicture() != null) {
                Glide.with(itemView.getContext())
                        .load(request.getProfilePicture())
                        .placeholder(R.drawable.avatar)
                        .circleCrop()
                        .into(avatar);
            } else {
                avatar.setImageResource(R.drawable.avatar);
            }

            // Reset button states
            confirmButton.setEnabled(true);
            deleteButton.setEnabled(true);
            confirmButton.setText("Accept");
            deleteButton.setText("Reject");

            confirmButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    confirmButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    confirmButton.setText("Accepting...");
                    listener.onAccept(requests.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    confirmButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    deleteButton.setText("Rejecting...");
                    listener.onDelete(requests.get(position));
                }
            });
        }
    }

    public void removeItem(PendingFollowRequest request) {
        int position = requests.indexOf(request);
        if (position > -1) {
            requests.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, requests.size());
        }
    }
}