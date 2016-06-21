package org.tdar.core.bean.resource;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

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

}