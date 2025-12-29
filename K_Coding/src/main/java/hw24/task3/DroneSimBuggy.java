package hw24.task3;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;
import java.lang.management.*;

public class DroneSimBuggy {


    record Sector(int id) implements Comparable<Sector> {
        public int compareTo(Sector o) {
            return Integer.compare(this.id, o.id);
        }
    }

    record Order(long id, Sector pickup, Sector drop, int weight, Instant deadline) {
    }

    static class DroneState {
        final long id;
        private Sector sector;
        private int battery;     // 0..100
        private boolean busy;

        DroneState(long id, Sector sector, int battery) {
            this.id = id;
            this.sector = sector;
            this.battery = battery;
            this.busy = false;
        }

        public long getId() {
            return id;
        }

        public synchronized Sector getSector() {
            return sector;
        }

        public synchronized boolean isBusy() {
            return busy;
        }

        public synchronized int getBattery() {
            return battery;
        }

        public synchronized boolean tryToAssign() {
            if (this.busy) return false;
            if (this.battery < 10) return false;
            this.busy = true;
            return true;
        }

        public synchronized void completeMission(Sector newSector, int cost) {
            this.battery = Math.max(0, this.battery - cost);
            this.sector = newSector;
            this.busy = false;
        }
    }



    static class SectorLockManager {
        private final ConcurrentHashMap<Sector, ReentrantLock> locks = new ConcurrentHashMap<>();

        Lock get(DroneSimBuggy.Sector s) {
            return locks.computeIfAbsent(s, k -> new ReentrantLock());
        }
    }

    static class Repository {
        final ConcurrentHashMap<Long, DroneState> drones = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Sector, ConcurrentLinkedQueue<Order>> queues = new ConcurrentHashMap<>();

        void addDrone(DroneState d) {
            drones.put(d.id, d);
        }

        void addOrder(Order o) {
            queues.computeIfAbsent(o.pickup, s -> new ConcurrentLinkedQueue<>()).add(o);
        }


        boolean tryAssign(long droneId, Order o) {
            DroneState d = drones.get(droneId);
            return d != null && d.tryToAssign();
        }


        void finish(long droneId, Sector newSector, int batteryDelta) {
            DroneState d = drones.get(droneId);

            if (d != null) {
                d.completeMission(newSector, batteryDelta);
            }
        }
    }


    static class Router {

        List<Sector> path(Sector a, Sector b) {
            List<Sector> p = new ArrayList<>();
            int step = a.id <= b.id ? 1 : -1;
            for (int i = a.id; i != b.id; i += step) p.add(new Sector(i));
            p.add(b);
            return p;
        }
    }


    static class Scheduler {
        final Repository repo;
        final SectorLockManager slm;
        final Router router = new Router();
        final Random rnd = new Random();
        final AtomicLong completed = new AtomicLong();
        final AtomicLong retries = new AtomicLong();
        final AtomicLong failedAssign = new AtomicLong();

        Scheduler(Repository repo, SectorLockManager slm) {
            this.repo = repo;
            this.slm = slm;
        }


        void matchLoop() {

            while (!Thread.currentThread().isInterrupted()) {

                for (DroneState d : repo.drones.values()) {

                    Order o = pollAnyOrder(d.getSector());

                    if (o == null) continue;

                    if (!repo.tryAssign(d.getId(), o)) {
                        failedAssign.incrementAndGet();
                        continue;
                    }

                    runDeliveryAsync(d.getId(), o);
                }

                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
            }
        }

        Order pollAnyOrder(Sector s) {
            ConcurrentLinkedQueue<Order> q = repo.queues.get(s);
            if (q != null) {
                Order o = q.poll();
                if (o != null) return o;
            }
            ConcurrentLinkedQueue<Order> any = repo.queues.values().stream().findAny().orElse(null);
            return any == null ? null : any.poll();
        }

        void runDeliveryAsync(long droneId, Order o) {
            DroneState drone = repo.drones.get(droneId);
            Sector startSector = drone.getSector();

            List<Sector> route1 = router.path(startSector, o.pickup);
            List<Sector> route2 = router.path(o.pickup, o.drop);
            List<Sector> full = new ArrayList<>();
            full.addAll(route1);
            full.addAll(route2);
            Collections.sort(full);

            List<Sector> locksToAcquire = new ArrayList<>(full.size());
            if (!full.isEmpty()) {
                locksToAcquire.add(full.get(0));
                for (int i = 1; i < full.size(); i++) {
                    Sector prev = full.get(i - 1);
                    Sector curr = full.get(i);
                    if (curr.id() != prev.id()) {
                        locksToAcquire.add(curr);
                    }
                }
            }

            new Thread(() -> {
                try {
                    int flightTime = 20 + rnd.nextInt(20);
                    Thread.sleep(flightTime);
                    for (Sector s : locksToAcquire) {
                        slm.get(s).lock();
                    }

                    try {
                        int cost = 5 + rnd.nextInt(10);
                        repo.finish(droneId, o.drop(), cost);
                        completed.incrementAndGet();
                    } finally {
                        for (int i = locksToAcquire.size() - 1; i >= 0; i--) {
                            slm.get(locksToAcquire.get(i)).unlock();
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "worker-" + droneId).start();
        }
    }

    static class DeadlockWatchdog implements Runnable {
        public void run() {
            ThreadMXBean mx = ManagementFactory.getThreadMXBean();
            while (true) {
                long[] ids = mx.findDeadlockedThreads();
                if (ids != null && ids.length > 0) {
                    System.err.println("DEADLOCK DETECTED. Threads: " + Arrays.toString(ids));
                    System.exit(1);
                    System.exit(1);
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Repository repo = new Repository();
        SectorLockManager slm = new SectorLockManager();
        Scheduler sch = new Scheduler(repo, slm);

        int sectors = 40;
        List<Sector> sectorList = new ArrayList<>();
        for (int i = 1; i <= sectors; i++) sectorList.add(new Sector(i));

        for (int i = 1; i <= 60; i++) {
            repo.addDrone(new DroneState(i, sectorList.get(i % sectors), 50 + (i % 40)));
        }

        long oid = 1;
        Random r = new Random();
        for (int i = 0; i < 2000; i++) {
            Sector a = sectorList.get(r.nextInt(sectors));
            Sector b = sectorList.get(r.nextInt(sectors));
            repo.addOrder(new Order(oid++, a, b, 1 + r.nextInt(3), Instant.now().plusSeconds(600)));
        }

        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) pool.submit(sch::matchLoop);


        Thread wd = new Thread(new DeadlockWatchdog(), "deadlock-watchdog");
        wd.setDaemon(true);
        wd.start();

        long end = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < end) {

            Sector a = sectorList.get(r.nextInt(sectors));
            Sector b = sectorList.get(r.nextInt(sectors));
            repo.addOrder(new Order(oid++, a, b, 1 + r.nextInt(3), Instant.now().plusSeconds(600)));
            Thread.sleep(5);
        }

        pool.shutdownNow();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        long busy = repo.drones.values().stream().filter(d -> d.busy).count();
        long activeOrders = repo.queues.values().stream().mapToLong(Queue::size).sum();
        System.out.println("Completed: " + sch.completed.get());
        System.out.println("Retries:   " + sch.retries.get());
        System.out.println("FailedAssign: " + sch.failedAssign.get());
        System.out.println("Busy now:  " + busy);
        System.out.println("Active orders: " + activeOrders);

        long neg = repo.drones.values().stream().filter(d -> d.battery < 0).count();
        if (neg > 0) throw new AssertionError("Є дрони з відʼємним зарядом: " + neg);

    }
}
