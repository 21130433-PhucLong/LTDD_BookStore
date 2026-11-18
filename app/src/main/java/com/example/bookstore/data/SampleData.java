package com.example.bookstore.data;

import com.example.bookstore.model.Book;

import java.util.ArrayList;
import java.util.List;

public class SampleData {

    public static List<Book> getBooks() {
        List<Book> list = new ArrayList<>();

        list.add(new Book(1, "Dế Mèn phiêu lưu ký", "Tô Hoài", "NXB Kim Đồng",
                68000, 85000, 40, 320, 4.8f,
                "Cuộc phiêu lưu của chú dế mèn qua nhiều vùng đất, gặp gỡ nhiều bạn bè và rút ra những bài học về lòng dũng cảm, tình bạn.",
                "book_01", 1));
        list.add(new Book(2, "Tắt đèn", "Ngô Tất Tố", "NXB Văn học",
                52000, 52000, 25, 210, 4.6f,
                "Tiểu thuyết hiện thực về cuộc sống cùng cực của người nông dân nghèo dưới ách sưu thuế, qua hình ảnh chị Dậu.",
                "book_02", 1));
        list.add(new Book(3, "Lão Hạc", "Nam Cao", "NXB Văn học",
                45000, 60000, 30, 180, 4.7f,
                "Truyện ngắn cảm động về một lão nông nghèo giàu lòng tự trọng, rất thương con và quý chú chó Vàng.",
                "book_03", 1));
        list.add(new Book(4, "Số đỏ", "Vũ Trọng Phụng", "NXB Văn học",
                79000, 99000, 18, 150, 4.5f,
                "Tiểu thuyết trào phúng châm biếm lối sống chạy theo văn minh rởm của xã hội thành thị, qua nhân vật Xuân Tóc Đỏ.",
                "book_04", 1));
        list.add(new Book(5, "Truyện Kiều", "Nguyễn Du", "NXB Văn học",
                95000, 95000, 12, 95, 4.9f,
                "Truyện thơ nổi tiếng kể về cuộc đời chìm nổi của nàng Kiều, thể hiện sự cảm thông với số phận người phụ nữ.",
                "book_05", 1));
        list.add(new Book(6, "Nhà giả kim", "Paulo Coelho", "NXB Văn học",
                79000, 99000, 50, 560, 4.7f,
                "Hành trình của chàng chăn cừu Santiago đi tìm kho báu và khám phá ý nghĩa của việc theo đuổi ước mơ.",
                "book_06", 2));
        list.add(new Book(7, "Đắc nhân tâm", "Dale Carnegie", "NXB Tổng hợp",
                86000, 108000, 60, 800, 4.8f,
                "Cuốn sách kinh điển về nghệ thuật giao tiếp, cách tạo thiện cảm và gây ảnh hưởng tích cực tới người khác.",
                "book_07", 3));
        // Sách hết hàng (tồn kho = 0) để thử giao diện "Hết hàng"
        list.add(new Book(8, "Harry Potter và Hòn đá Phù thủy", "J. K. Rowling", "NXB Trẻ",
                135000, 168000, 0, 430, 4.9f,
                "Cậu bé Harry phát hiện mình là phù thủy và bắt đầu năm học đầu tiên ở trường Hogwarts.",
                "book_08", 2));
        list.add(new Book(9, "Sapiens: Lược sử loài người", "Yuval Noah Harari", "NXB Tổng hợp",
                189000, 235000, 22, 340, 4.6f,
                "Tóm lược lịch sử loài người từ thời săn bắt hái lượm đến xã hội hiện đại, với nhiều góc nhìn gợi mở.",
                "book_09", 4));
        list.add(new Book(10, "Tuổi thơ dữ dội", "Phùng Quán", "NXB Kim Đồng",
                98000, 98000, 15, 120, 4.4f,
                "Câu chuyện về những thiếu niên tham gia kháng chiến, dũng cảm nhưng cũng đầy tình cảm trong sáng.",
                "book_10", 1));

        return list;
    }
}