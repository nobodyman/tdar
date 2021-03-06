package org.tdar.core.dao;

public interface TdarNamedQueries {
    /**
     * constants to map between the Annotation Keys for HQL queries and the queries in the DAOs
     */
    String QUERY_CURRENT_USER_NOTIFICATIONS = "userNotifications.current";
    String QUERY_USER_NOTIFICATIONS_BY_TYPE = "userNotifications.byType";
    String QUERY_DELETE_INFORMATION_RESOURCE_FILE_DERIVATIVES = "informationResourceFileVersion.deleteDerivatives";
    String QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_ONTOLOGY = "ontology.isMapped";
    String QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN = "ontology.isMappedToColumn";
    String QUERY_READ_ONLY_FULLUSER_PROJECTS = "fullUser.projects";
    String QUERY_FULLUSER_RESOURCES = "fullUser.resources";
    String QUERY_FULLUSER_DATASET = "fullUser.datasets";
    String QUERY_READUSER_PROJECTS = "readUser.projects";
    String QUERY_SPARSE_RECENT_EDITS = "resources.recent";
    String QUERY_CONTRIBUTORREQUEST_PENDING = "contributorRequest.pending";
    String QUERY_BOOKMARKEDRESOURCE_IS_ALREADY_BOOKMARKED = "bookmarkedResource.isAlreadyBookmarked";
    String QUERY_BOOKMARKEDRESOURCE_REMOVE_BOOKMARK = "bookmarkedResource.removeBookmark";
    String QUERY_BOOKMARKEDRESOURCE_FIND_RESOURCE_BY_PERSON = "bookmarkedResource.findResourcesByPerson";
    String QUERY_BOOKMARKEDRESOURCES_FOR_USER = "bookmarkedResource.byPerson";
    String QUERY_DATASET_CAN_LINK_TO_ONTOLOGY = "dataset.canLinkToOntology";
    String QUERY_DATATABLE_RELATED_ID = "dataTable.relatedId";
    String QUERY_DATATABLE_DATASET = "dataTable.datasetId";
    String QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY = "dataTableColumn.withDefaultOntology";
    String QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME = "informationResource.findByFileName";
    String QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD = "ontologyNode.allChildrenWithIndexWildcard";
    String QUERY_ONTOLOGYNODE_PARENT = "ontologyNode.parent";
    String QUERY_ONTOLOGYNODE_ALL_CHILDREN = "ontologyNode.allChildren";
    String QUERY_PROJECT_CODINGSHEETS = "project.codingSheets";
    String QUERY_PROJECT_DATASETS = "project.datasets";
    String QUERY_PROJECT_DOCUMENTS = "project.documents";
    String QUERY_PROJECT_INDEPENDENTRESOURCES_PROJECTS = "project.independentResourcesProject";
    String QUERY_PROJECT_EDITABLE = "project.editable";
    String QUERY_PROJECT_VIEWABLE = "project.viewable";
    String QUERY_FILE_STATUS = "file.with.statuses";
    String QUERY_RESOURCE_FILE_STATUS = "resources.with.file.status";
    String QUERY_LOGIN_STATS = "admin.userLogin";
    String QUERY_PROJECT_ALL_OTHER = "project.all.other";
    String QUERY_RESOURCE_RESOURCETYPE = "resource.resourceType";
    String QUERY_RESOURCE_MODIFIED_SINCE = "resource.modifiedSince";
    String QUERY_SPARSE_RESOURCES = "all.resources";
    String QUERY_SPARSE_RESOURCES_SUBMITTER = "submitter.resources.sparse";
    String QUERY_RESOURCES_SUBMITTER = "submitter.resources";
    String QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS = "project.countIntegratableDatasets";
    String QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS = "projects.countIntegratableDatasets";
    String QUERY_READ_ONLY_EDITABLE_PROJECTS = "sparse.editableProject";
    String QUERY_MANAGED_ISO_COUNTRIES = "resource.iso_code";
    String QUERY_ACTIVE_RESOURCE_TYPE_COUNT = "resource.typeCounts";
    String QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS = "resource.countByTypeAndStatus";
    String QUERY_USER_GET_ALL_RESOURCES_COUNT = "resource.active.count";
    String QUERY_SPARSE_EMPTY_PROJECTS = "projects.empty";
    String QUERY_RESOURCE_EDITABLE = "resource.editable";
    String QUERY_SPARSE_PROJECTS = "project.all.sparse";
    String QUERY_IS_ALLOWED_TO = "resource.isEditable";
    String QUERY_IS_ALLOWED_TO_NEW = "resource.isEditable2";
    String QUERY_IS_ALLOWED_TO_MANAGE = "resourcecollection.isEditable";
    String QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS_BY_USER = "dashboard.resourceByPerson";
    String QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO = "rescol.accessible";
    String QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME = "rescol.accessibleName";
    String QUERY_LIST_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME = "rescol.listAccessibleName";
    String QUERY_SPARSE_EDITABLE_RESOURCES = "resource.editable.sparse";
    String QUERY_EDITABLE_RESOURCES = "resource.editable";
    String QUERY_SPARSE_EDITABLE_SORTED_RESOURCES = "resource.editable.sorted.sparse";
    String QUERY_SHARED_COLLECTION_BY_PARENT = "sharedcollection.parent";
    String QUERY_COLLECTIONS_PUBLIC_ACTIVE = "collection.activeId";
    String QUERY_COLLECTION_RESOURCES_WITH_STATUS = "collection.resourcesWithStatus";
    String QUERY_SHARED_COLLECTION_BY_AUTH_OWNER = "sharedCollection.authOwnerId_name";
    String QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT = "collection.hiddenParent";
    String QUERY_EXTERNAL_ID_SYNC = "resource.externalId";
    String QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED = "adminStats.cultureKeywordControlled";
    String QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_UNCONTROLLED = "adminStats.cultureKeywordUnControlled";
    String QUERY_KEYWORD_COUNT_GEOGRAPHIC_KEYWORD = "adminStats.geographicKeyword";
    String QUERY_KEYWORD_COUNT_INVESTIGATION_TYPE = "adminStats.investigationType";
    String QUERY_KEYWORD_COUNT_MATERIAL_KEYWORD = "adminStats.materialKeyword";
    String QUERY_KEYWORD_COUNT_OTHER_KEYWORD = "adminStats.otherKeyword";
    String QUERY_KEYWORD_COUNT_SITE_NAME_KEYWORD = "adminStats.siteNameKeyword";
    String QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_CONTROLLED = "adminStats.siteTypeKeywordControlled";
    String QUERY_KEYWORD_COUNT_SITE_TYPE_KEYWORD_UNCONTROLLED = "adminStats.siteTypeKeywordUnControlled";
    String QUERY_KEYWORD_COUNT_TEMPORAL_KEYWORD = "adminStats.temporalKeyword";
    String QUERY_KEYWORD_COUNT_FILE_EXTENSION = "adminStats.fileExtensions";
    String QUERY_RECENT_USERS_ADDED = "adminStats.recentUsers";
    String QUERY_RECENT = "adminStats.recentFiles";
    // String QUERY_MATCHING_FILES = "datasetRelated.Files";
    String QUERY_USAGE_STATS = "adminStats.usage";
    String QUERY_FILE_STATS = "adminStats.fileDetails";
    String QUERY_MAPPED_CODING_RULES = "dataTableColumn.mappedCodingRules";
    String QUERY_RESOURCES_IN_PROJECT = "resources.inProject";
    String QUERY_RESOURCES_IN_PROJECT_WITH_STATUS = "project.informationResourcesWithStatus";
    String QUERY_DASHBOARD = "dashboard.sql";
    String QUERY_RESOURCES_BY_DECADE = "resources.byDecade";
    String QUERY_SPARSE_RESOURCE_LOOKUP = "resource.sparseLookup";
    String QUERY_SPARSE_SHARED_COLLECTION_LOOKUP = "sharedCollection.sparseLookup";
    String SPACE_BY_PROJECT = "admin.size.project";
    String SPACE_BY_RESOURCE = "admin.size.resource";
    String SPACE_BY_COLLECTION = "admin.size.collection";
    String SPACE_BY_SUBMITTER = "admin.size.submitter";
    String DOWNLOAD_BY = "admin.download";
    String LOGS_FOR_RESOURCE = "admin.logsforResource";
    String FIND_ACTIVE_COUPON = "coupon.active";
    String FILE_DOWNLOAD_HISTORY = "admin.fileFileHistory";
    String RESOURCES_WITH_NULL_ACCOUNT_ID = "account.resourceNull";
    String ACCOUNT_QUOTA_INIT = "account.quota.init";
    String RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID = "account.resourceDifferent";
    String ACCOUNT_GROUP_FOR_ACCOUNT = "account.group";
    String ACCOUNTS_FOR_PERSON = "accounts.forPerson";
    String ACCOUNT_GROUPS_FOR_PERSON = "accountgroups.forPerson";
    String QUERY_INFORMATIONRESOURCES_WITH_FILES = "informationResources.withFiles";
    String QUERY_SPARSE_ACTIVE_RESOURCES = "resources.active";
    String INVOICES_FOR_PERSON = "invoices.forPerson";
    String UNASSIGNED_INVOICES_FOR_PERSON = "invoices.unassignedForPerson";
    String FIND_INVOICE_FOR_COUPON = "invoices.coupons";
    String QUERY_SPARSE_CODING_SHEETS_USING_ONTOLOGY = "sparseCodingSheets.ontology";
    String QUERY_FILE_SIZE_TOTAL = "file.total_size";
    String QUERY_RELATED_RESOURCES = "resource.related";
    String QUERY_PROXY_RESOURCE_FULL = "resourceProxy.full";
    String QUERY_PROXY_RESOURCE_SHORT = "resourceProxy.short";
    String QUERY_RESOURCE_FIND_OLD_LIST = "resource.old";
    String FIND_ACCOUNT_FOR_INVOICE = "account.forInvoice";
    String DELETE_INFORMATION_RESOURCE_FILE_VERSION_IMMEDIATELY = "irfv.delete";
    String SHARED_COLLECTION_LIST_WITH_AUTHUSER = "sharedcollection.idlest.with.authuser";
    String QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED = "query.sparse.editable.sorted.resources.inherited";
    String QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED_SORTED = "query.sparse.editable.sorted.resources.inherited.sorted";
    String QUERY_SPARSE_COLLECTION_RESOURCES = "query.sparse.collection.resources";
    String COLLECTION_VIEW = "collection.views";
    String CREATOR_VIEW = "creator.views";
    String QUERY_COLLECTION_CHILDREN = "resourceCollection.allChildren";
    String QUERY_SHARED_COLLECTION_CHILDREN_RESOURCES = "sharedCollection.allChildrenResources";
    String QUERY_COLLECTION_CHILDREN_RESOURCES_COUNT = "resourceCollection.allChildrenResources_count";
    String QUERY_INFORMATION_RESOURCE_FILE_VERSION_VERIFICATION = "versions.verify";
    String QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES = "update.clearOntologyNodeReferences";
    String UPDATE_DATATABLECOLUMN_ONTOLOGIES = "update.dataTableColumnOntologies";
    String FIND_BY_TDAR_YEAR = "query.sparse_by_tdar_year";
    String FIND_BY_TDAR_YEAR_COUNT = "query.sparse_by_tdar_year_count";
    String QUERY_RESOURCE_FILE_EMBARGO_EXIPRED = "query.expired";
    String QUERY_HOSTED_DOWNLOAD_AUTHORIZATION = "query.hosted_download_auth";
    String QUERY_RESOURCE_FILE_EMBARGOING_TOMORROW = "query.expires_tomorrow";
    String QUERY_INTEGRATION_DATA_TABLE = "query.integration_data_table";
    String QUERY_INTEGRATION_DATA_TABLE_COUNT = "query.integration_data_table_count";
    String QUERY_INTEGRATION_ONTOLOGY = "query.integration_ontology";
    String CAN_EDIT_INSTITUTION = "query.authorize_edit_institution";
    String WORKFLOWS_BY_USER = "query.workflow_by_user";
    String WORKFLOWS_BY_USER_ADMIN = "query.workflow_by_user_admin";
    String AGREEMENT_COUNTS = "query.agreementCounts";
    String AFFILIATION_COUNTS = "query.affiliationCounts";
    String AFFILIATION_COUNTS_CONTRIBUTOR = "query.affiliationCounts.contributor";
    String DELETE_DATA_TABLE_COLUMN_RELATIONSHIPS = "delete.data_table_column_relationships";
    String DELETE_DATA_TABLE_RELATIONSHIPS = "delete.data_table_relationships";
    String SCROLLABLE_SITEMAP = "sitemap.active_resources";
    String QUERY_BY_DOI = "query.by_doi";
    String QUERY_RECENT_INFORMATION_RESOURCE_WITH_DOI = "query.recent_ir_with_doi";
    String QUERY_DATAONE_LIST_OBJECTS = "query.d1_list_objects";
    String UPDATE_RESOURCE_IN_COLLECTION_TO_ACTIVE = "update.updateResourcesToActive";
    String USERS_IN_COLLECTION = "query.users.in.collection";
    String COLLECTION_TIME_LIMITED_IDS = "query.collection_time_limited";
    String QUERY_SIMILAR_PEOPLE = "query.similar_people";
    String MAPPED_RESOURCES = "query.mapped_resources";
    String COUNT_MAPPED_RESOURCES = "query.count_mapped_resources";
    String ALL_RESOURCES_IN_COLLECTION = "query.non_deleted_in_collection";
    String CHECK_INVITES = "check.invites";
    String ALL_INTERNAL_COLLECTIONS = "all.internalCollections";
    String FIND_DOWNLOAD_AUTHORIZATION = "delete.downloadAuthorization";
    String WEEKLY_EMAIL_STATS = "stats.weekly_emails";
    String FIND_RESOURCES_SHARED_WITH = "query.resources_shared_with";
    String FIND_COLLECTIONS_SHARED_WITH = "query.collections_shared_with";
    String FIND_RESOURCES_SHARED_WITH_USERS = "query.resources_shared_with_users";
    String FIND_COLLECTIONS_SHARED_WITH_USERS = "query.collections_shared_with_users";
    String AUTHORIZED_USERS_FOR_RESOURCE = "query.authusers_for_resource";
    String FIND_USERINVITES_BY_COLLECTION = "query.user_invites_by_collection";
    String FIND_USERINVITES_BY_USER = "query.user_invites_by_user";
    String FIND_USERINVITES_BY_RESOURCE = "query.find_user_invites_by_resource";
    String FIND_ALTERNATE_CHILDRENS = "query.alternate_children";
    String FIND_ALTERNATE_CHILDRENS_TREE = "query.alternate_children_tree";
    String FIND_ALTERNATE_LIST_CHILDRENS_TREE = "query.alternate_list_children_tree";
    String QUERY_INSTITUTION_PEOPLE = "query.find_people_by_institution";
    String FIND_EXPIRING_AUTH_USERS_FOR_RESOURCE = "query.expiring_auth_users_resource";
    String FIND_EXPIRING_AUTH_USERS_FOR_COLLECTION = "query.expiring_auth_users_collection";
    String QUERY_RIGHTS_EXPIRY_RESOURCE = "query.expiry_authuser";
    String QUERY_RIGHTS_EXPIRY_COLLECTION = "query.expiry_authuser_collection";
    String QUERY_RIGHTS_EXPIRY_ACCOUNT = "query.expiry_account";
    String QUERY_RIGHTS_EXPIRY_WORKFLOW = "query.expiry_authuser_workflow";
    String FIND_EMAIL_BY_GUID = "query.find_email_by_guid";
    String FIND_FILES_BY_STATUS = "find.files.by.status";
    String LIST_FILES_FOR_DIR = "find.files_for_dir";
    String FIND_DIR_BY_NAME = "find.by_dir_name";
    String LIST_DIR = "find.dir_by_dir";
    String SUMMARIZE_BY_ACCOUNT = "files.report_by_account";
    String BY_ACCOUNT_RECENT = "files.report_by_account_date";
    String QUERY_FILE_UPLOAD_SWEEP = "files.to_process";
    
