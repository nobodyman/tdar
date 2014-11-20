package org.tdar.struts.action.ontology;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.struts.action.resource.AbstractResourceViewAction;


@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/ontology")
public class OntologyViewAction extends AbstractResourceViewAction<Ontology> {

    private static final long serialVersionUID = -826507251116794622L;

}
