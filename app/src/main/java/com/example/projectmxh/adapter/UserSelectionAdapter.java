package com.example.projectmxh.adapter;

import android.util.Log;
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

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {
    private static final String TAG = "UserSelectionAdapter";
    private List<AppUserDto> users;
    private final Set<AppUserDto> selectedUsers;
    private final UserSelectionListener listener;


    public interface UserSelectionListener {
        void onUserSelected(AppUserDto user);
    }

    public UserSelectionAdapter(List<AppUserDto> users, UserSelectionListener listener) {
        this.users = users;
        this.selectedUsers = new HashSet<>();
        this.listener = listener;
    }

    public void updateUsers(List<AppUserDto> newUsers) {
        Log.d(TAG, "Updating users list: " + newUsers.size());
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_selection, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AppUserDto user = users.get(position);
        holder.bind(user, selectedUsers.contains(user));  // Pass selection state
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final CheckBox selectionCheckBox;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            selectionCheckBox = itemView.findViewById(R.id.selectionCheckBox);
        }

        void bind(AppUserDto user, boolean isSelected) {
            nameText.setText(user.getDisplayName());
            selectionCheckBox.setChecked(isSelected);

            View.OnClickListener clickListener = v -> {
                boolean newState = !selectionCheckBox.isChecked();
                selectionCheckBox.setChecked(newState);

                if (newState) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }

                // Notify listener immediately after state change
                if (listener != null) {
                    listener.onUserSelected(user);
                }
            };

            itemView.setOnClickListener(clickListener);
            selectionCheckBox.setOnClickListener(clickListener);
        }
    }
}
