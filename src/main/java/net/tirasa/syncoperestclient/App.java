package net.tirasa.syncoperestclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.patch.AnyObjectPatch;
import org.apache.syncope.common.lib.patch.AttrPatch;
import org.apache.syncope.common.lib.patch.GroupPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.to.SchemaTO;
import org.apache.syncope.common.lib.to.TaskTO;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.ExecTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.PatchOperation;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.lib.types.TaskType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.ExecuteQuery;
import org.apache.syncope.common.rest.api.service.AnyObjectService;
import org.apache.syncope.common.rest.api.service.AnyTypeClassService;
import org.apache.syncope.common.rest.api.service.AnyTypeService;
import org.apache.syncope.common.rest.api.service.ConfigurationService;
import org.apache.syncope.common.rest.api.service.ConnectorService;
import org.apache.syncope.common.rest.api.service.DomainService;
import org.apache.syncope.common.rest.api.service.LoggerService;
import org.apache.syncope.common.rest.api.service.NotificationService;
import org.apache.syncope.common.rest.api.service.PolicyService;
import org.apache.syncope.common.rest.api.service.ReportService;
import org.apache.syncope.common.rest.api.service.ResourceService;
import org.apache.syncope.common.rest.api.service.GroupService;
import org.apache.syncope.common.rest.api.service.ImplementationService;
import org.apache.syncope.common.rest.api.service.MailTemplateService;
import org.apache.syncope.common.rest.api.service.RealmService;
import org.apache.syncope.common.rest.api.service.RelationshipTypeService;
import org.apache.syncope.common.rest.api.service.ReportTemplateService;
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

    private static final String ADMIN_UNAME = "admin";

    private static final String ADMIN_PWD = "password";

    private static final String ANONYMOUS_UNAME = "anonymous";

    private static final String ANONYMOUS_KEY = "anonymousKey";

    private static final SyncopeClientFactoryBean CLIENT_FACTORY = new SyncopeClientFactoryBean().setAddress(ADDRESS);

    private static final SyncopeClient CLIENT = CLIENT_FACTORY.create(ADMIN_UNAME, ADMIN_PWD);

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

    private static final String RESOURCE_NAME_DBSCRIPTED = "resource-db-scripted";

    private static SyncopeService syncopeService;

    private static DomainService domainService;

    private static ImplementationService implementationService;

    private static AnyTypeClassService anyTypeClassService;

    private static AnyTypeService anyTypeService;

    private static RelationshipTypeService relationshipTypeService;

    private static RealmService realmService;

    private static AnyObjectService anyObjectService;

    private static RoleService roleService;

    private static UserService userService;

    private static GroupService groupService;

    private static ResourceService resourceService;

    private static ConfigurationService configurationService;

    private static ConnectorService connectorService;

    private static LoggerService loggerService;

    private static ReportTemplateService reportTemplateService;

    private static ReportService reportService;

    private static TaskService taskService;

    private static WorkflowService workflowService;

    private static MailTemplateService mailTemplateService;

    private static NotificationService notificationService;

    private static SchemaService schemaService;

    private static UserSelfService userSelfService;

    private static UserWorkflowService userWorkflowService;

    private static PolicyService policyService;

    private static SecurityQuestionService securityQuestionService;

    private static AttrTO attrTO(final String schema, final String value) {
        return new AttrTO.Builder().schema(schema).value(value).build();
    }

    private static AttrPatch attrAddReplacePatch(final String schema, final String value) {
        return new AttrPatch.Builder().operation(PatchOperation.ADD_REPLACE).attrTO(attrTO(schema, value)).build();
    }

    private static String getUUIDString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static AnyObjectTO getSampleAnyObjectTO(final String location) {
        AnyObjectTO anyObjectTO = new AnyObjectTO();
        anyObjectTO.setRealm(SyncopeConstants.ROOT_REALM);
        anyObjectTO.setType("PRINTER");
        anyObjectTO.getPlainAttrs().add(attrTO("location", location + getUUIDString()));

        anyObjectTO.getResources().add(RESOURCE_NAME_DBSCRIPTED);
        return anyObjectTO;
    }

    private static GroupTO getBasicSampleGroupTO(final String name) {
        final GroupTO groupTO = new GroupTO();
        groupTO.setName(name + getUUIDString());
        groupTO.setRealm(SyncopeConstants.ROOT_REALM);
        return groupTO;
    }

    private static GroupTO getSampleGroupTO(final String name) {
        final GroupTO groupTO = getBasicSampleGroupTO(name);

        groupTO.getPlainAttrs().add(attrTO("icon", "anIcon"));

        groupTO.getResources().add(RESOURCE_NAME_LDAP);
        return groupTO;
    }

    public static UserTO getSampleUserTO(final String email) {
        String uid = email;
        UserTO userTO = new UserTO();
        userTO.setRealm("/");
        userTO.setPassword("password123");
        userTO.setUsername(uid);

        userTO.getPlainAttrs().add(attrTO("fullname", uid));
        userTO.getPlainAttrs().add(attrTO("firstname", uid));
        userTO.getPlainAttrs().add(attrTO("surname", "surname"));
        userTO.getPlainAttrs().add(attrTO("type", "a type"));
        userTO.getPlainAttrs().add(attrTO("userId", uid));
        userTO.getPlainAttrs().add(attrTO("email", uid));
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        userTO.getPlainAttrs().add(attrTO("loginDate", sdf.format(new Date())));
        userTO.getDerAttrs().add(attrTO("cn", null));
        userTO.getVirAttrs().add(attrTO("virtualdata", "virtualvalue"));
        return userTO;
    }

    private static UserTO getUniqueSampleUserTO(final String email) {
        return getSampleUserTO(getUUIDString() + email);
    }

    public static ExecTO execProvisioningTask(
            final TaskService taskService, final TaskType type, final String taskKey,
            final int maxWaitSeconds, final boolean dryRun) {

        TaskTO taskTO = taskService.read(type, taskKey, true);
        assertNotNull(taskTO);
        assertNotNull(taskTO.getExecutions());

        int preSyncSize = taskTO.getExecutions().size();
        ExecTO execution = taskService.execute(
                new ExecuteQuery.Builder().key(taskTO.getKey()).dryRun(dryRun).build());
        assertEquals("JOB_FIRED", execution.getStatus());

        int i = 0;
        int maxit = maxWaitSeconds;

        // wait for completion (executions incremented)
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            taskTO = taskService.read(type, taskTO.getKey(), true);

            assertNotNull(taskTO);
            assertNotNull(taskTO.getExecutions());

            i++;
        } while (preSyncSize == taskTO.getExecutions().size() && i < maxit);
        if (i == maxit) {
            fail("Timeout when executing task " + taskKey);
        }
        return taskTO.getExecutions().get(taskTO.getExecutions().size() - 1);
    }

    private static <T> T getObject(final URI location, final Class<?> serviceClass, final Class<T> resultClass) {
        WebClient webClient = WebClient.fromClient(WebClient.client(CLIENT.getService(serviceClass)));
        webClient.accept(CLIENT_FACTORY.getContentType().getMediaType()).to(location.toASCIIString(), false);

        return webClient.
                header(RESTHeaders.DOMAIN, CLIENT.getDomain()).
                header(HttpHeaders.AUTHORIZATION, "Bearer " + CLIENT.getJWT()).
                get(resultClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SchemaTO> T createSchema(final SchemaType type, final T schemaTO) {
        Response response = CLIENT.getService(SchemaService.class).create(type, schemaTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }

        return (T) getObject(response.getLocation(), SchemaService.class, schemaTO.getClass());
    }

    private static ProvisioningResult<UserTO> createUser(final UserTO userTO) {
        return createUser(userTO, true);
    }

    private static ProvisioningResult<UserTO> createUser(final UserTO userTO, final boolean storePassword) {
        Response response = userService.create(userTO, storePassword);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = CLIENT_FACTORY.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return response.readEntity(new GenericType<ProvisioningResult<UserTO>>() {
        });
    }

    private static ProvisioningResult<UserTO> updateUser(final UserPatch userPatch) {
        return userService.update(userPatch).
                readEntity(new GenericType<ProvisioningResult<UserTO>>() {
                });
    }

    private static ProvisioningResult<UserTO> deleteUser(final String key) {
        return userService.delete(key).
                readEntity(new GenericType<ProvisioningResult<UserTO>>() {
                });
    }

    private static ProvisioningResult<AnyObjectTO> createAnyObject(final AnyObjectTO anyObjectTO) {
        Response response = anyObjectService.create(anyObjectTO);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = CLIENT_FACTORY.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return response.readEntity(new GenericType<ProvisioningResult<AnyObjectTO>>() {
        });
    }

    private static ProvisioningResult<AnyObjectTO> updateAnyObject(final AnyObjectPatch anyObjectPatch) {
        return anyObjectService.update(anyObjectPatch).
                readEntity(new GenericType<ProvisioningResult<AnyObjectTO>>() {
                });
    }

    private static ProvisioningResult<AnyObjectTO> deleteAnyObject(final String key) {
        return anyObjectService.delete(key).
                readEntity(new GenericType<ProvisioningResult<AnyObjectTO>>() {
                });
    }

    private static ProvisioningResult<GroupTO> createGroup(final GroupTO groupTO) {
        Response response = groupService.create(groupTO);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = CLIENT_FACTORY.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return response.readEntity(new GenericType<ProvisioningResult<GroupTO>>() {
        });
    }

    private static ProvisioningResult<GroupTO> updateGroup(final GroupPatch groupPatch) {
        return groupService.update(groupPatch).
                readEntity(new GenericType<ProvisioningResult<GroupTO>>() {
                });
    }

    private static ProvisioningResult<GroupTO> deleteGroup(final String key) {
        return groupService.delete(key).
                readEntity(new GenericType<ProvisioningResult<GroupTO>>() {
                });
    }

    private static void init() {
        syncopeService = CLIENT.getService(SyncopeService.class);
        domainService = CLIENT.getService(DomainService.class);
        implementationService = CLIENT.getService(ImplementationService.class);
        anyTypeClassService = CLIENT.getService(AnyTypeClassService.class);
        anyTypeService = CLIENT.getService(AnyTypeService.class);
        relationshipTypeService = CLIENT.getService(RelationshipTypeService.class);
        realmService = CLIENT.getService(RealmService.class);
        anyObjectService = CLIENT.getService(AnyObjectService.class);
        roleService = CLIENT.getService(RoleService.class);
        userService = CLIENT.getService(UserService.class);
        userWorkflowService = CLIENT.getService(UserWorkflowService.class);
        groupService = CLIENT.getService(GroupService.class);
        resourceService = CLIENT.getService(ResourceService.class);
        configurationService = CLIENT.getService(ConfigurationService.class);
        connectorService = CLIENT.getService(ConnectorService.class);
        loggerService = CLIENT.getService(LoggerService.class);
        reportTemplateService = CLIENT.getService(ReportTemplateService.class);
        reportService = CLIENT.getService(ReportService.class);
        taskService = CLIENT.getService(TaskService.class);
        policyService = CLIENT.getService(PolicyService.class);
        workflowService = CLIENT.getService(WorkflowService.class);
        mailTemplateService = CLIENT.getService(MailTemplateService.class);
        notificationService = CLIENT.getService(NotificationService.class);
        schemaService = CLIENT.getService(SchemaService.class);
        userSelfService = CLIENT.getService(UserSelfService.class);
        securityQuestionService = CLIENT.getService(SecurityQuestionService.class);
    }

    public static void main(final String[] args) {
        init();

        // *do* something
    }
}
