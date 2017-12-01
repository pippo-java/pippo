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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ro.pippo.core.Session;
import ro.pippo.core.route.RouteContext;

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

        assertThat(session, is(mockSession));

        verify(mockRouteContext, times(1)).getSession();
        verify(mockPippoWebContext, times(1)).getRouteContext();
    }

    @Test
    public void shouldGetSessionFromSuppliedPippoWebContextAsTrackableSession() {
        when(mockRouteContext.getSession()).thenReturn(mockSession);
        when(mockPippoWebContext.getRouteContext()).thenReturn(mockRouteContext);

        PippoSessionStore sessionStore = new PippoSessionStore();
        Object session = sessionStore.getTrackableSession(mockPippoWebContext);

        assertThat(session, is(mockSession));

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

        assertThat(sessionStore.get(mockPippoWebContext, expectedKey), is(expectedValue));

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

        assertThat(sessionStore.get(mockPippoWebContext, expectedKey), is(nullValue()));

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

        assertThat(sessionStore.getOrCreateSessionId(mockPippoWebContext), is(expectedSessionId));

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

}
