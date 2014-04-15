package org.tdar.core.service.bulk;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.AsyncUpdateReceiver.DefaultReceiver;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BulkUploadTemplateService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.ReflectionService;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.utils.MessageHelper;

/**
 * The BulkManifestProxy helps keep track of state throughout the @link BulkUploadService's run process. It tracks filenames, fields, the resources created and
 * other information
 * 
 * @author abrin
 * 
 */
public class BulkManifestProxy implements Serializable {

    private static final long serialVersionUID = -3716153931002809635L;
    public static final String ASTERISK = "*";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private DataFormatter formatter = new HSSFDataFormatter();

    private List<String> filenames = new ArrayList<>();
    private List<String> filenamesInsensitive = new ArrayList<>();
    private boolean caseSensitive = false;
    private Map<Row, List<String>> rowFilenameMap = new HashMap<>();
    private Map<String, CellMetadata> cellLookupMap = new HashMap<>();
    private List<String> columnNames = new ArrayList<>();
    private LinkedHashSet<CellMetadata> allValidFields = new LinkedHashSet<>();
    private Collection<FileProxy> fileProxies;
    private Sheet sheet;
    private Person submitter;
    private AsyncUpdateReceiver asyncUpdateReceiver = new DefaultReceiver();
    private transient ExcelService excelService;
    private Map<String, Resource> resourcesCreated = new HashMap<>();

    private Row columnNamesRow;

    private Set<CellMetadata> required = new HashSet<>();

    public BulkManifestProxy(Sheet sheet2, LinkedHashSet<CellMetadata> allValidFields2, Map<String, CellMetadata> cellLookupMap2, ExcelService excelService) {
        this.sheet = sheet2;
        this.allValidFields = allValidFields2;
        this.cellLookupMap = cellLookupMap2;
        this.excelService  = excelService;
    }

    /**
     * Add file to proxy
     * 
     * @param filename
     */
    public void addFilename(String filename) {
        filenames.add(filename);
        filenamesInsensitive.add(filename.toLowerCase());
    }

    // don't want to expose the list directly for manipulation because we're managing two variants (case sensitive and insensitive)
    public String listFilenames() {
        return String.format("[%s]", StringUtils.join(filenames, ","));
    }

    public boolean containsFilename(String filename) {
        if (!caseSensitive) {
            return filenamesInsensitive.contains(filename.toLowerCase());
        }
        return filenames.contains(filename);
    }

    


