package com.khazhimetov.library.model;

import java.util.List;

/**
 * Generic pagination wrapper class
 * <p>
 * @param <T> the type of objects in the page
 */
public class Page<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;

    public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public boolean isFirst() {
        return pageNumber == 0;
    }

    public boolean isLast() {
        return pageNumber == totalPages - 1;
    }

    public int getNextPageNumber() {
        return hasNext() ? pageNumber + 1 : pageNumber;
    }

    public int getPreviousPageNumber() {
        return hasPrevious() ? pageNumber - 1 : 0;
    }

    /**
     * Get a range of page numbers to display in pagination controls
     * @param maxDisplay maximum number of page links to display
     * @return array of page numbers
     */
    public int[] getPageRange(int maxDisplay) {
        int start = Math.max(0, pageNumber - maxDisplay / 2);
        int end = Math.min(totalPages - 1, start + maxDisplay - 1);

        // Adjust start if we're near the end
        if (end - start < maxDisplay - 1) {
            start = Math.max(0, end - maxDisplay + 1);
        }

        int[] range = new int[end - start + 1];
        for (int i = 0; i < range.length; i++) {
            range[i] = start + i;
        }
        return range;
    }
}