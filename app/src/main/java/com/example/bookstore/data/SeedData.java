package com.example.bookstore.data;

import com.example.bookstore.model.Book;

import java.util.ArrayList;
import java.util.List;

public class SeedData {

    public static final String[] CATEGORY_NAMES = {
            "Văn học Việt Nam",
            "Văn học nước ngoài",
            "Kỹ năng sống",
            "Khoa học – Lịch sử",
            "Thiếu nhi"
    };

    public static List<Book> getBooks() {
        List<Book> list = new ArrayList<>();

        // Tham số: tiêu đề, tác giả, nhà xuất bản, giá bán, giá gốc, tồn kho, đã bán, sao, mô tả, ảnh, id thể loại
        list.add(book("Dế Mèn phiêu lưu ký", "Tô Hoài", "NXB Kim Đồng", 68000, 85000, 40, 320, 4.8f,
                "Cuộc phiêu lưu của chú dế mèn qua nhiều vùng đất, gặp gỡ nhiều bạn bè và rút ra những bài học về lòng dũng cảm, tình bạn.",
                "book_cover_01", 5));
        list.add(book("Tắt đèn", "Ngô Tất Tố", "NXB Văn học", 52000, 52000, 25, 210, 4.6f,
                "Tiểu thuyết hiện thực về cuộc sống cùng cực của người nông dân nghèo dưới ách sưu thuế, qua hình ảnh chị Dậu.",
                "book_cover_02", 1));
        list.add(book("Lão Hạc", "Nam Cao", "NXB Văn học", 45000, 60000, 30, 180, 4.7f,
                "Truyện ngắn cảm động về một lão nông nghèo giàu lòng tự trọng, rất thương con và quý chú chó Vàng.",
                "book_cover_03", 1));
        list.add(book("Số đỏ", "Vũ Trọng Phụng", "NXB Văn học", 79000, 99000, 18, 150, 4.5f,
                "Tiểu thuyết trào phúng châm biếm lối sống chạy theo văn minh rởm của xã hội thành thị, qua nhân vật Xuân Tóc Đỏ.",
                "book_cover_04", 1));
        list.add(book("Truyện Kiều", "Nguyễn Du", "NXB Văn học", 95000, 95000, 12, 95, 4.9f,
                "Truyện thơ nổi tiếng kể về cuộc đời chìm nổi của nàng Kiều, thể hiện sự cảm thông với số phận người phụ nữ.",
                "book_cover_05", 1));
        list.add(book("Nhà giả kim", "Paulo Coelho", "NXB Văn học", 79000, 99000, 50, 560, 4.7f,
                "Hành trình của chàng chăn cừu Santiago đi tìm kho báu và khám phá ý nghĩa của việc theo đuổi ước mơ.",
                "book_cover_06", 2));
        list.add(book("Đắc nhân tâm", "Dale Carnegie", "NXB Tổng hợp", 86000, 108000, 60, 800, 4.8f,
                "Cuốn sách kinh điển về nghệ thuật giao tiếp, cách tạo thiện cảm và gây ảnh hưởng tích cực tới người khác.",
                "book_cover_07", 3));
        // Tồn kho = 0 để thử giao diện "Hết hàng"
        list.add(book("Harry Potter và Hòn đá Phù thủy", "J. K. Rowling", "NXB Trẻ", 135000, 168000, 0, 430, 4.9f,
                "Cậu bé Harry phát hiện mình là phù thủy và bắt đầu năm học đầu tiên ở trường Hogwarts.",
                "book_cover_08", 2));
        list.add(book("Sapiens: Lược sử loài người", "Yuval Noah Harari", "NXB Tổng hợp", 189000, 235000, 22, 340, 4.6f,
                "Tóm lược lịch sử loài người từ thời săn bắt hái lượm đến xã hội hiện đại, với nhiều góc nhìn gợi mở.",
                "book_cover_09", 4));
        list.add(book("Tuổi thơ dữ dội", "Phùng Quán", "NXB Kim Đồng", 98000, 98000, 15, 120, 4.4f,
                "Câu chuyện về những thiếu niên tham gia kháng chiến, dũng cảm nhưng cũng đầy tình cảm trong sáng.",
                "book_cover_10", 1));
        list.add(book("Chí Phèo", "Nam Cao", "NXB Văn học", 42000, 55000, 28, 260, 4.7f,
                "Truyện ngắn nổi tiếng về bi kịch của người nông dân bị xã hội tha hóa, qua nhân vật Chí Phèo và Thị Nở.",
                "book_cover_11", 1));
        list.add(book("Vợ nhặt", "Kim Lân", "NXB Văn học", 48000, 48000, 20, 140, 4.6f,
                "Truyện ngắn về nạn đói năm 1945, nói về tình người và hy vọng sống của những con người nghèo khổ.",
                "book_cover_12", 1));
        list.add(book("Mắt biếc", "Nguyễn Nhật Ánh", "NXB Trẻ", 89000, 110000, 35, 520, 4.8f,
                "Câu chuyện tình yêu đơn phương day dứt của Ngạn dành cho Hà Lan, gắn với làng quê và những kỷ niệm tuổi trẻ.",
                "book_cover_13", 1));
        list.add(book("Cho tôi xin một vé đi tuổi thơ", "Nguyễn Nhật Ánh", "NXB Trẻ", 82000, 82000, 30, 470, 4.7f,
                "Những câu chuyện hóm hỉnh, trong trẻo về tuổi thơ, gợi nhắc ký ức và niềm vui giản dị thời bé.",
                "book_cover_14", 5));
        list.add(book("Hoàng tử bé", "Antoine de Saint-Exupéry", "NXB Hội Nhà văn", 59000, 75000, 45, 610, 4.8f,
                "Câu chuyện nhỏ về cậu bé đến từ một hành tinh xa xôi, gửi gắm nhiều suy ngẫm về tình bạn, tình yêu và cuộc sống.",
                "book_cover_15", 5));
        list.add(book("Totto-chan bên cửa sổ", "Tetsuko Kuroyanagi", "NXB Hội Nhà văn", 75000, 92000, 26, 300, 4.8f,
                "Hồi ức của cô bé Totto-chan về ngôi trường đặc biệt với cách dạy tôn trọng cá tính của từng học sinh.",
                "book_cover_16", 5));
        list.add(book("Ông già và biển cả", "Ernest Hemingway", "NXB Văn học", 55000, 70000, 33, 190, 4.6f,
                "Cuộc chiến bền bỉ của ông lão đánh cá Santiago với con cá kiếm khổng lồ giữa biển khơi.",
                "book_cover_17", 2));
        list.add(book("1984", "George Orwell", "NXB Văn học", 98000, 125000, 24, 380, 4.7f,
                "Tiểu thuyết viễn tưởng về một xã hội bị giám sát chặt chẽ, nơi sự thật và tự do cá nhân bị kiểm soát.",
                "book_cover_18", 2));
        list.add(book("Trăm năm cô đơn", "Gabriel García Márquez", "NXB Văn học", 145000, 180000, 14, 220, 4.7f,
                "Câu chuyện nhiều thế hệ của gia tộc Buendía ở làng Macondo, đan xen hiện thực và huyền ảo.",
                "book_cover_19", 2));
        list.add(book("Cha giàu cha nghèo", "Robert T. Kiyosaki", "NXB Trẻ", 99000, 125000, 55, 700, 4.5f,
                "Sách về tư duy tài chính cá nhân, so sánh cách nghĩ về tiền bạc của hai người cha.",
                "book_cover_20", 3));
        list.add(book("Nghĩ giàu và làm giàu", "Napoleon Hill", "NXB Tổng hợp", 90000, 115000, 40, 460, 4.5f,
                "Tổng hợp các nguyên tắc về tư duy, mục tiêu và sự kiên trì để đạt được thành công.",
                "book_cover_21", 3));
        list.add(book("Tư duy nhanh và chậm", "Daniel Kahneman", "NXB Thế giới", 169000, 210000, 16, 240, 4.6f,
                "Phân tích hai hệ thống tư duy của con người và những sai lệch thường gặp khi ra quyết định.",
                "book_cover_22", 3));
        list.add(book("Lược sử thời gian", "Stephen Hawking", "NXB Trẻ", 120000, 150000, 20, 280, 4.6f,
                "Trình bày các ý tưởng về vũ trụ, hố đen và thời gian bằng ngôn ngữ dễ hiểu cho bạn đọc phổ thông.",
                "book_cover_23", 4));
        list.add(book("Súng, vi trùng và thép", "Jared Diamond", "NXB Thế giới", 165000, 205000, 10, 150, 4.5f,
                "Tìm hiểu vì sao các nền văn minh phát triển khác nhau, từ góc nhìn địa lý, môi trường và sinh học.",
                "book_cover_24", 4));

        return list;
    }

    private static Book book(String title, String author, String publisher, long price, long originalPrice,
                             int stock, int sold, float rating, String description, String image, int categoryId) {
        return new Book(0, title, author, publisher, price, originalPrice, stock, sold, rating,
                description, image, categoryId);
    }
}