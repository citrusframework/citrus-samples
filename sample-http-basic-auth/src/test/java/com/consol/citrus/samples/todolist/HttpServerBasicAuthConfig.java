/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import com.consol.citrus.http.security.*;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.util.security.Credential;
import org.springframework.context.annotation.*;

import javax.security.auth.Subject;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
@Configuration
public class HttpServerBasicAuthConfig {

    private static final String[] USER_ROLES = new String[] { "CitrusRole" };

    @Bean
    public SecurityHandlerFactory basicAuthSecurityHandler() {
        SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
        securityHandlerFactory.setUsers(users());
        securityHandlerFactory.setLoginService(basicAuthLoginService(basicAuthUserStore()));
        securityHandlerFactory.setConstraints(Collections.singletonMap("/todo/*", new BasicAuthConstraint(USER_ROLES)));

        return securityHandlerFactory;
    }

    @Bean
    public HashLoginService basicAuthLoginService(PropertyUserStore basicAuthUserStore) {
        return new HashLoginService() {
            @Override
            protected void doStart() throws Exception {
                setUserStore(basicAuthUserStore);
                basicAuthUserStore.start();

                super.doStart();
            }
        };
    }

    @Bean
    public PropertyUserStore basicAuthUserStore() {
        return new PropertyUserStore() {
            @Override
            protected void loadUsers() throws IOException {
                getKnownUserIdentities().clear();

                for (User user : users()) {
                    Credential credential = Credential.getCredential(user.getPassword());

                    Principal userPrincipal = new AbstractLoginService.UserPrincipal(user.getName(),credential);
                    Subject subject = new Subject();
                    subject.getPrincipals().add(userPrincipal);
                    subject.getPrivateCredentials().add(credential);

                    String[] roleArray = IdentityService.NO_ROLES;
                    if (user.getRoles() != null && user.getRoles().length > 0) {
                        roleArray = user.getRoles();
                    }

                    for (String role : roleArray) {
                        subject.getPrincipals().add(new AbstractLoginService.RolePrincipal(role));
                    }

                    subject.setReadOnly();

                    getKnownUserIdentities().put(user.getName(), getIdentityService().newUserIdentity(subject, userPrincipal, roleArray));
                }
            }
        };
    }

    private List<User> users() {
        return Collections.singletonList(new User("citrus", "secr3t", USER_ROLES));
    }

}