    // raw SQL/HQL queries

    /**
     * Static HQL and SQL queries that cannot be represented as annotations because they are either pure SQL or use String replacement.
     */
    String QUERY_SQL_DASHBOARD = "select r.status, resource_type , count(*) "
            + " from resource r where  exists (select 1 from collection_resource cr join collection c on cr.collection_id =c.id and c.status='ACTIVE' "
            + "     left join authorized_user au on c.id=au.resource_collection_id left join collection_parents cp on c.id=cp.collection_id "
            + "     join collection c2 on cp.parent_id=c2.id join authorized_user au2 on c2.id=au2.resource_collection_id where"
            + "          (au.user_id=:submitterId and au.general_permission_int > :effectivePermissions (au.date_expires is null or au.date_expires > now())) or"
            + "          (au2.user_id=:submitterId and au2.general_permission_int > :effectivePermissions and (au2.date_expires is null or au2.date_expires > now())))  "
            + " or exists (select 1 from authorized_user au where r.id=au.resource_id and au.user_id=:submitterId "
            + "     and au.general_permission_int > :effectivePermissions and (au.date_expires is null or au.date_expires > now()) ) "
            + " group by 1,2";

    String QUERY_SQL_COUNT = "SELECT COUNT(*) FROM %1$s";
    String QUERY_FIND_ALL = "FROM %s";
    String QUERY_FIND_ALL_WITH_IDS = "FROM %s WHERE id in (:ids)";
    String QUERY_FIND_ALL_WITH_STATUS = "FROM %s WHERE status in (:statuses)";
    String QUERY_SQL_COUNT_ACTIVE_RESOURCE = "SELECT COUNT(*) FROM %1$s where status='ACTIVE' and resourceType='%2$s' ";
    String QUERY_SQL_COUNT_ACTIVE_RESOURCE_WITH_FILES = "select count(distinct  resource.id) from  resource, information_resource_file where  resource.status='ACTIVE' and resource.resource_type='%1$s' and resource.id=information_resource_id";
    String QUERY_SQL_RESOURCE_INCREMENT_USAGE = "update Resource r set r.accessCounter=accessCounter+1 where r.id=:resourceId";
    String QUERY_SQL_RAW_RESOURCE_STAT_LOOKUP = "select rt.* , "
            + "(select count(*) from resource where resource_type = rt.resource_type and status = 'ACTIVE') as all,  "
            + "(select count(*) from resource where resource_type = rt.resource_type and status = 'DRAFT') as draft, "
            + "(select count(distinct information_resource_id) from information_resource_file irf join resource rr on (rr.id = irf.information_resource_id) where rr.resource_type = rt.resource_type and rr.status = 'ACTIVE') as with_files, "
            + "(select count(distinct information_resource_id) from information_resource_file irf join resource rr on (rr.id = irf.information_resource_id) where rr.resource_type = rt.resource_type and rr.status = 'ACTIVE' and irf.restriction = 'CONFIDENTIAL') as with_conf "
            + "from (select distinct resource_type from resource) as rt";

