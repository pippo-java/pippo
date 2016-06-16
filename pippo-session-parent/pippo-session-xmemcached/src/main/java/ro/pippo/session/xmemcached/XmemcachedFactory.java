/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.session.xmemcached;

import java.io.IOException;
import java.util.List;
import net.rubyeye.xmemcached.CommandFactory;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.auth.PlainCallbackHandler;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.command.TextCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;

/**
 * Utility to build the client.
 *
 * @author Herman Barrantes
 */
public class XmemcachedFactory {

    private static final Logger log = LoggerFactory.getLogger(XmemcachedFactory.class);
    private static final String HOST = "memcached.hosts";
    private static final String PROT = "memcached.protocol";
    private static final String USER = "memcached.user";
    private static final String PASS = "memcached.password";
    private static final String AUTM = "memcached.authMechanisms";

    private XmemcachedFactory() {
        throw new PippoRuntimeException("You can't make a instance of factory.");
    }

    /**
     * Create a memcached client with pippo settings.
     *
     * @param settings pippo settings
     * @return memcached client
     */
    public static MemcachedClient create(final PippoSettings settings) {
        String host = settings.getString(HOST, "localhost:11211");
        String prot = settings.getString(PROT, "BINARY");
        CommandFactory protocol;
        switch (prot) {
            case "BINARY":
                protocol = new BinaryCommandFactory();
                break;
            case "TEXT":
                protocol = new TextCommandFactory();
                break;
            default:
                protocol = new BinaryCommandFactory();
                break;
        }
        String user = settings.getString(USER, "");
        String pass = settings.getString(PASS, "");
        List<String> autM = settings.getStrings(AUTM);
        String[] mechanisms = autM.toArray(new String[autM.size()]);
        return create(host, protocol, user, pass, mechanisms);
    }

    /**
     * Create a memcached client with params.
     *
     * @param hosts whitespace separated host or IP addresses and port numbers
     * of the form "host:port host2:port hostN:portN"
     * @param protocol opcional, BINARY or TEXT
     * @param user opcional, user name o null
     * @param pass opcional, password o null
     * @param authMechanisms opcional, CRAM-MD5 and/or PLAIN
     * @return memcached client
     */
    public static MemcachedClient create(
            String hosts,
            CommandFactory protocol,
            String user,
            String pass,
            String[] authMechanisms) {
        MemcachedClient client = null;
        try {
            MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(hosts));
            builder.setCommandFactory(protocol);
            if (isNotNullOrEmpty(user)) {
                builder.addAuthInfo(
                        AddrUtil.getAddresses(hosts).get(0),
                        new AuthInfo(
                                new PlainCallbackHandler(user, pass),
                                authMechanisms));
            }
            client = builder.build();
        } catch (IOException ex) {
            log.error("An error occurred when creating the MemcachedClient.", ex);
            throw new PippoRuntimeException(ex);
        }
        return client;
    }

    /**
     * Validate if String is not null and not empty
     *
     * @param value String to validate
     * @return true if is not null or not empty, otherwise false
     */
    private static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
