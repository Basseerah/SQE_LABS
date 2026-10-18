package org.example.tdd.gpasystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    private GradingPolicyManager policy;

    @BeforeEach
    void setUp() {
        policy = GradingPolicyManager.standardFourPointScale();
    }

    private CourseResult createResult(String code, String title, double credits, double percentage) {
        Course course = new Course(code, title, credits);
        return policy.createCourseResult(course, percentage);
    }

    @Test
    @DisplayName("Boundary: Student with no semesters has 0 completed semesters and 0.0 completed credits")
    void returnsZeroCompletedSemestersAndCreditsWhenStudentHasNoSemesters() {
        Student student = new Student("S101", "Alice Smith");

        assertEquals(0, student.getCompletedSemesterCount(),
                "Completed semesters count should be 0 when student has no semesters");
        assertEquals(0.0, student.getTotalCompletedCredits(), 0.0001,
                "Completed credits should be 0.0 when student has no semesters");
    }

    @Test
    @DisplayName("Boundary: In-progress semesters do not count toward completed semesters or completed credits")
    void returnsZeroWhenStudentHasOnlyInProgressSemesters() {
        Student student = new Student("S102", "Bob Jones");

        SemesterRecord inProgressSemester = new SemesterRecord(1);
        inProgressSemester.addCourseResult(createResult("CS101", "Intro to CS", 3.0, 85.0));
        inProgressSemester.addCourseResult(createResult("MATH101", "Calculus I", 4.0, 90.0));
        // Note: inProgressSemester.complete() is NOT called

        student.addSemester(inProgressSemester);

        assertEquals(0, student.getCompletedSemesterCount(),
                "In-progress semester should not count as completed");
        assertEquals(0.0, student.getTotalCompletedCredits(), 0.0001,
                "Courses in in-progress semesters must not count toward completed credits");
    }

    @Test
    @DisplayName("Normal: Reports completed semesters count and total credits for fully completed semester")
    void reportsCompletedSemestersAndCreditsForSingleCompletedSemester() {
        Student student = new Student("S103", "Charlie Brown");

        SemesterRecord semester = new SemesterRecord(1);
        semester.addCourseResult(createResult("CS101", "Intro to CS", 3.0, 85.0));
        semester.addCourseResult(createResult("MATH101", "Calculus I", 3.0, 90.0));
        semester.addCourseResult(createResult("ENG101", "English Comp", 3.0, 75.0));
        semester.addCourseResult(createResult("PHYS101", "Physics I", 3.0, 80.0));
        semester.complete();

        student.addSemester(semester);

        assertEquals(1, student.getCompletedSemesterCount(),
                "Should report 1 completed semester");
        assertEquals(12.0, student.getTotalCompletedCredits(), 0.0001,
                "Should report total 12.0 credits for 4 three-credit courses");
    }

    @Test
    @DisplayName("Mixture: Only completed semesters count when mixed with in-progress semesters")
    void reportsWorkloadForMixtureOfCompletedAndInProgressSemesters() {
        Student student = new Student("S104", "Diana Prince");

        // Semester 1: Completed with 4 courses = 13.0 credits
        SemesterRecord semester1 = new SemesterRecord(1);
        semester1.addCourseResult(createResult("CS101", "Intro to CS", 4.0, 88.0));
        semester1.addCourseResult(createResult("MATH101", "Calculus I", 3.0, 92.0));
        semester1.addCourseResult(createResult("ENG101", "English Comp", 3.0, 80.0));
        semester1.addCourseResult(createResult("PHYS101", "Physics I", 3.0, 78.0));
        semester1.complete();
        student.addSemester(semester1);

        // Semester 2: In-progress with 2 courses (7.0 credits)
        SemesterRecord semester2 = new SemesterRecord(2);
        semester2.addCourseResult(createResult("CS201", "Data Structures", 4.0, 85.0));
        semester2.addCourseResult(createResult("MATH201", "Calculus II", 3.0, 70.0));
        // NOT completed
        student.addSemester(semester2);

        assertEquals(1, student.getCompletedSemesterCount(),
                "Only completed semesters should be counted");
        assertEquals(13.0, student.getTotalCompletedCredits(), 0.0001,
                "Credits from in-progress semester must not be included");
    }

    @Test
    @DisplayName("Credit Values: Correctly accumulates different credit values (e.g. 1.0, 2.0, 3.5, 4.0)")
    void correctlyCalculatesTotalCreditsForCoursesWithDifferentCreditValues() {
        Student student = new Student("S105", "Evan Wright");

        SemesterRecord semester = new SemesterRecord(1);
        semester.addCourseResult(createResult("LAB101", "CS Lab", 1.0, 95.0));
        semester.addCourseResult(createResult("SEM101", "Freshman Seminar", 2.0, 88.0));
        semester.addCourseResult(createResult("ENG102", "Technical Writing", 3.5, 82.0));
        semester.addCourseResult(createResult("CS102", "Programming II", 4.0, 91.0));
        semester.complete();
        student.addSemester(semester);

        // Expected total credits: 1.0 + 2.0 + 3.5 + 4.0 = 10.5
        assertEquals(1, student.getCompletedSemesterCount());
        assertEquals(10.5, student.getTotalCompletedCredits(), 0.0001,
                "Should accurately sum different fractional and whole credit values");
    }

    @Test
    @DisplayName("Accumulation: Accumulates workload across multiple completed semesters")
    void accumulatesWorkloadAcrossMultipleCompletedSemesters() {
        Student student = new Student("S106", "Fiona Gallagher");

        // Semester 1: 4 courses of 3.0 = 12.0 credits
        SemesterRecord sem1 = new SemesterRecord(1);
        sem1.addCourseResult(createResult("C101", "Course 1", 3.0, 80.0));
        sem1.addCourseResult(createResult("C102", "Course 2", 3.0, 85.0));
        sem1.addCourseResult(createResult("C103", "Course 3", 3.0, 90.0));
        sem1.addCourseResult(createResult("C104", "Course 4", 3.0, 75.0));
        sem1.complete();
        student.addSemester(sem1);

        // Semester 2: 5 courses (4.0, 3.0, 3.0, 2.0, 1.0 = 13.0 credits)
        SemesterRecord sem2 = new SemesterRecord(2);
        sem2.addCourseResult(createResult("C201", "Course 5", 4.0, 82.0));
        sem2.addCourseResult(createResult("C202", "Course 6", 3.0, 88.0));
        sem2.addCourseResult(createResult("C203", "Course 7", 3.0, 78.0));
        sem2.addCourseResult(createResult("C204", "Course 8", 2.0, 92.0));
        sem2.addCourseResult(createResult("C205", "Course 9", 1.0, 85.0));
        sem2.complete();
        student.addSemester(sem2);

        // Semester 3: In progress with 2 courses (3.0 + 3.0 = 6.0 credits)
        SemesterRecord sem3 = new SemesterRecord(3);
        sem3.addCourseResult(createResult("C301", "Course 10", 3.0, 70.0));
        sem3.addCourseResult(createResult("C302", "Course 11", 3.0, 80.0));
        student.addSemester(sem3);

        assertEquals(2, student.getCompletedSemesterCount(),
                "Should count exactly the 2 completed semesters");
        assertEquals(25.0, student.getTotalCompletedCredits(), 0.0001,
                "Should accumulate credits from semester 1 and 2 (12.0 + 13.0 = 25.0)");
    }
}
