package org.example.tdd.gpasystem;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

final class FakeStudent implements IStudent {
    private final String id;
    private final String name;
    private final Map<Integer, SemesterRecord> semesters = new TreeMap<>();

    FakeStudent(String id, String name) {
        this.id = id;
        this.name = name;
    }

    void addSemester(SemesterRecord semester) {
        semesters.put(semester.getSemesterNumber(), semester);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SemesterRecord getSemester(int semesterNumber) {
        return semesters.get(semesterNumber);
    }

    @Override
    public Map<Integer, SemesterRecord> getSemesters() {
        return Collections.unmodifiableMap(semesters);
    }

    @Override
    public int getCompletedSemesterCount() {
        int count = 0;
        for (SemesterRecord record : semesters.values()) {
            if (record.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public double getTotalCompletedCredits() {
        double totalCredits = 0.0;
        for (SemesterRecord record : semesters.values()) {
            if (record.isCompleted()) {
                for (CourseResult result : record.getCourseResults()) {
                    totalCredits += result.getCourse().getCredits();
                }
            }
        }
        return totalCredits;
    }
}
