/*
 * Copyright (C) 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.pac4j;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import ro.pippo.core.Session;
import java.util.Optional;

/**
 * @author Ranganath Kini
 */
public class PippoSessionStore implements SessionStore {

    protected Session getSession(PippoWebContext pippoWebContext) {
        return pippoWebContext.getRouteContext().getSession();
    }

    @Override
    public Optional<String> getSessionId(WebContext webContext, boolean create) {
        PippoWebContext context = getPippoWebContext(webContext);
        Session session = getSession(context);
        if (session == null) {
            session = context.getRequest().getSession(create);
        }

        if (session == null){
            return Optional.empty();
        } else {
            return Optional.of(session.getId());
        }
    }

    private PippoWebContext getPippoWebContext(WebContext webContext){
        return ((PippoWebContext) webContext);
    }

    @Override
    public Optional<Object> get(WebContext webContext, String key) {
        Object value = getSession(getPippoWebContext(webContext)).get(key);
        return Optional.ofNullable(value);
    }

    @Override
    public void set(WebContext webContext, String key, Object value) {
        PippoWebContext ctx = getPippoWebContext(webContext);
        Session session = getSession(ctx);
        if (value == null ){
            session.remove(key);
        } else {
            session.put(key,value);
        }
    }

    @Override
    public boolean destroySession(WebContext webContext) {
        getSession(getPippoWebContext(webContext)).invalidate();
        return true;
    }

    @Override
    public Optional<Object> getTrackableSession(WebContext webContext) {
        return Optional.of(getSession(getPippoWebContext(webContext)));
    }

    @Override
    public Optional<SessionStore> buildFromTrackableSession(WebContext webContext, Object trackableSession) {
        return Optional.of(new ProvidedPippoSessionStore((Session) trackableSession));
    }

    @Override
    public boolean renewSession(WebContext webContext) {
        getPippoWebContext(webContext).getRouteContext().recreateSession();
        return true;
    }

    private static class ProvidedPippoSessionStore extends PippoSessionStore {

        private final Session providedSession;

        public ProvidedPippoSessionStore(Session providedSession) {
            this.providedSession = providedSession;
        }

        @Override
        protected Session getSession(PippoWebContext pippoWebContext) {
            return providedSession;
        }

    }

}