    /**
     * Read the entire excel file in row-by-row. Process the row by looking at
     * the fields and reflecting the values into their appropriate beans.
     * 
     * FIXME: This method needs refactoring and is overly complex
     * 
     */
    @SuppressWarnings("unchecked")
    public <R extends Resource> void readExcelFile(BulkUploadTemplateService bulkUploadTemplateService, EntityService entityService, ReflectionService reflectionService) throws InvalidFormatException, IOException {

        AsyncUpdateReceiver asyncUpdateReceiver = getAsyncUpdateReceiver();
        FormulaEvaluator evaluator = getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        int rowNum = 0;
        Map<String, CellMetadata> cellLookupMap = getCellLookupMap();

        // if we use a manifest file, then keep track of all resources that have errors
        List<String> allFilenames = new ArrayList<>();
        for (Row row : getSheet()) {
            if (row == null) {
                logger.warn("null row.");
                continue;
            }
            rowNum++;
            if (ExcelService.FIRST_ROW == (rowNum - 1)) {
                continue;
            }
            // find the resource for the identifier (based on current title

            int startColumnIndex = getFirstCellNum();
            int endColumnIndex = getLastCellNum();

            String filename = excelService.getCellValue(formatter, evaluator, row, startColumnIndex);

            // look in the hashmap for the filename, skip the examples
            Resource resourceToProcess = findResource(filename, getResourcesCreated());
            boolean skip = shouldSkipFilename(filename, resourceToProcess);
            logger.debug("fn: {} resource to Process: {} skip:{}", filename, resourceToProcess, skip);
            if (skip) {
                List<Object> vals = new ArrayList<>();
                vals.add(filename);
                asyncUpdateReceiver.addError(new TdarRecoverableRuntimeException("bulkUploadService.skipping_line_filename_not_found", vals));

                continue;
            }
            if (isCaseSensitive()) {
                allFilenames.add(filename);
            } else {
                allFilenames.add(filename.toLowerCase());
            }
            logger.info("processing:" + filename);

            asyncUpdateReceiver.setPercentComplete(asyncUpdateReceiver.getPercentComplete() + 1f);
            asyncUpdateReceiver.setStatus(MessageHelper.getMessage("bulkUploadService.processing_file",
                    Arrays.asList(filename)));

            ResourceCreatorProxy creatorProxy = new ResourceCreatorProxy();

            // there has to be a smarter way to do this generically... iterate
            // through valid field names for class
            boolean seenCreatorFields = false;

            Set<CellMetadata> requiredFields = bulkUploadTemplateService.getRequiredFields(getAllValidFields());
            requiredFields.remove(cellLookupMap.get(BulkUploadTemplate.FILENAME));
            // iterate through the spreadsheet
            try {
                for (int columnIndex = (startColumnIndex + 1); columnIndex < endColumnIndex; ++columnIndex) {
                    String value = excelService.getCellValue(formatter, evaluator, row, columnIndex);
                    String name = getColumnNames().get(columnIndex);
                    CellMetadata cellMetadata = cellLookupMap.get(name);
                    logger.trace("cell metadata: {}", cellMetadata);

                    if (StringUtils.isBlank(name) || StringUtils.isBlank(value) || (cellMetadata == null)) {
                        continue;
                    }

                    Class<?> mappedClass = cellMetadata.getMappedClass();
                    boolean creatorAssignableFrom = Creator.class.isAssignableFrom(mappedClass);
                    boolean resourceSubtypeAssignableFrom = false;
                    if ((mappedClass != null) && (resourceToProcess != null)) {
                        resourceSubtypeAssignableFrom = mappedClass.isAssignableFrom(resourceToProcess.getClass());
                    }
                    boolean resourceAssignableFrom = Resource.class.isAssignableFrom(mappedClass);
                    boolean resourceCreatorAssignableFrom = ResourceCreator.class.isAssignableFrom(mappedClass);
                    if ((cellMetadata == null) || !((mappedClass != null) && (resourceSubtypeAssignableFrom
                            || resourceCreatorAssignableFrom || creatorAssignableFrom))) {
                        if (mappedClass != null) {
                            throw new TdarRecoverableRuntimeException("bulkUploadService.fieldname_is_not_valid_for_type",
                                    (List<?>) Arrays.asList(filename, name, resourceToProcess.getResourceType()));
                        }
                    }
                    requiredFields.remove(cellMetadata);
                    if (resourceAssignableFrom) {
                        try {
                            reflectionService.validateAndSetProperty(resourceToProcess, cellMetadata.getPropertyName(), value);
                        } catch (RuntimeException re) {
                            asyncUpdateReceiver.addError(re);
                        }
                    } else {
                        if ((resourceCreatorAssignableFrom || creatorAssignableFrom)) {

                            logger.trace(String.format("%s - %s - %s", mappedClass, cellMetadata.getPropertyName(), value));
                            if (resourceCreatorAssignableFrom) {
                                seenCreatorFields = true;
                                reflectionService.validateAndSetProperty(creatorProxy, cellMetadata.getPropertyName(), value);

                                // FIXME: This is a big assumption that role is
                                // the last field and then we repeat
                                reconcileResourceCreator(resourceToProcess, creatorProxy, entityService);
                                creatorProxy = new ResourceCreatorProxy();
                                seenCreatorFields = false;
                            }
                            if (Person.class.isAssignableFrom(mappedClass)) {
                                reflectionService.validateAndSetProperty(creatorProxy.getPerson(), cellMetadata.getPropertyName(), value);
                            }
                            if (Institution.class.isAssignableFrom(mappedClass)) {
                                logger.trace("{} ", cellMetadata);
                                Object bean = creatorProxy.getInstitution();
                                if (cellMetadata.getName().contains("Person.Institution")) {
                                    if (creatorProxy.getPerson().getInstitution() == null) {
                                        creatorProxy.getPerson().setInstitution(new Institution());
                                    }
                                    bean = creatorProxy.getPerson().getInstitution();
                                }
                                reflectionService.validateAndSetProperty(bean, cellMetadata.getPropertyName(), value);
                            }
                        }
                    }
                }
                if (seenCreatorFields) {
                    reconcileResourceCreator(resourceToProcess, creatorProxy, entityService);
                }
                logger.debug("resourceCreators:{}", resourceToProcess.getResourceCreators());
                if (requiredFields.size() > 0) {
                    List<String> required = (List<String>) CollectionUtils.collect(requiredFields,
                            new BeanToPropertyValueTransformer("displayName"));
                    throw new TdarRecoverableRuntimeException("bulkUploadService.required_fields_missing",
                            Arrays.asList(filename, StringUtils.join(required, ", ")));
                }
            } catch (Throwable t) {
                logger.debug("excel mapping error: {}", t.getMessage(), t);
                resourceToProcess.setStatus(Status.DELETED);
                asyncUpdateReceiver.addError(t);
            }
        }

        for (String filename : getResourcesCreated().keySet()) {
            if (isCaseSensitive()) {
                allFilenames.remove(filename);
            } else {
                allFilenames.remove(filename);
                allFilenames.remove(filename.toLowerCase());
            }
            logger.debug("removing: {}", filename);
        }
        logger.debug("{}", allFilenames);
        if (CollectionUtils.isNotEmpty(allFilenames)) {
            asyncUpdateReceiver.addError(new TdarRecoverableRuntimeException("bulkUploadService.tooManyFiles",
                    Arrays.asList(StringUtils.join(allFilenames.toArray(), ", "))));
        }
    }

    


