package org.tdar.core.dao;

import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.utils.jaxb.XMLFilestoreLogger;

public class FilestoreLoggingEventListener implements PostInsertEventListener,
        PostUpdateEventListener, PostDeleteEventListener {

    private static final long serialVersionUID = -2773973927518207238L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    XMLFilestoreLogger xmlLogger;

    public FilestoreLoggingEventListener() throws ClassNotFoundException {
        xmlLogger = new XMLFilestoreLogger();
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", event.getEntity());
            return;
        }
        logToXml(event.getEntity());
    }

    private void logToXml(Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Indexable && !((Indexable) obj).isReadyToIndex()) {
            return;
        }

        if (obj instanceof Persistable) {
            xmlLogger.logRecordXmlToFilestore((Persistable) obj);
        }
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", event.getEntity());
            return;
        }
        logToXml(event.getEntity());
    }

    private boolean testSession(EventSource session) {
        return session.isClosed();
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (testSession(event.getSession())) {
            logger.error("trying to logToXML: {} but session is closed", event.getEntity());
            return;
        }
        logToXml(event.getEntity());
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

}
