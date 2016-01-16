package hu.fnf.devel.jmeter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class JmeterHttpWorker implements Callable<List<Long>> {
    private static final String AUTHORIZATION = "Authorization";
    private String token = "c";
    private int count;
    private String url;
    private HttpClient httpClient;
    private int status_code;

    /**
     * Create http client for parallel run.
     *
     * @param count       the http request execution counter
     * @param url         the target url
     * @param token       the auth token for the request
     * @param status_code the status code of an expected response
     */
    public JmeterHttpWorker(int count, String url, String token, int status_code) {
        this.count = count;
        this.url = url;
        this.token = token;
        this.status_code = status_code;
        httpClient = new DefaultHttpClient();
    }

    /**
     * Parallel run execution method.
     *
     * @return all the response times
     * @throws Exception any exception during the http request-response
     */
    public List<Long> call() throws Exception {
        final List<Long> result = new ArrayList<>();

        while (count > 0) {
            count--;
            long start = System.currentTimeMillis();
            HttpUriRequest req = new HttpGet(url);
            req.addHeader(AUTHORIZATION, token);
            HttpResponse response = httpClient.execute(req);
            if (response.getStatusLine().getStatusCode() == status_code) {
                long end = System.currentTimeMillis();
                result.add(end - start);
                /**
                 * print in csv format for graphic analysis
                 */
                System.out.println(Thread.currentThread().getId() + "," + count + "," + System.currentTimeMillis() + "," + (end - start));
            }
            EntityUtils.consume(response.getEntity());
        }

        return result;
    }
}
