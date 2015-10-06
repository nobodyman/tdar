package org.tdar.core.bean.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonIntegrationFilter;
import org.tdar.utils.json.JsonIntegrationSearchResultFilter;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * A Dataset information resource can currently be an Excel file, Access MDB file, or plaintext CSV file.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Indexed
@Table(name = "dataset")
@XmlRootElement(name = "dataset")
public class Dataset extends InformationResource {

    private static final long serialVersionUID = -5796154884019127904L;

    public enum IntegratableOptions implements HasLabel, Localizable {
        YES("Ready for Data Integration"), NO("Needs Ontology Mappings");

        private String label;

        private IntegratableOptions(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getLocaleKey() {
            return MessageHelper.formatLocalizableKey(this);
        }

        public Boolean getBooleanValue() {
            switch (this) {
                case NO:
                    return Boolean.FALSE;

                default:
                    return Boolean.TRUE;
            }
        }

    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset", orphanRemoval = true)
    @IndexedEmbedded
    private Set<DataTable> dataTables = new LinkedHashSet<DataTable>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "dataset_id")
    private Set<DataTableRelationship> relationships = new HashSet<DataTableRelationship>();

    public Dataset() {
        setResourceType(ResourceType.DATASET);
    }

    private transient Map<String, DataTable> nameToTableMap;
    private transient Map<String, DataTable> genericNameToTableMap;
    private transient int dataTableHashCode = -1;

    @XmlElementWrapper(name = "dataTables")
    @XmlElement(name = "dataTable")
    public Set<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(Set<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

    @Field(norms = Norms.NO, store = Store.YES, name = QueryFieldNames.INTEGRATABLE, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    // @Transient
    @JsonView({ JsonIntegrationFilter.class, JsonIntegrationSearchResultFilter.class })
    public IntegratableOptions getIntegratableOptions() {
        for (DataTable dt : getDataTables()) {
            for (DataTableColumn dtc : dt.getDataTableColumns()) {
                if (dtc.getMappedOntology() != null) {
                    return IntegratableOptions.YES;
                }
            }
        }
        return IntegratableOptions.NO;
    }

    /**
     * @param string
     * @return
     */
    @Transient
    public DataTable getDataTableByName(String name) {
        if ((nameToTableMap == null) || !Objects.equals(dataTableHashCode, getDataTables().hashCode())) {
            initializeNameToTableMap();
        }
        // NOTE: IF the HashCode is not implemented properly, on DataTableColumn, this may get out of sync
        return nameToTableMap.get(name);
    }

    /**
     * @param string
     * @return
     */
    @Transient
    public DataTable getDataTableById(Long id) {
        for (DataTable datatable : getDataTables()) {
            if (Objects.equals(datatable.getId(), id)) {
                return datatable;
            }
        }
        return null;
    }

    private void initializeNameToTableMap() {
        nameToTableMap = new HashMap<String, DataTable>();
        genericNameToTableMap = new HashMap<String, DataTable>();

        for (DataTable dt : getDataTables()) {
            nameToTableMap.put(dt.getName(), dt);
            String simpleName = dt.getName().replaceAll("^((\\w+_)(\\d+)(_?))", "");
            genericNameToTableMap.put(simpleName, dt);
        }

    }

    @Transient
    public DataTable getDataTableByGenericName(String name) {
        if ((genericNameToTableMap == null) || !Objects.equals(dataTableHashCode, getDataTables().hashCode())) {
            initializeNameToTableMap();
        }
        // NOTE: IF the HashCode is not implemented properly, on DataTableColumn, this may get out of sync
        return genericNameToTableMap.get(name);
    }

    public void setRelationships(Set<DataTableRelationship> relationships) {
        this.relationships = relationships;
    }

    public Set<DataTableRelationship> getRelationships() {
        return relationships;
    }

    @Transient
    public boolean hasMappingColumns() {
        if (CollectionUtils.isEmpty(getDataTables())) {
            return false;
        }
        for (DataTable dt : getDataTables()) {
            if (CollectionUtils.isEmpty(dt.getDataTableColumns())) {
                return false;
            }
            for (DataTableColumn col : dt.getDataTableColumns()) {
                if (col.isMappingColumn()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transient
    public boolean hasCodingColumns() {
        if (CollectionUtils.isEmpty(getDataTables())) {
            return false;
        }
        for (DataTable dt : getDataTables()) {
            if (CollectionUtils.isEmpty(dt.getDataTableColumns())) {
                return false;
            }
            for (DataTableColumn col : dt.getDataTableColumns()) {
                if (PersistableUtils.isNotNullOrTransient(col.getDefaultCodingSheet())) {
                    return true;
                }
            }
        }
        return false;
    }
}