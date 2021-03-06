package org.tdar.core.service.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.HasTables;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.resource.DataTableDao;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.service.resource.dataset.DatasetChangeLogger;
import org.tdar.db.datatable.ImportColumn;
import org.tdar.db.datatable.ImportTable;
import org.tdar.db.datatable.TDataTable;
import org.tdar.db.datatable.TDataTableRelationship;
import org.tdar.db.model.TargetDatabase;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.utils.Pair;

@Service
public class DatasetImportServiceImpl implements DatasetImportService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DatasetDao datasetDao;
    @Autowired
    private DataTableDao dataTableDao;
    @Autowired
    private InformationResourceFileDao inormationResourceFileDao;
    @Autowired
    private FileAnalyzer analyzer;
    @Autowired
    @Qualifier("target")
    private TargetDatabase tdarDataImportDatabase;

    /*
     * When we import a @link Dataset, if there's an existing set of @link DataTable entries mapped to a Dataset, we reconcile each @link DataTable and @link
     * DataTableColunn on import such that if the old DataTables and Columns match the incomming, then we'll re-use the mappings. If they're different, their
     * either added or dropped respectively.
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetImportService#reconcileDataset(org.tdar.core.bean.resource.file.InformationResourceFile,
     * org.tdar.core.bean.resource.Dataset, org.tdar.core.bean.resource.Dataset)
     */
    @Override
    @Transactional(noRollbackFor = TdarRecoverableRuntimeException.class)
    public void reconcileDataset(InformationResourceFile datasetFile, Dataset dataset_, List<TDataTable> incomingDataTables,
            List<TDataTableRelationship> incomingRelationships) {
        Dataset dataset = dataset_;
        // helper Map to manage existing tables - all remaining entries in this existingTablesMap will be purged at the end of this process
        // take the dataset off the session at the last moment, and then bring it back on

        DatasetChangeLogger dsChangeLog = new DatasetChangeLogger(dataset);

        Pair<Collection<DataTable>, Collection<DataTableColumn>> reconcileTables = reconcileTables(dataset, incomingDataTables);

        datasetDao.deleteRelationships(dataset.getRelationships());
        datasetDao.cleanupUnusedTablesAndColumns(dataset, reconcileTables.getFirst(), reconcileTables.getSecond());
        reconcileRelationships(dataset, incomingRelationships);
        reconcileTables = null; // resetting and removing references
        logger.debug("dataset: {} id: {}", dataset.getTitle(), dataset.getId());
        for (DataTable dataTable : dataset.getDataTables()) {
            logger.debug("dataTable: {}", dataTable);
            List<DataTableColumn> columns = dataTable.getDataTableColumns();
            logger.debug("dataTableColumns: {}", columns);
            for (DataTableColumn column : columns) {
                datasetDao.translate(column, column.getDefaultCodingSheet());
            }
        }
        datasetFile.setStatus(FileStatus.PROCESSED);
        datasetFile.setInformationResource(dataset);
        // NOTE: do I need to clear the incoming?
        // dataset = datasetDao.merge(dataset);
        datasetDao.saveOrUpdate(dataset);
        dsChangeLog.compare(dataset);
    }

    
    @Override
    @Transactional(noRollbackFor = TdarRecoverableRuntimeException.class)
    public void reconcileDataset(TdarFile dataset, List<TDataTable> incomingDataTables,
            List<TDataTableRelationship> incomingRelationships) {
        // helper Map to manage existing tables - all remaining entries in this existingTablesMap will be purged at the end of this process
        // take the dataset off the session at the last moment, and then bring it back on

        Pair<Collection<DataTable>, Collection<DataTableColumn>> reconcileTables = reconcileTables(dataset, incomingDataTables);

        datasetDao.deleteRelationships(dataset.getRelationships());
        datasetDao.cleanupUnusedTablesAndColumns(dataset, reconcileTables.getFirst(), reconcileTables.getSecond());
        reconcileRelationships(dataset, incomingRelationships);
        reconcileTables = null; // resetting and removing references
        logger.debug("dataset id: {}", dataset.getId());
        for (DataTable dataTable : dataset.getDataTables()) {
            logger.debug("dataTable: {}", dataTable);
            List<DataTableColumn> columns = dataTable.getDataTableColumns();
            logger.debug("dataTableColumns: {}", columns);
            for (DataTableColumn column : columns) {
                datasetDao.translate(column, column.getDefaultCodingSheet());
            }
        }
        // NOTE: do I need to clear the incoming?
        // dataset = datasetDao.merge(dataset);
        datasetDao.saveOrUpdate(dataset);
    }

    /*
     * Reconciles two @link Dataset entities together based on the transient entries coming from the @link WorkflowContext and the existing ones. First, it
     * tries to match name-by-name. Second, if there is "just" a in both, eg. in a CSV, TAB, or other Format, then don't match on name, assume that they're the
     * same table, as table name was generated by us instead of the user.
     */
    private Pair<Collection<DataTable>, Collection<DataTableColumn>> reconcileTables(HasTables dataset, List<TDataTable> incomingDataTables) {
        HashMap<String, DataTable> existingTablesMap = new HashMap<String, DataTable>();
        HashMap<String, String> secondaryLookupMap = new HashMap<>();
        for (DataTable existingDataTable : dataset.getDataTables()) {
            String internalName = existingDataTable.getInternalName();
            existingTablesMap.put(internalName, existingDataTable);
            logger.debug("existingTableName: {} ({}/{}/{})", internalName, existingDataTable.getName(), existingDataTable.getDisplayName(), existingDataTable.getId());
            // note there may be failures to match here as table names are too long and thus get arbitrary numbers that cannot be matched
            String name = tdarDataImportDatabase.normalizeTableOrColumnNames(existingDataTable.getDisplayName());
            if (!StringUtils.equals(name, internalName)) {
                secondaryLookupMap.put(name, internalName);
            }

        }
        dataset.getDataTables().clear();
        logger.debug("Existing name to table map: {}", existingTablesMap);
        Set<DataTableColumn> columnsToUnmap = new HashSet<DataTableColumn>();

        for (TDataTable incomingtable : incomingDataTables) {
            // first check that the incoming data table has data table columns.
            String internalTableName = incomingtable.getInternalName();
            DataTable existingTable = existingTablesMap.get(internalTableName);
            logger.debug("compare: {} <==> {} ==> ", existingTable, internalTableName, secondaryLookupMap.containsKey(internalTableName));
            if ((existingTable == null) && (existingTablesMap.size() == 1) && (incomingDataTables.size() == 1)) {
                // the table names did not match, but we have one incoming table and one existing table. Try to match them regardless.
                existingTable = existingTablesMap.values().iterator().next();
            }

            // our naming conventions change from time-to-time, fallback to "renormalize" and use that for lookup
            if (existingTable == null) {
                if (secondaryLookupMap.containsKey(internalTableName)) {
                    existingTable = existingTablesMap.get(secondaryLookupMap.get(internalTableName));
                    internalTableName = secondaryLookupMap.get(internalTableName);
                }
            }

            DataTable tableToPersist = new DataTable();
            if (existingTable != null) {
                existingTablesMap.remove(existingTable.getInternalName());
                if (!datasetDao.checkExists(existingTable)) {
                    if (!datasetDao.checkExists(incomingtable)) {
                        throw new TdarRecoverableRuntimeException("datasetImportService.no_tables_exist");
                    }
                    existingTable.setName(incomingtable.getName());
                }
                logger.debug("matching existing table for :{}", existingTable);

                Collection<DataTableColumn> columnsToRemove = reconcileDataTable(dataset, existingTable, incomingtable);
                tableToPersist = existingTable;
                if (CollectionUtils.isNotEmpty(columnsToRemove)) {
                    columnsToUnmap.addAll(columnsToRemove);
                }
            } else {
                logger.debug("Creating new table for :{}", incomingtable);
                // continue with the for loop, tableToPersist does not require any metadata merging because
                // we can't find an existing table to merge it with
                DatasetImportUtils.copyValuesFromIncomingTDataTable(incomingtable, tableToPersist);
                DatasetImportUtils.copyColumnsFromIncomingTDataTable(incomingtable, tableToPersist);
                logger.trace("No analogous existing table to merge with incoming data table {}, moving on", tableToPersist);
            }
            dataset.getDataTables().add(tableToPersist);
        }

        // any tables left in existingTables didn't have an analog in the incoming dataset, so clean them up
        Collection<DataTable> tablesToRemove = existingTablesMap.values();
        return new Pair<Collection<DataTable>, Collection<DataTableColumn>>(tablesToRemove, columnsToUnmap);
    }

    /*
     * Reconciles DataTableRelationships between two datasets, this is not well supported at the moment.
     */
    private void reconcileRelationships(HasTables dataset, List<TDataTableRelationship> incomingRelationships) {
        // refresh the column relationships so that they refer to new versions of the columns which have the same names as the old columns
        // dataset.getRelationships().clear();

        for (TDataTableRelationship rel : incomingRelationships) {
            dataset.getRelationships().add(DatasetImportUtils.convertToRelationship(dataset, rel));
        }
    }

    /*
     * Iterate through each @link DataTableColumn on the @link DataTable and reconcile them by name.
     */
    private Collection<DataTableColumn> reconcileDataTable(HasTables dataset, DataTable existingTable, ImportTable tableToPersist) {
        // Pair<DataTable, Collection<DataTableColumn>> toReturn = new Pair<DataTable, Collection<DataTableColumn>>(null, null);
        List<DataTableColumn> toRemove = new ArrayList<>();
        if (CollectionUtils.isEmpty(tableToPersist.getDataTableColumns())) {
            return toRemove;
        }

        // if there is an analogous existing table, try to reconcile all the columns from the incoming data table
        // with the columns from the existing data table.
        HashMap<String, DataTableColumn> existingColumnsMap = new HashMap<String, DataTableColumn>();
        HashMap<String, String> secondaryLookupMap = new HashMap<>();
        for (DataTableColumn existingColumn : existingTable.getDataTableColumns()) {
            String key = existingColumn.getName().toLowerCase().trim();
            existingColumnsMap.put(key, existingColumn);
            String name = tdarDataImportDatabase.normalizeTableOrColumnNames(existingColumn.getDisplayName());
            if (!StringUtils.equals(name, key)) {
                secondaryLookupMap.put(name, key);
            }
        }

        logger.debug("existing columns: {}", existingColumnsMap);
        List<? extends ImportColumn> incomingColumns = tableToPersist.getDataTableColumns();
        // for each incoming data table column, try to match it with an equivalent column
        // from existingTable using the existingNameToColumnMap
        for (int i = 0; i < incomingColumns.size(); i++) {
            ImportColumn incomingColumn = incomingColumns.get(i);
            String normalizedColumnName = incomingColumn.getName().toLowerCase().trim();

            DataTableColumn existingColumn = existingColumnsMap.get(normalizedColumnName);
            // our naming conventions change from time-to-time, fallback to "renormalize" and use that for lookup
            if (existingColumn == null) {
                if (secondaryLookupMap.containsKey(normalizedColumnName)) {
                    existingColumn = existingColumnsMap.get(secondaryLookupMap.get(normalizedColumnName));
                    normalizedColumnName = secondaryLookupMap.get(normalizedColumnName);
                }
            }
            logger.debug("Reconciling existing {} with incoming column {}", existingColumn, incomingColumn);
            reconcileColumn(tableToPersist, existingTable, existingColumnsMap, normalizedColumnName, incomingColumn, existingColumn);
        }

        logger.debug("deleting unmerged columns: {}", existingColumnsMap);
        logger.debug("result: {}", incomingColumns);
        // datasetDao.detachFromSession(existingTable);
        // tableToPersist.setId(existingTable.getId());

        logger.debug("merged data table is now {}", tableToPersist);
        logger.debug("actual data table columns {}, incoming data table columns {}", tableToPersist.getDataTableColumns(), incomingColumns);
        return existingColumnsMap.values();
    }

    /**
     * Using the existing column map, we try and find a matching @link DataTableColumn, if we do, we copy the values off of the
     * existing column before returning.
     * 
     * @param incomingTable
     * @param existingNameToColumnMap
     * @param normalizedColumnName
     * @param incomingColumn
     * @param existingColumn
     * @return
     */
    @Transactional
    private void reconcileColumn(ImportTable incomingTable, DataTable existingTable, HashMap<String, DataTableColumn> existingNameToColumnMap,
            String normalizedColumnName, ImportColumn incomingColumn, DataTableColumn existingColumn) {
        // FIXME: check that types are compatible before merging

        if (existingColumn == null) {
            DatasetImportUtils.createDataTableColumn(incomingColumn, existingTable);
            return;
        }
        /*
         * if we've gotten this far, we know that the incoming column should be saved onto the existing table instead of the transient table that it was
         * originally set on. copy all values that should be retained
         */
        logger.trace("Merging incoming column with existing column");
        existingColumn.setName(incomingColumn.getName());
        // if our column type changes, then we need to reset the mappings
        if (!Objects.equals(existingColumn.getColumnDataType(), incomingColumn.getColumnDataType())) {
            existingColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
            existingColumn.setMappedOntology(null);
            existingColumn.setDefaultCodingSheet(null);
            existingColumn.setColumnDataType(incomingColumn.getColumnDataType());
        }

        // overwrite the old set of distinct column values w/ incoming values
        existingColumn.getValues().clear();
        existingColumn.getIntValues().clear();
        existingColumn.getFloatValues().clear();
        DatasetImportUtils.copyValues(incomingColumn, existingColumn);
        existingNameToColumnMap.remove(normalizedColumnName);
    }

    /*
     * Each @link CodingSheet is mapped to one or many @link Dataset records. Because of this, when we re-map a @link CodingSheet to a @link Ontology, we need
     * to retranslate each of the @link Dataset records
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.DatasetImportService#refreshAssociatedDataTables(org.tdar.core.bean.resource.CodingSheet)
     */
    @Override
    @Transactional
    public void refreshAssociatedDataTables(CodingSheet codingSheet) {
        // retranslate associated datatables, and recreate translated files
        Set<DataTableColumn> associatedDataTableColumns = codingSheet.getAssociatedDataTableColumns();
        if (CollectionUtils.isEmpty(associatedDataTableColumns)) {
            return;
        }

        datasetDao.translate(associatedDataTableColumns, codingSheet);
        for (DataTable dataTable : dataTableDao.findDataTablesUsingResource(codingSheet)) {
            Dataset dataset = dataTableDao.findDatasetForTable(dataTable);
            InformationResourceFile file = datasetDao.createTranslatedFile(dataset, analyzer, inormationResourceFileDao);
            dataTableDao.saveOrUpdate(file);
        }
    }

}
