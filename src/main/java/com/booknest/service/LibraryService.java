package com.booknest.service;

import com.booknest.config.JPAConfig;
import com.booknest.model.*;
import com.booknest.model.enums.BookStatus;
import com.booknest.model.enums.LoanStatus;
import com.booknest.exception.*;
import com.booknest.util.ValidationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LibraryService {
    public void createCategory(String name) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Category category = new Category();
            category.setName(name);
            ValidationUtil.validate(category);
            em.persist(category);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new DataAccessException("Lỗi khi tạo danh mục: " + e.getMessage(), e);
        } finally { em.close(); }
    }

    public List<Category> getAllCategories() {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
        }
    }
    public void createBook(String isbn, String title, int year, int copies, Long categoryId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Category category = em.find(Category.class, categoryId);
            if (category == null) throw new InvalidInputException("Không tìm thấy danh mục ID: " + categoryId);

            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setPublishedYear(year);
            book.setAvailableCopies(copies);
            book.setStatus(copies > 0 ? BookStatus.AVAILABLE : BookStatus.DAMAGED);

            category.addBook(book); // Đồng bộ 2 đầu mối quan hệ

            ValidationUtil.validate(book);
            em.persist(book);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new DataAccessException("Lỗi khi tạo sách: " + e.getMessage(), e);
        } finally { em.close(); }
    }

    public List<Book> getAllBooks() {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createQuery("SELECT b FROM Book b", Book.class).getResultList();
        }
    }

    public void registerMember(String fullName, String email, String phone, String address) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Member member = new Member();
            member.setFullName(fullName);
            member.setEmail(email);
            member.setPhone(phone);

            MemberProfile profile = new MemberProfile();
            profile.setAddress(address);
            profile.setJoinedAt(java.time.LocalDateTime.now());

            member.setProfile(profile); // Đồng bộ 1-1

            ValidationUtil.validate(member);
            em.persist(member);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new DataAccessException("Lỗi đăng ký thành viên: " + e.getMessage(), e);
        } finally { em.close(); }
    }

    public List<Member> getAllMembers() {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createQuery("SELECT m FROM Member m", Member.class).getResultList();
        }
    }

    public void borrowBooks(Long memberId, Map<Long, Integer> bookOrder) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Member member = em.find(Member.class, memberId);
            if (member == null) throw new MemberNotFoundException("Không tìm thấy thành viên ID: " + memberId);
            if (bookOrder == null || bookOrder.isEmpty()) throw new InvalidInputException("Phiếu mượn phải có ít nhất 1 cuốn sách!");

            Loan loan = new Loan();
            loan.setLoanDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusDays(14)); // Hạn mượn 14 ngày
            loan.setStatus(LoanStatus.ACTIVE);
            member.addLoan(loan);

            for (Map.Entry<Long, Integer> entry : bookOrder.entrySet()) {
                Long bookId = entry.getKey();
                int qty = entry.getValue();

                if (qty <= 0) throw new InvalidInputException("Số lượng mượn phải lớn hơn 0!");

                Book book = em.find(Book.class, bookId);
                if (book == null) throw new BookNotFoundException("Không tìm thấy sách ID: " + bookId);

                if (book.getAvailableCopies() < qty) {
                    throw new InsufficientCopiesException("Sách '" + book.getTitle() + "' không đủ trong kho! (Còn lại: " + book.getAvailableCopies() + ")");
                }

                // Trừ kho sách
                book.setAvailableCopies(book.getAvailableCopies() - qty);
                if (book.getAvailableCopies() == 0) {
                    book.setStatus(BookStatus.ON_LOAN);
                }

                LoanItem item = new LoanItem();
                item.setBook(book);
                item.setQuantity(qty);
                loan.addLoanItem(item);
            }

            ValidationUtil.validate(loan);
            em.persist(loan);
            tx.commit();
            System.out.println("Cho mượn sách thành công! Mã phiếu mượn ID: " + loan.getId());
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback(); // Rollback ngay lập tức nếu bất kỳ item nào lỗi
            throw e;
        } finally { em.close(); }
    }

    public void returnBooks(Long loanId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Loan loan = em.find(Loan.class, loanId);
            if (loan == null) throw new InvalidInputException("Không tìm thấy phiếu mượn ID: " + loanId);
            if (loan.getStatus() == LoanStatus.RETURNED) throw new InvalidInputException("Phiếu mượn này đã được trả từ trước!");

            loan.setReturnedAt(LocalDate.now());
            loan.setStatus(LoanStatus.RETURNED);

            for (LoanItem item : loan.getItems()) {
                Book book = item.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + item.getQuantity());
                book.setStatus(BookStatus.AVAILABLE);
            }

            tx.commit();
            System.out.println("Đã nhận trả sách thành công cho phiếu mượn ID: " + loanId);
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }
}