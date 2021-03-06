package com.oberasoftware.home.security.jasdb;

import com.oberasoftware.home.security.common.api.TokenStore;
import com.oberasoftware.home.security.common.model.Token;
import com.oberasoftware.jasdb.api.entitymapper.EntityManager;
import nl.renarj.jasdb.api.DBSession;
import nl.renarj.jasdb.api.query.QueryBuilder;
import nl.renarj.jasdb.core.exceptions.JasDBStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author Renze de Vries
 */
@Component
public class JasDBTokenStore implements TokenStore {
    private static final Logger LOG = LoggerFactory.getLogger(JasDBTokenStore.class);

    @Autowired
    private JasDBSessionFactory sessionFactory;

    @Override
    public boolean storeToken(String clientId, Token token) {
        try {
            DBSession session = sessionFactory.createSession();
            session.getEntityManager().persist(token);
            return true;
        } catch (JasDBStorageException e) {
            LOG.error("Unale to persist token", e);
            return false;
        }
    }

    @Override
    public Optional<Token> validateToken(String clientId, String token) {
        try {
            LOG.info("Validating token: {} for clientId: {}", token, clientId);
            DBSession session = sessionFactory.createSession();
            EntityManager entityManager = session.getEntityManager();
            List<Token> tokens = entityManager.findEntities(Token.class, QueryBuilder.createBuilder().field("token")
                    .value(token).field("resourceId").value(clientId));
            if(tokens.size() == 1) {
                LOG.info("Token: {} found", token);
                return Optional.of(tokens.get(0));
            } else {
                LOG.info("No records found for token: {}", token);
            }
        } catch (JasDBStorageException e) {
            LOG.error("Unable to read token", e);
        }

        return Optional.empty();
    }
}
