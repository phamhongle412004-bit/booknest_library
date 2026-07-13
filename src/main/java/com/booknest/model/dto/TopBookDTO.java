package com.booknest.model.dto;

public class TopBookDTO {
    private Long bookId;
    private String title;
    private Long borrowCount;

    public TopBookDTO(Long bookId, String title, Long borrowCount) {
        this.bookId = bookId;
        this.title = title;
        this.borrowCount = borrowCount;
    }

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public Long getBorrowCount() { return borrowCount; }
}