package de.imi.mopat.helper.controller;

import java.io.Serializable;
import org.springframework.cache.support.NoOpCache;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;

/**
 * Just a simple implementation of the AclCache interface
 * that does nothing. Thus disabling the caching mechanism
 * of Springs ACL mechanism.
 */
public class NoOpAclCache extends NoOpCache implements AclCache {

    public NoOpAclCache(String name) {
        super(name);
    }

    @Override
    public void evictFromCache(Serializable pk) {

    }

    @Override
    public void evictFromCache(ObjectIdentity objectIdentity) {

    }

    @Override
    public MutableAcl getFromCache(ObjectIdentity objectIdentity) {
        return null;
    }

    @Override
    public MutableAcl getFromCache(Serializable pk) {
        return null;
    }

    @Override
    public void putInCache(MutableAcl acl) {

    }

    @Override
    public void clearCache() {

    }
}
