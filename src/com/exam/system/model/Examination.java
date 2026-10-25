package com.exam.system.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A locally stored examination configuration. */
public class Examination implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String title;
    private String subject;
    private String instructions;
    private int durationMinutes;
    private List<Integer> questionIds;
    private LocalDateTime availableFrom;
    private LocalDateTime availableUntil;
    private boolean randomizeQuestions;
    private boolean oneAttempt;
    private boolean active;

    public Examination(int id, String title, String subject, String instructions, int durationMinutes,
                       List<Integer> questionIds, LocalDateTime availableFrom, LocalDateTime availableUntil,
                       boolean randomizeQuestions, boolean oneAttempt, boolean active) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.instructions = instructions;
        this.durationMinutes = durationMinutes;
        this.questionIds = new ArrayList<>(questionIds);
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.randomizeQuestions = randomizeQuestions;
        this.oneAttempt = oneAttempt;
        this.active = active;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getInstructions() { return instructions; }
    public int getDurationMinutes() { return durationMinutes; }
    public List<Integer> getQuestionIds() { return new ArrayList<>(questionIds); }
    public LocalDateTime getAvailableFrom() { return availableFrom; }
    public LocalDateTime getAvailableUntil() { return availableUntil; }
    public boolean isRandomizeQuestions() { return randomizeQuestions; }
    public boolean isOneAttempt() { return oneAttempt; }
    public boolean isActive() { return active; }
    public void setTitle(String value) { title = value; }
    public void setSubject(String value) { subject = value; }
    public void setInstructions(String value) { instructions = value; }
    public void setDurationMinutes(int value) { durationMinutes = value; }
    public void setQuestionIds(List<Integer> value) { questionIds = new ArrayList<>(value); }
    public void setAvailableFrom(LocalDateTime value) { availableFrom = value; }
    public void setAvailableUntil(LocalDateTime value) { availableUntil = value; }
    public void setRandomizeQuestions(boolean value) { randomizeQuestions = value; }
    public void setOneAttempt(boolean value) { oneAttempt = value; }
    public void setActive(boolean value) { active = value; }
    public boolean isAvailableAt(LocalDateTime now) {
        return active && (availableFrom == null || !now.isBefore(availableFrom))
                && (availableUntil == null || !now.isAfter(availableUntil));
    }
    @Override public String toString() {
        return title + " — " + subject + " (" + durationMinutes + " min)";
    }
}