    String QUERY_SQL_CONVERT_WHITELABEL_TO_COLLECTION = "update collection set collection_type= :type where id = :id";
    String QUERY_SQL_CONVERT_COLLECTION_TO_WHITELABEL = "update collection set collection_type='WHITELABEL' where id in (:id)";

    // generated HQL formats
    String QUERY_CREATOR_MERGE_ID = "select merge_creator_id from creator where id=%1$s";

    String QUERY_KEYWORD_MERGE_ID = "select merge_keyword_id from %1$s where id=%2$s";

    // e.g."from Resource r1 where exists (from Resource r2 inner join r2.cultureKeywords ck where r2.id = r1.id and ck.id in (:idlist))"
    String QUERY_HQL_MANY_TO_MANY_REFERENCES = "from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
    // e.g. "from Resource r1 where submitter_id in (:idlist)"
    String QUERY_HQL_MANY_TO_ONE_REFERENCES = "from %1$s r1 where r1.%2$s.id in (:idlist)";

    String QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES = "select count(*) as referenceCount from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
    String QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES = "select count(*) as referenceCount from %1$s r1 where %2$s.id in (:idlist)";

    String QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES_MAP = "select new map(ck.id as id, count(*) as referenceCount) from %1$s r2 inner join r2.%2$s ck where ck.id in (:idlist) group by ck.id";
    String QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES_MAP = "select new map(%2$s.id as id, count(*) as referenceCount) from %1$s r1 where %2$s.id in (:idlist) group by %2$s.id";

