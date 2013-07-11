package net.tirasa.syncoperestclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.syncope.client.http.PreemptiveAuthHttpRequestFactory;
import org.apache.syncope.client.services.proxy.ConfigurationServiceProxy;
import org.apache.syncope.client.services.proxy.ConnectorServiceProxy;
import org.apache.syncope.client.services.proxy.EntitlementServiceProxy;
import org.apache.syncope.client.services.proxy.LoggerServiceProxy;
import org.apache.syncope.client.services.proxy.NotificationServiceProxy;
import org.apache.syncope.client.services.proxy.PolicyServiceProxy;
import org.apache.syncope.client.services.proxy.ReportServiceProxy;
import org.apache.syncope.client.services.proxy.ResourceServiceProxy;
import org.apache.syncope.client.services.proxy.RoleServiceProxy;
import org.apache.syncope.client.services.proxy.SchemaServiceProxy;
import org.apache.syncope.client.services.proxy.SpringServiceProxy;
import org.apache.syncope.client.services.proxy.TaskServiceProxy;
import org.apache.syncope.client.services.proxy.UserRequestServiceProxy;
import org.apache.syncope.client.services.proxy.UserServiceProxy;
import org.apache.syncope.client.services.proxy.UserWorkflowServiceProxy;
import org.apache.syncope.client.services.proxy.WorkflowServiceProxy;
import org.apache.syncope.common.mod.AttributeMod;
import org.apache.syncope.common.services.ConfigurationService;
import org.apache.syncope.common.services.ConnectorService;
import org.apache.syncope.common.services.EntitlementService;
import org.apache.syncope.common.services.LoggerService;
import org.apache.syncope.common.services.NotificationService;
import org.apache.syncope.common.services.PolicyService;
import org.apache.syncope.common.services.ReportService;
import org.apache.syncope.common.services.ResourceService;
import org.apache.syncope.common.services.RoleService;
import org.apache.syncope.common.services.SchemaService;
import org.apache.syncope.common.services.TaskService;
import org.apache.syncope.common.services.UserRequestService;
import org.apache.syncope.common.services.UserService;
import org.apache.syncope.common.services.UserWorkflowService;
import org.apache.syncope.common.services.WorkflowService;
import org.apache.syncope.common.to.AttributeTO;
import org.apache.syncope.common.to.RoleTO;
import org.apache.syncope.common.to.TaskExecTO;
import org.apache.syncope.common.to.TaskTO;
import org.apache.syncope.common.to.UserTO;
import org.apache.syncope.common.types.TaskType;
import org.apache.syncope.common.validation.SyncopeClientErrorHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class App {

    private static final String ADMIN_ID = "admin";

    private static final String ADMIN_PWD = "password";

    private static final ClassPathXmlApplicationContext CTX =
            new ClassPathXmlApplicationContext("applicationContext.xml");

    private static final RestTemplate restTemplate = CTX.getBean(RestTemplate.class);

    private static final JAXRSClientFactoryBean restClientFactory = CTX.getBean(JAXRSClientFactoryBean.class);

    private static final String BASE_URL = (String) CTX.getBean("baseURL");

    private static final MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter =
            CTX.getBean(MappingJacksonHttpMessageConverter.class);

    private static final PreemptiveAuthHttpRequestFactory httpClientFactory =
            CTX.getBean(PreemptiveAuthHttpRequestFactory.class);

    private static UserService userService;

    private static RoleService roleService;

    private static ResourceService resourceService;

    private static EntitlementService entitlementService;

    private static ConfigurationService configurationService;

    private static ConnectorService connectorService;

    private static LoggerService loggerService;

    private static ReportService reportService;

    private static TaskService taskService;

    private static WorkflowService workflowService;

    private static NotificationService notificationService;

    private static SchemaService schemaService;

    private static UserRequestService userRequestService;

    private static UserWorkflowService userWorkflowService;

    private static PolicyService policyService;

    private static AttributeTO attributeTO(final String schema, final String value) {
        AttributeTO attr = new AttributeTO();
        attr.setSchema(schema);
        attr.addValue(value);
        return attr;
    }

    private static AttributeMod attributeMod(final String schema, final String valueToBeAdded) {
        AttributeMod attr = new AttributeMod();
        attr.setSchema(schema);
        attr.addValueToBeAdded(valueToBeAdded);
        return attr;
    }

    private static String getUUIDString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static RoleTO buildBasicRoleTO(final String name) {
        RoleTO roleTO = new RoleTO();
        roleTO.setName(name + getUUIDString());
        roleTO.setParent(8L);
        return roleTO;
    }

    private static RoleTO buildRoleTO(final String name) {
        RoleTO roleTO = buildBasicRoleTO(name);

        // verify inheritance password and account policies
        roleTO.setInheritAccountPolicy(false);
        // not inherited so setter execution shouldn't be ignored
        roleTO.setAccountPolicy(6L);

        roleTO.setInheritPasswordPolicy(true);
        // inherited so setter execution should be ignored
        roleTO.setPasswordPolicy(2L);

        roleTO.addAttribute(attributeTO("icon", "anIcon"));

        roleTO.addResource("resource-ldap");
        return roleTO;
    }

    public static UserTO getSampleTO(final String email) {
        String uid = email;
        UserTO userTO = new UserTO();
        userTO.setPassword("password123");
        userTO.setUsername(uid);

        userTO.addAttribute(attributeTO("fullname", uid));
        userTO.addAttribute(attributeTO("firstname", uid));
        userTO.addAttribute(attributeTO("surname", "surname"));
        userTO.addAttribute(attributeTO("type", "a type"));
        userTO.addAttribute(attributeTO("userId", uid));
        userTO.addAttribute(attributeTO("email", uid));
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        userTO.addAttribute(attributeTO("loginDate", sdf.format(new Date())));
        userTO.addDerivedAttribute(attributeTO("cn", null));
        userTO.addVirtualAttribute(attributeTO("virtualdata", "virtualvalue"));
        return userTO;
    }

    public static UserTO getUniqueSampleTO(final String email) {
        return getSampleTO(getUUIDString() + email);
    }

    private static void setupRestTemplate(final String uid, final String pwd) {
        PreemptiveAuthHttpRequestFactory requestFactory = ((PreemptiveAuthHttpRequestFactory) restTemplate
                .getRequestFactory());

        ((DefaultHttpClient) requestFactory.getHttpClient()).getCredentialsProvider().setCredentials(
                requestFactory.getAuthScope(), new UsernamePasswordCredentials(uid, pwd));
    }

    private static RestTemplate getAnonymousRestTemplate() {
        RestTemplate template = new RestTemplate(httpClientFactory);
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        converters.add(mappingJacksonHttpMessageConverter);
        template.setMessageConverters(converters);
        template.setErrorHandler(new SyncopeClientErrorHandler());
        return template;
    }

    @SuppressWarnings("unchecked")
    private static <T> T setupCredentials(final T proxy, final Class<?> serviceInterface, final String username,
            final String password) {
        if (proxy instanceof SpringServiceProxy) {
            SpringServiceProxy service = (SpringServiceProxy) proxy;
            if (username == null && password == null) {
                service.setRestTemplate(getAnonymousRestTemplate());
            } else {
                setupRestTemplate(username, password);
            }
            return proxy;
        } else {
            restClientFactory.setUsername(username);
            restClientFactory.setPassword(password);
            restClientFactory.setServiceClass(serviceInterface);
            T serviceProxy = (T) restClientFactory.create(serviceInterface);
            WebClient.client(serviceProxy).accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML);
            return serviceProxy;
        }
    }

    private static final void initSpring() {
        PreemptiveAuthHttpRequestFactory requestFactory =
                ((PreemptiveAuthHttpRequestFactory) restTemplate.getRequestFactory());
        ((DefaultHttpClient) requestFactory.getHttpClient()).getCredentialsProvider().setCredentials(
                requestFactory.getAuthScope(), new UsernamePasswordCredentials(ADMIN_ID, ADMIN_PWD));

        userService = new UserServiceProxy(BASE_URL, restTemplate);
        roleService = new RoleServiceProxy(BASE_URL, restTemplate);
        resourceService = new ResourceServiceProxy(BASE_URL, restTemplate);
        entitlementService = new EntitlementServiceProxy(BASE_URL, restTemplate);
        configurationService = new ConfigurationServiceProxy(BASE_URL, restTemplate);
        connectorService = new ConnectorServiceProxy(BASE_URL, restTemplate);
        loggerService = new LoggerServiceProxy(BASE_URL, restTemplate);
        reportService = new ReportServiceProxy(BASE_URL, restTemplate);
        taskService = new TaskServiceProxy(BASE_URL, restTemplate);
        policyService = new PolicyServiceProxy(BASE_URL, restTemplate);
        workflowService = new WorkflowServiceProxy(BASE_URL, restTemplate);
        notificationService = new NotificationServiceProxy(BASE_URL, restTemplate);
        schemaService = new SchemaServiceProxy(BASE_URL, restTemplate);
        userRequestService = new UserRequestServiceProxy(BASE_URL, restTemplate);
        userWorkflowService = new UserWorkflowServiceProxy(BASE_URL, restTemplate);
    }

    private static <T> T createServiceInstance(final Class<T> serviceClass, final String username, final String password) {
        restClientFactory.setUsername(username);
        restClientFactory.setPassword(password);
        restClientFactory.setServiceClass(serviceClass);
        T serviceProxy = restClientFactory.create(serviceClass);
        WebClient.client(serviceProxy).type("application/json").accept("application/json");
        return serviceProxy;
    }

    private static void initCXF() {
        userService = createServiceInstance(UserService.class, ADMIN_ID, ADMIN_PWD);
        userWorkflowService = createServiceInstance(UserWorkflowService.class, ADMIN_ID, ADMIN_PWD);
        roleService = createServiceInstance(RoleService.class, ADMIN_ID, ADMIN_PWD);
        resourceService = createServiceInstance(ResourceService.class, ADMIN_ID, ADMIN_PWD);
        entitlementService = createServiceInstance(EntitlementService.class, ADMIN_ID, ADMIN_PWD);
        configurationService = createServiceInstance(ConfigurationService.class, ADMIN_ID, ADMIN_PWD);
        connectorService = createServiceInstance(ConnectorService.class, ADMIN_ID, ADMIN_PWD);
        loggerService = createServiceInstance(LoggerService.class, ADMIN_ID, ADMIN_PWD);
        reportService = createServiceInstance(ReportService.class, ADMIN_ID, ADMIN_PWD);
        taskService = createServiceInstance(TaskService.class, ADMIN_ID, ADMIN_PWD);
        policyService = createServiceInstance(PolicyService.class, ADMIN_ID, ADMIN_PWD);
        workflowService = createServiceInstance(WorkflowService.class, ADMIN_ID, ADMIN_PWD);
        notificationService = createServiceInstance(NotificationService.class, ADMIN_ID, ADMIN_PWD);
        schemaService = createServiceInstance(SchemaService.class, ADMIN_ID, ADMIN_PWD);
        userRequestService = createServiceInstance(UserRequestService.class, ADMIN_ID, ADMIN_PWD);
    }

    private static TaskExecTO execSyncTask(final Long taskId, final int maxWaitSeconds,
            final boolean dryRun) {

        TaskTO taskTO = taskService.read(TaskType.SYNCHRONIZATION, taskId);
        assertNotNull(taskTO);
        assertNotNull(taskTO.getExecutions());

        int preSyncSize = taskTO.getExecutions().size();
        TaskExecTO execution = taskService.execute(taskTO.getId(), dryRun);
        assertEquals("JOB_FIRED", execution.getStatus());

        int i = 0;
        int maxit = maxWaitSeconds;

        // wait for sync completion (executions incremented)
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            taskTO = taskService.read(TaskType.SYNCHRONIZATION, taskTO.getId());

            assertNotNull(taskTO);
            assertNotNull(taskTO.getExecutions());

            i++;
        } while (preSyncSize == taskTO.getExecutions().size() && i < maxit);
        if (i == maxit) {
            throw new RuntimeException("Timeout when executing task " + taskId);
        }
        return taskTO.getExecutions().get(0);
    }

    private static UserTO createUser(final UserTO userTO) {
        Response response = userService.create(userTO);
        if (response.getStatus() != HttpStatus.SC_CREATED) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(UserTO.class);
    }

    private static RoleTO createRole(final RoleTO roleTO) {
        Response response = roleService.create(roleTO);
        if (response.getStatus() != HttpStatus.SC_CREATED) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(RoleTO.class);
    }

    public static void main(final String[] args)
            throws Exception {

        initSpring();
        // initCXF();

        // *do* something
    }
}
