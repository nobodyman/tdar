package org.tdar.core.bean.resource.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.HasStatic;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.db.datatable.DataTableColumnType;
import org.tdar.db.datatable.ImportColumn;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonIdNameFilter;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Metadata for a column in a data table.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Table(name = "data_table_column", indexes = {
        @Index(name = "data_table_column_data_table_id_idx", columnList = "data_table_id"),
        @Index(name = "data_table_column_default_coding_sheet_id_idx", columnList = "default_coding_sheet_id")
})
@XmlRootElement
@JsonInclude(Include.NON_NULL)
public class DataTableColumn extends AbstractSequenced<DataTableColumn> implements Validatable, HasStatic, ImportColumn {

    private static final long serialVersionUID = 430090539610139733L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final DataTableColumn TDAR_ROW_ID = new DataTableColumn() {

        private static final long serialVersionUID = 3518018865128797773L;

        @Override
        public String getDisplayName() {
            return "Row Id";
        }

        @Override
        public String getName() {
            return TDAR_ID_COLUMN;
        }

        @Override
        public boolean isValid() {
            return true;
        };

        @Override
        public Long getId() {
            return -1L;
        }

        @Override
        public Integer getImportOrder() {
            return -1;
        }

        @Override
        public boolean isStatic() {
            return true;
        }
        
        @Override
        public ColumnVisibility getVisible() {
            return ColumnVisibility.HIDDEN;
        }
    };

    @Override
    @XmlTransient
    @Transient
    public boolean isStatic() {
        return false;
    }

    @ManyToOne(optional = false)
    // , cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "data_table_id", nullable = false)
    private DataTable dataTable;

    @Column(nullable = false)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String name;

