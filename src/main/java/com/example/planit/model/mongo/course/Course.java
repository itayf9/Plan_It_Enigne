package com.example.planit.model.mongo.course;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("courses")
public class Course {

    @Field(name = "name")
    private String courseName;
    @Field(name = "level")
    private int difficultyLevel;
    @Field(name = "credits")
    private int credits;
    @Field(name = "study-time")
    private int recommendedStudyTime;

    @Field(name = "subjects")
    private String[] courseSubjects;

    @Field(name = "subjects-practice-percentage")
    private int subjectsPracticePercentage;

    public Course() {
    }

    public Course(String courseName, int difficultyLevel, int credits, int recommendedStudyTime, String[] courseSubjects, int subjectsPracticePercentage) {
        this.courseName = courseName;
        this.difficultyLevel = difficultyLevel;
        this.credits = credits;
        this.recommendedStudyTime = recommendedStudyTime;
        this.courseSubjects = courseSubjects;
        this.subjectsPracticePercentage = subjectsPracticePercentage;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public int getCredits() {
        return credits;
    }

    public int getRecommendedStudyTime() {
        return recommendedStudyTime;
    }

    public String[] getCourseSubjects() {
        return courseSubjects;
    }

    public int getSubjectsPracticePercentage() {
        return subjectsPracticePercentage;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setRecommendedStudyTime(int recommendedStudyTime) {
        this.recommendedStudyTime = recommendedStudyTime;
    }

    public void setCourseSubjects(String[] courseSubjects) {
        this.courseSubjects = courseSubjects;
    }

    public void setSubjectsPracticePercentage(int subjectsPracticePercentage) {
        this.subjectsPracticePercentage = subjectsPracticePercentage;
    }
}
