package com.booknest.repository;

import com.booknest.config.JPAConfig;
import com.booknest.model.Loan;
import com.booknest.model.enums.LoanStatus;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;

public class LoanRepository {

    //Liệt kê các phiếu mượn đang hoạt động dựa theo email thành viên
    public List<Loan> findActiveLoansByEmail(String email) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createQuery(
                            "SELECT l FROM Loan l WHERE l.member.email = :email AND l.status = :status", Loan.class)
                    .setParameter("email", email)
                    .setParameter("status", LoanStatus.ACTIVE)
                    .getResultList();
        }
    }

    //Liệt kê các phiếu mượn quá hạn
    public List<Loan> findOverdueLoans() {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createNamedQuery("Loan.findOverdue", Loan.class)
                    .setParameter("currentDate", LocalDate.now())
                    .getResultList();
        }
    }

    //Xem chi tiết tối ưu phiếu mượn
    public Loan findLoanDetailWithFetchJoin(Long loanId) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            List<Loan> results = em.createQuery(
                            "SELECT l FROM Loan l " +
                                    "JOIN FETCH l.member m " +
                                    "JOIN FETCH l.items i " +
                                    "JOIN FETCH i.book b " +
                                    "WHERE l.id = :loanId", Loan.class)
                    .setParameter("loanId", loanId)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }

    //Đếm số lượng phiếu mượn theo từng trạng thái
    public Map<LoanStatus, Long> countLoansByStatus() {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            List<Object[]> results = em.createNamedQuery("Loan.countByStatus", Object[].class).getResultList();
            Map<LoanStatus, Long> statusMap = new HashMap<>();
            for (Object[] row : results) {
                statusMap.put((LoanStatus) row[0], (Long) row[1]);
            }
            return statusMap;
        }
    }
}