package ro.pippo.core.entity;

import ro.pippo.core.Request;

/**
 * Contains methods that create/update an entity from a request.
 *
 * @author Decebal Suiu
 */
public interface EntityRequestEngine {

    <T> T createEntityFromParameters(Class<T> entityClass, Request request);

    <T> T createEntityFromBody(Class<T> entityClass, Request request);

    <T, X> T updateEntityFromParameters(T entity, Request request);

}
