package com.example.projectmxh.screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.projectmxh.Model.Post;
import com.example.projectmxh.R;
import com.example.projectmxh.dto.AppUserDto;
import com.example.projectmxh.dto.ReportDTO;
import com.example.projectmxh.dto.response.ReportResponse;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDialog extends DialogFragment {
    private static final String TAG = "ReportDialog";
    private Spinner reasonSpinner;
    private EditText additionalTextInput;
    private Post post;
    private ReportSubmitListener listener;

    private static final String[] REPORT_REASONS = {
            "SPAM", "HARASSMENT", "HATE_SPEECH", "FAKE_ACCOUNT", "OTHER"
    };

    public interface ReportSubmitListener {
        void onReportSubmitted();
    }

    public static ReportDialog newInstance(Post post) {
        ReportDialog dialog = new ReportDialog();
        dialog.post = post;
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_report_post, container, false);

        reasonSpinner = view.findViewById(R.id.reportReasonSpinner);
        additionalTextInput = view.findViewById(R.id.reportAdditionalText);
        Button submitButton = view.findViewById(R.id.submitButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                REPORT_REASONS
        );
        reasonSpinner.setAdapter(adapter);

        submitButton.setOnClickListener(v -> submitReport());
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void submitReport() {
        String selectedReason = (String) reasonSpinner.getSelectedItem();
        Log.d(TAG, "Submitting report with reason: " + selectedReason);

        // Get current user first, then submit report
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getMe().enqueue(new Callback<AppUserDto>() {
            @Override
            public void onResponse(Call<AppUserDto> call, Response<AppUserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String currentUserId = response.body().getId().toString();
                    submitReportWithUserId(currentUserId, selectedReason);
                } else {
                    Log.e(TAG, "Failed to get current user");
                    Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppUserDto> call, Throwable t) {
                Log.e(TAG, "Network error getting current user", t);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReportWithUserId(String currentUserId, String reason) {
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setReportedContentId(post.getId());
        reportDTO.setReportedContentType("POST");
        reportDTO.setReportedUserId(post.getUser().getId());
        reportDTO.setReason(reason);
        reportDTO.setAdditionalText(additionalTextInput.getText().toString().trim());

        Log.d(TAG, "Submitting report: " +
                "\nContent ID: " + reportDTO.getReportedContentId() +
                "\nContent Type: " + reportDTO.getReportedContentType() +
                "\nReported User ID: " + reportDTO.getReportedUserId() +
                "\nReason: " + reportDTO.getReason() +
                "\nAdditional Text: " + reportDTO.getAdditionalText());

        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.submitReport(reportDTO, currentUserId).enqueue(new Callback<ReportResponse>() {
            @Override
            public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {
                if (getContext() == null) {
                    Log.e(TAG, "Context is null in onResponse");
                    return;
                }

                if (response.isSuccessful()) {
                    Log.d(TAG, "Report submitted successfully");
                    Toast.makeText(getContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Failed to submit report: " + errorBody);
                        Toast.makeText(getContext(),
                                "Failed to submit report: " + response.message(),
                                Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReportResponse> call, Throwable t) {
                Log.e(TAG, "Network error submitting report", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setListener(ReportSubmitListener listener) {
        this.listener = listener;
    }
}
