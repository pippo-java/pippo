/*
 * Copyright (C) 2017 the original author or authors.
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

import org.pac4j.core.context.session.SessionStore;
import ro.pippo.core.Session;

/**
 * @author Ranganath Kini
 */
public class PippoSessionStore implements SessionStore<PippoWebContext> {

    @Override
    public String getOrCreateSessionId(PippoWebContext pippoWebContext) {
        return getSession(pippoWebContext).getId();
    }

    @Override
    public Object get(PippoWebContext pippoWebContext, String name) {
        return getSession(pippoWebContext).get(name);
    }

    @Override
    public void set(PippoWebContext pippoWebContext, String name, Object value) {
        Session session = getSession(pippoWebContext);

        if (value == null) {
            session.remove(name);
        } else {
            session.put(name, value);
        }
    }

    @Override
    public boolean destroySession(PippoWebContext pippoWebContext) {
        getSession(pippoWebContext).invalidate();

        return true;
    }

    @Override
    public Object getTrackableSession(PippoWebContext pippoWebContext) {
        return getSession(pippoWebContext);
    }

    @Override
    public SessionStore<PippoWebContext> buildFromTrackableSession(PippoWebContext pippoWebContext, Object trackableSession) {
        return (trackableSession != null) ? new ProvidedPippoSessionStore((Session) trackableSession) : null;
    }

    public boolean renewSession(PippoWebContext pippoWebContext) {
        pippoWebContext.getRouteContext().recreateSession();

        return true;
    }

    protected Session getSession(PippoWebContext pippoWebContext) {
        return pippoWebContext.getRouteContext().getSession();
    }

    private static class ProvidedPippoSessionStore extends PippoSessionStore {

        private Session providedSession;

        public ProvidedPippoSessionStore(Session providedSession) {
            this.providedSession = providedSession;
        }

        @Override
        protected Session getSession(PippoWebContext pippoWebContext) {
            return providedSession;
        }

    }

}