    String HQL_EDITABLE_RESOURCE_SUFFIX = " FROM Resource as res left join res.authorizedUsers rau  where "
            +
            " (TRUE=:allResourceTypes or res.resourceType in (:resourceTypes)) "
            + "and (TRUE=:allStatuses or res.status in (:statuses) )  AND "
            +
            " ((exists "
            + "( from ResourceCollection rescol left join rescol.parentIds parentId join rescol.managedResources as colres where colres.id = res.id and rescol.status='ACTIVE' and "
            +
            " (TRUE=:admin or exists ( "
            + "select 1 from ResourceCollection r join r.authorizedUsers as auth where (rescol.id=r.id or parentId=r.id) and auth.user.id=:userId and auth.effectiveGeneralPermission > :effectivePermission"
            + " and (auth.dateExpires is null or auth.dateExpires > now()))) "
            + ") ) "
            + " OR (TRUE=:admin or rau.user.id=:userId and rau.effectiveGeneralPermission > :effectivePermission) and (rau.dateExpires is null or rau.dateExpires > now())) ";

    String INTEGRATION_DATA_TABLE_SUFFIX = "from DataTable dt left join dt.dataTableColumns as dtc left join dtc.defaultCodingSheet.defaultOntology as ont left join dtc.defaultCodingSheet as code left join code.defaultOntology as ont2 join dt.dataset as ds "
            + "where ds.status='ACTIVE' and (:projectId=-1L or ds.project.id=:projectId) and "
            + " lower(ds.title) like :titleLookup and "
            + "(:collectionId=-1L or ds.id in (select distinct r.id from ResourceCollection rc left join rc.parentIds parentId inner join rc.managedResources r where rc.status='ACTIVE' and (rc.id=:collectionId or parentId=:collectionId))) and "
            + "(:hasOntologies=false or ont.id in :paddedOntologyIds ) and "
            + "(:ableToIntegrate=false or ont.id is not NULL or ont2.id is not NULL) and "
            + "(:bookmarked=false or ds.id in (select distinct b.resource.id from BookmarkedResource b where b.person.id=:submitterId) ) "
            + "";

