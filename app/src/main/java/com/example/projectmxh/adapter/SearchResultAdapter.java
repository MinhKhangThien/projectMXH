package com.example.projectmxh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<AppUserDto> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(AppUserDto user);
    }

    public SearchResultAdapter(List<AppUserDto> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUserDto user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateResults(List<AppUserDto> newUsers) {
        users = newUsers;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name;
        private ImageView closeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivAvatar);
            name = itemView.findViewById(R.id.tvName);
            closeButton = itemView.findViewById(R.id.ivClose);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(users.get(position));
                }
            });
        }

        void bind(AppUserDto user) {
            name.setText(user.getDisplayName());

            if (user.getProfilePicture() != null) {
                Glide.with(itemView.getContext())
                        .load(user.getProfilePicture())
                        .placeholder(R.drawable.avatar)
                        .into(avatar);
            }
        }
    }
}