package com.university.erp.admin;

public class Section {
    private final int sectionId;
    private final int courseId;
    private final String courseCode;
    private final String courseTitle;
    private final Integer instructorId; // nullable
    private final String instructorUsername; // nullable
    private final String dayTime;
    private final String room;
    private final int capacity;
    private final String semester;
    private final int year;

    public Section(int sectionId, int courseId, String courseCode, String courseTitle,
                   Integer instructorId, String instructorUsername,
                   String dayTime, String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.instructorId = instructorId;
        this.instructorUsername = instructorUsername;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public Integer getInstructorId() { return instructorId; }
    public String getInstructorUsername() { return instructorUsername; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
}
