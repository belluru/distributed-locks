package com.example.paxos;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the Paxos-based distributed lock and how fencing tokens
 * protect against stale writes even when clock synchronization is not present.
 */
public class PaxosDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Paxos-based Distributed Lock Demo ===");

        // 1. Initialize 5 Paxos nodes
        List<PaxosNode> nodes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            nodes.add(new PaxosNode(i));
        }

        PaxosLock paxosLock = new PaxosLock(nodes);

        // 2. Client A acquires the lock
        System.out.println("\n[Client A] Attempting to acquire lock...");
        FencingToken tokenA = paxosLock.tryAcquire("Client-A");
        if (tokenA != null) {
            System.out.println("[Client A] SUCCESS! Acquired lock with " + tokenA);
        }

        // 3. Client A experiences a "Pause" (simulated)
        System.out.println("[Client A] Performing work (simulated pause/delay)...");
        
        // 4. Client B acquires the lock while Client A is paused
        // Since Redlock uses TTL, it would expire. In Paxos/Consensus, 
        // a new leader/proposer can take over or the same proposer can update.
        System.out.println("\n[Client B] Attempting to acquire lock...");
        FencingToken tokenB = paxosLock.tryAcquire("Client-B");
        if (tokenB != null) {
            System.out.println("[Client B] SUCCESS! Acquired lock with " + tokenB);
        }

        // 5. Simulated Storage System
        StorageSystem storage = new StorageSystem();

        // 6. Client B writes to storage with its token
        System.out.println("\n[Client B] Writing to storage with token " + tokenB.getSequenceNumber());
        storage.write("Data from B", tokenB);

        // 7. Client A "wakes up" and tries to write with its OLD token
        System.out.println("[Client A] Waking up and trying to write with stale token " + tokenA.getSequenceNumber());
        try {
            storage.write("Data from A (STALE)", tokenA);
        } catch (IllegalStateException e) {
            System.err.println("[Client A] ERROR: Write rejected! " + e.getMessage());
        }

        // 8. Final Status
        System.out.println("\nFinal Storage Content: " + storage.getData());
        System.out.println("Final Storage Last Token: " + storage.getLastToken());
    }

    /**
     * A simple storage system that uses fencing tokens to ensure safety.
     */
    static class StorageSystem {
        private String data = "Empty";
        private long lastToken = -1;

        public synchronized void write(String newData, FencingToken token) {
            if (token.getSequenceNumber() <= lastToken) {
                throw new IllegalStateException("Stale fencing token: " + token.getSequenceNumber() + 
                        ". Expected > " + lastToken);
            }
            this.data = newData;
            this.lastToken = token.getSequenceNumber();
            System.out.println("[Storage] Write Successful: " + newData);
        }

        public String getData() { return data; }
        public long getLastToken() { return lastToken; }
    }
}
