package com.example.projectmxh.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectmxh.R;
import com.example.projectmxh.adapter.ReportsAdapter;
import com.example.projectmxh.dto.PageData;
import com.example.projectmxh.dto.response.ReportResponse;
import com.example.projectmxh.service.ApiClient;
import com.example.projectmxh.service.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {
    private RecyclerView reportsRecyclerView;
    private ReportsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView;
    private View errorView;
    private View emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        initViews(view);
        loadReports();
        return view;
    }


    private void initViews(View view) {
        reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        loadingView = view.findViewById(R.id.loadingView);
        errorView = view.findViewById(R.id.errorView);
        emptyView = view.findViewById(R.id.emptyView);

        // Setup RecyclerView
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportsAdapter(this::onReportClick);
        reportsRecyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadReports);

        // Setup retry button
        View retryButton = view.findViewById(R.id.retryButton);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> loadReports());
        }
    }

    private void loadReports() {
        showLoading();
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        apiService.getAllReports(0, 50, "PENDING").enqueue(new Callback<PageData<ReportResponse>>() {
            @Override
            public void onResponse(Call<PageData<ReportResponse>> call, Response<PageData<ReportResponse>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportResponse> reports = response.body().getData(); // Changed from getContent() to getData()
                    if (reports.isEmpty()) {
                        showEmptyState();
                    } else {
                        adapter.setReports(reports);
                        showContent();
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<PageData<ReportResponse>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showError();
            }
        });
    }

    private void onReportClick(ReportResponse report) {
        if ("POST".equals(report.getReportedContentType())) {
            // Navigate to PostFragment
            PostFragment postFragment = PostFragment.newInstance(
                    report.getReportedContentId().toString()
            );

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.admin_content_frame, postFragment)
                    .addToBackStack(null)
                    .commit();
        }
        // Add handling for other content types if needed
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        reportsRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        reportsRecyclerView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void showError() {
        loadingView.setVisibility(View.GONE);
        reportsRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingView.setVisibility(View.GONE);
        reportsRecyclerView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    public void refreshReports() {
        loadReports();
    }
}
