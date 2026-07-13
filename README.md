# Dự án BookNest - Hệ thống Quản lý Thư viện Học tập (JPA Thuần)

## 1. Mô tả dự án
BookNest là một ứng dụng quản lý thư viện tinh gọn được xây dựng trên nền tảng **Java Core phối hợp cùng JPA/Hibernate thuần**, hoàn toàn không sử dụng framework Spring Boot hay Spring Data JPA. Hệ thống quản lý toàn diện luồng nghiệp vụ từ Danh mục, Sách, Tác giả, Thành viên, cho tới các quy trình mượn trả sách mang tính toàn vẹn dữ liệu (Transactional).
- **Link Git:** https://github.com/phamhongle412004-bit/booknest_library
## 2. Tech Stack (Công nghệ sử dụng)
- **Ngôn ngữ:** Java 17+
- **JPA Provider:** Hibernate ORM 6.x (Jakarta Persistence 3.1)
- **Cơ sở dữ liệu:** H2 Database Engine (Cấu hình dạng file nhúng cục bộ)
- **Xác thực dữ liệu:** Hibernate Validator 8.x (Bean Validation 3.0)
- **Hệ thống quản lý cấu trúc:** Maven

---

## 3. Hướng dẫn các câu lệnh Cài đặt & Khởi chạy

### Cấu hình Cơ sở dữ liệu (`src/main/resources/META-INF/persistence.xml`)
Cơ sở dữ liệu H2 được cấu hình tự động tạo bảng thông qua thuộc tính:
```xml
<property name="jakarta.persistence.schema-generation.database.action" value="drop-and-create"/>
```
Đường dẫn lưu file cơ sở dữ liệu sẽ tự động tạo một thư mục cục bộ ngay trong lòng dự án tại:
`jdbc:h2:./data/booknestdb`

### Các câu lệnh khởi chạy bằng Terminal/CMD:
1. Dọn dẹp và Biên dịch dự án:
```bash
mvn clean compile
```

2. Khởi chạy ứng dụng Console Menu:
```bash
mvn exec:java -Dexec.mainClass="com.booknest.App"
```

## 4. Giải thích Sơ đồ & Mối quan hệ giữa các Thực thể (Entities)
### Hệ thống thiết lập các mối quan hệ
- Member (1-1) MemberProfile: Sử dụng @OneToOne. Member giữ vai trò sở hữu (owner). Bảng member_profiles chứa khóa ngoại member_id (được cấu hình qua @JoinColumn).
- Member (1-N) Loan: Một thành viên có thể lập nhiều phiếu mượn. Đầu Loan chứa @ManyToOne với cột khóa ngoại member_id.
- Loan (1-N) LoanItem: Phiếu mượn chứa danh sách các dòng chi tiết sách. Mối quan hệ bidirectional sử dụng thuộc tính mappedBy = "loan" ở thực thể cha để đồng bộ trạng thái.
- LoanItem (N-1) Book: Nhiều dòng chi tiết mượn có thể trỏ chung về một cuốn sách trong kho thông qua khóa ngoại book_id.
- Book (N-1) Category: Nhiều cuốn sách thuộc về cùng một danh mục sách (category_id).
- Book (N-N) Author: Một cuốn sách có thể do nhiều tác giả viết và ngược lại. Hibernate tự động tạo bảng trung gian mang tên book_author chứa hai cột khóa ngoại book_id và author_id bằng nhãn @JoinTable.

### Chiến lược sinh Khóa chính (Primary Key Generation)
Tất cả các thực thể trong hệ thống đều áp dụng chiến lược:

```bash
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
```
- **Lý do lựa chọn:** Tận dụng tối đa tính năng tự động tăng (AUTO_INCREMENT) nguyên thủy của cơ sở dữ liệu nền tảng (H2 Engine). Chiến lược này giúp giảm tải tài nguyên bộ nhớ cho ứng dụng so với việc quản lý vùng đệm của SEQUENCE hay TABLE, đồng thời bảo toàn tính tuần tự tuyệt đối của ID bản ghi trên môi trường lưu trữ cục bộ.

