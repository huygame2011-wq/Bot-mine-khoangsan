public enum Level {
    THONG_TIN("Thông tin", 0xFFE9D5FF),
    CANH_BAO("Cảnh báo", 0xFFFFD54F),
    LOI("Lỗi", 0xFFFF5252);

    public final String label;
    private final int argb;

    Level(String label, int argb) {
        this.label = label;
        this.argb = argb;
    }

    public int color() {
        return argb;
    }
}
