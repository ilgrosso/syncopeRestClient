package net.tirasa.syncoperestclient;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.syncope.client.SyncopeClient;
import org.apache.syncope.client.SyncopeClientFactoryBean;
import org.apache.syncope.common.mod.AttributeMod;
import org.apache.syncope.common.services.RoleService;
import org.apache.syncope.common.services.TaskService;
import org.apache.syncope.common.services.UserService;
import org.apache.syncope.common.to.AttributeTO;
import org.apache.syncope.common.to.RoleTO;
import org.apache.syncope.common.to.TaskExecTO;
import org.apache.syncope.common.to.AbstractTaskTO;
import org.apache.syncope.common.to.UserTO;

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

    private static final SyncopeClient client =
            new SyncopeClientFactoryBean().setAddress(ADDRESS).create(ADMIN_ID, ADMIN_PWD);

    private static AttributeTO attributeTO(final String schema, final String value) {
        final AttributeTO attr = new AttributeTO();
        attr.setSchema(schema);
        attr.getValues().add(value);
        return attr;
    }

    private static AttributeMod attributeMod(final String schema, final String valueToBeAdded) {
        final AttributeMod attr = new AttributeMod();
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

    private static TaskExecTO execSyncTask(final Long taskId, final int maxWaitSeconds,
            final boolean dryRun) {

        AbstractTaskTO taskTO = client.getService(TaskService.class).read(taskId);

        final int preSyncSize = taskTO.getExecutions().size();
        final TaskExecTO execution = client.getService(TaskService.class).execute(taskTO.getId(), dryRun);

        int i = 0;
        int maxit = maxWaitSeconds;

        // wait for sync completion (executions incremented)
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            taskTO = client.getService(TaskService.class).read(taskTO.getId());

            i++;
        } while (preSyncSize == taskTO.getExecutions().size() && i < maxit);
        if (i == maxit) {
            throw new RuntimeException("Timeout when executing task " + taskId);
        }
        return taskTO.getExecutions().get(0);
    }

    private static UserTO createUser(final UserTO userTO) {
        final Response response = client.getService(UserService.class).create(userTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(UserTO.class);
    }

    private static RoleTO createRole(final RoleTO roleTO) {
        final Response response = client.getService(RoleService.class).create(roleTO);
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new RuntimeException("Bad response: " + response);
        }
        return response.readEntity(RoleTO.class);
    }

    public static void main(final String[] args) {
        // *do* something
    }
}