    String HQL_EDITABLE_RESOURCE_SORTED_SUFFIX = HQL_EDITABLE_RESOURCE_SUFFIX + " order by res.title, res.id";
    String QUERY_ACCOUNTS_FOR_RESOURCES = "select id, account_id from resource res where res.id in (%s) ";
    String QUERY_SQL_RESOURCES_BY_YEAR = "select date_part('year', date_created), count(id) from resource where status='ACTIVE' and date_created is not null group by date_part('year', date_created)  order by date_part('year', date_created)  asc";
    String DISTINCT_SUBMITTERS = "SELECT DISTINCT submitter_id from resource";

    String UPDATE_KEYWORD_OCCURRENCE_CLEAR_COUNT = "update %1$s set occurrence=0";
    String UPDATE_KEYWORD_OCCURRENCE_COUNT_INHERITANCE = "update %1$s set occurrence = occurrence + coalesce((select count(resource_id) from resource_%1$s where keyword_id =%1$s.id and status='ACTIVE' and resource_id in (select project_id from information_resource where %2$s is true) group by keyword_id),0)";
    String UPDATE_KEYWORD_OCCURRENCE_COUNT = "update %1$s set occurrence =  occurrence + coalesce((select count(resource_id) from resource_%1$s where keyword_id =%1$s.id and status='ACTIVE' group by keyword_id),0)";
    String UPDATE_CREATOR_OCCURRENCE_CLEAR_COUNT = "update creator set occurrence=0, browse_occurrence=0";
    String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PUBLISHER = "update creator set %s=%s + coalesce((select count(information_resource.id) from information_resource where publisher_id=creator.id group by publisher_id),0)";
    String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_PROVIDER = "update creator set %s=%s + coalesce((select count(information_resource.id) from information_resource where provider_institution_id=creator.id group by provider_institution_id),0)";
    String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INFORMATION_RESOURCE_COPYRIGHT = "update creator set occurrence=occurrence + coalesce((select count(information_resource.id) from information_resource where copyright_holder_id=creator.id group by copyright_holder_id),0) ";
    String UPDATE_CREATOR_OCCURRENCE_RESOURCE_SUBMITTER = "update creator set occurrence=occurrence + coalesce((select count(resource.id) from resource where submitter_id=creator.id group by submitter_id),0)";
    String UPDATE_CREATOR_OCCURRENCE_RESOURCE = "update creator set occurrence = occurrence+ coalesce((select count(resource_id) from resource_creator where creator_id=creator.id group by creator_id),0) ";
    String UPDATE_CREATOR_OCCURRENCE_RESOURCE_INHERITED = "update creator set occurrence = occurrence+ coalesce((select count(resource_id) from resource_creator where creator_id=creator.id and resource_id in (select project_id from information_resource where inheriting_individual_institutional_credit is true)  group by creator_id),0) ";
    String UPDATE_CREATOR_OCCURRENCE_INSTITUTION = "update creator set occurrence = occurrence+ coalesce((select count(person.id) from person where institution_id=creator.id group by institution_id),0) ";
    String DATASETS_USING_NODES = "select id from resource where id in (select dataset_id from data_table where data_table.id in (select data_table_id from data_table_column, coding_rule, coding_sheet where data_table_column.default_coding_sheet_id=coding_sheet_id and coding_rule.coding_sheet_id=coding_sheet.id and  ontology_node_id=%s)) and status = 'ACTIVE'";

