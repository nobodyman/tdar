package org.tdar.struts.action.resource.share;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by jimdevos on 3/6/17.
 */
public class ResourceAdhocShareEditAction extends AbstractResourceAdhocShareAction {


    @Action(value="edit", results = {
            @Result(name="success", location="edit.ftl"),
            @Result(name="input", type="redirect", location="/dashboard")
    })
    public String execute() {
        return "success";
    }

}
