package com.example.projectmxh.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    private List<AppUserDto> users;
    private final Set<AppUserDto> selectedUsers = new HashSet<>();
    private final UserSelectionListener listener;

    public interface UserSelectionListener {
        void onUserSelected(AppUserDto user);
    }

    public UserManagementAdapter(List<AppUserDto> users, UserSelectionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void clearSelection() {
        selectedUsers.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<AppUserDto> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView emailText;
        private final CheckBox selectionCheckBox;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.userName);
            emailText = itemView.findViewById(R.id.userEmail);
            selectionCheckBox = itemView.findViewById(R.id.userCheckbox);
        }

        void bind(AppUserDto user) {
            nameText.setText(user.getDisplayName());
            emailText.setText(user.getUsername());
            selectionCheckBox.setChecked(selectedUsers.contains(user));

            View.OnClickListener clickListener = v -> {
                boolean newState = !selectionCheckBox.isChecked();
                selectionCheckBox.setChecked(newState);

                if (newState) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }

                itemView.setBackgroundResource(newState ?
                        R.color.selected_item_background :
                        android.R.color.transparent);

                if (listener != null) {
                    listener.onUserSelected(user);
                }
            };

            itemView.setOnClickListener(clickListener);
            selectionCheckBox.setOnClickListener(clickListener);

            itemView.setBackgroundResource(selectedUsers.contains(user) ?
                    R.color.selected_item_background :
                    android.R.color.transparent);
        }
    }
}
