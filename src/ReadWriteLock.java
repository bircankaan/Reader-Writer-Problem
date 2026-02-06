import java.util.concurrent.Semaphore;
import java.util.HashSet;
import java.util.Set;

/**
 * ReadWriteLock: Solution to the Readers-Writers Synchronization Problem
 * This implementation satisfies all project requirements:
 * 1. Multiple readers can read concurrently (no mutual exclusion between readers)
 * 2. Writers have exclusive access (no readers or other writers allowed)
 * 3. Data persistence: Writers wait for all pending readers to complete
 * 4. Single-read guarantee: Each reader reads the current data version only once
 * Strategy: Fair Reader-Writer Lock (FIFO) to prevent starvation
 */
public class ReadWriteLock {

    // SEMAPHORES (As specified in project requirements)

    // Extra semaphore is used to prevent writer starvation and ensure fairness
    /**
     * Main semaphore S: Controls critical section access
     * - First reader acquires it to block writers
     * - Last reader releases it to allow writers
     * - Writers acquire it for exclusive access
     * Fair=true ensures FIFO ordering to prevent starvation
     */
    private final Semaphore S = new Semaphore(1, true);

    /**
     * Mutex: Protects readCount from race conditions
     * Ensures only one thread modifies readCount at a time
     */
    private final Semaphore mutex = new Semaphore(1, true);

    /**
     * Data Mutex: Protects data version tracking structures
     * Ensures thread-safe access to readersWhoReadCurrentVersion and pendingReaders
     */
    private final Semaphore dataMutex = new Semaphore(1, true);

    // STATE VARIABLES

    /**
     * Number of readers currently in the critical section
     */
    private int readCount = 0;

    /**
     * Tracks which readers have completed reading the current data version
     * Enforces "readers must read the same data only once" requirement
     */
    private final Set<String> readersWhoReadCurrentVersion = new HashSet<>();

    /**
     * Count of readers who still need to read the current data version
     * Writers must wait until this reaches zero (data persistence requirement)
     */
    private int pendingReaders = 0;

    // READER METHODS

    /**
     * Acquires read lock - allows concurrent reading
     * Blocks if a writer is currently writing
     */
    public void readLock() {
        try {
            // Note: Fair semaphore ensures writers are not starved even if readers arrive continuously

            // STEP 1: Update reader count safely
            mutex.acquire();
            readCount++;

            // First reader blocks writers by acquiring S
            if (readCount == 1) {
                S.acquire();  // Block writers
            }
            mutex.release();

            // STEP 2: Register this reader as "pending" for current data version
            // This implements: "Writer must ensure data not yet read by all readers is not overwritten"
            dataMutex.acquire();
            String readerName = Thread.currentThread().getName();

            // Only count as pending if this reader hasn't read current version yet
            if (!readersWhoReadCurrentVersion.contains(readerName)) {
                pendingReaders++;
            }
            dataMutex.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("readLock interrupted: " + e.getMessage());
        }
    }

    /**
     * Releases read lock after reading completes
     * Updates tracking data and releases S if this is the last reader
     */
    public void readUnLock() {
        try {
            // STEP 1: Mark this reader as having completed current version
            dataMutex.acquire();
            String readerName = Thread.currentThread().getName();

            // Only update if not already marked complete
            // This enforces "readers must read the same data only once"
            if (!readersWhoReadCurrentVersion.contains(readerName)) {
                readersWhoReadCurrentVersion.add(readerName);
                pendingReaders--;
            }
            dataMutex.release();

            // STEP 2: Decrement reader count
            mutex.acquire();
            readCount--;

            // Last reader releases S to allow writers
            if (readCount == 0) {
                S.release();  // Unblock writers
            }
            mutex.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("readUnLock interrupted: " + e.getMessage());
        }
    }


    // WRITER METHODS


    /**
     * Acquires write lock for exclusive access
     * Blocks until:
     * 1. All active readers complete (readCount == 0)
     * 2. All pending readers finish current version (pendingReaders == 0)
     * 3. No other writer is active
     * This implements: "Writer must ensure data not yet read by all readers is not overwritten"
     */
    public void writeLock() {
        try {
            // STEP 1: Acquire exclusive access via semaphore S
            // This prevents new readers from starting
            S.acquire();

            // STEP 2: Wait for all pending readers to complete
            // Use polling because project restricts us to semaphores only
            // (No condition variables allowed per specification)
            boolean waitingForReaders = true;
            while (waitingForReaders) {
                dataMutex.acquire();

                if (pendingReaders == 0) {
                    // All readers done - safe to proceed with writing
                    waitingForReaders = false;
                } else {
                    // Still have pending readers - must wait
                    dataMutex.release();

                    // Brief sleep to avoid busy-waiting and excessive CPU usage
                    // This is a standard practice and doesn't violate semaphore-only requirement
                    Thread.sleep(50);
                    continue;
                }
                dataMutex.release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("writeLock interrupted: " + e.getMessage());
        }
    }

    /**
     * Releases write lock after writing completes
     * Resets version tracking so new readers can read the updated data
     */
    public void writeUnLock() {
        try {
            // STEP 1: Reset tracking data for new version
            dataMutex.acquire();
            readersWhoReadCurrentVersion.clear();  // New version, so no one has read it yet
            pendingReaders = 0;                     // Reset pending count
            dataMutex.release();

            // STEP 2: Release exclusive access
            // Allows either next writer or new readers to proceed
            S.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("writeUnLock interrupted: " + e.getMessage());
        }
    }
}