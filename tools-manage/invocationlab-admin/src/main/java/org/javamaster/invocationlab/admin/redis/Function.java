package org.javamaster.invocationlab.admin.redis;

@FunctionalInterface
public interface Function<T, U> {

    U apply(T t) throws Exception;

}
