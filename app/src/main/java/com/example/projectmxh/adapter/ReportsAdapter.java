package com.example.projectmxh.adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmxh.Model.Message;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.response.ReportResponse;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {
    private List<ReportResponse> reports = new ArrayList<>();
    private final OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(ReportResponse report);
    }

    public ReportsAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setReports(List<ReportResponse> newReports) {
        reports.clear();
        reports.addAll(newReports);
        notifyDataSetChanged();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView reporterName;
        private final TextView reportReason;
        private final TextView reportDate;
        private final TextView additionalText;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reporterName = itemView.findViewById(R.id.reporterName);
            reportReason = itemView.findViewById(R.id.reportReason);
            reportDate = itemView.findViewById(R.id.reportDate);
            additionalText = itemView.findViewById(R.id.additionalInfo);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onReportClick(reports.get(position));
                }
            });
        }

        void bind(ReportResponse report) {
            reporterName.setText(report.getReporterId().getDisplayName());
            reportReason.setText(report.getReason());
            reportDate.setText(report.getCreatedAt());

            if (report.getAdditionalText() != null && !report.getAdditionalText().isEmpty()) {
                additionalText.setVisibility(View.VISIBLE);
                additionalText.setText(report.getAdditionalText());
            } else {
                additionalText.setVisibility(View.GONE);
            }
        }

        private String formatDate(LocalDateTime dateTime) {
            // Implement date formatting
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                        .format(dateTime);
            }
            return "Just now";
        }
    }
}
