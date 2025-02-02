package com.example.projectmxh.adapter;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.NotificationUserResponseDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<NotificationUserResponseDto> notifications;
    private final NotificationClickListener listener;

    public NotificationAdapter(List<NotificationUserResponseDto> notifications, NotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationUserResponseDto notification = notifications.get(position);

        // Set notification message and description
        holder.notificationText.setText(notification.getMessage());
        holder.descriptionText.setText(notification.getDescription());

        // Load profile picture
        if (notification.getCreatedBy() != null && notification.getCreatedBy().getProfilePicture() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(notification.getCreatedBy().getProfilePicture())
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .circleCrop()
                    .into(holder.avatarImage);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.avatar)
                    .circleCrop()
                    .into(holder.avatarImage);
        }

        // Handle time formatting
        if (notification.getCreatedAt() != null) {
            try {
                String dateStr = notification.getCreatedAt().replace('T', ' '); // "2025-01-31 23:42:46"
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(dateStr.split("\\.")[0]);

                if (date != null) {
                    CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                            date.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    );
                    holder.timeText.setText(timeAgo);
                }
            } catch (ParseException e) {
                Log.e("NotificationAdapter", "Error parsing date: " + e.getMessage());
                holder.timeText.setText(notification.getCreatedAt());
            }
        }

        // Set background for unread notifications
        if (!notification.getRead()) {
            holder.itemView.setBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.unread_notification)
            );
        } else {
            holder.itemView.setBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.white)
            );
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView notificationText;
        final TextView descriptionText;
        final TextView timeText;
        final ImageView avatarImage;

        ViewHolder(View view) {
            super(view);
            notificationText = view.findViewById(R.id.notificationText);
            descriptionText = view.findViewById(R.id.descriptionText);
            timeText = view.findViewById(R.id.timeText);
            avatarImage = view.findViewById(R.id.avatarImage);
        }
    }

    public interface NotificationClickListener {
        void onNotificationClick(NotificationUserResponseDto notification);
    }
}