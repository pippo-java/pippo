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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ro.pippo.core.Session;
import ro.pippo.core.route.RouteContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Ranganath Kini
 */
@RunWith(MockitoJUnitRunner.class)
public class PippoSessionStoreTest {

    @Mock
    private PippoWebContext mockPippoWebContext;

    @Mock
    private RouteContext mockRouteContext;

    @Mock
    private Session mockSession;

    @Test
    public void shouldGetSessionFromSuppliedPippoWebContext() {
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();
        Session session = sessionStore.getSession(mockPippoWebContext);

        assertThat(session, is(Optional.of(mockSession).get()));

        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldGetSessionFromSuppliedPippoWebContextAsTrackableSession() {
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();
        Object session = sessionStore.getTrackableSession(mockPippoWebContext);

        assertThat(session, is(Optional.of(mockSession)));

        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldGetValueOfEntryWithSpecifiedKeyFromSession() {
        String expectedKey = "FooBar";
        String expectedValue = "value_FOO_BAR";

        when(mockSession.get(anyString())).thenReturn(expectedValue);
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();

        assertThat(sessionStore.get(mockPippoWebContext, expectedKey), is(Optional.of(expectedValue)));

        verify(mockSession, times(1)).get(expectedKey);
        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldReturnNullValueFromSessionIfNoEntryWithSpecifiedKeyExists() {
        String expectedKey = "FooBar";

        when(mockSession.get(anyString())).thenReturn(null);
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();

        assertThat(sessionStore.get(mockPippoWebContext, expectedKey), is(Optional.ofNullable(null)));

        verify(mockSession, times(1)).get(expectedKey);
        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldCreateEntryInSessionWithSpecifiedKeyAndValue() {
        String expectedKey = "FooBar";
        String expectedValue = "value_FOO_BAR";

        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();

        sessionStore.set(mockPippoWebContext, expectedKey, expectedValue);

        verify(mockSession, times(1)).put(expectedKey, expectedValue);
        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldRemoveExistingEntryWithSpecifiedKeyWhenSpecifiedValueIsNull() {
        String expectedKey = "FooBar";

        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();
        sessionStore.set(mockPippoWebContext, expectedKey, null);

        verify(mockSession, times(1)).remove(expectedKey);
        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldInvalidateSessionOnDestroy() {
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();

        assertThat(sessionStore.destroySession(mockPippoWebContext), is(true));

        verify(mockSession, times(1)).invalidate();
        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldGetSessionIdFromUnderlyingSession() {
        String expectedSessionId = "FOO_SESSION_ID";

        when(mockSession.getId()).thenReturn(expectedSessionId);
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();
        assertThat(sessionStore.getSessionId(mockPippoWebContext,false), is(Optional.of(expectedSessionId)));

        verify(mockSession, times(1)).getId();
        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldAskRouteContextToRecreateSessionOnRenewSession() {
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();

        assertThat(sessionStore.renewSession(mockPippoWebContext), is(true));

        verify(mockRouteContext, times(1)).recreateSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldBuildFromProvidedTrackableSession() {
    }

    private class HttpSession implements javax.servlet.http.HttpSession{

        private final long creationTime;
        private final String id;
        private final long lastAccessTime;
        private Map<String, Object> attributeMap = new HashMap<String, Object>();
        private Map<String, Object> valueMap = new HashMap<String, Object>();

        private HttpSession(){
            this.creationTime = 0;
            this.id = "SESSION-ID";
            this.lastAccessTime = 0;
        }
        @Override
        public long getCreationTime() {
            return this.creationTime;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public long getLastAccessedTime() {
            return this.lastAccessTime;
        }

        @Override
        public ServletContext getServletContext() {
            throw new RuntimeException("NOT IMPLEMENTED");
        }

        @Override
        public void setMaxInactiveInterval(int i) {

        }

        @Override
        public int getMaxInactiveInterval() {
            return 0;
        }

        /**
         * @deprecated
         */
        @Override
        public HttpSessionContext getSessionContext() {
            throw new RuntimeException("NOT IMPLEMENTED");
        }

        @Override
        public Object getAttribute(String key) {
            return attributeMap.get(key);
        }

        @Override
        public Object getValue(String key) {
            return valueMap.get(key);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(attributeMap.keySet());
        }

        /**
         * @deprecated
         */
        @Override
        public String[] getValueNames() {
            return new String[0];
        }

        @Override
        public void setAttribute(String key, Object value) {
            attributeMap.put(key,value);
        }

        /**
         * @param s
         * @param o
         * @deprecated
         */
        @Override
        public void putValue(String s, Object o) {
            valueMap.put(s,o);
        }

        @Override
        public void removeAttribute(String s) {
            attributeMap.remove(s);
        }

        /**
         * @param s
         * @deprecated
         */
        @Override
        public void removeValue(String s) {
            valueMap.remove(s);
        }

        @Override
        public void invalidate() {
            throw new RuntimeException("NOT IMPLEMENTED");
        }

        @Override
        public boolean isNew() {
            return false;
        }
    }

}
