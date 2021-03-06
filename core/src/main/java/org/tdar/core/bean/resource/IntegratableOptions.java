package org.tdar.core.bean.resource;

import org.tdar.locale.HasLabel;
import org.tdar.locale.Localizable;
import org.tdar.utils.MessageHelper;

public enum IntegratableOptions implements HasLabel, Localizable {
    INTEGRATABLE("Ready for Data Integration"),
    NOT_INTEGRATABLE("Needs Ontology Mappings");

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