    String BROWSE_CREATOR_CREATE_TEMP = "create temporary table rctest (rid bigint, cid bigint, role int);create temporary table rctest_creator (cid bigint, cnt bigint)";
    String BROWSE_CREATOR_ACTIVE_USERS_1 = "insert into rctest select resource.id, submitter_id, 0 from resource where status='ACTIVE'";
    String BROWSE_CREATOR_ROLES_2 = "insert into rctest select resource_id, creator_id, %2$s from resource_creator where resource_id in (select rid from rctest) and role in (%1$s)";
    String BROWSE_CREATOR_IR_ROLES_3 = "insert into rctest select resource_id, creator_id, %2$s from resource_creator where resource_id in (select project_id from rctest, information_resource ir where rid=ir.id and inheriting_individual_institutional_credit=true ) and role in (%1$s)";
    String BROWSE_CREATOR_IR_FIELDS_4 = "insert into rctest select id, provider_institution_id, -1 from information_resource where id in (select rid from rctest) and provider_institution_id is not null union select id, publisher_id, -1 from information_resource where id in (select rid from rctest) and publisher_id is not null";
    String BROWSE_CREATOR_CREATOR_TEMP_5 = "insert into rctest_creator (cid, cnt) select cid, count(rid) from rctest where rid in (select rid from rctest where role >= 0 group by rid having sum(role) = 0 union select rid from rctest where role!=0) group by cid";
    String BROWSE_CREATOR_UPDATE_CREATOR_6 = "update creator SET browse_occurrence = cnt from creator c, rctest_creator where cid=c.id and c.id=creator.id";

    String SELECT_RAW_IMAGE_SITEMAP_FILES = "select r.id as resourceId, r.title, r.description as resourceDescription, r.resource_type, irf.description, irfv.id from resource r, information_resource ir, information_resource_file irf, "
            + "information_resource_file_version irfv where r.id=ir.id and ir.id=irf.information_resource_id and "
            + "irf.id=irfv.information_resource_file_id and internal_type='WEB_SMALL' and resource_type in ('IMAGE','SENSORY_DATA','GEOSPATIAL') "
            + "and restriction='PUBLIC' and r.status='ACTIVE'";

    String CONVERT_PERSON_TO_USER = "INSERT INTO tdar_user (id, username) VALUES(%s, '%s')";

