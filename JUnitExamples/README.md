# GPA system TDD template

The sample domain lives in `org.example.tdd.gpasystem`; its calculator tests,
`FakeStudent`, and `TestDataGenerator` are under `src/test/java`.
See [the UML class diagram](docs/GpaSystemClassDiagram.md) for class
relationships and calculator behavior.

## Model and relationships

- `Student` implements `IStudent`, which extends `IPerson`, and aggregates
  semester records, indexed from 1 through 8.
- A `SemesterRecord` owns its course results. A completed semester must contain
  four or five courses; a semester still in progress may have fewer.
- Each `CourseResult` associates a course with a percentage and its policy grade.
  `GradingPolicyManager` creates results so letter grades and points are kept in
  sync.
- `GpaCalculator` depends on these domain types to calculate weighted GPA and
  summarize performance. Course credits weight grade points in semester and
  cumulative GPA.

`GpaCalculator` receives its student dependency through method parameters typed
as `IStudent` (or a collection of `IStudent`s). This is parameter injection:
production callers pass `Student`, while unit tests can pass `FakeStudent`
without constructing a validated production entity. `IStudent` defines only
the data the calculator needs; names and IDs are reporting details, and GPA
calculation does not reject a student because its ID is blank or missing. The
tests demonstrate both an individual semester calculation and course rankings
using fake students.

For example, a unit test can supply the fake directly:

```java
IStudent fake = new FakeStudent(null, "Ada Lovelace");
fake.addSemester(completedSemesterWithKnownCourseResults);
double gpa = new GpaCalculator().calculateSemesterGpa(fake, 1);
```

The default grading policy maps 90/80/70/60 percent to 4/3/2/1 points, with
lower percentages earning 0 points. Supply custom `GradeBand`s to use a
different policy. Cumulative GPA and cohort comparisons include completed
semesters only. Course comparisons combine completed attempts of that course.
The reported average performer is the student whose GPA is closest to the
cohort's arithmetic mean.

Run the JUnit 5 examples with `mvn test`.

# Enhancements Scenarios
For each task, do not modify GpaCalculator. Write a failing JUnit 5 test first, implement the smallest change to make it pass, then refactor. Include normal, boundary, and invalid-input tests.
1. Add a plus/minus grading scale — GradeBand, GradingPolicyManager
    The university introduces grades such as A-, B+, and C-. Add a grading policy factory for a defined percentage-to-grade table. Test scores exactly at each cutoff and just below it, as well as invalid percentages and an incomplete or invalid policy.
2. Allow students to drop a course before semester completion — SemesterRecord
    Add a way to drop a course by code while a semester is in progress. Test dropping an existing course, rejecting an unknown course, rejecting drops after completion, and confirming that a dropped course can be added again if space remains.
3. Report a student’s completed workload — Student
    Add methods to report the number of completed semesters and the total credits from completed semesters. Test students with no semesters, a mixture of completed and in-progress semesters, and multiple courses with different credit values. In-progress courses should not count toward completed credits.
4. Check course prerequisites — new PrerequisiteChecker class
    Before a student registers for a course, check whether they have passed its required prerequisite courses in completed semesters. Define a passing grade as at least 1.0 grade points. Test a satisfied prerequisite, a missing prerequisite, a prerequisite taken only in an in-progress semester, and a course with no prerequisites.
5. Determine scholarship eligibility — new ScholarshipEligibilityPolicy class
    A student qualifies with a cumulative GPA of at least 3.5 and at least 24 completed course credits. Test students who meet both requirements, miss each requirement individually, land exactly on each threshold, and have no completed coursework.
