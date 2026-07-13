package com.booknest.model;

import com.booknest.model.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@NamedQueries({
        @NamedQuery(
                name = "Loan.findOverdue",
                query = "SELECT l FROM Loan l WHERE l.status = com.booknest.model.enums.LoanStatus.ACTIVE AND l.dueDate < :currentDate"
        ),
        @NamedQuery(
                name = "Loan.countByStatus",
                query = "SELECT l.status, COUNT(l) FROM Loan l GROUP BY l.status"
        )
})
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Ngày mượn không được để trống")
    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @NotNull(message = "Hạn trả không được để trống")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @NotNull(message = "Trạng thái phiếu mượn không được trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanItem> items = new ArrayList<>();

    public void addLoanItem(LoanItem item) {
        this.items.add(item);
        item.setLoan(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDate returnedAt) { this.returnedAt = returnedAt; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public List<LoanItem> getItems() { return items; }
    public void setItems(List<LoanItem> items) { this.items = items; }
}