    String DAILY_DOWNLOAD_UPDATE = "INSERT INTO file_download_day_agg (information_resource_file_id, year, month, date_accessed, count) select information_resource_file_id, date_part('year', date_accessed), date_part('month', date_accessed), date_trunc('day',date_accessed), count(id) from information_resource_file_download_statistics where date_trunc('day',date_accessed)='%1$tF' group by information_resource_file_id, date_part('year', date_accessed), date_part('month', date_accessed), date_trunc('day', date_accessed)";

    String FIND_ACTIVE_PERSISTABLE_BY_ID = "select id from %s where status in ('ACTIVE')";
    String COUNT_ACTIVE_PERSISTABLE_BY_ID = "select count(id) from %s where status in ('ACTIVE')";
    String FIND_ACTIVE_PERSON_BY_ID = "select id from %s where status in ('ACTIVE') and browse_occurrence > 0 and hidden=false";
    String FIND_ACTIVE_INSTITUTION_BY_ID = "select id from %s where status in ('ACTIVE') and browse_occurrence > 0 and hidden=false";

    String RESOURCE_ACCESS_COUNT_SQL = "select coalesce((select count(ras.id)  from resource_access_statistics ras where ras.resource_id='%1$s' and date_trunc('day',ras.date_accessed) >= '%2$tY-%2$tm-%2$td') ,0) + coalesce((select sum(rad.total) from resource_access_month_agg rad where rad.resource_id='%1$s'),0)";
    String DOWNLOAD_COUNT_SQL = "select coalesce((select count(irfds.id)  from information_resource_file_download_statistics irfds where irfds.information_resource_file_id='%1$s' and irfds.date_accessed > '%2$tY-%2$tm-%2$td') ,0) + coalesce((select sum(fda.count) from file_download_day_agg fda where fda.information_resource_file_id='%1$s'),0)";
    String ANNUAL_ACCESS_SKELETON = "select res.id, res.title, res.resource_type, res.status, %s %s from resource res ";

    String ANNUAL_VIEW_PART = "(select sum(total%2$s) from resource_access_month_agg r where res.id=r.resource_id and r.year=%1$s) as \"%1$s Views %2$s\"";
    String MONTH_VIEW_PART = "(select sum(total%3$s) from resource_access_month_agg r where res.id=r.resource_id and r.year=%2$s and r.month=%1$s) as \"%2$s-%1$02d Views %3$s\"";
    String DAY_VIEW_PART = "(select sum(coalesce(r.d%2$s%5$s,0)) from resource_access_month_agg r where r.resource_id=res.id and r.year=%3$s and r.month=%4$s) as \"%1$s Views %5$s\"";
    String ANNUAL_DOWNLOAD_PART = "(select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=res.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='%1$s') as \"%1$s Downloads\"";
    String MONTH_DOWNLOAD_PART = "(select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=res.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and year='%2$s' and month='%1$s') as \"%2$s-%1$02d Downloads\"";
    String DAY_DOWNLAOD_PART = "(select sum(count) from file_download_day_agg, information_resource_file where information_resource_id=res.id and information_resource_file.id=file_download_day_agg.information_resource_file_id and date_accessed = '%1$s') as \"%1$s Downloads\"";

    String HOMEPAGE_GEOGRAPHIC = "select code, resource_type, sum(count), id from ( ( select code, count(*), r.resource_type, gk.id from geographic_keyword gk join resource_managed_geographic_keyword rgk on gk.id = rgk.keyword_id join resource r on r.id = rgk.resource_id left join information_resource ir on (ir.id = r.id and ir.inheriting_spatial_information = false) where (code !='') and r.status = 'ACTIVE' group by code, r.resource_type, gk.id ) union all select code, count(*), irr.resource_type, gk.id from geographic_keyword gk join resource_managed_geographic_keyword rgk on gk.id = rgk.keyword_id join resource p on p.id = rgk.resource_id join information_resource ir on (ir.project_id = p.id and ir.inheriting_spatial_information = true) join resource irr on (irr.id = ir.id) where (code !='') and irr.status = 'ACTIVE' group by code, irr.resource_type, gk.id ) as allrecs group by code, resource_type, id order by 1, 2";

    String DAILY_RESOURCE_STATS_CLEANUP = "delete from resource_access_statistics where date_trunc('day',date_accessed) < '%1$tY-%1$tm-%1$td' ";

