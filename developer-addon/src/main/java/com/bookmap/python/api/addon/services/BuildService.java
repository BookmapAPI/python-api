package com.bookmap.python.api.addon.services;

import com.bookmap.python.api.addon.exceptions.FailedToBuildException;

/**
 * Service responsible for building and new addon from source. Source can be represented by any entity.
 *
 * @param <T> representation of source codes
 */
public interface BuildService<T> {
    /**
     * @param addonName name of the addon
     * @param source    source code
     */
    void build(String addonName, T source) throws FailedToBuildException;
}
