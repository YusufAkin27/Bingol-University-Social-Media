package bingol.campus.student.core.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStudentProfileRequest {

    private String firstName; // Öğrenci Adı
    private String lastName; // Öğrenci Soyadı
    private String mobilePhone; // Telefon Numarası
    private String department; // Bölüm
    private String faculty; // Fakülte
    private String grade; // Sınıf
    private Boolean gender; // Cinsiyet (true: Erkek, false: Kadın)
}
