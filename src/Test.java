/**
 * Test class for ReadWriteLock implementation
 * Demonstrates correct behavior of the Readers-Writers solution
 * Test Scenario:
 * - 3 Readers (R1, R2, R3)
 * - 2 Writers (W1, W2)

 * Expected Behavior:
 * 1. Multiple readers can read concurrently
 * 2. Writers execute exclusively (one at a time, no readers)
 * 3. Writers wait for all pending readers to complete
 */
public class Test {

    public static void main(String[] args) {

        System.out.println("=== Fair Reader-Writer Lock Test (No Starvation Guaranteed) ===");


        // Shared ReadWriteLock instance
        ReadWriteLock rwLock = new ReadWriteLock();

        // Create reader threads
        Thread reader1 = new Thread(new Reader(rwLock), "Reader-1");
        Thread reader2 = new Thread(new Reader(rwLock), "Reader-2");
        Thread reader3 = new Thread(new Reader(rwLock), "Reader-3");

        // Create writer threads
        Thread writer1 = new Thread(new Writer(rwLock), "Writer-1");
        Thread writer2 = new Thread(new Writer(rwLock), "Writer-2");

        // Start threads in mixed order to test synchronization
        // Expected: Readers run concurrently, writers wait and run exclusively
        reader1.start();
        writer1.start();  // Should wait for readers to finish
        reader2.start();
        reader3.start();
        writer2.start();  // Should wait for writer1 to finish

        // Wait for all threads to complete
        try {
            reader1.join();
            reader2.join();
            reader3.join();
            writer1.join();
            writer2.join();

            System.out.println("  All threads completed successfully!");

        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
        }
    }

    /**
     * Reader task - simulates reading shared data
     */
    static class Reader implements Runnable {
        private final ReadWriteLock lock;

        public Reader(ReadWriteLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            try {
                // Acquire read lock
                lock.readLock();

                System.out.println("[" + Thread.currentThread().getName() + "] Started READING");

                // Simulate reading operation (1.5 seconds)
                Thread.sleep(1500);

                System.out.println("[" + Thread.currentThread().getName() + "] Finished reading");

                // Release read lock
                lock.readUnLock();

            } catch (InterruptedException e) {
                System.err.println("[" + Thread.currentThread().getName() + "] Interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Writer task - simulates writing to shared data
     */
    static class Writer implements Runnable {
        private final ReadWriteLock lock;

        public Writer(ReadWriteLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            try {
                // Acquire write lock (exclusive access)
                lock.writeLock();

                System.out.println("\n>>> [" + Thread.currentThread().getName() + "] Started WRITING (EXCLUSIVE ACCESS) <<<");

                // Simulate writing operation (2 seconds)
                Thread.sleep(2000);

                System.out.println(">>> [" + Thread.currentThread().getName() + "] Finished writing <<<\n");

                // Release write lock
                lock.writeUnLock();

            } catch (InterruptedException e) {
                System.err.println("[" + Thread.currentThread().getName() + "] Interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}