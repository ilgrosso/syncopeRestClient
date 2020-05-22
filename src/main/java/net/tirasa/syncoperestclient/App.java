package net.tirasa.syncoperestclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.ws.Response;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.apache.syncope.common.lib.Attr;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.request.AnyObjectCR;
import org.apache.syncope.common.lib.request.AnyObjectUR;
import org.apache.syncope.common.lib.request.AttrPatch;
import org.apache.syncope.common.lib.request.GroupCR;
import org.apache.syncope.common.lib.request.GroupUR;
import org.apache.syncope.common.lib.request.UserCR;
import org.apache.syncope.common.lib.request.UserUR;
import org.apache.syncope.common.lib.to.SchemaTO;
import org.apache.syncope.common.lib.to.TaskTO;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.syncope.common.lib.to.ExecTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.lib.types.TaskType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.ExecuteQuery;
import org.apache.syncope.common.rest.api.service.AnyObjectService;
import org.apache.syncope.common.rest.api.service.AnyTypeClassService;
import org.apache.syncope.common.rest.api.service.AnyTypeService;
import org.apache.syncope.common.rest.api.service.ApplicationService;
import org.apache.syncope.common.rest.api.service.AuthModuleService;
import org.apache.syncope.common.rest.api.service.BpmnProcessService;
import org.apache.syncope.common.rest.api.service.ClientAppService;
import org.apache.syncope.common.rest.api.service.ConnectorService;
import org.apache.syncope.common.rest.api.service.DynRealmService;
import org.apache.syncope.common.rest.api.service.GatewayRouteService;
import org.apache.syncope.common.rest.api.service.LoggerService;
import org.apache.syncope.common.rest.api.service.NotificationService;
import org.apache.syncope.common.rest.api.service.PolicyService;
import org.apache.syncope.common.rest.api.service.ReportService;
import org.apache.syncope.common.rest.api.service.ResourceService;
import org.apache.syncope.common.rest.api.service.GroupService;
import org.apache.syncope.common.rest.api.service.ImplementationService;
import org.apache.syncope.common.rest.api.service.MailTemplateService;
import org.apache.syncope.common.rest.api.service.RealmService;
import org.apache.syncope.common.rest.api.service.ReconciliationService;
import org.apache.syncope.common.rest.api.service.RelationshipTypeService;
import org.apache.syncope.common.rest.api.service.RemediationService;
import org.apache.syncope.common.rest.api.service.ReportTemplateService;
import org.apache.syncope.common.rest.api.service.RoleService;
import org.apache.syncope.common.rest.api.service.SAML2IdPMetadataConfService;
import org.apache.syncope.common.rest.api.service.SAML2SPKeystoreConfService;
import org.apache.syncope.common.rest.api.service.SAML2SPMetadataConfService;
import org.apache.syncope.common.rest.api.service.SchemaService;
import org.apache.syncope.common.rest.api.service.SecurityQuestionService;
import org.apache.syncope.common.rest.api.service.SyncopeService;
import org.apache.syncope.common.rest.api.service.TaskService;
import org.apache.syncope.common.rest.api.service.UserRequestService;
import org.apache.syncope.common.rest.api.service.UserSelfService;
import org.apache.syncope.common.rest.api.service.UserService;
import org.apache.syncope.common.rest.api.service.UserWorkflowTaskService;
import org.apache.syncope.common.rest.api.service.wa.SAML2IdPMetadataService;
import org.apache.syncope.common.rest.api.service.wa.SAML2SPKeystoreService;
import org.apache.syncope.common.rest.api.service.wa.SAML2SPMetadataService;

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

    private static final String ADMIN_UNAME = "admin";

    private static final String ADMIN_PWD = "password";

    private static final String ANONYMOUS_UNAME = "anonymous";

    private static final String ANONYMOUS_KEY = "anonymousKey";

    private static SyncopeClientFactoryBean CLIENT_FACTORY;

    private static SyncopeClient CLIENT;

    private static SyncopeService syncopeService;

    private static ApplicationService applicationService;

    private static AnyTypeClassService anyTypeClassService;

    private static AnyTypeService anyTypeService;

    private static RelationshipTypeService relationshipTypeService;

    private static RealmService realmService;

    private static AnyObjectService anyObjectService;

    private static RoleService roleService;

    private static DynRealmService dynRealmService;

    private static UserService userService;

    private static UserSelfService userSelfService;

    private static UserRequestService userRequestService;

    private static UserWorkflowTaskService userWorkflowTaskService;

    private static GroupService groupService;

    private static ResourceService resourceService;

    private static ConnectorService connectorService;

    private static LoggerService loggerService;

    private static ReportTemplateService reportTemplateService;

    private static ReportService reportService;

    private static TaskService taskService;

    private static ReconciliationService reconciliationService;

    private static BpmnProcessService bpmnProcessService;

    private static MailTemplateService mailTemplateService;

    private static NotificationService notificationService;

    private static SchemaService schemaService;

    private static PolicyService policyService;

    private static AuthModuleService authModuleService;

    private static SAML2SPMetadataService saml2SPMetadataService;

    private static SAML2SPMetadataConfService saml2SPMetadataConfService;

    private static SAML2SPKeystoreService saml2SPKeystoreService;

    private static SAML2SPKeystoreConfService saml2SPKeystoreConfService;

    private static SAML2IdPMetadataService saml2IdPMetadataService;

    private static SAML2IdPMetadataConfService saml2IdPMetadataConfService;

    private static SecurityQuestionService securityQuestionService;

    private static ImplementationService implementationService;

    private static RemediationService remediationService;

    private static GatewayRouteService gatewayRouteService;

    private static ClientAppService clientAppService;

    private static Attr attr(final String schema, final String value) {
        return new Attr.Builder(schema).value(value).build();
    }

    private static AttrPatch attrAddReplacePatch(final String schema, final String value) {
        return new AttrPatch.Builder(attr(schema, value)).build();
    }

    private static String getUUIDString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static AnyObjectTO getSampleAnyObjectTO(final String location) {
        AnyObjectTO anyObjectTO = new AnyObjectTO();
        anyObjectTO.setRealm(SyncopeConstants.ROOT_REALM);
        anyObjectTO.setType("PRINTER");
        anyObjectTO.getPlainAttrs().add(attr("location", location + getUUIDString()));

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

        groupTO.getPlainAttrs().add(attr("icon", "anIcon"));

        groupTO.getResources().add(RESOURCE_NAME_LDAP);
        return groupTO;
    }

    public static UserCR getSampleUserCR(final String email) {
        return new UserCR.Builder(SyncopeConstants.ROOT_REALM, email).
                password("password123").
                plainAttr(attr("fullname", email)).
                plainAttr(attr("firstname", email)).
                plainAttr(attr("surname", "surname")).
                plainAttr(attr("ctype", "a type")).
                plainAttr(attr("userId", email)).
                plainAttr(attr("email", email)).
                plainAttr(attr("loginDate", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date()))).
                build();
    }

    private static UserCR getUniqueSampleUserCR(final String email) {
        return getSampleUserCR(getUUIDString() + email);
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

    private static ProvisioningResult<UserTO> createUser(final UserCR req) {
        Response response = userService.create(req);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = CLIENT_FACTORY.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return response.readEntity(new GenericType<ProvisioningResult<UserTO>>() {
        });
    }

    private static ProvisioningResult<UserTO> updateUser(final UserUR req) {
        return userService.update(req).
                readEntity(new GenericType<ProvisioningResult<UserTO>>() {
                });
    }

    private static ProvisioningResult<UserTO> deleteUser(final String key) {
        return userService.delete(key).
                readEntity(new GenericType<ProvisioningResult<UserTO>>() {
                });
    }

    private static ProvisioningResult<AnyObjectTO> createAnyObject(final AnyObjectCR req) {
        Response response = anyObjectService.create(req);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = CLIENT_FACTORY.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return response.readEntity(new GenericType<ProvisioningResult<AnyObjectTO>>() {
        });
    }

    private static ProvisioningResult<AnyObjectTO> updateAnyObject(final AnyObjectUR req) {
        return anyObjectService.update(req).
                readEntity(new GenericType<ProvisioningResult<AnyObjectTO>>() {
                });
    }

    private static ProvisioningResult<AnyObjectTO> deleteAnyObject(final String key) {
        return anyObjectService.delete(key).
                readEntity(new GenericType<ProvisioningResult<AnyObjectTO>>() {
                });
    }

    private static ProvisioningResult<GroupTO> createGroup(final GroupCR req) {
        Response response = groupService.create(req);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = CLIENT_FACTORY.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return response.readEntity(new GenericType<ProvisioningResult<GroupTO>>() {
        });
    }

    private static ProvisioningResult<GroupTO> updateGroup(final GroupUR req) {
        return groupService.update(req).
                readEntity(new GenericType<ProvisioningResult<GroupTO>>() {
                });
    }

    private static ProvisioningResult<GroupTO> deleteGroup(final String key) {
        return groupService.delete(key).
                readEntity(new GenericType<ProvisioningResult<GroupTO>>() {
                });
    }

    private static void init() {
        CLIENT_FACTORY = new SyncopeClientFactoryBean().setAddress(ADDRESS).
                setContentType(SyncopeClientFactoryBean.ContentType.YAML);
        CLIENT = CLIENT_FACTORY.create(ADMIN_UNAME, ADMIN_PWD);

        syncopeService = CLIENT.getService(SyncopeService.class);
        applicationService = CLIENT.getService(ApplicationService.class);
        anyTypeClassService = CLIENT.getService(AnyTypeClassService.class);
        anyTypeService = CLIENT.getService(AnyTypeService.class);
        relationshipTypeService = CLIENT.getService(RelationshipTypeService.class);
        realmService = CLIENT.getService(RealmService.class);
        anyObjectService = CLIENT.getService(AnyObjectService.class);
        roleService = CLIENT.getService(RoleService.class);
        dynRealmService = CLIENT.getService(DynRealmService.class);
        userService = CLIENT.getService(UserService.class);
        userSelfService = CLIENT.getService(UserSelfService.class);
        userRequestService = CLIENT.getService(UserRequestService.class);
        userWorkflowTaskService = CLIENT.getService(UserWorkflowTaskService.class);
        groupService = CLIENT.getService(GroupService.class);
        resourceService = CLIENT.getService(ResourceService.class);
        connectorService = CLIENT.getService(ConnectorService.class);
        loggerService = CLIENT.getService(LoggerService.class);
        reportTemplateService = CLIENT.getService(ReportTemplateService.class);
        reportService = CLIENT.getService(ReportService.class);
        taskService = CLIENT.getService(TaskService.class);
        reconciliationService = CLIENT.getService(ReconciliationService.class);
        policyService = CLIENT.getService(PolicyService.class);
        bpmnProcessService = CLIENT.getService(BpmnProcessService.class);
        mailTemplateService = CLIENT.getService(MailTemplateService.class);
        notificationService = CLIENT.getService(NotificationService.class);
        schemaService = CLIENT.getService(SchemaService.class);
        securityQuestionService = CLIENT.getService(SecurityQuestionService.class);
        implementationService = CLIENT.getService(ImplementationService.class);
        remediationService = CLIENT.getService(RemediationService.class);
        gatewayRouteService = CLIENT.getService(GatewayRouteService.class);
        clientAppService = CLIENT.getService(ClientAppService.class);
        authModuleService = CLIENT.getService(AuthModuleService.class);
        saml2SPMetadataService = CLIENT.getService(SAML2SPMetadataService.class);
        saml2SPMetadataConfService = CLIENT.getService(SAML2SPMetadataConfService.class);
        saml2IdPMetadataService = CLIENT.getService(SAML2IdPMetadataService.class);
        saml2IdPMetadataConfService = CLIENT.getService(SAML2IdPMetadataConfService.class);
        saml2SPKeystoreService = CLIENT.getService(SAML2SPKeystoreService.class);
        saml2SPKeystoreConfService = CLIENT.getService(SAML2SPKeystoreConfService.class);
    }

    public static void main(final String[] args) throws Exception {
        init();

        // *do* something
    }
}
