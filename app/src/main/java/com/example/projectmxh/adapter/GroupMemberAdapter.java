package com.example.projectmxh.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {
    private static final String TAG = "GroupMemberAdapter";
    private final List<AppUserDto> members;
    private String currentUserId;
    private final String groupId;
    private String hostId;
    private Context context; // Add context field

    public GroupMemberAdapter(List<AppUserDto> members, String currentUserId, String groupId) {
        this.members = members;
        this.currentUserId = currentUserId;
        this.groupId = groupId;
        // Get context from first view creation in onCreateViewHolder
        this.context = null;
        loadHostInfo();
    }

    private void loadHostInfo() {
        if (context == null) return;

        Log.d(TAG, "Loading host info for group: " + groupId);
        ApiService apiService = ApiClient.getClientWithToken(context).create(ApiService.class);

        // First get current user's username
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().getUsername(); // Change to use username
                    Log.d(TAG, "Current user username: " + currentUserId);

                    // Then get host info
                    apiService.getGroupHost(groupId).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                hostId = response.body();
                                Log.d(TAG, "Host username: " + hostId);
                                notifyDataSetChanged();
                            } else {
                                Log.e(TAG, "Failed to get host. Response: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e(TAG, "Failed to load host info", t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Failed to get current user", t);
            }
        });
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_groupmember, parent, false);
        if (context == null) {
            // Initialize context on first view creation
            ((GroupMemberAdapter)this).context = parent.getContext();
            loadHostInfo(); // Now we can load host info
        }
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        AppUserDto member = members.get(position);

        Log.d(TAG, "Binding member: " + member.getUsername());
        Log.d(TAG, "Current user ID: " + currentUserId);
        Log.d(TAG, "Host ID (username): " + hostId);

        // Use memberName instead of nameText
        holder.memberName.setText(member.getDisplayName());

        boolean isCurrentUserHost = currentUserId.equals(hostId);
        boolean isMemberHost = member.getUsername().equals(hostId); // Changed from getId() to getUsername()

        Log.d(TAG, "Is current user host? " + isCurrentUserHost);
        Log.d(TAG, "Is this member host? " + isMemberHost);

        // Show remove icon only for other members if current user is host
        holder.removeIcon.setVisibility(
                isCurrentUserHost && !isMemberHost && !member.getUsername().equals(currentUserId)
                        ? View.VISIBLE : View.GONE
        );

        // Add host indicator text in the memberName
        if (isMemberHost) {
            holder.memberName.setText(member.getDisplayName() + " (Host)");
        } else {
            holder.memberName.setText(member.getDisplayName());
        }

        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<AppUserDto> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
        notifyDataSetChanged();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView memberAvatar;
        private final TextView memberName;
        private final ImageView removeIcon;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberAvatar = itemView.findViewById(R.id.memberAvatar);
            memberName = itemView.findViewById(R.id.memberName);
            removeIcon = itemView.findViewById(R.id.removeIcon);
        }

        void bind(AppUserDto member) {
            memberName.setText(member.getDisplayName());

            if (member.getProfilePicture() != null) {
                Glide.with(itemView.getContext())
                        .load(member.getProfilePicture())
                        .placeholder(R.drawable.avatar)
                        .into(memberAvatar);
            }

            // Show remove icon only for other members if current user is host
            boolean isCurrentUserHost = currentUserId.equals(hostId);
            boolean isMemberHost = member.getId().equals(hostId);
            removeIcon.setVisibility(
                    isCurrentUserHost && !isMemberHost && !member.getId().equals(currentUserId)
                            ? View.VISIBLE : View.GONE
            );

            removeIcon.setOnClickListener(v -> {
                if (isCurrentUserHost && !isMemberHost) {
                    showRemoveDialog(member);
                }
            });
        }

        private void showRemoveDialog(AppUserDto member) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Remove Member")
                    .setMessage("Are you sure you want to remove this member?")
                    .setPositiveButton("Remove", (dialog, which) -> removeMember(member))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void removeMember(AppUserDto member) {
            ApiService apiService = ApiClient.getClientWithToken(itemView.getContext())
                    .create(ApiService.class);

            Log.d("GroupMemberAdapter", "Removing member: " + member.getId() + " from group: " + groupId);

            apiService.removeUserFromGroupChat(groupId, member.getId().toString())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                // Remove member locally first
                                int position = members.indexOf(member);
                                if (position != -1) {
                                    members.remove(position);
                                    notifyItemRemoved(position);
                                }
                                Toast.makeText(itemView.getContext(),
                                        "Member removed successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("GroupMemberAdapter", "Failed to remove member: " + response.code());
                                Toast.makeText(itemView.getContext(),
                                        "Server error: Could not remove member", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("GroupMemberAdapter", "Network error removing member", t);
                            Toast.makeText(itemView.getContext(),
                                    "Network error: Could not remove member", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
