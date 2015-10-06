package org.tdar.core.dao.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.CriteriaSpecification;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceProxy;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.dao.Dao.HibernateBase;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.PersistableUtils;

@Component
public class InformationResourceFileDao extends HibernateBase<InformationResourceFile> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public InformationResourceFileDao() {
        super(InformationResourceFile.class);
    }

    @Autowired
    private InformationResourceFileVersionDao informationResourceFileVersionDao;

    public InformationResourceFile findByFilestoreId(String filestoreId) {
        return findByProperty("filestoreId", filestoreId);
    }

    public Map<String, Long> getAdminFileExtensionStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_KEYWORD_COUNT_FILE_EXTENSION);
        query.setParameterList("internalTypes", Arrays.asList(VersionType.ARCHIVAL,
                VersionType.UPLOADED, VersionType.UPLOADED_ARCHIVAL));
        Map<String, Long> toReturn = new HashMap<>();
        for (Object o : query.list()) {
            try {
                Object[] objs = (Object[]) o;
                if (ArrayUtils.isEmpty(objs)) {
                    continue;
                }
                // 0 == extension
                // 1 == count
                toReturn.put((String)objs[0], (Long)objs[1]);
            } catch (Exception e) {
                logger.debug("exception get admin file extension stats", e);
            }
        }

        return toReturn;
    }

    public Number getDownloadCount(InformationResourceFile irFile) {
        String sql = String.format(TdarNamedQueries.DOWNLOAD_COUNT_SQL, irFile.getId(), new Date());
        return (Number) getCurrentSession().createSQLQuery(sql).uniqueResult();
    }

    public void deleteTranslatedFiles(Dataset dataset) {
        for (InformationResourceFile irFile : dataset.getInformationResourceFiles()) {
            logger.debug("deleting {}", irFile);
            deleteTranslatedFiles(irFile);
        }
    }

    public void deleteTranslatedFiles(InformationResourceFile irFile) {
        for (InformationResourceFileVersion version : irFile.getLatestVersions()) {
            logger.debug("deleting version:{}  isTranslated:{}", version, version.isTranslated());
            if (version.isTranslated()) {
                // HQL here avoids issue where hibernate delays the delete
                deleteVersionImmediately(version);
                // we don't need safeguards on a translated file, so tell the dao to delete no matter what.
                // informationResourceFileVersionDao.forceDelete(version);
            }
        }
    }

    public void deleteVersionImmediately(InformationResourceFileVersion version) {
        if (PersistableUtils.isNullOrTransient(version)) {
            throw new TdarRecoverableRuntimeException("error.cannot_delete_transient");
        }

        if (version.isUploadedOrArchival()) {
            throw new TdarRecoverableRuntimeException("error.cannot_delete_archival");
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.DELETE_INFORMATION_RESOURCE_FILE_VERSION_IMMEDIATELY);
        query.setParameter("id", version.getId()).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<InformationResourceFile> findFilesWithStatus(FileStatus[] statuses) {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_STATUS);
        query.setParameterList("statuses", Arrays.asList(statuses));
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<InformationResource> findInformationResourcesWithFileStatus(
            Person authenticatedUser, List<Status> resourceStatus,
            List<FileStatus> fileStatus) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_FILE_STATUS);
        query.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        query.setParameterList("statuses", resourceStatus);
        query.setParameterList("fileStatuses", fileStatus);
        query.setParameter("submitterId", authenticatedUser.getId());
        List<InformationResource> list = new ArrayList<>();
        for (ResourceProxy proxy : (List<ResourceProxy>) query.list()) {
            try {
                list.add((InformationResource) proxy.generateResource());
            } catch (IllegalAccessException | InvocationTargetException
                    | InstantiationException e) {
                logger.error("error happened manifesting: {} ", e);
            }
        }
        return list;
    }

    public ScrollableResults findScrollableVersionsForVerification() {
        Query query = getCurrentSession().getNamedQuery(QUERY_INFORMATION_RESOURCE_FILE_VERSION_VERIFICATION);
        return query.setReadOnly(true).setCacheable(false).scroll(ScrollMode.FORWARD_ONLY);
    }

    @SuppressWarnings("unchecked")
    public List<InformationResourceFile> findAllExpiredEmbargoes() {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_FILE_EMBARGO_EXIPRED);
        DateTime today = new DateTime().withTimeAtStartOfDay();
        query.setParameter("dateStart", today.toDate());
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<InformationResourceFile> findAllEmbargoFilesExpiringTomorrow() {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_FILE_EMBARGOING_TOMORROW);
        DateTime today = new DateTime().plusDays(1).withTimeAtStartOfDay();
        query.setParameter("dateStart", today.toDate());
        query.setParameter("dateEnd", today.plusDays(1).toDate());
        return query.list();
    }
}