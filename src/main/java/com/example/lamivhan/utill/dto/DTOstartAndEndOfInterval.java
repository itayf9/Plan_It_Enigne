package com.example.lamivhan.utill.dto;

import java.time.Instant;

/**
 * DTO that represents a single slot contain a start and end
 *
 */
public class DTOstartAndEndOfInterval {
    private Instant StartOfInterval;
    private Instant EndOfInterval;

    public DTOstartAndEndOfInterval(Instant startOfInterval, Instant endOfInterval) {
        StartOfInterval = startOfInterval;
        EndOfInterval = endOfInterval;
    }

    public Instant getStartOfInterval() {
        return StartOfInterval;
    }

    public Instant getEndOfInterval() {
        return EndOfInterval;
    }

    public void setStartOfInterval(Instant startOfInterval) {
        StartOfInterval = startOfInterval;
    }

    public void setEndOfInterval(Instant endOfInterval) {
        EndOfInterval = endOfInterval;
    }
}
