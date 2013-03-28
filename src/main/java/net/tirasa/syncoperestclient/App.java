package net.tirasa.syncoperestclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.syncope.client.http.PreemptiveAuthHttpRequestFactory;
import org.apache.syncope.client.to.AttributeTO;
import org.apache.syncope.client.to.MembershipTO;
import org.apache.syncope.client.to.UserTO;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;

public class App {

    private static final ClassPathXmlApplicationContext CTX =
            new ClassPathXmlApplicationContext("applicationContext.xml");

    private static final RestTemplate restTemplate = (RestTemplate) CTX.getBean("restTemplate");

    private static final String BASE_URL = (String) CTX.getBean("baseURL");

    public static UserTO getSampleTO(final String email) {
        UserTO userTO = new UserTO();
        userTO.setPassword("password123");
        userTO.setUsername(email);

        AttributeTO fullnameTO = new AttributeTO();
        fullnameTO.setSchema("fullname");
        fullnameTO.addValue(email);
        userTO.addAttribute(fullnameTO);

        AttributeTO firstnameTO = new AttributeTO();
        firstnameTO.setSchema("firstname");
        firstnameTO.addValue(email);
        userTO.addAttribute(firstnameTO);

        AttributeTO surnameTO = new AttributeTO();
        surnameTO.setSchema("surname");
        surnameTO.addValue("Surname");
        userTO.addAttribute(surnameTO);

        AttributeTO typeTO = new AttributeTO();
        typeTO.setSchema("type");
        typeTO.addValue("a type");
        userTO.addAttribute(typeTO);

        AttributeTO userIdTO = new AttributeTO();
        userIdTO.setSchema("userId");
        userIdTO.addValue(email);
        userTO.addAttribute(userIdTO);

        AttributeTO emailTO = new AttributeTO();
        emailTO.setSchema("email");
        emailTO.addValue(email);
        userTO.addAttribute(emailTO);

        AttributeTO loginDateTO = new AttributeTO();
        loginDateTO.setSchema("loginDate");
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        loginDateTO.addValue(sdf.format(new Date()));
        userTO.addAttribute(loginDateTO);

        // add a derived attribute
        AttributeTO cnTO = new AttributeTO();
        cnTO.setSchema("cn");
        userTO.addDerivedAttribute(cnTO);

        // add a virtual attribute
        AttributeTO virtualdata = new AttributeTO();
        virtualdata.setSchema("virtualdata");
        virtualdata.addValue("virtualvalue");
        userTO.addVirtualAttribute(virtualdata);

        return userTO;
    }

    private static final void init() {
        PreemptiveAuthHttpRequestFactory requestFactory =
                ((PreemptiveAuthHttpRequestFactory) restTemplate.getRequestFactory());
        ((DefaultHttpClient) requestFactory.getHttpClient()).getCredentialsProvider().setCredentials(
                requestFactory.getAuthScope(), new UsernamePasswordCredentials("admin", "password"));
    }

    public static void main(final String[] args)
            throws Exception {

        init();

        for (int i = 0; i < 10; i++) {
            UserTO userTO = getSampleTO("syncope276." + i + "@syncope.apache.org");

            MembershipTO membershipTO = new MembershipTO();
            membershipTO.setRoleId(7L);
            userTO.addMembership(membershipTO);

            userTO = restTemplate.postForObject(BASE_URL + "user/create", userTO, UserTO.class);
            System.out.println(userTO.getUsername() + " created");
        }
    }
}
