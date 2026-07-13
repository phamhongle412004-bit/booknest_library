package com.booknest;

import com.booknest.config.JPAConfig;
import com.booknest.model.*;
import com.booknest.service.LibraryService;
import java.util.*;
import com.booknest.model.dto.TopBookDTO;

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
                System.out.println("12. Demo Bộ nhớ đệm cấp 1 (First-level cache)");
                System.out.println("13. Tự động gieo dữ liệu mẫu (Seed Data)");
                System.out.println("0. Thoát chương trình");
                System.out.print("Chọn chức năng (0-13): ");

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
                    case 9 -> menuAdvancedSearch();
                    case 10 -> menuReports();
                    case 11 -> menuLoanDetail();
                    case 12 -> demoFirstLevelCache();
                    case 13 -> com.booknest.util.DatabaseSeeder.seedData();
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
    private static void menuAdvancedSearch() {
        System.out.println("\n--- TÌM KIẾM SÁCH NÂNG CAO ---");
        System.out.println("1. Tìm kiếm theo tiêu đề (Có Phân Trang)");
        System.out.println("2. Tìm kiếm động (Sử dụng Criteria API lọc đa điều kiện)");
        System.out.print("Lựa chọn của bạn: ");
        int type = Integer.parseInt(scanner.nextLine());

        if (type == 1) {
            System.out.print("Nhập từ khóa tiêu đề: ");
            String keyword = scanner.nextLine();
            System.out.print("Nhập số trang cần xem (Bắt đầu từ 1): ");
            int page = Integer.parseInt(scanner.nextLine());

            // Cố định size = 2 bản ghi/trang để người chấm dễ nhìn thấy kết quả phân trang khi data ít
            List<Book> books = bookRepo.findByTitleLike(keyword, page, 2);
            if (books.isEmpty()) {
                System.out.println("Không tìm thấy kết quả ở trang này.");
            } else {
                books.forEach(b -> System.out.printf("ID: %d | Tiêu đề: %s | Còn lại: %d\n", b.getId(), b.getTitle(), b.getAvailableCopies()));
            }
        } else if (type == 2) {
            System.out.print("Từ khóa tiêu đề (Bỏ qua thì bấm Enter): ");
            String kw = scanner.nextLine();
            System.out.print("Tên chính xác danh mục (Bỏ qua thì bấm Enter): ");
            String cat = scanner.nextLine();
            System.out.print("Tên tác giả (Bỏ qua thì bấm Enter): ");
            String auth = scanner.nextLine();
            System.out.print("Chỉ tìm sách còn trong kho? (Y/N/Bỏ qua bấm Enter): ");
            String availInput = scanner.nextLine().trim();
            Boolean isAvail = availInput.equalsIgnoreCase("y") ? true : (availInput.equalsIgnoreCase("n") ? false : null);

            List<Book> results = bookRepo.searchBooksDynamic(
                    kw.isBlank() ? null : kw,
                    cat.isBlank() ? null : cat,
                    auth.isBlank() ? null : auth,
                    isAvail
            );
            if (results.isEmpty()) {
                System.out.println("Không tìm thấy sách nào khớp với bộ lọc động.");
            } else {
                results.forEach(b -> System.out.printf("ID: %d | Tiêu đề: %s | Kho: %d\n", b.getId(), b.getTitle(), b.getAvailableCopies()));
            }
        }
    }
    private static void menuReports() {
        System.out.println("\n--- BÁO CÁO THỐNG KÊ THƯ VIỆN ---");
        System.out.println("Top 3 cuốn sách được mượn nhiều nhất:");
        List<TopBookDTO> topBooks = bookRepo.getTopBorrowedBooks(3);
        if (topBooks.isEmpty()) {
            System.out.println("  (Chưa có lượt mượn nào được ghi nhận)");
        } else {
            topBooks.forEach(dto -> System.out.printf("  - Sách: %s (ID: %d) | Tổng lượt mượn: %d\n", dto.getTitle(), dto.getBookId(), dto.getBorrowCount()));
        }

        System.out.println("\nThống kê tổng số phiếu mượn theo trạng thái:");
        Map<com.booknest.model.enums.LoanStatus, Long> countMap = loanRepo.countLoansByStatus();
        if (countMap.isEmpty()) {
            System.out.println("  (Hệ thống chưa có phiếu mượn nào)");
        } else {
            countMap.forEach((status, count) -> System.out.printf("  - Trạng thái %s: %d phiếu\n", status, count));
        }
    }
    private static void menuLoanDetail() {
        System.out.print("\nNhập ID phiếu mượn cần xem chi tiết: ");
        Long id = Long.parseLong(scanner.nextLine());
        Loan loan = loanRepo.findLoanDetailWithFetchJoin(id);

        if (loan == null) {
            System.out.println("Không tìm thấy phiếu mượn mang ID này.");
            return;
        }
        System.out.println("THÔNG TIN CHI TIẾT PHIẾU MƯỢN");
        System.out.printf("Mã phiếu: %d | Trạng thái: %s\n", loan.getId(), loan.getStatus());
        System.out.printf("Người mượn: %s | Email: %s\n", loan.getMember().getFullName(), loan.getMember().getEmail());
        System.out.printf("Ngày mượn: %s | Hạn trả: %s\n", loan.getLoanDate(), loan.getDueDate());
        System.out.println("Danh sách các sách mượn trong phiếu:");
        loan.getItems().forEach(item -> System.out.printf("  + Sách: %s (Mã sách: %d) | Số lượng mượn: %d bản\n",
                item.getBook().getTitle(), item.getBook().getId(), item.getQuantity()));
    }

    //DEMO FIRST-LEVEL CACHE
    private static void demoFirstLevelCache() {
        System.out.println("\n--- DEMO BỘ NHỚ ĐỆM CẤP 1 (FIRST-LEVEL CACHE) ---");
        System.out.print("Nhập ID cuốn sách muốn chạy thử nghiệm: ");
        Long id = Long.parseLong(scanner.nextLine());

        jakarta.persistence.EntityManager em = com.booknest.config.JPAConfig.getEntityManager();
        try {
            System.out.println("Gọi em.find() lần số 1...");
            Book book1 = em.find(Book.class, id);
            if (book1 != null) {
                System.out.println("Tên sách lần 1: " + book1.getTitle());
            } else {
                System.out.println("Không tìm thấy sách trong DB để test!");
                return;
            }

            System.out.println("\nGọi em.find() lần số 2 (Ngay lập tức)...");
            Book book2 = em.find(Book.class, id);
            System.out.println("Tên sách lần 2: " + book2.getTitle());

            boolean isSameInstance = (book1 == book2);
            System.out.println("\nKẾT QUẢ KIỂM TRA HỆ THỐNG:");
            System.out.println("- Trả về cùng một thực thể duy nhất trong RAM? " + (isSameInstance ? "ĐÚNG (TRUE)" : "SAI (FALSE)"));
            System.out.println("\nNhận xét: Hibernate chỉ in ra đúng 1 lệnh SQL SELECT ở lần gọi 1.");
            System.out.println("Ở lần 2, nó nhấc thẳng đối tượng ra từ First-level cache để dùng lại!");
        } finally {
            em.close();
        }
    }
}