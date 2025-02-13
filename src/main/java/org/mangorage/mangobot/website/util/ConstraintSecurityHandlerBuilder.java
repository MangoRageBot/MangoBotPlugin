package org.mangorage.mangobot.website.util;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

import java.util.Set;

public final class ConstraintSecurityHandlerBuilder {
    public static ConstraintSecurityHandlerBuilder create() {
        return new ConstraintSecurityHandlerBuilder();
    }

    private final HashLoginService hashLoginService = new HashLoginService("MyRealm");
    private final UserStore userStore = new UserStore();
    private final ConstraintSecurityHandler handler = new ConstraintSecurityHandler();

    ConstraintSecurityHandlerBuilder() {
        handler.setLoginService(hashLoginService);
        hashLoginService.setUserStore(userStore);
    }

    public ConstraintSecurityHandlerBuilder lock(Set<String> permittedRoles, String pathSpec) {
        Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(permittedRoles.toArray(String[]::new));

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(pathSpec);

        handler.addConstraintMapping(mapping);

        return this;
    }

    public ConstraintSecurityHandlerBuilder setFullValidate(boolean validate) {
        hashLoginService.setFullValidate(validate);
        return this;
    }

    public ConstraintSecurityHandlerBuilder addUser(String userName, String password, Set<String> providedRoles) {
        userStore.addUser(userName, Credential.getCredential(password), providedRoles.toArray(String[]::new));
        return this;
    }

    public ConstraintSecurityHandlerBuilder setAuthenticator(Authenticator authenticator) {
        handler.setAuthenticator(authenticator);
        return this;
    }

    public ConstraintSecurityHandler getConstraintSecurityHandler() {
        return handler;
    }
}
