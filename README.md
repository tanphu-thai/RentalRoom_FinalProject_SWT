# Hệ Thống Quản Lý Phòng Trọ (Rental Room Management System)

Dự án Demo hoàn chỉnh dành cho đồ án môn học (SWT/Software Testing), được thiết kế dựa trên tài liệu **Đặc Tả Yêu Cầu (SRS)** và **Kế Hoạch Test (Test Plan)**.

## Công nghệ sử dụng
- **Backend:** Java 17, Spring Boot, Spring Data JPA, H2 Database (mặc định), JUnit 5 + Mockito. (Chạy bằng IntelliJ IDEA)
- **Frontend:** React + Vite, Tailwind CSS v4, React Router v6. (Chạy bằng Visual Studio Code)
- **Mục tiêu:** Cung cấp ứng dụng để thực thi các kịch bản kiểm thử giao diện (UI), kiểm thử nghiệp vụ và làm bằng chứng cho các bài Lab.

## Tính năng nổi bật
- **Bảo mật:** Đăng nhập, tự động khóa tài khoản khi nhập sai nhiều lần, quên mật khẩu (Mã OTP hết hạn sau 5 phút).
- **Quản lý Phòng:** Xem danh sách, tìm kiếm, lọc, thêm/sửa/xóa phòng, xác thực trùng mã phòng, kiểm tra diện tích & giá tiền.
- **Quản lý Khách thuê:** Xác minh CCCD (12 số), chống trùng lặp, xác minh định dạng số điện thoại.
- **Hợp đồng Thuê phòng:** Chỉ cho thuê phòng trống, tiền cọc & số điện nước không được âm, tự động cập nhật trạng thái phòng, thanh lý hợp đồng.
- **Hóa đơn & Thanh toán:** Bắt lỗi chỉ số điện nước (không được nhỏ hơn tháng trước), tính toán tổng tiền, cho phép sửa hóa đơn chưa thanh toán, hủy/thanh toán hóa đơn.
- **Cổng thông tin Khách thuê (Customer Portal):** Khách thuê có thể đăng nhập để xem thông tin hợp đồng và lịch sử hóa đơn của chính mình. (Hỗ trợ Dark Mode & Giao diện hiện đại)
- **Báo cáo doanh thu:** Thống kê doanh thu theo tháng/năm.

## Hướng dẫn cài đặt và chạy thử

### 1) Chạy Backend (IntelliJ IDEA)
1. Mở thư mục `backend` trong IntelliJ IDEA dưới dạng Maven project.
2. Cài đặt **JDK 17+**.
3. Chờ Maven tải các thư viện.
4. Chạy file `com.rrms.RrmsApplication`.
5. Backend sẽ hoạt động tại: `http://localhost:8080`.
6. Truy cập H2 Database (Nếu cần): `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/rrms;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`
   - Username: `sa` (Mật khẩu để trống)

### 2) Chạy Frontend (Visual Studio Code)
1. Mở terminal tại thư mục `frontend` và chạy:
   ```bash
   npm install
   npm run dev
   ```
2. Mở trình duyệt tại đường dẫn: `http://localhost:5173`.

## Tài khoản Demo có sẵn

| Vai trò | Tên đăng nhập | Mật khẩu | Email |
|---|---|---|---|
| **Admin** | `admin` | `Admin@123` | `admin@rrms.local` |
| **Khách thuê** | `tenant1` | `Tenant@123` | `tenant1@rrms.local` |

**Dữ liệu mẫu (Seed Data):**
- **R101**: Đang trống (Có thể dùng để tạo hợp đồng mới).
- **R102**: Đang bảo trì.
- **R201**: Đã có người thuê (Đã có sẵn hợp đồng và hóa đơn).
- Số CCCD khách thuê đã tồn tại: `079123456789`.

## Hướng dẫn Test (Kiểm thử)
Đối với Lab 1, hãy chạy trang web, thực hiện từng bước trong file kịch bản Excel của bạn. Sau đó cập nhật các cột **Result** (Kết quả), **Test Date** (Ngày Test), và **Test Report**.
Để chạy Unit Test Backend, gõ lệnh:
```bash
mvn test
```

> **⚠️ LƯU Ý QUAN TRỌNG:** Ở chức năng *Quên mật khẩu*, mã OTP được hiển thị trực tiếp trên giao diện. Đây **chỉ là tính năng để phục vụ mục đích Demo và chấm điểm đồ án**. Trong thực tế, không bao giờ được để lộ mã OTP ra ngoài!
