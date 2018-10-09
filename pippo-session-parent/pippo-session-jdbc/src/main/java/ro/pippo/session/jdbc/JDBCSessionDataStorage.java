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
package ro.pippo.session.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.session.SerializationSessionDataTranscoder;
import ro.pippo.session.SessionDataStorage;
import ro.pippo.session.SessionDataTranscoder;
import ro.pippo.session.SessionData;
import ro.pippo.session.DefaultSessionData;

/**
 * SessionDataStorage implementation with JDBC.
 *
 * @author Herman Barrantes
 */
public class JDBCSessionDataStorage implements SessionDataStorage {

    // Logger
    private static final Logger log = LoggerFactory.getLogger(JDBCSessionDataStorage.class);
    // CRUD Statements
    public static final String SELECT = "select data from session where id = ?";
    public static final String INSERT = "insert into session (id, time, data) values (?, ?, ?)";
    public static final String UPDATE = "update session set time = ?, data = ? where id = ?";
    public static final String DELETE = "delete from session where id = ?";
    // Variables
    private final DataSource dataSource;
    private final String select;
    private final String insert;
    private final String update;
    private final String delete;
    private final SessionDataTranscoder transcoder;

    public JDBCSessionDataStorage(DataSource dataSource) {
        this(dataSource, SELECT, INSERT, UPDATE, DELETE, new SerializationSessionDataTranscoder());
    }

    public JDBCSessionDataStorage(DataSource dataSource, String select, String insert, String update, String delete) {
        this(dataSource, select, insert, update, delete, new SerializationSessionDataTranscoder());
    }

    public JDBCSessionDataStorage(DataSource dataSource, String select, String insert, String update, String delete, SessionDataTranscoder transcoder) {
        this.dataSource = dataSource;
        this.select = select;
        this.insert = insert;
        this.update = update;
        this.delete = delete;
        this.transcoder = transcoder;
    }

    @Override
    public SessionData create() {
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        String id = sessionData.getId();
        Timestamp time = new Timestamp(System.currentTimeMillis());
        String data = transcoder.encode(sessionData);
        if (executeUpdate(update, time, data, id) == 0) {
            executeUpdate(insert, id, time, data);
        }
    }

    @Override
    public SessionData get(String sessionId) {
        String sessionStored = executeSelect(select, sessionId);
        if (sessionStored == null) {
            return null;
        }
        SessionData sessionData = transcoder.decode(sessionStored);
        return sessionData;
    }

    @Override
    public void delete(String sessionId) {
        executeUpdate(delete, sessionId);
    }

    protected String executeSelect(String query, Object... parameters) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException ex) {
            log.error("Error executing the statement", ex);
            throw new PippoRuntimeException(ex);
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
        return null;
    }

    protected int executeUpdate(String query, Object... parameters) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            log.error("Error executing the statement", ex);
            throw new PippoRuntimeException(ex);
        } finally {
            close(preparedStatement);
            close(connection);
        }
    }

    protected void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ex) {
                log.error("Error closing resource", ex);
            }
        }
    }

}
