package com.booknest.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "loan_items")
public class LoanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Positive(message = "Số lượng sách mượn phải lớn hơn 0")
    @Column(name = "quantity", nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Loan getLoan() { return loan; }
    public void setLoan(Loan loan) { this.loan = loan; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}