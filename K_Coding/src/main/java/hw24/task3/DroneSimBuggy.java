package hw24.task3;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;
import java.lang.management.*;

public class DroneSimBuggy {


    record Sector(int id) implements Comparable<Sector> {
        public int compareTo(Sector o) { return Integer.compare(this.id, o.id); }
    }
    record Order(long id, Sector pickup, Sector drop, int weight, Instant deadline) { }
    static class DroneState {
        final long id;
        volatile Sector sector;
        volatile int battery;     // 0..100
        volatile boolean busy;
        DroneState(long id, Sector sector, int battery) {
            this.id = id; this.sector = sector; this.battery = battery; this.busy = false;
        }
    }

    // --------- Сховище та блокування секторів ---------
    static class SectorLockManager {
        private final ConcurrentHashMap<Sector, ReentrantLock> locks = new ConcurrentHashMap<>();
        Lock get(Sector s) {
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
        // БАГ: перевірки робляться окремо від змін. Неатомарні читання та записи.
        boolean tryAssign(long droneId, Order o) {
            DroneState d = drones.get(droneId);
            if (d == null) return false;
            if (d.busy) return false;              // окреме читання
            if (d.battery < 10) return false;
            d.busy = true;                         // окремий запис
            return true;
        }
        void finish(long droneId, Sector newSector, int batteryDelta) {
            DroneState d = drones.get(droneId);
            // БАГ: можливий відʼємний заряд та гонка записів
            d.battery = d.battery - batteryDelta;
            d.sector = newSector;
            d.busy = false;
        }
    }

    // --------- Маршрутизація ---------
    static class Router {
        // Повертає ланцюжок секторів з A у B
        List<Sector> path(Sector a, Sector b) {
            List<Sector> p = new ArrayList<>();
            int step = a.id <= b.id ? 1 : -1;
            for (int i = a.id; i != b.id; i += step) p.add(new Sector(i));
            p.add(b);
            return p;
        }
    }

    // --------- Планувальник з навмисними проблемами ---------
    static class Scheduler {
        final Repository repo;
        final SectorLockManager slm;
        final Router router = new Router();
        final Random rnd = new Random();
        final AtomicLong completed = new AtomicLong();
        final AtomicLong retries = new AtomicLong();
        final AtomicLong failedAssign = new AtomicLong();

        Scheduler(Repository repo, SectorLockManager slm) {
            this.repo = repo; this.slm = slm;
        }

        // Воркер матчить дронів і замовлення
        void matchLoop() {
            while (!Thread.currentThread().isInterrupted()) {
                for (DroneState d : repo.drones.values()) {
                    // Спроба взяти замовлення з сектору де стоїть дрон
                    Order o = pollAnyOrder(d.sector);
                    if (o == null) continue;
                    if (!repo.tryAssign(d.id, o)) {
                        failedAssign.incrementAndGet();
                        continue;
                    }
                    // Запуск виконання
                    runDeliveryAsync(d.id, o);
                }
                sleepMs(2);
            }
        }

        // Беремо замовлення або з поточного сектору, або з сусідніх
        Order pollAnyOrder(Sector s) {
            ConcurrentLinkedQueue<Order> q = repo.queues.get(s);
            if (q != null) {
                Order o = q.poll();
                if (o != null) return o;
            }
            // випадковий сусід
            ConcurrentLinkedQueue<Order> any = repo.queues.values().stream().findAny().orElse(null);
            return any == null ? null : any.poll();
        }