    /**
     * Check whether this is one of the test file names, or is there something wrong with the
     * resource?
     */
    private boolean shouldSkipFilename(String filename, Resource resourceToProcess) {
        if (filename.equalsIgnoreCase(BulkUploadTemplate.EXAMPLE_PDF)
                || filename.equalsIgnoreCase(BulkUploadTemplate.EXAMPLE_TIFF)) {
            logger.debug("skipping template sample filenames (example...)");
            return true;
        }
        return (resourceToProcess == null) || StringUtils.isBlank(filename);
    }

    /**
     * Confirm that a @link ResourceCreator is valid, and then set it properly
     * on the @link Resource
     * 
     */
    private void reconcileResourceCreator(Resource resource, ResourceCreatorProxy proxy, EntityService entityService) {
        ResourceCreator creator = proxy.getResourceCreator();
        logger.info("reconciling creator... {}", creator);
        if (creator.isValidForResource(resource)) {
            entityService.findResourceCreator(creator);
            creator.setSequenceNumber(resource.getResourceCreators().size());
            logger.debug(creator + " (" + creator.getSequenceNumber() + ")");

            resource.getResourceCreators().add(creator);
            logger.debug("added " + creator + " successfully");
        } else {
            throw new TdarRecoverableRuntimeException("bulkUploadService.resource_creator_is_not_valid_for_type",
                    Arrays.asList(creator.getCreator().getName(),
                            creator.getRole(), resource.getResourceType()));
        }
    }

    

    /**
     * Special Case lookup: (a) look for exact match (b) look for case where
     * person forgot file extension
     * 
     * @param filename
     * @param filenameResourceMap
     * @return
     */
    public Resource findResource(String filename, Map<String, Resource> filenameResourceMap) {
        /*
         * DEPENDING ON WHETHER THE MANIFEST IS CASE SENSITIVE OR NOT, the MAP
         * WILL EITHER BE (A) a TreeMap(case insensitive) or a HashMap
         */
        Resource toReturn = filenameResourceMap.get(filename);
        if (toReturn != null) {
            return toReturn;
        }

        for (String name : filenameResourceMap.keySet()) {
            String base = FilenameUtils.getBaseName(name);
            if (base.equals(filename)) {
                if (toReturn != null) {
                    throw new TdarRecoverableRuntimeException("bulkUploadService.please_include_the_file_extension_in_the_filename");
                }
                toReturn = filenameResourceMap.get(name);
            }
        }

        return toReturn;
    }

    
    /**
     * Evaluates all of the filenames in the ExcelSheet by iterating over the
     * Row, determines whether we're dealing with a case-sensitive or inensitive
     * system.
     * 
     * @param rowIterator
     * @param proxy
     */
    public void testFilenameCaseAndAddFiles(Iterator<Row> rowIterator) {
        Map<String, String> caseTest = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell = row.getCell(ExcelService.FIRST_COLUMN);
            if (cell == null) {
                continue;
            }
            String filename = cell.getStringCellValue();
            // if not the label, then...
            if (filename.equalsIgnoreCase(BulkUploadTemplate.FILENAME)) {
                continue;
            }

            addFilename(filename);
            List<String> list = getRowFilenameMap().get(row);
            if (list == null) {
                list = new ArrayList<String>();
                getRowFilenameMap().put(row, list);
            }
            if (caseTest.containsKey(filename)) {
                String testFile = caseTest.get(filename);
                if (testFile.equals(filename)) {
                    throw new TdarRecoverableRuntimeException("bulkUploadService.duplicate_filename_s_was_found_in_manifest_file",
                            Arrays.asList(filename));
                }
                if (testFile.equalsIgnoreCase(filename)) {
                    setCaseSensitive(true);
                }
            }
            list.add(filename);
        }

