# Readers-Writers Synchronization Solution
A robust, Java-based solution to the classic Readers-Writers Problem, implemented using semaphores to ensure thread-safe access to shared resources. This project was developed as part of the CMP3001 Operating Systems course.

# Project Overview
The primary objective of this implementation is to facilitate concurrent read access while maintaining strict exclusive write access, preventing common concurrency issues such as race conditions, deadlocks, and starvation.

# Key Features

Concurrent Reading: Multiple readers can access the critical section simultaneously without blocking each other.


Exclusive Writing: Writers are granted exclusive access, blocking both other writers and all readers.


Data Persistence: A writer must wait until all "pending" readers have finished reading the current version of the data before overwriting it.


Single-Read Guarantee: Implements a tracking mechanism using a HashSet to ensure each reader thread processes the same data version exactly once.


Starvation Prevention: Utilizes Java's Fair Semaphores to enforce a First-In-First-Out (FIFO) policy, ensuring writers are not indefinitely delayed by a continuous stream of readers.

# Technical Architecture
The solution employs a Three-Semaphore Architecture to coordinate thread activity:


S (Main Lock): Controls primary access to the critical section.


mutex: Ensures atomic updates to the readCount variable.


dataMutex: Protects state-tracking structures from race conditions.

# Usage
The Test.java class simulates a concurrent environment with 3 Readers and 2 Writers to verify synchronization logic.
javac *.java
java Test
# Test Results 
<img width="660" height="506" alt="output" src="https://github.com/user-attachments/assets/cece724b-d0a7-40e0-8107-28fbd2b8df3b" />
