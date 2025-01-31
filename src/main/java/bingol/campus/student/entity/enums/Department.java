package bingol.campus.student.entity.enums;

public enum Department {
    COMPUTER_SCIENCE("Bilgisayar Mühendisliği"),
    MECHANICAL_ENGINEERING("Makine Mühendisliği"),
    BIOLOGY("Biyoloji"),
    LAW("Hukuk"),
    ECONOMICS("İktisat");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