        if (!isCaseSensitive()) {
            setResourcesCreated(new TreeMap<String, Resource>(String.CASE_INSENSITIVE_ORDER));
        }

    }

    
    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public Map<Row, List<String>> getRowFilenameMap() {
        return rowFilenameMap;
    }

    public void setRowFilenameMap(Map<Row, List<String>> rowFilenameMap) {
        this.rowFilenameMap = rowFilenameMap;
    }

    public Map<String, CellMetadata> getCellLookupMap() {
        return cellLookupMap;
    }

    public void setCellLookupMap(Map<String, CellMetadata> cellLookupMap) {
        this.cellLookupMap = cellLookupMap;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public LinkedHashSet<CellMetadata> getAllValidFields() {
        return allValidFields;
    }

    public void setAllValidFields(LinkedHashSet<CellMetadata> allValidFields) {
        this.allValidFields = allValidFields;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public int getLastCellNum() {
        return columnNamesRow.getLastCellNum();
    }

    public int getFirstCellNum() {
        return columnNamesRow.getFirstCellNum();
    }

    public void setColumnNamesRow(Row columnNamesRow) {
        this.columnNamesRow = columnNamesRow;

    }

    public Row getColumnNamesRow() {
        return columnNamesRow;

    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public Map<String, Resource> getResourcesCreated() {
        return resourcesCreated;
    }

    public void setResourcesCreated(Map<String, Resource> resourcesCreated) {
        this.resourcesCreated = resourcesCreated;
    }

    public void setRequired(Set<CellMetadata> required) {
        this.required = required;
    }

    public Set<CellMetadata> getRequired() {
        return required;
    }

    public Collection<FileProxy> getFileProxies() {
        return fileProxies;
    }

    public void setFileProxies(Collection<FileProxy> fileProxies) {
        this.fileProxies = fileProxies;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public AsyncUpdateReceiver getAsyncUpdateReceiver() {
        return asyncUpdateReceiver;
    }

    public void setAsyncUpdateReceiver(AsyncUpdateReceiver asyncUpdateReceiver) {
        this.asyncUpdateReceiver = asyncUpdateReceiver;
    }

    /**
     * For validation, we create file proxies from all of the filenames.
     * @param sheet
     */
    public void createFakeFileProxies(Sheet sheet) {
        setFileProxies(new ArrayList<FileProxy>());
        String tableCellName = CellMetadata.FILENAME.getName();
        for (Row row : sheet) {
            String stringCellValue = null;
            if (row.getCell(ExcelService.FIRST_COLUMN) != null) {
                stringCellValue = row.getCell(ExcelService.FIRST_COLUMN).getStringCellValue();
            }
            if (StringUtils.isBlank(stringCellValue) || tableCellName.equals(stringCellValue) || stringCellValue.startsWith(tableCellName)) {
                continue;
            }
            FileProxy fp = new FileProxy();
            fp.setFilename(stringCellValue);
            fp.setAction(FileAction.ADD);
            logger.debug("creating validation proxy from {}", stringCellValue);
            getFileProxies().add(fp);
        }
    }

    /**
     * Iterates over the column headers for the Excel Workbook and grabs all of
     * the required and known fields
     * 
     * @param proxy
     * @param cellLookupMap
     * @param evaluator
     */
    public void initializeColumnMetadata(Map<String, CellMetadata> cellLookupMap, FormulaEvaluator evaluator) {
        Set<CellMetadata> required = new HashSet<CellMetadata>();
        Row columnNamesRow = getColumnNamesRow();
        for (int i = columnNamesRow.getFirstCellNum(); i <= columnNamesRow.getLastCellNum(); i++) {
            String name = excelService.getCellValue(formatter, evaluator, columnNamesRow, i);
            name = StringUtils.replace(name, ASTERISK, "").trim();
            // remove required char
            getColumnNames().add(name);

            CellMetadata cellMetadata = cellLookupMap.get(name);
            if (cellMetadata != null) {
                if (cellMetadata.isRequired()) {
                    required.add(cellMetadata);
                }
            }
        }
        getRequired().addAll(required);
    }

}
