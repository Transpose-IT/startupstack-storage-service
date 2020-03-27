package dev.startupstack.storageservice;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.jboss.logging.Logger;
import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class QuarkusOIDCTenantResolver implements TenantResolver {

    private static final Logger LOG = Logger.getLogger(QuarkusOIDCTenantResolver.class);

    @Override
    public String resolve(RoutingContext context) {
        DecodedJWT jwt;

        try {
            jwt = JWT.decode(context.request().getHeader("Authorization").split(" ")[1]);
        } catch (JWTDecodeException exception){
            // We ignore this error as Quarkus OIDC handles invalid tokens for us
            return null;
        }

        if (new Date(java.lang.System.currentTimeMillis()).compareTo(jwt.getExpiresAt()) > 0) {
            LOG.warnf("Token expired for user: %s/%s", jwt.getClaim("tenant_id").asString(), jwt.getClaim("upn").asString());
        }

        // At this time we are only interested in intercepting the call, so returning null to go to the default
        return null;
    }
}