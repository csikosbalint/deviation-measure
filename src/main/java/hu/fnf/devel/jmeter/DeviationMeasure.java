package hu.fnf.devel.jmeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class DeviationMeasure {
    private static final int CONCURRENT_THREADS = 10;
    private static final String URL = "http://ogee.hu";
    private static final int COUNT = 500;
    private static final int STATUS_CODE = 200;

    private static String token;

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        /**
         * Start all threads at once.
         */
        ExecutorService service = Executors.newFixedThreadPool(CONCURRENT_THREADS);

        List<Callable<List<Long>>> callable = new ArrayList<>();
        IntStream.range(0, CONCURRENT_THREADS).forEach(i ->
                /**
                 * Instantiate workers with the appropriate url, token and the expected status_code.
                 * Only responses with the provided status_code will be counted into the result calculation.
                 */
                callable.add(new JmeterHttpWorker(COUNT, URL, getToken(), STATUS_CODE))
        );
        List<Future<List<Long>>> futures = service.invokeAll(callable);
        /**
         * Wait maximum token expiration time.
         */
        service.shutdown();
        service.awaitTermination(4, TimeUnit.HOURS);
        /**
         * Collect results.
         */
        List<Long> allResult = new ArrayList<>();
        futures.stream().forEach((f) -> {
            try {
                allResult.addAll(f.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        for (Future<List<Long>> f : futures) {
            allResult.addAll(f.get());
        }

        /**
         * Calculate the standard deviation.
         */
        LongSummaryStatistics statistics = allResult.parallelStream().mapToLong((x) -> x).summaryStatistics();

        System.out.println(statistics);
        double variance = 0;
        for (int i = 0; i < statistics.getCount(); i++) {
            variance += Math.pow(statistics.getAverage() - allResult.get(i), 2);
        }
        variance /= statistics.getCount();
        System.out.println(
                "Standard deviation: " + Math.ceil(statistics.getAverage()) + "±" + Math.ceil(Math.sqrt(variance)));
    }

    /**
     * Token cache proxy.
     *
     * @return auth token string of already retrieved
     */
    private static String getToken() {
        if (token == null) {
            token = retrieveToken();
        }
        return token;
    }

    /**
     * Token retrieve mechanism.
     *
     * @return auth token from token service
     */
    private static String retrieveToken() {
        return "token";
    }
}

