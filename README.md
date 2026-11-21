# SlidingWindowRateLimiter
Sliding time window rate limiter implemented in Java for interface QPS control.

## ✨ Features
Solves the burst traffic problem of fixed window rate limiting.
Split the time window into multiple small slots, automatically expire outdated slots, count valid requests dynamically.

## 🛠 Tech Stack
- Java 1.8
- ConcurrentHashMap + AtomicInteger, thread-safe for multi-thread requests

## 📌 Core Logic
1. Divide the total time window into equal small time slots
2. Each slot records request count independently
3. Remove expired slots before every request
4. Sum all valid requests in the current window
5. Accept request if total ≤ threshold; reject and rollback counter otherwise

## 📊 Test Demo
Window: 1000ms, split into 5 slots, max limit = 5.
Excess requests will be rejected. Quota recycles automatically as time slides.
