package com.booknest.util;

import com.booknest.config.JPAConfig;
import com.booknest.model.*;
import com.booknest.model.enums.BookStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;

public class DatabaseSeeder {
    public static void seedData() {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            //Kiểm tra nếu đã có dữ liệu thì không seed nữa
            Long count = em.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult();
            if (count > 0) {
                System.out.println("⚠️ Cơ sở dữ liệu đã có dữ liệu mẫu từ trước!");
                return;
            }
            // Tạo Danh mục (Category)
            Category catIt = new Category(); catIt.setName("Công nghệ thông tin");
            Category catLit = new Category(); catLit.setName("Văn học");
            em.persist(catIt); em.persist(catLit);

            //Tạo Tác giả (Author)
            Author authBloch = new Author(); authBloch.setFullName("Joshua Bloch");
            Author authNam = new Author(); authNam.setFullName("Nam Cao");
            em.persist(authBloch); em.persist(authNam);

            //Tạo Sách & Đồng bộ quan hệ nhiều-nhiều, nhiều-1
            Book book1 = new Book();
            book1.setIsbn("9780134685991");
            book1.setTitle("Effective Java (3rd Edition)");
            book1.setPublishedYear(2018);
            book1.setAvailableCopies(5);
            book1.setStatus(BookStatus.AVAILABLE);
            catIt.addBook(book1);
            book1.getAuthors().add(authBloch);
            authBloch.getBooks().add(book1);

            Book book2 = new Book();
            book2.setIsbn("9786046923456");
            book2.setTitle("Chí Phèo");
            book2.setPublishedYear(1941);
            book2.setAvailableCopies(2);
            book2.setStatus(BookStatus.AVAILABLE);
            catLit.addBook(book2);
            book2.getAuthors().add(authNam);
            authNam.getBooks().add(book2);

            em.persist(book1);
            em.persist(book2);

            //Tạo Thành viên
            Member member = new Member();
            member.setFullName("Nguyễn Văn A");
            member.setEmail("anguyen@gmail.com");
            member.setPhone("0912345678");

            MemberProfile profile = new MemberProfile();
            profile.setAddress("Cầu Giấy, Hà Nội");
            profile.setJoinedAt(LocalDateTime.now());
            member.setProfile(profile);

            em.persist(member);

            tx.commit();
            System.out.println("Gieo dữ liệu mẫu (Seed Data) thành công vào Database H2!");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("Lỗi khi gieo dữ liệu: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}