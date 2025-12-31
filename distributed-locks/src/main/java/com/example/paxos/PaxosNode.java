package com.example.paxos;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A simplified Paxos node that acts as both Proposer and Acceptor.
 * In a real system, these would be separate processes communicating over the network.
 * For this prototype, we'll simulate the interaction within the same JVM.
 */
public class PaxosNode {
    private final int nodeId;
    private long promisedProposalId = -1;
    private long acceptedProposalId = -1;
    private String acceptedValue = null;
    private long fencingTokenSequence = 0;

    public PaxosNode(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

    // --- Acceptor Role ---

    /**
     * Phase 1b: Promise
     * If proposalId is greater than any previously promised proposal,
     * promises not to accept any proposal numbered less than proposalId.
     */
    public synchronized PrepareResponse onPrepare(long proposalId) {
        if (proposalId > promisedProposalId) {
            promisedProposalId = proposalId;
            return new PrepareResponse(true, acceptedProposalId, acceptedValue, fencingTokenSequence);
        }
        return new PrepareResponse(false, -1, null, -1);
    }

    /**
     * Phase 2b: Accepted
     * If node has not promised to a higher proposal, accepts the value.
     */
    public synchronized boolean onAccept(long proposalId, String value, long token) {
        if (proposalId >= promisedProposalId) {
            promisedProposalId = proposalId;
            acceptedProposalId = proposalId;
            acceptedValue = value;
            fencingTokenSequence = token;
            return true;
        }
        return false;
    }

    public synchronized String getAcceptedValue() {
        return acceptedValue;
    }

    public synchronized long getFencingTokenSequence() {
        return fencingTokenSequence;
    }

    // Response objects for internal communication simulation
    public static class PrepareResponse {
        public final boolean promised;
        public final long prevAcceptedId;
        public final String prevAcceptedValue;
        public final long fencingToken;

        public PrepareResponse(boolean promised, long prevAcceptedId, String prevAcceptedValue, long fencingToken) {
            this.promised = promised;
            this.prevAcceptedId = prevAcceptedId;
            this.prevAcceptedValue = prevAcceptedValue;
            this.fencingToken = fencingToken;
        }
    }
}
