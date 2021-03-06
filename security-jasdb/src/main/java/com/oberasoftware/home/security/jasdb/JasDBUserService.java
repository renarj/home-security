package com.oberasoftware.home.security.jasdb;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.oberasoftware.home.api.exceptions.RuntimeHomeAutomationException;
import com.oberasoftware.home.security.common.api.UserService;
import com.oberasoftware.home.security.common.model.LocalUser;
import com.oberasoftware.home.security.common.model.User;
import com.oberasoftware.home.util.crypto.CryptoEngine;
import com.oberasoftware.home.util.crypto.CryptoFactory;
import com.oberasoftware.jasdb.api.entitymapper.EntityManager;
import nl.renarj.core.utilities.StringUtils;
import nl.renarj.jasdb.api.DBSession;
import nl.renarj.jasdb.api.query.QueryBuilder;
import nl.renarj.jasdb.core.exceptions.JasDBStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Optional.ofNullable;
import static nl.renarj.jasdb.api.query.QueryBuilder.createBuilder;

/**
 * @author Renze de Vries
 */
@Component
public class JasDBUserService implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(JasDBUserService.class);

    private static final CryptoEngine cryptoEngine = CryptoFactory.getEngine();

    @Autowired
    private JasDBSessionFactory sessionFactory;

    @Override
    public User createUser(String clientId, String password, String email, List<String> roles) {
        try {
            String salt = cryptoEngine.generateSalt();
            String hash = cryptoEngine.hash(salt, password);
            String userId = UUID.randomUUID().toString();
            LocalUser user = new LocalUser(userId, clientId, email, roles, hash, salt);

            DBSession session = sessionFactory.createSession();
            session.getEntityManager().persist(user);
            LOG.debug("Persisted user with id: {} and username: {}", userId, clientId);

            return user;
        } catch (JasDBStorageException e) {
            LOG.error("Unable to store user", e);
            throw new RuntimeHomeAutomationException("Unable to store user data", e);
        }
    }

    @Override
    public User updateUser(String userName, String password, String email, List<String> roles, Map<String, String> metadata) {
        LocalUser user = findInternalUser(userName);
        if(user != null) {
            if(StringUtils.stringNotEmpty(password)) {
                String salt = cryptoEngine.generateSalt();
                String hash = cryptoEngine.hash(salt, password);

                user.setPasswordHash(hash);
                user.setSalt(salt);
            }

            if(StringUtils.stringNotEmpty(email)) {
                user.setUserMail(email);
            }

            if(!roles.isEmpty()) {
                user.setRoles(Lists.newArrayList(roles));
            }

            if(!metadata.isEmpty()) {
                user.setMetadata(Maps.newHashMap(metadata));
            }

            return persist(user) ? user : null;
        } else {
            throw new RuntimeHomeAutomationException("Could not find user: " + userName + " to update");
        }
    }

    private boolean persist(LocalUser user) {
        try {
            DBSession session = sessionFactory.createSession();
            EntityManager entityManager = session.getEntityManager();
            entityManager.persist(user);

            return true;
        } catch (JasDBStorageException e) {
            LOG.error("Unable to store user", e);
            throw new RuntimeHomeAutomationException("Unable to store user data", e);
        }
    }

    @Override
    public User updateMetadata(String userName, Map<String, String> metadata) {
        LocalUser localUser = findInternalUser(userName);
        if(localUser != null) {
            localUser.setMetadata(metadata);
            return persist(localUser) ? localUser : null;
        }

        return null;
    }

    @Override
    public User setMetadata(String userName, String key, String value) {
        LocalUser localUser = findInternalUser(userName);
        if(localUser != null) {
            Map<String, String> metadata = new HashMap<>(localUser.getMetadata());
            metadata.put(key, value);
            localUser.setMetadata(metadata);

            return persist(localUser) ? localUser : null;
        }

        return null;
    }

    @Override
    public Optional<User> findUser(String clientId) {
        return ofNullable(findInternalUser(clientId));
    }

    private LocalUser findInternalUser(String clientId) {
        try {
            DBSession session = sessionFactory.createSession();
            EntityManager entityManager = session.getEntityManager();
            List<LocalUser> users = entityManager.findEntities(LocalUser.class,
                    createBuilder().field("userName").value(clientId));

            if(users.size() == 1) {
                return users.get(0);
            }
        } catch(JasDBStorageException e) {
            LOG.error("Unable to find user", e);
        }

        return null;
    }

    @Override
    public boolean deleteUser(String clientId) {
        try {
            DBSession session = sessionFactory.createSession();
            EntityManager entityManager = session.getEntityManager();
            List<LocalUser> users = entityManager.findEntities(LocalUser.class,
                    QueryBuilder.createBuilder().field("userName").value(clientId));

            if(users.size() == 1) {
                LocalUser user = users.get(0);
                LOG.debug("Removing user: {}", user.getUserId());
                entityManager.remove(user);
                return true;
            } else {
                LOG.debug("Could not delete user: {}", clientId);
                return false;
            }
        } catch (JasDBStorageException e) {
            throw new RuntimeHomeAutomationException("Unable to delete user data", e);
        }
    }

    @Override
    public User updateRoles(String userName, List<String> roles) {
        return null;
    }
}
