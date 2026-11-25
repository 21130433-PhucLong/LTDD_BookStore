package com.example.bookstore.data;

import com.example.bookstore.model.Book;

import java.util.ArrayList;
import java.util.List;

public class SeedData {

    // Thể loại có id lần lượt 1, 2, 3, 4, 5 (theo thứ tự trong mảng)
    public static final String[] CATEGORY_NAMES = {
            "Văn học Việt Nam",
            "Văn học nước ngoài",
            "Kỹ năng sống",
            "Truyện tranh – Manga",
            "Ngoại ngữ"
    };

    public static List<Book> getBooks() {
        List<Book> list = new ArrayList<>();

        // Tham số: tiêu đề, tác giả, nhà xuất bản, giá bán, giá gốc, tồn kho, đã bán, sao, mô tả, ảnh, id thể loại
        list.add(book("Dế Mèn phiêu lưu ký", "Tô Hoài", "NXB Kim Đồng", 68000, 85000, 40, 320, 4.8f,
                "Cuộc phiêu lưu của chú dế mèn qua nhiều vùng đất, gặp gỡ nhiều bạn bè và rút ra những bài học về lòng dũng cảm, tình bạn.",
                "book_01", 1));
        list.add(book("Đắc nhân tâm", "Dale Carnegie", "NXB Tổng hợp TP.HCM", 86000, 108000, 60, 800, 4.8f,
                "Cuốn sách kinh điển về nghệ thuật giao tiếp, cách tạo thiện cảm và gây ảnh hưởng tích cực tới người khác.",
                "book_02", 3));
        list.add(book("Tôi thấy hoa vàng trên cỏ xanh", "Nguyễn Nhật Ánh", "NXB Trẻ", 89000, 110000, 35, 520, 4.8f,
                "Câu chuyện về tuổi thơ ở một miền quê nghèo, kể qua đôi mắt cậu bé Thiều với tình anh em, tình bạn và những nỗi buồn đầu đời.",
                "book_03", 1));
        list.add(book("Doraemon Tiểu thuyết: Nobita và Bản giao hưởng địa cầu", "Fujiko F. Fujio", "NXB Kim Đồng",
                55000, 55000, 28, 190, 4.6f,
                "Tiểu thuyết chuyển thể từ bộ phim Doraemon, xoay quanh chuyến phiêu lưu của Nobita và các bạn với chủ đề âm nhạc.",
                "book_04", 4));
        // Tồn kho = 0 để thử giao diện "Hết hàng"
        list.add(book("Thanh Gươm Diệt Quỷ – The Movie: Chuyến tàu vô tận", "Koyoharu Gotouge", "NXB Kim Đồng",
                69000, 69000, 0, 430, 4.9f,
                "Tiểu thuyết chuyển thể từ phim điện ảnh, kể về Tanjiro và những người bạn trên một chuyến tàu bí ẩn, nơi họ phải đối đầu với con quỷ nguy hiểm.",
                "book_05", 4));
        list.add(book("Những cuộc phiêu lưu của Sherlock Holmes", "Arthur Conan Doyle", "NXB Văn học",
                129000, 165000, 22, 260, 4.7f,
                "Tuyển tập các vụ án nổi tiếng của thám tử Sherlock Holmes, được kể qua lời của bác sĩ Watson.",
                "book_06", 2));
        list.add(book("Ông già và biển cả", "Ernest Hemingway", "NXB Văn học", 55000, 70000, 33, 190, 4.6f,
                "Cuộc chiến bền bỉ của ông lão đánh cá Santiago với con cá kiếm khổng lồ giữa biển khơi.",
                "book_07", 2));
        list.add(book("Tư duy mở", "Nguyễn Anh Dũng", "SBooks – NXB Dân Trí", 79000, 99000, 25, 150, 4.5f,
                "Cuốn sách bàn về cách mở rộng góc nhìn, biết đặt câu hỏi và rèn luyện tư duy cởi mở trong học tập và công việc.",
                "book_08", 3));
        list.add(book("One Piece – Tập 76: Tiến lên đi, đừng ngại", "Eiichiro Oda", "NXB Kim Đồng",
                25000, 25000, 60, 600, 4.9f,
                "Tập 76 của bộ truyện tranh One Piece, tiếp tục hành trình phiêu lưu của Luffy và băng Mũ Rơm.",
                "book_09", 4));
        list.add(book("Bên nhau trọn đời", "Cố Mạn", "NXB Văn học", 110000, 139000, 18, 280, 4.6f,
                "Tiểu thuyết ngôn tình của Cố Mạn, kể về tình yêu và những gắn bó trong gia đình.",
                "book_10", 2));
        list.add(book("Tự học tiếng Anh trong 24 ngày", "Nguyễn Đại", "NXB Hồng Đức", 69000, 89000, 45, 380, 4.5f,
                "Cẩm nang cho người mới bắt đầu, gồm từ vựng thông dụng, mẫu câu đơn giản, phiên âm dễ đọc và các tình huống giao tiếp hằng ngày.",
                "book_11", 5));
        list.add(book("Mindmap Ngữ pháp tiếng Hàn", "TS. Lê Thị Thu Giang, Im Seung Seon", "MCBooks – NXB Hồng Đức",
                129000, 159000, 20, 140, 4.6f,
                "Hệ thống ngữ pháp tiếng Hàn bằng sơ đồ tư duy, giúp người học nắm cấu trúc và cách kết hợp nhanh, dễ nhớ.",
                "book_12", 5));
        list.add(book("Naruto – Quyển 66: Thế trận 3 chân mới", "Masashi Kishimoto", "NXB Kim Đồng",
                25000, 25000, 50, 450, 4.8f,
                "Quyển 66 của bộ truyện tranh Naruto, tiếp tục hành trình của Naruto cùng Sasuke và Sakura.",
                "book_13", 4));
        list.add(book("Thám tử lừng danh Conan – Hoạt hình màu: Tàu ngầm sắt màu đen (Tập 1)", "Gosho Aoyama", "NXB Kim Đồng",
                59000, 59000, 30, 330, 4.7f,
                "Truyện tranh hoạt hình màu chuyển thể từ phim điện ảnh Thám tử lừng danh Conan, với những vụ án và màn đấu trí hồi hộp.",
                "book_14", 4));
        list.add(book("Doraemon Bóng chày – Truyện kí về bóng chày siêu cấp (Tập 10)", "Fujiko F. Fujio, Mugiwara Shintaro", "NXB Kim Đồng",
                30000, 30000, 35, 210, 4.6f,
                "Truyện kí về bóng chày siêu cấp, nơi Doraemon và các bạn tham gia những trận đấu bóng chày vui nhộn.",
                "book_15", 4));
        list.add(book("Bear Town – Thị trấn nhỏ, giấc mơ lớn", "Fredrik Backman", "NXB Trẻ", 129000, 159000, 27, 240, 4.8f,
                "Tiểu thuyết của Fredrik Backman về một thị trấn nhỏ chìm trong tuyết, nơi niềm đam mê khúc côn cầu gắn kết và thử thách cả cộng đồng.",
                "book_16", 2));

        return list;
    }

    // Hàm phụ cho gọn: tạo một Book từ các giá trị
    private static Book book(String title, String author, String publisher, long price, long originalPrice,
                             int stock, int sold, float rating, String description, String image, int categoryId) {
        return new Book(0, title, author, publisher, price, originalPrice, stock, sold, rating,
                description, image, categoryId);
    }
}