    @Column(nullable = false, name = "display_name")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String displayName;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "column_data_type", length = FieldLength.FIELD_LENGTH_255)
    private DataTableColumnType columnDataType = DataTableColumnType.VARCHAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "column_encoding_type", length = FieldLength.FIELD_LENGTH_25)
    private DataTableColumnEncodingType columnEncodingType;

    @ManyToOne
    @JoinColumn(name = "category_variable_id")
    private CategoryVariable categoryVariable;

    @Transient
    private transient Ontology transientOntology;

    @ManyToOne
    @JoinColumn(name = "default_coding_sheet_id")
    private CodingSheet defaultCodingSheet;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", length = FieldLength.FIELD_LENGTH_25)
    private MeasurementUnit measurementUnit;

    @Column(columnDefinition = "boolean default FALSE")
    private boolean mappingColumn = false;

    @ElementCollection()
    @CollectionTable(name = "data_table_column_values", joinColumns = @JoinColumn(name = "column_id"))
    @Column(name = "value")
    private Set<String> values = new HashSet<>();

    @ElementCollection()
    @CollectionTable(name = "data_table_column_int_values", joinColumns = @JoinColumn(name = "column_id"))
    @Column(name = "value")
    private Set<Long> intValues = new HashSet<>();

    @ElementCollection()
    @CollectionTable(name = "data_table_column_float_values", joinColumns = @JoinColumn(name = "column_id"))
    @Column(name = "value")
    private Set<Double> floatValues = new HashSet<>();
    
    @Column
    @Length(max = 4)
    private String delimiterValue;

    @Column(columnDefinition = "boolean default TRUE")
    private boolean ignoreFileExtension = true;

    @Column(name="visibility")
    @Enumerated(EnumType.STRING)
    private ColumnVisibility visible = ColumnVisibility.VISIBLE;

    @Column(name="search_field",columnDefinition = "boolean default TRUE")
    private boolean searchField = true;

    @Transient
    private Map<Long, List<String>> ontologyNodeIdToValuesMap;

    @Column(name = "import_order")
    private Integer importOrder;

    @Transient
    private Integer length = -1;

    // XXX: only used for data transfer from the web layer.
    @Transient
    private transient CategoryVariable tempSubCategoryVariable;

    @XmlElement(name = "dataTableRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @JsonView({ JsonIntegrationDetailsFilter.class, JsonIdNameFilter.class })
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataTableColumnType getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(DataTableColumnType type) {
        this.columnDataType = type;
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public DataTableColumnEncodingType getColumnEncodingType() {
        return columnEncodingType;
    }

    public void setColumnEncodingType(DataTableColumnEncodingType columnEncodingType) {
        this.columnEncodingType = columnEncodingType;
    }

    public CategoryVariable getCategoryVariable() {
        return categoryVariable;
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public Long getCategoryVariableId() {
        if (PersistableUtils.isTransient(categoryVariable))
            return null;
        return categoryVariable.getId();
    }

    public void setCategoryVariable(CategoryVariable categoryVariable) {
        this.categoryVariable = categoryVariable;
    }

    @XmlElement(name = "codingSheetRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public CodingSheet getDefaultCodingSheet() {
        return defaultCodingSheet;
    }

    public void setDefaultCodingSheet(CodingSheet defaultCodingSheet) {
        this.defaultCodingSheet = defaultCodingSheet;
        if (defaultCodingSheet != null) {
            setColumnEncodingType(DataTableColumnEncodingType.CODED_VALUE);
        }
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    @Transient
    public Map<String, OntologyNode> getValueToOntologyNodeMap() {
        CodingSheet codingSheet = getDefaultCodingSheet();
        if (codingSheet == null) {
            return Collections.emptyMap();
        }
        return codingSheet.getTermToOntologyNodeMap();
    }

    @Transient
    public Map<String, List<Long>> getValueToOntologyNodeIdMap() {
        CodingSheet codingSheet = getDefaultCodingSheet();
        if (codingSheet == null) {
            return Collections.emptyMap();
        }
        return codingSheet.getTermToOntologyNodeIdMap();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s", name, columnDataType, getId());
    }

    @JsonView(value = { JsonIntegrationFilter.class, JsonIntegrationDetailsFilter.class })
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getLength() {
        return length;
    }

    public String getDelimiterValue() {
        if (StringUtils.isEmpty(delimiterValue)) {
            return null;
        }
        return delimiterValue;
    }

    public void setDelimiterValue(String delimiterValue) {
        this.delimiterValue = delimiterValue;
    }

    public boolean isIgnoreFileExtension() {
        return ignoreFileExtension;
    }

    public void setIgnoreFileExtension(boolean ignoreFileExtension) {
        this.ignoreFileExtension = ignoreFileExtension;
    }

    public void copyUserMetadataFrom(DataTableColumn column) {
        if (StringUtils.isNotBlank(column.getDisplayName())) { // NOT NULLABLE FIELD
            setDisplayName(column.getDisplayName());
        }
        setDescription(column.getDescription());
        // XXX: this should be set by the dataset conversion process
        // if (column.getColumnDataType() != null) { // NOT NULLABLE FIELD
        // setColumnDataType(column.getColumnDataType());
        // }

        if (getColumnDataType().isNumeric()) {
            setMeasurementUnit(column.getMeasurementUnit());
        }
        if (column.getColumnEncodingType() != null) { // NOT NULLABLE FIELD
            setColumnEncodingType(column.getColumnEncodingType());
        }
    }

    public void copyMappingMetadataFrom(DataTableColumn column) {
        setMappingColumn(column.isMappingColumn());
        setDelimiterValue(column.getDelimiterValue());
        setIgnoreFileExtension(column.isIgnoreFileExtension());
    }

    public CategoryVariable getTempSubCategoryVariable() {
        return tempSubCategoryVariable;
    }

    public void setTempSubCategoryVariable(CategoryVariable tempSubCategoryVariable) {
        this.tempSubCategoryVariable = tempSubCategoryVariable;
    }

    @Override
    public boolean isValidForController() {
        // not implemented
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public boolean isValid() {
        List<Object> keys = new ArrayList<>();
        keys.add(getName());
        if (columnEncodingType == null) {
            throw new TdarValidationException("dataTableColumn.invalid_encoding_type", keys);
        }
        switch (columnEncodingType) {
            case CODED_VALUE:
                if (getDefaultCodingSheet() == null) {
                    throw new TdarValidationException("dataTableColumn.invalid_coded_value", keys);
                }
                break;
            case MEASUREMENT:
                if (measurementUnit == null) {
                    throw new TdarValidationException("dataTableColumn.invalid_measurement", keys);
                }
                // FIXME: Not 100% sure this is correct with the NUMERIC check
                if ((columnDataType == null) || !columnDataType.isNumeric()) {
                    throw new TdarValidationException("dataTableColumn.invalid_measurement_numeric", keys);
                }
                break;
            case COUNT:
                // FIXME: Not 100% sure this is correct with the NUMERIC check
                if ((columnDataType == null) || !columnDataType.isNumeric()) {
                    keys.add("count was not numeric");
                    throw new TdarValidationException("dataTableColumn.invalid_count_numeric", keys);
                }
            case UNCODED_VALUE:
        }
        return true;
    }

    public boolean isMappingColumn() {
        return mappingColumn;
    }

    public void setMappingColumn(boolean mappingColumn) {
        this.mappingColumn = mappingColumn;
    }

    public boolean hasDifferentMappingMetadata(DataTableColumn column) {
        logger.debug("delim: '{}' - '{}'", getDelimiterValue(), column.getDelimiterValue());
        if (!StringUtils.equals(getDelimiterValue(), column.getDelimiterValue())) {
            return true;
        }
        logger.debug("extension: {} - {}", isIgnoreFileExtension(), column.isIgnoreFileExtension());
        if (!Objects.equals(isIgnoreFileExtension(), column.isIgnoreFileExtension())) {
            return true;
        }
        logger.debug("mapping: {} - {}", isMappingColumn(), column.isMappingColumn());
        if (!Objects.equals(isMappingColumn(), column.isMappingColumn())) {
            return true;
        }
        return false;
    }

    @Transient
    @XmlTransient
    public String getJsSimpleName() {
        return getName().replaceAll("[\\s\\,\"\']", "_");
    }

    public Set<String> getMappedDataValues(OntologyNode node) {
        Set<String> values = new HashSet<>();
        for (CodingRule rule : getDefaultCodingSheet().getCodingRules()) {
            if (Objects.equals(node, rule.getOntologyNode())) {
                values.add(rule.getTerm());
            }
        }
        return values;
    }

    @XmlTransient
    @Transient
    public Set<String> getUnmappedDataValues() {
        Set<String> values = new HashSet<>();
        if (getDefaultCodingSheet() == null || CollectionUtils.isEmpty(getDefaultCodingSheet().getCodingRules())) {
            return values;
        }
        for (CodingRule rule : getDefaultCodingSheet().getCodingRules()) {
            if (rule.getOntologyNode() == null) {
                values.add(rule.getTerm());
            }
        }
        return values;
    }

    /**
     * Return true if this column has a mapped ontology and has a mapped coding sheet that is not invalid.
     * 
     * @return
     */
    @Deprecated
    public boolean isActuallyMapped() {
        if (PersistableUtils.isNullOrTransient(getMappedOntology()) && PersistableUtils.isNullOrTransient(getDefaultCodingSheet())) {
            return false;
        }

        for (CodingRule rule : getDefaultCodingSheet().getCodingRules()) {
            if (rule != null && rule.getOntologyNode() != null) {
                return true;
            }
        }
        return false;
    }

    @XmlElement(name = "mappedOntologyRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @Deprecated()
    public Ontology getMappedOntology() {
        if (getDefaultCodingSheet() != null && getDefaultCodingSheet().getDefaultOntology() != null) {
            return getDefaultCodingSheet().getDefaultOntology();
        }
        return null;
    }

    @Deprecated
    public void setMappedOntology(Ontology ont) {
        logger.warn("setting mappedOntology does nothing...");
    }

    @JsonView(JsonIntegrationDetailsFilter.class)
    public Long getMappedOntologyId() {
        if (getMappedOntology() == null) {
            return null;
        }
        return getMappedOntology().getId();
    }

    @XmlTransient
    public Ontology getTransientOntology() {
        return transientOntology;
    }

    public void setTransientOntology(Ontology transientOntology) {
        this.transientOntology = transientOntology;
    }

    @Transient
    @XmlTransient
    public boolean isFilenameColumn() {
        return this.getColumnEncodingType().isFilename();
    }

    @Transient
    @XmlTransient
    @JsonIgnore
    public String getPrettyDisplayName() {
        String displayName = getDisplayName().replaceAll(" (?i)Ontology", "");
        displayName = StringUtils.replace(displayName, "  ", " ");
        return displayName;
    }

    public Integer getImportOrder() {
        return importOrder;
    }

    public void setImportOrder(Integer importOrder) {
        this.importOrder = importOrder;
    }

    @Override
    public int compareToBySequenceNumber(ImportColumn b) {
        if ((sequenceNumber == null) || (b.getSequenceNumber() == null)) {
            return 0;
        }
        return sequenceNumber.compareTo(b.getSequenceNumber());
    }

    public boolean isSearchField() {
        return searchField;
    }

    public void setSearchField(boolean searchField) {
        this.searchField = searchField;
    }

    @Override
    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        this.values = values;
    }

    @Override
    public Set<Long> getIntValues() {
        return intValues;
    }

    public void setIntValues(Set<Long> intValues) {
        this.intValues = intValues;
    }

    @Override
    public Set<Double> getFloatValues() {
        return floatValues;
    }

    public void setFloatValues(Set<Double> floatValues) {
        this.floatValues = floatValues;
    }

    public ColumnVisibility getVisible() {
        return visible;
    }

    public void setVisible(ColumnVisibility visible) {
        this.visible = visible;
    }
}
