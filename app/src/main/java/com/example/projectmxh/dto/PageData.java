package com.example.projectmxh.dto;

import java.util.List;

public class PageData<T> {
    private List<T> data;
    private Integer totalPages;
    private Long totalElements;
    private Boolean hasNext;

    public PageData(List<T> data, Integer totalPages, Long totalElements, Boolean hasNext) {
        this.data = data;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
    }

    // Getters
    public List<T> getData() {
        return data;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    // Setters
    public void setData(List<T> data) {
        this.data = data;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }
}