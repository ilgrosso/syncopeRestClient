package net.tirasa.syncoperestclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.apache.syncope.common.lib.mod.AttrMod;
import org.apache.syncope.common.lib.mod.UserMod;
import org.apache.syncope.common.lib.to.AbstractSchemaTO;
import org.apache.syncope.common.lib.to.AbstractTaskTO;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.RoleTO;
import org.apache.syncope.common.lib.to.TaskExecTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.AttributableType;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.service.ConfigurationService;
import org.apache.syncope.common.rest.api.service.ConnectorService;
import org.apache.syncope.common.rest.api.service.EntitlementService;
import org.apache.syncope.common.rest.api.service.LoggerService;
import org.apache.syncope.common.rest.api.service.NotificationService;
import org.apache.syncope.common.rest.api.service.PolicyService;
import org.apache.syncope.common.rest.api.service.ReportService;
import org.apache.syncope.common.rest.api.service.ResourceService;
import org.apache.syncope.common.rest.api.service.RoleService;
import org.apache.syncope.common.rest.api.service.SchemaService;
import org.apache.syncope.common.rest.api.service.SecurityQuestionService;
import org.apache.syncope.common.rest.api.service.SyncopeService;
import org.apache.syncope.common.rest.api.service.TaskService;
import org.apache.syncope.common.rest.api.service.UserSelfService;
import org.apache.syncope.common.rest.api.service.UserService;
import org.apache.syncope.common.rest.api.service.UserWorkflowService;
import org.apache.syncope.common.rest.api.service.WorkflowService;

public class App {

    private static final String ADDRESS;

    static {
        final InputStream configuration = App.class.getResourceAsStream("/configuration.properties");
        final Properties prop = new Properties();
        try {
            prop.load(configuration);
            ADDRESS = prop.getProperty("address");
        } catch (IOException e) {
            throw new IllegalStateException("Could not read address from configuration.properties", e);
        } finally {
            if (configuration != null) {
                try {
                    configuration.close();
                } catch (IOException ignore) {
                    //ignore
                }
            }
        }
    }

    private static final String ADMIN_ID = "admin";

    private static final String ADMIN_PWD = "password";

    private static final SyncopeClientFactoryBean clientFactory = new SyncopeClientFactoryBean().setAddress(ADDRESS);

    private static final SyncopeClient client = clientFactory.create(ADMIN_ID, ADMIN_PWD);

    private static final String RESOURCE_NAME_WS1 = "ws-target-resource-1";

    private static final String RESOURCE_NAME_WS2 = "ws-target-resource-2";

    private static final String RESOURCE_NAME_LDAP = "resource-ldap";

    private static final String RESOURCE_NAME_TESTDB = "resource-testdb";

    private static final String RESOURCE_NAME_TESTDB2 = "resource-testdb2";

    private static final String RESOURCE_NAME_CSV = "resource-csv";

    private static final String RESOURCE_NAME_DBSYNC = "resource-db-sync";

    private static final String RESOURCE_NAME_DBVIRATTR = "resource-db-virattr";

    private static final String RESOURCE_NAME_NOPROPAGATION = "ws-target-resource-nopropagation";

    private static final String RESOURCE_NAME_NOPROPAGATION2 = "ws-target-resource-nopropagation2";

    private static final String RESOURCE_NAME_NOPROPAGATION3 = "ws-target-resource-nopropagation3";

    private static final String RESOURCE_NAME_NOPROPAGATION4 = "ws-target-resource-nopropagation4";

    private static final String RESOURCE_NAME_RESETSYNCTOKEN = "ws-target-resource-update-resetsynctoken";

    private static final String RESOURCE_NAME_TIMEOUT = "ws-target-resource-timeout";

    private static final String RESOURCE_NAME_MAPPINGS1 = "ws-target-resource-list-mappings-1";

    private static final String RESOURCE_NAME_MAPPINGS2 = "ws-target-resource-list-mappings-2";

    private static SyncopeService syncopeService;

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

    private static UserSelfService userSelfService;

    private static UserWorkflowService userWorkflowService;

    private static PolicyService policyService;

    private static SecurityQuestionService securityQuestionService;

    private static AttrTO attrTO(final String schema, final String value) {
        final AttrTO attr = new AttrTO();
        attr.setSchema(schema);
        attr.getValues().add(value);
        return attr;
    }

    private static AttrMod attrMod(final String schema, final String valueToBeAdded) {
        final AttrMod attr = new AttrMod();
        attr.setSchema(schema);
        attr.getValuesToBeAdded().add(valueToBeAdded);
        return attr;
    }

    private static String getUUIDString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static RoleTO buildBasicRoleTO(final String name) {
        final RoleTO roleTO = new RoleTO();
        roleTO.setName(name + getUUIDString());
        roleTO.setParent(8L);
        return roleTO;
    }

