package bingol.campus.student.entity.enums;

public enum Grade {
    HAZIRLIK,
    BIRINCI_SINIF,  // 1. Sınıf
    IKI_SINIF,      // 2. Sınıf
    UCUNCU_SINIF,   // 3. Sınıf
    DORDUNCU_SINIF, // 4. Sınıf
    MEZUN;


    // İsterseniz enum'da her sınıf için bir numara da tutabilirsiniz:
    private int seviye;

    Grade() {
    }

    Grade(int seviye) {
        this.seviye = seviye;
    }

    public int getSeviye() {
        return seviye;
    }

    public void setSeviye(int seviye) {
        this.seviye = seviye;
    }
}
