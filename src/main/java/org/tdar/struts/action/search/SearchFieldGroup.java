package org.tdar.struts.action.search;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.utils.MessageHelper;

public enum SearchFieldGroup implements HasLabel, Localizable {

    BASIC_FIELDS("Basic Fields"),
    CONTROLLED_KEYWORDS("Controlled Keywords"),
    FREEFORM_KEYWORDS("Freeform Keywords"),
    EXPLORE("Explore the site");

    private String label;

    private SearchFieldGroup(String label) {
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
