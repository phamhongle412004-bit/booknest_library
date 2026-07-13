package com.booknest;

import com.booknest.config.JPAConfig;
import com.booknest.model.*;
import com.booknest.service.LibraryService;
import java.util.*;

public class App {
    private static final com.booknest.repository.BookRepository bookRepo = new com.booknest.repository.BookRepository();
    private static final com.booknest.repository.LoanRepository loanRepo = new com.booknest.repository.LoanRepository();
    private static final LibraryService service = new LibraryService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            try {
                System.out.println("\n HỆ THỐNG QUẢN LÝ THƯ VIỆN BOOKNEST ");
                System.out.println("1. Tạo danh mục sách mới");
                System.out.println("2. Hiển thị danh sách danh mục");
                System.out.println("3. Thêm sách mới vào thư viện");
                System.out.println("4. Hiển thị danh sách sách");
                System.out.println("5. Đăng ký thành viên mới");
                System.out.println("6. Hiển thị danh sách thành viên");
                System.out.println("7. Lập phiếu cho mượn sách");
                System.out.println("8. Nhận trả sách");
                System.out.println("9. Tìm kiếm sách nâng cao (Phân trang & Criteria)");
                System.out.println("10. Thống kê báo cáo (Top sách mượn & Trạng thái phiếu)");
                System.out.println("11. Xem chi tiết phiếu mượn (Tối ưu Fetch Join)");
                System.out.println("0. Thoát chương trình");
                System.out.print("Chọn chức năng (0-8): ");

                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> menuCreateCategory();
                    case 2 -> menuShowCategories();
                    case 3 -> menuCreateBook();
                    case 4 -> menuShowBooks();
                    case 5 -> menuRegisterMember();
                    case 6 -> menuShowMembers();
                    case 7 -> menuBorrowBooks();
                    case 8 -> menuReturnBooks();
                    case 0 -> {
                        System.out.println("Tạm biệt!");
                        JPAConfig.shutdown();
                        System.exit(0);
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ từ 0-8!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số nguyên hợp lệ!");
            } catch (Exception e) {
                System.out.println("LỖI HỆ THỐNG: " + e.getMessage());
            }
        }
    }

    private static void menuCreateCategory() {
        System.out.print("Nhập tên danh mục: ");
        String name = scanner.nextLine();
        service.createCategory(name);
        System.out.println("Tạo danh mục thành công!");
    }

    private static void menuShowCategories() {
        List<Category> list = service.getAllCategories();
        if (list.isEmpty()) System.out.println("Chưa có danh mục nào.");
        list.forEach(c -> System.out.printf("ID: %d | Tên: %s\n", c.getId(), c.getName()));
    }

    private static void menuCreateBook() {
        System.out.print("Nhập mã ISBN (10-13 số): ");
        String isbn = scanner.nextLine();
        System.out.print("Nhập tiêu đề sách: ");
        String title = scanner.nextLine();
        System.out.print("Nhập năm xuất bản: ");
        int year = Integer.parseInt(scanner.nextLine());
        System.out.print("Nhập số lượng nhập kho: ");
        int copies = Integer.parseInt(scanner.nextLine());
        System.out.print("Nhập ID danh mục của sách: ");
        Long categoryId = Long.parseLong(scanner.nextLine());

        service.createBook(isbn, title, year, copies, categoryId);
        System.out.println("Thêm sách thành công!");
    }

    private static void menuShowBooks() {
        List<Book> list = service.getAllBooks();
        if (list.isEmpty()) System.out.println("Thư viện trống.");
        list.forEach(b -> System.out.printf("ID: %d | Title: %s | ISBN: %s | Kho: %d | Trạng thái: %s\n",
                b.getId(), b.getTitle(), b.getIsbn(), b.getAvailableCopies(), b.getStatus()));
    }

    private static void menuRegisterMember() {
        System.out.print("Nhập họ tên thành viên: ");
        String name = scanner.nextLine();
        System.out.print("Nhập Email: ");
        String email = scanner.nextLine();
        System.out.print("Nhập Số điện thoại: ");
        String phone = scanner.nextLine();
        System.out.print("Nhập Địa chỉ: ");
        String address = scanner.nextLine();

        service.registerMember(name, email, phone, address);
        System.out.println("Đăng ký thành viên thành công!");
    }

    private static void menuShowMembers() {
        List<Member> list = service.getAllMembers();
        if (list.isEmpty()) System.out.println("Chưa có thành viên nào.");
        list.forEach(m -> System.out.printf("ID: %d | Tên: %s | Email: %s | Địa chỉ: %s\n",
                m.getId(), m.getFullName(), m.getEmail(), m.getProfile().getAddress()));
    }

    private static void menuBorrowBooks() {
        System.out.print("Nhập ID thành viên mượn: ");
        Long memberId = Long.parseLong(scanner.nextLine());

        Map<Long, Integer> order = new HashMap<>();
        while (true) {
            System.out.print("Nhập ID cuốn sách muốn mượn: ");
            Long bookId = Long.parseLong(scanner.nextLine());
            System.out.print("Nhập số lượng mượn: ");
            int qty = Integer.parseInt(scanner.nextLine());
            order.put(bookId, qty);

            System.out.print("Bạn có muốn mượn thêm cuốn khác không? (Y/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;
        }

        service.borrowBooks(memberId, order);
    }

    private static void menuReturnBooks() {
        System.out.print("Nhập mã phiếu mượn ID cần trả: ");
        Long loanId = Long.parseLong(scanner.nextLine());
        service.returnBooks(loanId);
    }
}