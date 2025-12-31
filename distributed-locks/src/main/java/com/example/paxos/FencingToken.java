package com.example.paxos;

/**
 * A fencing token is a monotonically increasing number that is issued
 * every time a client acquires a lock. It is used to protect against
 * writes from clients that have lost their lock (e.g., due to process pauses).
 */
public class FencingToken implements Comparable<FencingToken> {
    private final long sequenceNumber;

    public FencingToken(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public int compareTo(FencingToken o) {
        return Long.compare(this.sequenceNumber, o.sequenceNumber);
    }

    @Override
    public String toString() {
        return "FencingToken{" +
                "sequenceNumber=" + sequenceNumber +
                '}';
    }
}
