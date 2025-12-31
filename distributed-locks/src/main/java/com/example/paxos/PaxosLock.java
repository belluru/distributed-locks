package com.example.paxos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Coordinates a group of PaxosNodes to achieve consensus on a lock.
 * This class acts as the Proposer in the Paxos algorithm.
 */
public class PaxosLock {
    private final List<PaxosNode> nodes;
    private final int quorumSize;
    private final AtomicLong proposalCounter = new AtomicLong(0);
    private final AtomicLong tokenCounter = new AtomicLong(0);

    public PaxosLock(List<PaxosNode> nodes) {
        this.nodes = nodes;
        this.quorumSize = (nodes.size() / 2) + 1;
    }

    /**
     * Attempts to acquire the lock using Paxos.
     * Returns a FencingToken if successful, null otherwise.
     */
    public FencingToken tryAcquire(String clientId) {
        long proposalId = proposalCounter.incrementAndGet();
        
        // Phase 1: Prepare
        int promises = 0;
        long maxFencingToken = -1;
        
        for (PaxosNode node : nodes) {
            PaxosNode.PrepareResponse response = node.onPrepare(proposalId);
            if (response.promised) {
                promises++;
                maxFencingToken = Math.max(maxFencingToken, response.fencingToken);
            }
        }

        if (promises < quorumSize) {
            return null; // Failed to get quorum for Prepare
        }

        // Phase 2: Accept
        // In this implementation, the value being proposed is always the clientId
        // The fencing token is incremented from the highest one seen
        long nextToken = maxFencingToken + 1;
        int accepts = 0;
        
        for (PaxosNode node : nodes) {
            if (node.onAccept(proposalId, clientId, nextToken)) {
                accepts++;
            }
        }

        if (accepts >= quorumSize) {
            return new FencingToken(nextToken);
        }

        return null; // Failed to get quorum for Accept
    }

    /**
     * Releasing a lock in this Paxos model means proposing a "None" value
     * to clear the owner on a quorum of nodes.
     */
    public boolean release(String clientId) {
        long proposalId = proposalCounter.incrementAndGet();
        int promises = 0;
        for (PaxosNode node : nodes) {
            if (node.onPrepare(proposalId).promised) {
                promises++;
            }
        }
        
        if (promises < quorumSize) return false;

        int accepts = 0;
        for (PaxosNode node : nodes) {
            // We use token 0 for released state, or just let it stay at last token
            if (node.onAccept(proposalId, null, 0)) {
                accepts++;
            }
        }
        
        return accepts >= quorumSize;
    }
}
