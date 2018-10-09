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
package ro.pippo.session.mongodb;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.IndexOptions;
import static com.mongodb.client.model.Projections.include;
import com.mongodb.client.model.UpdateOptions;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import ro.pippo.session.*;

/**
 * SessionDataStorage implementation with MongoDB.
 *
 * @author Herman Barrantes
 */
public class MongoDBSessionDataStorage implements SessionDataStorage {

    private static final String SESSION_NAME = "session";
    private static final int IDLE_TIME = DefaultSessionData.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;
    private static final String SESSION_ID = "_id";
    private static final String SESSION_DATA = "session_data";
    private static final String SESSION_TTL = "creation_time";
    private static final String SESSION_INDEX_NAME = "session_ttl_index";
    private final MongoCollection<Document> sessions;
    private final SessionDataTranscoder transcoder;

    /**
     * Manage session with MongoDB in the collection named "session" and 30
     * minutes idle time.
     *
     * @param database MongoDB database object
     * @see #MongoDBSessionDataStorage(com.mongodb.client.MongoDatabase,
     * java.lang.String, long, ro.pippo.session.mongodb.SessionDataTranscoder)
     */
    public MongoDBSessionDataStorage(MongoDatabase database) {
        this(database, SESSION_NAME, IDLE_TIME, new SerializationSessionDataTranscoder());
    }

    /**
     * Manage session with MongoDB in the collection named "session" and idle
     * time indicated.
     *
     * @param database MongoDB database object
     * @param idleTime idle time of the session in seconds
     * @see #MongoDBSessionDataStorage(com.mongodb.client.MongoDatabase,
     * java.lang.String, long, ro.pippo.session.mongodb.SessionDataTranscoder)
     */
    public MongoDBSessionDataStorage(MongoDatabase database, int idleTime) {
        this(database, SESSION_NAME, idleTime, new SerializationSessionDataTranscoder());
    }

    /**
     * Manage session with MongoDB in the specified collection and 30 minutes
     * idle time.
     *
     * @param database MongoDB database object
     * @param collection name of collection to manage the session
     * @see #MongoDBSessionDataStorage(com.mongodb.client.MongoDatabase,
     * java.lang.String, long, ro.pippo.session.mongodb.SessionDataTranscoder)
     */
    public MongoDBSessionDataStorage(MongoDatabase database, String collection) {
        this(database, collection, IDLE_TIME, new SerializationSessionDataTranscoder());
    }

    /**
     * Manage session with MongoDB in the specified collection and idle time
     * indicated.
     *
     * @param database MongoDB database object
     * @param collection name of collection to manage the session
     * @param idleTime idle time of the session in seconds
     * @param transcoder trancoder
     */
    public MongoDBSessionDataStorage(MongoDatabase database, String collection, long idleTime, SessionDataTranscoder transcoder) {
        this.transcoder = transcoder;
        this.sessions = database.getCollection(collection);
        createIndex(idleTime);
    }

    /**
     * Create TTL index
     *
     * @param idleTime idle time in seconds
     * @see https://docs.mongodb.com/manual/core/index-ttl/
     */
    private void createIndex(long idleTime) {
        try {
            this.sessions.createIndex(
                    new Document(SESSION_TTL, 1),
                    new IndexOptions()
                    .expireAfter(idleTime, TimeUnit.SECONDS)
                    .name(SESSION_INDEX_NAME));
        } catch (MongoException ex) {//update idle time
            this.sessions.dropIndex(SESSION_INDEX_NAME);
            this.sessions.createIndex(
                    new Document(SESSION_TTL, 1),
                    new IndexOptions()
                    .expireAfter(idleTime, TimeUnit.SECONDS)
                    .name(SESSION_INDEX_NAME));
        }
    }

    @Override
    public SessionData create() {
        return new DefaultSessionData();
    }

    @Override
    public void save(SessionData sessionData) {
        String sessionId = sessionData.getId();
        this.sessions.updateOne(
                eq(SESSION_ID, sessionId),
                combine(
                        set(SESSION_ID, sessionId),
                        set(SESSION_TTL, new Date()),
                        set(SESSION_DATA, transcoder.encode(sessionData))),
                new UpdateOptions().upsert(true));
    }

    @Override
    public SessionData get(String sessionId) {
        Document doc = this.sessions
                .find(eq(SESSION_ID, sessionId))
                .projection(include(SESSION_DATA))
                .first();
        if (doc == null) {
            return null;
        }
        String sessionStored = doc.getString(SESSION_DATA);
        SessionData sessionData = transcoder.decode(sessionStored);
        return sessionData;
    }

    @Override
    public void delete(String sessionId) {
        this.sessions.deleteOne(eq(SESSION_ID, sessionId));
    }

}
