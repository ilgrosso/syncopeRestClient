package net.tirasa.syncoperestclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.syncope.client.rest.RestClientFactoryBean;
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
import org.apache.syncope.common.to.AbstractTaskTO;
import org.apache.syncope.common.to.UserTO;
import org.apache.syncope.common.types.LoggerType;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

    private static final String ADMIN_ID = "admin";

    private static final String ADMIN_PWD = "password";

    private static final ClassPathXmlApplicationContext CTX =
            new ClassPathXmlApplicationContext("applicationContext.xml");

    private static final RestClientFactoryBean restClientFactory = CTX.getBean(RestClientFactoryBean.class);

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
        attr.getValues().add(value);
        return attr;
    }

    private static AttributeMod attributeMod(final String schema, final String valueToBeAdded) {
        AttributeMod attr = new AttributeMod();
        attr.setSchema(schema);
        attr.getValuesToBeAdded().add(valueToBeAdded);
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

        roleTO.getAttributes().add(attributeTO("icon", "anIcon"));

        roleTO.getResources().add("resource-ldap");
        return roleTO;
    }

    public static UserTO getSampleTO(final String email) {
        final String uid = email;
        final UserTO userTO = new UserTO();
        userTO.setPassword("password123");
        userTO.setUsername(uid);

        userTO.getAttributes().add(attributeTO("fullname", uid));
        userTO.getAttributes().add(attributeTO("firstname", uid));
        userTO.getAttributes().add(attributeTO("surname", "surname"));
        userTO.getAttributes().add(attributeTO("type", "a type"));
        userTO.getAttributes().add(attributeTO("userId", uid));
        userTO.getAttributes().add(attributeTO("email", uid));
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        userTO.getAttributes().add(attributeTO("loginDate", sdf.format(new Date())));
        userTO.getDerivedAttributes().add(attributeTO("cn", null));
        userTO.getVirtualAttributes().add(attributeTO("virtualdata", "virtualvalue"));
        return userTO;
    }

    private static UserTO getUniqueSampleTO(final String email) {
        return getSampleTO(getUUIDString() + email);
    }

    private static <T> T getObject(final Response response, final Class<T> type, final Object serviceProxy) {
        String location = response.getLocation().toString();
        WebClient webClient = WebClient.fromClient(WebClient.client(serviceProxy));
        webClient.to(location, false);

        return webClient.get(type);
    }

    private static void init() {
        userService = restClientFactory.createServiceInstance(UserService.class, ADMIN_ID, ADMIN_PWD);
        userWorkflowService = restClientFactory.createServiceInstance(UserWorkflowService.class, ADMIN_ID, ADMIN_PWD);
        roleService = restClientFactory.createServiceInstance(RoleService.class, ADMIN_ID, ADMIN_PWD);
        resourceService = restClientFactory.createServiceInstance(ResourceService.class, ADMIN_ID, ADMIN_PWD);
        entitlementService = restClientFactory.createServiceInstance(EntitlementService.class, ADMIN_ID, ADMIN_PWD);
        configurationService = restClientFactory.createServiceInstance(ConfigurationService.class, ADMIN_ID, ADMIN_PWD);
        connectorService = restClientFactory.createServiceInstance(ConnectorService.class, ADMIN_ID, ADMIN_PWD);
        loggerService = restClientFactory.createServiceInstance(LoggerService.class, ADMIN_ID, ADMIN_PWD);
        reportService = restClientFactory.createServiceInstance(ReportService.class, ADMIN_ID, ADMIN_PWD);
        taskService = restClientFactory.createServiceInstance(TaskService.class, ADMIN_ID, ADMIN_PWD);
        policyService = restClientFactory.createServiceInstance(PolicyService.class, ADMIN_ID, ADMIN_PWD);
        workflowService = restClientFactory.createServiceInstance(WorkflowService.class, ADMIN_ID, ADMIN_PWD);
        notificationService = restClientFactory.createServiceInstance(NotificationService.class, ADMIN_ID, ADMIN_PWD);
        schemaService = restClientFactory.createServiceInstance(SchemaService.class, ADMIN_ID, ADMIN_PWD);
        userRequestService = restClientFactory.createServiceInstance(UserRequestService.class, ADMIN_ID, ADMIN_PWD);
    }

    private static TaskExecTO execSyncTask(final Long taskId, final int maxWaitSeconds,
            final boolean dryRun) {

        AbstractTaskTO taskTO = taskService.read(taskId);
        assertNotNull(taskTO);
        assertNotNull(taskTO.getExecutions());

        final int preSyncSize = taskTO.getExecutions().size();
        final TaskExecTO execution = taskService.execute(taskTO.getId(), dryRun);
        assertEquals("JOB_FIRED", execution.getStatus());

        int i = 0;
        int maxit = maxWaitSeconds;

        // wait for sync completion (executions incremented)
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            taskTO = taskService.read(taskTO.getId());

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
        final Response response = userService.create(userTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(UserTO.class);
    }

    private static RoleTO createRole(final RoleTO roleTO) {
        final Response response = roleService.create(roleTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(RoleTO.class);
    }

    public static void main(final String[] args)
            throws Exception {

        init();
        // *do* something
        System.out.println("FFFFFF\n" + loggerService.list(LoggerType.LOG));

    }
}
