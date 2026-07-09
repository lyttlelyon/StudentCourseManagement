import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseManager {
    private final List<Course> courses = new ArrayList<>();

    public boolean addCourse(String courseCode, String courseTitle, int unit) {
        String normalizedCode = courseCode.trim().toUpperCase();

        if (normalizedCode.isEmpty() || courseTitle.trim().isEmpty() || unit <= 0) {
            throw new IllegalArgumentException("Course code, title, and unit must be valid.");
        }

        if (searchCourse(normalizedCode) != null) {
            return false;
        }

        courses.add(new Course(normalizedCode, courseTitle, unit));
        return true;
    }

    public List<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    public Course searchCourse(String courseCode) {
        return recursiveSearch(courseCode.trim().toUpperCase(), 0);
    }

    private Course recursiveSearch(String courseCode, int index) {
        if (index >= courses.size()) {
            return null;
        }

        Course currentCourse = courses.get(index);
        if (currentCourse.getCourseCode().equalsIgnoreCase(courseCode)) {
            return currentCourse;
        }

        return recursiveSearch(courseCode, index + 1);
    }

    public int computeTotalUnits() {
        int total = 0;
        for (Course course : courses) {
            total += course.getUnit();
        }
        return total;
    }

    public boolean deleteCourse(String courseCode) {
        String normalizedCode = courseCode.trim().toUpperCase();

        for (int index = 0; index < courses.size(); index++) {
            if (courses.get(index).getCourseCode().equalsIgnoreCase(normalizedCode)) {
                courses.remove(index);
                return true;
            }
        }

        return false;
    }

    public void saveToFile(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Course course : courses) {
                writer.write(course.toFileLine());
                writer.newLine();
            }
        }
    }

    public int loadFromFile(String fileName) throws IOException {
        List<Course> loadedCourses = new ArrayList<>();
        int skippedRecords = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Course course = Course.fromFileLine(line);
                    if (containsCode(loadedCourses, course.getCourseCode())) {
                        skippedRecords++;
                    } else {
                        loadedCourses.add(course);
                    }
                } catch (IllegalArgumentException exception) {
                    skippedRecords++;
                }
            }
        }

        courses.clear();
        courses.addAll(loadedCourses);
        return skippedRecords;
    }

    private boolean containsCode(List<Course> courseList, String courseCode) {
        for (Course course : courseList) {
            if (course.getCourseCode().equalsIgnoreCase(courseCode)) {
                return true;
            }
        }
        return false;
    }
}
