package com.example.projectmxh.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.NotificationUserResponseDto;
import java.util.List;

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

        holder.nameText.setText(notification.getComponentName());
        holder.actionText.setText(notification.getEntityType());

        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.getCreatedAt().getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.timeText.setText(timeAgo);

        // Use getRead() instead of getIsRead()
        if (!notification.getRead()) {
            holder.itemView.setBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray)
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
        final TextView nameText;
        final TextView actionText;
        final TextView timeText;
        final ImageView avatarImage;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            actionText = view.findViewById(R.id.actionText);
            timeText = view.findViewById(R.id.timeText);
            avatarImage = view.findViewById(R.id.avatarImage);
        }
    }

    public interface NotificationClickListener {
        void onNotificationClick(NotificationUserResponseDto notification);
    }
}