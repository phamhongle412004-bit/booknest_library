package com.booknest.model;

import com.booknest.model.enums.BookStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@NamedQueries({
        @NamedQuery(
                name = "Book.findByIsbn",
                query = "SELECT b FROM Book b WHERE b.isbn = :isbn"
        ),
        @NamedQuery(
                name = "Book.findByCategory",
                query = "SELECT b FROM Book b WHERE b.category.id = :categoryId"
        )
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "ISBN không được để trống")
    @Size(min = 10, max = 13, message = "ISBN phải từ 10 đến 13 ký tự")
    @Column(name = "isbn", unique = true, nullable = false, length = 13)
    private String isbn;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Column(name = "title", nullable = false)
    private String title;

    @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
    @Column(name = "published_year")
    private int publishedYear;

    @PositiveOrZero(message = "Số lượng sách có sẵn không được âm")
    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @NotNull(message = "Trạng thái sách không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();
    public void addAuthor(Author author) {
        this.authors.add(author);
        author.getBooks().add(this);
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPublishedYear() { return publishedYear; }
    public void setPublishedYear(int publishedYear) { this.publishedYear = publishedYear; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors; }
}