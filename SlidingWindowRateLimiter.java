import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 滑动时间窗口限流实现
 * 原理：把一个大时间窗口切分成多个小格子，随时间滑动，淘汰过期格子，统计有效请求数
 */
public class SlidingWindowRateLimiter {
    // 窗口总时长，单位ms
    private final long windowMs;
    // 分成多少个小槽
    private final int slotCount;
    // 窗口内最大允许请求
    private final int maxLimit;
    // 每个槽的时间跨度
    private final long slotMs;

    private final ConcurrentHashMap<Long, AtomicInteger> slotMap = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(long windowMs, int slotCount, int maxLimit) {
        this.windowMs = windowMs;
        this.slotCount = slotCount;
        this.maxLimit = maxLimit;
        this.slotMs = windowMs / slotCount;
    }

    /**
     * 判断本次请求是否放行
     * @return true 通过，false 限流拒绝
     */
    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        long currentSlotId = now / slotMs;

        // 1. 清理已经过期的槽
        long expireThreshold = currentSlotId - slotCount;
        slotMap.keySet().removeIf(key -> key <= expireThreshold);

        // 2. 拿到当前槽，计数+1
        AtomicInteger counter = slotMap.computeIfAbsent(currentSlotId, k -> new AtomicInteger(0));
        counter.incrementAndGet();

        // 3. 统计窗口内全部请求总数
        int total = slotMap.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();

        if (total <= maxLimit) {
            return true;
        } else {
            // 超了，回滚刚才的计数
            counter.decrementAndGet();
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 示例：1000ms窗口，分成5个槽，最多允许5次请求
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1000, 5, 5);

        for (int i = 1; i <= 12; i++) {
            boolean pass = limiter.tryAcquire();
            System.out.printf("请求%d: %s%n", i, pass ? "✅ 通过" : "❌ 限流拒绝");
            Thread.sleep(120);
        }
    }
}
