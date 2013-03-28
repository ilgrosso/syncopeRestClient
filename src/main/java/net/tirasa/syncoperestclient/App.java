package net.tirasa.syncoperestclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;

public class App {

    private static final ClassPathXmlApplicationContext CTX =
            new ClassPathXmlApplicationContext("applicationContext.xml");

    private static final RestTemplate restTemplate = (RestTemplate) CTX.getBean("restTemplate");

    private static final String BASE_URL = (String) CTX.getBean("baseURL");

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

    private static PolicyServiceProxy policyService;

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

    private static final void init() {
        PreemptiveAuthHttpRequestFactory requestFactory =
                ((PreemptiveAuthHttpRequestFactory) restTemplate.getRequestFactory());
        ((DefaultHttpClient) requestFactory.getHttpClient()).getCredentialsProvider().setCredentials(
                requestFactory.getAuthScope(), new UsernamePasswordCredentials("admin", "password"));

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

    public static void main(final String[] args)
            throws Exception {

        init();

        // *do* something
    }
}
