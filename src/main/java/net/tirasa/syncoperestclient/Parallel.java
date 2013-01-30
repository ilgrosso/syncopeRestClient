package net.tirasa.syncoperestclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.syncope.client.http.PreemptiveAuthHttpRequestFactory;
import org.apache.syncope.client.to.UserTO;
import org.apache.syncope.client.validation.SyncopeClientCompositeErrorException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

public class Parallel {

    private static final ClassPathXmlApplicationContext CTX =
            new ClassPathXmlApplicationContext("applicationContext.xml");

    private static final RestTemplate restTemplate =
            (RestTemplate) CTX.getBean("restTemplate");

    private static final String BASE_URL = (String) CTX.getBean("baseURL");

    final private static int NUM_THREADS = 15;

    final private static int BASE_ID = 200;

    public static void main(final String[] args)
            throws InterruptedException, ExecutionException {

        PreemptiveAuthHttpRequestFactory requestFactory =
                ((PreemptiveAuthHttpRequestFactory) restTemplate.
                getRequestFactory());
        ((DefaultHttpClient) requestFactory.getHttpClient()).
                getCredentialsProvider().setCredentials(
                requestFactory.getAuthScope(),
                new UsernamePasswordCredentials("admin", "password"));

        final ExecutorService executor =
                Executors.newFixedThreadPool(NUM_THREADS);

        final Map<String, SyncopeClientCompositeErrorException> exceptions =
                new HashMap<String, SyncopeClientCompositeErrorException>();
        final List<Future> result = new ArrayList<Future>();

        for (int i = 0; i < NUM_THREADS; i++) {
            final String email = "paralleltest" + (i + BASE_ID)
                    + "@syncope-idm.org";
            result.add(executor.submit(new Runnable() {

                @Override
                public void run() {
                    System.out.println("About to create " + email);

                    UserTO userTO = App.getSampleTO(email);
                    userTO.addResource("resource-testdb");
                    try {
                        UserTO actual = restTemplate.postForObject(
                                BASE_URL + "user/create",
                                userTO, UserTO.class);

                        if (actual == null) {
                            throw new SyncopeClientCompositeErrorException(
                                    HttpStatus.BAD_REQUEST);
                        }

                        System.out.println("Successfully created " + email);
                    } catch (SyncopeClientCompositeErrorException e) {
                        exceptions.put(email, e);
                        System.err.println("Could not create " + email);
                    }
                }
            }));
        }

        for (Future execResult : result) {
            execResult.get();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("While waiting for task executor termination");
            e.printStackTrace();
        }

        for (Map.Entry<String, SyncopeClientCompositeErrorException> entry :
                exceptions.entrySet()) {

            System.out.println("XXXXXXXX " + entry.getKey() + "\t" + entry.
                    getValue());
        }
    }
}
