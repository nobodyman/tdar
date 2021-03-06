package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.struts.action.AbstractAdvancedSearchController;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.AbstractLookupController;

import com.opensymphony.xwork2.Action;

@Transactional
public abstract class AbstractSearchControllerITCase extends AbstractControllerITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;

    protected static List<ResourceType> allResourceTypes = Arrays.asList(ResourceType.values());

    @Autowired
    protected GenericKeywordService genericKeywordService;

    @Before
    public void reset() {
        searchIndexService.purgeAll();
    }

    protected Long setupDataset() {
        return setupDataset(Status.DELETED);
    }

    protected Long setupDataset(Status status) {
        Dataset dataset = new Dataset();
        dataset.setTitle("precambrian dataset");
        dataset.setDescription("dataset description");
        dataset.markUpdated(getUser());
        SiteTypeKeyword siteType = genericKeywordService.findByLabel(SiteTypeKeyword.class, "Shell midden");
        dataset.getSiteTypeKeywords().add(siteType);
        assertFalse(siteType.getLabel().trim().startsWith(":"));
        assertFalse(siteType.getLabel().trim().endsWith(":"));
        genericService.saveOrUpdate(dataset);
        ResourceCreator rc = new ResourceCreator(createAndSaveNewPerson("atest@Test.com", "abc"), ResourceCreatorRole.CREATOR);
        ResourceCreator rc2 = new ResourceCreator(getUser().getInstitution(), ResourceCreatorRole.PREPARER);
        dataset.getResourceCreators().add(rc);
        dataset.getResourceCreators().add(rc2);
        dataset.setStatus(status);
        genericService.saveOrUpdate(dataset);

        Long datasetId = dataset.getId();
        return datasetId;
    }

    protected Long setupCodingSheet() {
        CodingSheet coding = new CodingSheet();
        coding.setTitle("precambrian codingsheet");
        coding.setDescription("codingsheet description");
        coding.markUpdated(getUser());
        coding.setStatus(Status.ACTIVE);
        genericService.save(coding);

        Long codingId = coding.getId();

        return codingId;
    }

    protected Long setupImage() {
        return setupImage(getUser());
    }

    protected Long setupImage(TdarUser user) {
        Image img = new Image();
        img.setTitle("precambrian Test");
        img.setDescription("image description");
        img.markUpdated(user);
        img.getAuthorizedUsers().add(new AuthorizedUser(user, user, Permissions.MODIFY_RECORD));
        CultureKeyword label = genericKeywordService.findByLabel(CultureKeyword.class, "Folsom");
        CultureKeyword label2 = genericKeywordService.findByLabel(CultureKeyword.class, "Early Archaic");
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setWest(-117.124);
        latLong.setEast(-117.101);
        latLong.setNorth(35.791);
        latLong.setSouth(33.354);
        img.getLatitudeLongitudeBoxes().add(latLong);
        assertNotNull(label.getId());
        img.getCultureKeywords().add(label);
        img.getCultureKeywords().add(label2);
        img.setStatus(Status.DRAFT);
        genericService.save(img);
        genericService.save(latLong);
        Long imgId = img.getId();
        return imgId;
    }

    protected Long setupDatedDocument() {
        Document doc = new Document();
        doc.setTitle("Calendar Date Test");
        doc.setDate(1000);
        doc.setProject(Project.NULL);
        doc.setDescription("Ensure we can find a resource given temporal limits.");
        doc.markUpdated(getUser());
        CoverageDate cd = new CoverageDate(CoverageType.CALENDAR_DATE, -1000, 2000);
        doc.getCoverageDates().add(cd);
        genericService.save(doc);
        Long docId = doc.getId();
        return docId;
    }

    protected boolean resultsContainId(AbstractAdvancedSearchController controller_, Long id) {
        boolean found = false;
        for (Resource r : controller_.getResults()) {
            logger.trace(r.getId() + " " + r.getResourceType());
            if (id.equals(r.getId())) {
                found = true;
                break;
            }
        }
        return found;
    }

    protected List<ObjectType> getInheritingTypes() {
        List<ObjectType> list = new ArrayList<>();
        list.add(ObjectType.IMAGE);
        list.add(ObjectType.DOCUMENT);
        return list;
    }

    @SuppressWarnings("rawtypes")
    public static void doSearch(AbstractLookupController controller, LookupSource resource) {
        doSearch(controller, resource, false);
    }

    @SuppressWarnings("rawtypes")
    public static void doSearch(AbstractLookupController controller, LookupSource resource, Boolean b) {
        Exception e = null;
        String msg = null;
        Logger logger = LoggerFactory.getLogger(AbstractControllerITCase.class);
        try {
            switch (resource) {
                case COLLECTION:
                    msg = ((CollectionSearchAction) controller).searchCollections();
                    break;
                case PERSON:
                    msg = ((PersonSearchAction) controller).searchPeople();
                    break;
                case INSTITUTION:
                    msg = ((InstitutionSearchAction) controller).searchInstitutions();
                    break;
                case RESOURCE:
                    if (controller instanceof AdvancedSearchDownloadAction) {
                        msg = ((AdvancedSearchDownloadAction) controller).viewExcelReport();
                    } else {
                        msg = ((AdvancedSearchController) controller).search();
                    }
                    break;
                default:
                    fail();
            }
        } catch (Exception ex) {
            e = ex;
            logger.error("exception: {} ", e);
        }
        if (b == Boolean.TRUE) {
            Assert.assertTrue(String.format("there should be an exception %s or returned input %s", e, msg),
                    (e != null) || Action.INPUT.equals(msg));
        } else if (b == Boolean.FALSE) {
            Assert.assertTrue("there should not be an exception: " + ExceptionUtils.getFullStackTrace(e), e == null);
            assertEquals(Action.SUCCESS, msg);
        } else {
            // "maybe" state -- in some cases (looped state in AdvancedSearchController.testResultCountsAsBasicUser for example)
        }
    }

    protected void doSearch(AbstractAdvancedSearchController controller_, String query) {
        doSearch(controller_, query, false);
    }

    protected void doSearch(AbstractAdvancedSearchController controller_, String query, Boolean exceptions) {
        evictCache();
        controller_.setQuery(query);
        doSearch(controller_, LookupSource.RESOURCE, exceptions);
        logger.info("search (" + controller_.getQuery() + ") found: " + controller_.getTotalRecords());
    }

    protected void setStatuses(AbstractAdvancedSearchController controller, Status... status) {
        controller.getIncludedStatuses().clear();

        controller.getIncludedStatuses().addAll(new ArrayList<Status>(Arrays.asList(status)));
    }

    protected void setStatusAll(AbstractAdvancedSearchController controller) {
        setStatuses(controller, Status.values());
    }

    protected void logResults(AbstractLookupController controller_) {
        for (Indexable r : (List<Indexable>) controller_.getResults()) {
            logger.debug("Search Result:" + r);
        }
    }

    // protected void setResourceTypes(ResourceType ... resourceTypes) {
    // setResourceTypes(Arrays.asList(resourceTypes));
    // }

    // protected void setResourceTypes(List<ResourceType> resourceTypes) {
    // controller.getResourceTypes().clear();
    // controller.getResourceTypes().addAll(resourceTypes);
    // }

    @Override
    public TdarUser getSessionUser() {
        return null;
    }

    protected SearchParameters firstGroup(AbstractAdvancedSearchController controller) {
        if (controller.getG().isEmpty()) {
            controller.getG().add(new SearchParameters());
        }
        return controller.getG().get(0);
    }

    protected LatitudeLongitudeBox firstMap(AbstractAdvancedSearchController controller) {
        return controller.getMap();
    }

    // protected void setObjectTypes(ObjectType ... resourceTypes) {
    // setObjectTypes(Arrays.asList(resourceTypes));
    // }

    protected void setObjectTypes(AbstractAdvancedSearchController controller, List<ObjectType> resourceTypes) {
        controller.getObjectTypes().clear();
        controller.getObjectTypes().addAll(resourceTypes);
    }
}
