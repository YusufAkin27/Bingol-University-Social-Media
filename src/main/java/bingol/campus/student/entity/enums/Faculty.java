package bingol.campus.student.entity.enums;

public enum Faculty {
    ENGINEERING("Mühendislik Fakültesi"),
    SCIENCE("Fen Fakültesi"),
    MEDICINE("Tıp Fakültesi"),
    LAW("Hukuk Fakültesi"),
    ECONOMICS("İktisat Fakültesi");

    private final String displayName;

    Faculty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