    String AGG_RESOURCE_INSERT_MONTH = "update resource_access_month_agg agg set d%1$s=valcount, total=coalesce(total,0) + coalesce(valcount,0) from (select resource_id, count(ras.id) as valcount from resource_access_statistics ras "
            + "where ras.date_accessed >= '%2$s' and ras.date_accessed < '%3$s' group by 1) ras where ras.resource_id= agg.resource_id and month=:month and year=:year";
    String AGG_RESOURCE_INSERT_MONTH_BOT = "update resource_access_month_agg agg set d%1$s_bot=valcount, total=coalesce(total,0) + coalesce(valcount,0), total_bot=coalesce(total_bot,0) + coalesce(valcount,0) from (select resource_id, count(ras.id) as valcount from resource_access_statistics ras "
            + "where ras.date_accessed >= '%2$s' and ras.date_accessed < '%3$s' and bot is true group by 1) ras where ras.resource_id= agg.resource_id and month=:month and year=:year";
    String AGG_RESOURCE_SETUP_MONTH = "insert into resource_access_month_agg(resource_id, year, month) select id, %s, %s from resource where date_created >=  :date";
    String WEEKLY_POPULAR = "select count(ras.id), resource_id  from resource_access_statistics ras, resource r where  r.id=ras.resource_id and r.status='ACTIVE' and date_accessed > '%s' and date_accessed < '%s' group by 2 order by 1 desc limit %s";
    String ANNUAL_RESOURCE_UPDATE = "insert into resource_access_year_agg (resource_id, year, total, total_bot) select resource_id, year, sum(total), sum(total_bot) from resource_access_month_agg where year=:year group by 1,2";
    String ANNUAL_RESOURCE_CLEANUP = "delete from resource_access_year_agg where year=:year";
    String MONTHLY_USAGE_FOR_RESOURCE = "query.monthly_for_resource";

    String MOST_POPULAR_BY_BILLING_ACCOUNT = "SELECT count(ras.id), resource_id  FROM resource_access_statistics ras, resource r "
            + "WHERE  r.id=ras.resource_id AND r.status='ACTIVE' AND r.account_id = %s GROUP BY 2 ORDER BY 1 DESC LIMIT %s";

    /**
     * it's possible this is too generous and we need something closer to the following which unions to explicit joins:
     * select au.user_id, au.resource_id, au.resource_collection_id from authorized_user au join resource r on au.resource_id=r.id and (r.status='ACTIVE' or
     * r.status='DRAFT')
     * join authorized_user au2 on r.id=au2.resource_id and au2.user_id=:submitterId union
     * select au.user_id, au.resource_id, au.resource_collection_id from authorized_user au join collection c on au.resource_collection_id=c.id and
     * c.status='ACTIVE'
     * left join authorized_user au3 on c.id=au3.resource_collection_id
     * left join collection_parents cp on c.id=cp.collection_id
     * join collection c2 on cp.parent_id=c2.id and c2.status='ACTIVE'
     * join authorized_user au4 on c2.id=au4.resource_collection_id where (au3.user_id=:submitterId or au4.user_id=:submitterId) order by 1;^C
     */
    String QUERY_USERS_SHARED_WITH = "select distinct user_id from authorized_user inner join collection on authorized_user.resource_collection_id=collection.id and exists ( "
            + "select resource_collection_id as collection_id from authorized_user where user_id=:userId and general_permission_int > 499 and resource_collection_id=collection.id union "
            + "select collection_parents.collection_id as collection_id  from authorized_user a1  join collection_parents on "
            + "a1.resource_collection_id=collection_parents.parent_id where user_id=:userId and general_permission_int > 499 and resource_collection_id=collection.id ) "
            + "and status='ACTIVE' union select user_id from authorized_user  inner join resource on authorized_user.resource_id=resource.id and exists ( "
            + "select resource_id from authorized_user where user_id=:userId and general_permission_int > 499 and resource_id=resource.id and resource.status in ('ACTIVE','DRAFT'))  ";

    String QUERY_RESOURCES_SHARED_WITH = "select id, title, status, resource_type from resource where id in "
            + " (select  au.resource_id from authorized_user au left join resource r on au.resource_id=r.id and (r.status='ACTIVE' or r.status='DRAFT') "
            + "left join authorized_user au2 on r.id=au2.resource_id and au2.user_id=:ownerId "
            + "left join collection c on au.resource_collection_id=c.id and c.status='ACTIVE' "
            + "left join authorized_user au3 on c.id=au3.resource_collection_id and au3.user_id=:ownerId "
            + "left join collection_parents cp on c.id=cp.collection_id "
            + "left join collection c2 on cp.parent_id=c2.id and c2.status='ACTIVE' "
            + "left join authorized_user au4 on c2.id=au4.resource_collection_id and au4.user_id=:ownerId where au.user_id=:userId and au.resource_id is not null)";
    String MAPPED_DATASETS = "query.mapped_datasets";
    String MAPPED_COLLECTIONS = "query.collecton_mapped_datasets";
    String UPDATE_CLEAR_MAPPINGS = "update information_resource set mappeddatakeyvalue=null,mappeddatakeycolumn_id=null where id in (select collection_id from collection_resource where collection_id =:collection_id or collection_id in (select collection_id from collection_parents where parent_id=:collection_id))";


}