    private static RoleTO buildRoleTO(final String name) {
        final RoleTO roleTO = buildBasicRoleTO(name);

        // verify inheritance password and account policies
        roleTO.setInheritAccountPolicy(false);
        // not inherited so setter execution shouldn't be ignored
        roleTO.setAccountPolicy(6L);

        roleTO.setInheritPasswordPolicy(true);
        // inherited so setter execution should be ignored
        roleTO.setPasswordPolicy(2L);

        roleTO.getRPlainAttrTemplates().add("icon");
        roleTO.getPlainAttrs().add(attrTO("icon", "anIcon"));

        roleTO.getResources().add("resource-ldap");
        return roleTO;
    }

    public static UserTO getSampleTO(final String email) {
        final String uid = email;
        final UserTO userTO = new UserTO();
        userTO.setPassword("password123");
        userTO.setUsername(uid);

        userTO.getPlainAttrs().add(attrTO("fullname", uid));
        userTO.getPlainAttrs().add(attrTO("firstname", uid));
        userTO.getPlainAttrs().add(attrTO("surname", "surname"));
        userTO.getPlainAttrs().add(attrTO("type", "a type"));
        userTO.getPlainAttrs().add(attrTO("userId", uid));
        userTO.getPlainAttrs().add(attrTO("email", uid));
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        userTO.getPlainAttrs().add(attrTO("loginDate", sdf.format(new Date())));
        userTO.getDerAttrs().add(attrTO("cn", null));
        userTO.getVirAttrs().add(attrTO("virtualdata", "virtualvalue"));
        return userTO;
    }

    private static UserTO getUniqueSampleTO(final String email) {
        return getSampleTO(getUUIDString() + email);
    }

    private static TaskExecTO execSyncTask(final Long taskId, final int maxWaitSeconds,
            final boolean dryRun) {

        AbstractTaskTO taskTO = client.getService(TaskService.class).read(taskId);

        final int preSyncSize = taskTO.getExecutions().size();
        final TaskExecTO execution = client.getService(TaskService.class).execute(taskTO.getKey(), dryRun);

        int i = 0;
        int maxit = maxWaitSeconds;

        // wait for sync completion (executions incremented)
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            taskTO = client.getService(TaskService.class).read(taskTO.getKey());

            i++;
        } while (preSyncSize == taskTO.getExecutions().size() && i < maxit);
        if (i == maxit) {
            throw new RuntimeException("Timeout when executing task " + taskId);
        }
        return taskTO.getExecutions().get(0);
    }

    private static <T> T getObject(final URI location, final Class<?> serviceClass, final Class<T> resultClass) {
        WebClient webClient = WebClient.fromClient(WebClient.client(client.getService(serviceClass)));
        webClient.accept(clientFactory.getContentType().getMediaType()).to(location.toASCIIString(), false);

        return webClient.get(resultClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractSchemaTO> T createSchema(final AttributableType kind,
            final SchemaType type, final T schemaTO) {

        Response response = client.getService(SchemaService.class).create(kind, type, schemaTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }

        return (T) getObject(response.getLocation(), SchemaService.class, schemaTO.getClass());
    }

    private static UserTO createUser(final UserTO userTO) {
        final Response response = client.getService(UserService.class).create(userTO, true);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(UserTO.class);
    }

    private static UserTO readUser(final String username) {
        return userService.read(Long.valueOf(
                userService.getUserId(username).getHeaderString(RESTHeaders.USER_ID)));
    }

    private static UserTO updateUser(final UserMod userMod) {
        return userService.update(userMod.getKey(), userMod).readEntity(UserTO.class);
    }

    private static UserTO deleteUser(final Long id) {
        return userService.delete(id).readEntity(UserTO.class);
    }

    private static RoleTO createRole(final RoleTO roleTO) {
        final Response response = client.getService(RoleService.class).create(roleTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }

        return getObject(response.getLocation(), RoleService.class, RoleTO.class);
    }

    private static void init() {
        syncopeService = client.getService(SyncopeService.class);
        userService = client.getService(UserService.class);
        userWorkflowService = client.getService(UserWorkflowService.class);
        roleService = client.getService(RoleService.class);
        resourceService = client.getService(ResourceService.class);
        entitlementService = client.getService(EntitlementService.class);
        configurationService = client.getService(ConfigurationService.class);
        connectorService = client.getService(ConnectorService.class);
        loggerService = client.getService(LoggerService.class);
        reportService = client.getService(ReportService.class);
        taskService = client.getService(TaskService.class);
        policyService = client.getService(PolicyService.class);
        workflowService = client.getService(WorkflowService.class);
        notificationService = client.getService(NotificationService.class);
        schemaService = client.getService(SchemaService.class);
        userSelfService = client.getService(UserSelfService.class);
        securityQuestionService = client.getService(SecurityQuestionService.class);
    }

    public static void main(final String[] args) {
        init();

        // *do* something
    }
}