        void runDeliveryAsync(long droneId, Order o) {
            // БАГ: можливий deadlock. Різні воркери беруть locks у різному порядку маршруту.
            List<Sector> route1 = router.path(repo.drones.get(droneId).sector, o.pickup);
            List<Sector> route2 = router.path(o.pickup, o.drop);
            List<Sector> full = new ArrayList<>();
            full.addAll(route1);
            full.addAll(route2);

            new Thread(() -> {
                // БАГ livelock: tryLock з коротким таймаутом і симетричний відступ у всіх
                for (Sector s : full) {
                    Lock L = slm.get(s);
                    while (true) {
                        try {
                            if (L.tryLock(5, TimeUnit.MILLISECONDS)) {
                                break;
                            } else {
                                retries.incrementAndGet();
                                // симетричний ввічливий відступ
                                sleepMs(rnd.nextInt(5));
                                // обидва потоки роблять одне і те саме
                            }
                        } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                    }
                }
                // Критична секція довга: «політ» та зменшення батареї
                sleepMs(20 + rnd.nextInt(20));
                // БАГ race: можливий відʼємний заряд та перепризначення у busy
                repo.finish(droneId, o.drop, 5 + rnd.nextInt(10));
                completed.incrementAndGet();

                // Розблокування секторів у протилежному порядку від захоплення
                // БАГ: порушення усталеного порядку звільнення збільшує шанс взаємоблокування
                Collections.reverse(full);
                for (Sector s : full) {
                    slm.get(s).unlock();
                }
            }, "delivery-" + droneId + "-" + o.id).start();
        }

        static void sleepMs(long ms) {
            try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    // --------- Watchdog для deadlock ---------
    static class DeadlockWatchdog implements Runnable {
        public void run() {
            ThreadMXBean mx = ManagementFactory.getThreadMXBean();
            while (true) {
                long[] ids = mx.findDeadlockedThreads();
                if (ids != null && ids.length > 0) {
                    System.err.println("DEADLOCK DETECTED. Threads: " + Arrays.toString(ids));
                    ThreadInfo[] infos = mx.getThreadInfo(ids, true, true);
                    for (ThreadInfo ti : infos) {
                        System.err.println(ti.toString());
                    }
                    return;
                }
                try { Thread.sleep(500); } catch (InterruptedException e) { return; }
            }
        }
    }

    // --------- Main: генерує навантаження та провокує проблеми ---------
    public static void main(String[] args) throws Exception {
        Repository repo = new Repository();
        SectorLockManager slm = new SectorLockManager();
        Scheduler sch = new Scheduler(repo, slm);

        int sectors = 40;
        List<Sector> sectorList = new ArrayList<>();
        for (int i = 1; i <= sectors; i++) sectorList.add(new Sector(i));

        // Дрони
        for (int i = 1; i <= 60; i++) {
            repo.addDrone(new DroneState(i, sectorList.get(i % sectors), 50 + (i % 40)));
        }

        // Початкові замовлення
        long oid = 1;
        Random r = new Random();
        for (int i = 0; i < 2000; i++) {
            Sector a = sectorList.get(r.nextInt(sectors));
            Sector b = sectorList.get(r.nextInt(sectors));
            repo.addOrder(new Order(oid++, a, b, 1 + r.nextInt(3), Instant.now().plusSeconds(600)));
        }

        // Воркери матчинг
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 8; i++) pool.submit(sch::matchLoop);


        // Watchdog
        Thread wd = new Thread(new DeadlockWatchdog(), "deadlock-watchdog");
        wd.setDaemon(true);
        wd.start();

        // Стрес 30 секунд
        long end = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < end) {
            // Додаємо замовлення під час роботи
            Sector a = sectorList.get(r.nextInt(sectors));
            Sector b = sectorList.get(r.nextInt(sectors));
            repo.addOrder(new Order(oid++, a, b, 1 + r.nextInt(3), Instant.now().plusSeconds(600)));
            Thread.sleep(5);
        }

        pool.shutdownNow();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        // Інваріанти: можуть падати у баговій версії
        long busy = repo.drones.values().stream().filter(d -> d.busy).count();
        long activeOrders = repo.queues.values().stream().mapToLong(Queue::size).sum();
        System.out.println("Completed: " + sch.completed.get());
        System.out.println("Retries:   " + sch.retries.get());
        System.out.println("FailedAssign: " + sch.failedAssign.get());
        System.out.println("Busy now:  " + busy);
        System.out.println("Active orders: " + activeOrders);

        // БАГ: заряд міг стати відʼємним
        long neg = repo.drones.values().stream().filter(d -> d.battery < 0).count();
        if (neg > 0) throw new AssertionError("Є дрони з відʼємним зарядом: " + neg);

        // БАГ: кількість зайнятих дронів може не збігатися з «у дорозі»
        // Тут спеціально не перевіряємо, щоб інколи завершувалось без падіння, але це симптом
    }
}
