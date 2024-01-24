package org.javamaster.invocationlab.admin.redis;

@FunctionalInterface
public interface TripleFunction<T, R,V, U> {

    U apply(T t, R r,V v);

}
