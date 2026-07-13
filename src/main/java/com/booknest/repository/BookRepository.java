package com.booknest.repository;

import com.booknest.config.JPAConfig;
import com.booknest.model.dto.TopBookDTO;
import com.booknest.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class BookRepository {
    //Tìm sách theo mã ISBN (Sử dụng Named Query)
    public Book findByIsbn(String isbn) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            List<Book> results = em.createNamedQuery("Book.findByIsbn", Book.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }
    //Tìm kiếm sách theo từ khóa trong tiêu đề
    public List<Book> findByTitleLike(String keyword, int page, int size) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            TypedQuery<Book> query = em.createQuery(
                    "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:keyword)", Book.class);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult((page - 1) * size); // Vị trí bắt đầu
            query.setMaxResults(size);              // Số lượng bản ghi lấy ra
            return query.getResultList();
        }
    }
    //Liệt kê sách theo danh mục
    public List<Book> findByCategoryId(Long categoryId) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createNamedQuery("Book.findByCategory", Book.class)
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        }
    }
    //Liệt kê sách theo tác giả (Sử dụng JOIN)
    public List<Book> findByAuthorName(String authorName) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createQuery(
                            "SELECT b FROM Book b JOIN b.authors a WHERE LOWER(a.fullName) LIKE LOWER(:name)", Book.class)
                    .setParameter("name", "%" + authorName + "%")
                    .getResultList();
        }
    }
    //Thống kê Sách được mượn nhiều nhất
    public List<TopBookDTO> getTopBorrowedBooks(int topN) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            return em.createQuery(
                            "SELECT new com.booknest.dto.TopBookDTO(b.id, b.title, SUM(li.quantity)) " +
                                    "FROM LoanItem li JOIN li.book b " +
                                    "GROUP BY b.id, b.title " +
                                    "ORDER BY SUM(li.quantity) DESC", TopBookDTO.class)
                    .setMaxResults(topN)
                    .getResultList();
        }
    }
    //Tìm kiếm ĐỘNG bằng Criteria API
    public List<Book> searchBooksDynamic(String keyword, String categoryName, String authorName, Boolean isAvailable) {
        try (EntityManager em = JPAConfig.getEntityManager()) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Book> cq = cb.createQuery(Book.class);
            Root<Book> book = cq.from(Book.class);
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(book.get("title")), "%" + keyword.toLowerCase() + "%"));
            }
            if (categoryName != null && !categoryName.isBlank()) {
                Join<Book, Category> category = book.join("category");
                predicates.add(cb.equal(cb.lower(category.get("name")), categoryName.toLowerCase()));
            }
            if (authorName != null && !authorName.isBlank()) {
                Join<Book, Author> author = book.join("authors");
                predicates.add(cb.like(cb.lower(author.get("fullName")), "%" + authorName.toLowerCase() + "%"));
            }
            // Bộ lọc tình trạng còn sách
            if (isAvailable != null) {
                if (isAvailable) {
                    predicates.add(cb.greaterThan(book.get("availableCopies"), 0));
                } else {
                    predicates.add(cb.equal(book.get("availableCopies"), 0));
                }
            }
            cq.where(predicates.toArray(new Predicate[0]));
            return em.createQuery(cq).getResultList();
        }
    }
}