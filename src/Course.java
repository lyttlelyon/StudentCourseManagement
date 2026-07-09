public record Course(String courseCode, String courseTitle, int unit) {
    public Course(String courseCode, String courseTitle, int unit) {
        this.courseCode = courseCode.trim().toUpperCase();
        this.courseTitle = courseTitle.trim();
        this.unit = unit;
    }

    public String toFileLine() {
        return courseCode + "|" + courseTitle + "|" + unit;
    }

    public static Course fromFileLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Course record must have 3 fields.");
        }

        String code = parts[0].trim();
        String title = parts[1].trim();
        int unit = Integer.parseInt(parts[2].trim());

        if (code.isEmpty() || title.isEmpty() || unit <= 0) {
            throw new IllegalArgumentException("Course record contains invalid values.");
        }

        return new Course(code, title, unit);
    }
}
