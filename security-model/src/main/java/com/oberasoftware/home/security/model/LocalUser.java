package com.oberasoftware.home.security.model;

import com.oberasoftware.jasdb.api.entitymapper.annotations.JasDBEntity;
import com.oberasoftware.jasdb.api.entitymapper.annotations.JasDBProperty;

/**
 * @author Renze de Vries
 */
@JasDBEntity(bagName = "MyBAG")
public class LocalUser extends User {
    private String passwordHash;
    private String salt;

    public LocalUser(String userId, String userName, String userMail, String passwordHash, String salt) {
        super(userId, userName, userMail);
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public LocalUser() {
    }

    @JasDBProperty
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @JasDBProperty
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LocalUser localUser = (LocalUser) o;

        if (!passwordHash.equals(localUser.passwordHash)) return false;
        return salt.equals(localUser.salt);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + passwordHash.hashCode();
        result = 31 * result + salt.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LocalUser{" +
                "passwordHash='" + passwordHash + '\'' +
                ", salt='" + salt + '\'' +
                '}';
    }
}