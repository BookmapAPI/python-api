package com.bookmap.python.api.addon.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public interface BuildValidator<T> {
    ExecutorService VALIDATING_EXECUTOR = Executors.newCachedThreadPool();

    Future<String> validate(T entityToValidate);
}