## 5. Ghi chú Phân tích Tối ưu hóa Hiệu năng
### Giải quyết triệt để lỗi truy vấn N+1
- **Kịch bản lỗi:** Khi thủ thư muốn xem thông tin chi tiết của một phiếu mượn (Loan) bao gồm cả dữ liệu người mượn và danh sách sách mượn bên trong. Nếu dùng câu lệnh em.find() thông thường, Hibernate sẽ thực hiện 1 câu lệnh lấy phiếu mượn và liên tiếp N câu lệnh đơn lẻ phía sau để tải thông tin của các thực thể liên kết, gây nghẽn cổ chai hệ thống.
- **Giải pháp:** Tại lớp LoanRepository.java, hàm findLoanDetailWithFetchJoin đã sử dụng kỹ thuật JOIN FETCH:
```bash
SELECT l FROM Loan l
JOIN FETCH l.member m
JOIN FETCH l.items i
JOIN FETCH i.book b
WHERE l.id = :loanId
```
- **Hiệu quả:** Cơ chế này ép buộc Hibernate gom tất cả các bảng liên quan vào đúng 1 câu lệnh SQL duy nhất sử dụng phép INNER JOIN dưới DB, tối ưu hóa triệt để số lượng kết nối dữ liệu.

### Chứng minh bộ nhớ đệm cấp 1 (First-level cache)
Hệ thống cung cấp chức năng số 12 trên Menu Console để thực hiện kiểm chứng thực tế: Khi gọi hàm em.find() hai lần liên tiếp cho cùng một thực thể Book mang cùng một ID trong một luồng Persistence Context, Hibernate chỉ in câu lệnh SELECT ở lần đầu tiên. Lần gọi thứ hai trả về chính xác cùng một instance được lưu trữ trong RAM, chứng minh bộ nhớ đệm cấp 1 hoạt động tức thì, ngăn chặn truy vấn trùng lặp xuống đĩa cứng.

## 6. Bảng đối chiếu Hoàn thành Tính năng (Feature Checklist)

| Nhiệm vụ | Yêu cầu chi tiết của đề bài | Trạng thái | Vị trí cài đặt / Minh chứng cụ thể |
| :---: | :--- |:----------:| :--- |
| **Nhiệm vụ 1** | Định nghĩa cấu trúc 7 Entity JPA kèm các thuộc tính, khóa chính, và Enums. | Hoàn thành | Toàn bộ các Class nằm trong package `com.booknest.model` |
| **Nhiệm vụ 2** | Cấu hình quan hệ hai chiều, thuộc tính `mappedBy`, tránh vòng lặp StackOverflow. | Hoàn thành | Khai báo quan hệ thực thể; Override `toString()` loại bỏ các trường liên kết đối ứng |
| **Nhiệm vụ 2** | Áp dụng Bean Validation (`@NotNull`, `@Size`...) và bộ kiểm tra lỗi tập trung. | Hoàn thành | Các annotation tại thuộc tính Entity và bộ xử lý tại `com.booknest.util.ValidationUtil` |
| **Nhiệm vụ 3** | Luồng nghiệp vụ Transactional cho Catalog, Sách, Đăng ký thành viên. | Hoàn thành | `com.booknest.service.LibraryService` |
| **Nhiệm vụ 3** | Nghiệp vụ mượn/trả sách phức tạp có kiểm tra kho dữ liệu và xử lý hủy giao dịch (`tx.rollback()`). | Hoàn thành | Các hàm `borrowBooks()` và `returnBooks()` trong `LibraryService` |
| **Nhiệm vụ 3** | Định nghĩa tối thiểu 5 lớp Custom Exception kế thừa từ `RuntimeException`. | Hoàn thành | Hệ thống lỗi tự định nghĩa tại package `com.booknest.exception` |
| **Nhiệm vụ 4** | Định nghĩa và thực thi ít nhất 2 nhãn `@NamedQuery` trong các lớp Entity. | Hoàn thành | Khai báo tại đầu lớp `Book.java` và `Loan.java` |
| **Nhiệm vụ 4** | Áp dụng phân trang (Pagination) cho chức năng hiển thị/tìm kiếm danh sách sách. | Hoàn thành | Hàm `findByTitleLike()` trong `BookRepository` và Chức năng số 9 trên CLI |
| **Nhiệm vụ 4** | Triển khai công nghệ tìm kiếm động đa điều kiện tùy chọn bằng Criteria API. | Hoàn thành | Hàm `searchBooksDynamic()` trong lớp `BookRepository` |
| **Nhiệm vụ 4** | Sử dụng cấu trúc DTO Projections cho báo cáo thống kê chỉ đọc (Read-only). | Hoàn thành | Khai báo lớp `com.booknest.dto.TopBookDTO` |
| **Nhiệm vụ 5** | Chiến lược nạp dữ liệu `LAZY`, giải trình lỗi N+1, minh chứng First-level cache. | Hoàn thành | Chức năng số 11, 12 trên CLI và mục 5 trong tài liệu `README.md` |
| **Nhiệm vụ 6** | Cung cấp tài liệu hoàn chỉnh, tệp dữ liệu mẫu khởi tạo nhanh hệ thống (Seed Data). | Hoàn thành | Lớp `com.booknest.util.DatabaseSeeder` và Chức năng số 13 trên CLI |