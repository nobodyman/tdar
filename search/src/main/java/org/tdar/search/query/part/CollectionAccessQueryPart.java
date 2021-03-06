package org.tdar.search.query.part;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * Query if the "user" has access to collection
 * 
 * @author abrin
 *
 */
public class CollectionAccessQueryPart implements QueryPart<TdarUser> {
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TdarUser user;
    private Permissions permissions;
    private boolean admin;

    public CollectionAccessQueryPart(TdarUser person, boolean admin, Permissions generalPermissions) {
        this.user = person;
        this.permissions = generalPermissions;
        this.admin = admin;
    }

    @Override
    public boolean isEmpty() {
        return (PersistableUtils.isNullOrTransient(user) || (permissions == null));
    }

    protected QueryPart<?> getQueryPart(TdarUser value, Permissions permissions) {
        if (isEmpty()) {
            return null;
        }
        // setup the rights; by default allow people to see things they have the rights to "view" or are public
        QueryPartGroup group = new QueryPartGroup(Operator.AND);
        group.append(new FieldQueryPart<CollectionResourceSection>(QueryFieldNames.COLLECTION_TYPE, CollectionResourceSection.MANAGED));

        // if the Permissions property is set, we're in the context of the Resource or Collection Controllers and are likely looking
        // for collections the person administers and thus can modify contents (ADMINISTER_GROUP); but MODIFY may be useful in the future
        if (!admin) {
            FieldQueryPart<Long> userAccessQueryPart = new FieldQueryPart<>(QueryFieldNames.COLLECTION_USERS_WHO_CAN_VIEW, user.getId());
            switch (permissions) {
                case MODIFY_RECORD:
                case MODIFY_METADATA:
                    userAccessQueryPart.setFieldName(QueryFieldNames.COLLECTION_USERS_WHO_CAN_MODIFY);
                    group.append(userAccessQueryPart);
                    break;
                case ADD_TO_COLLECTION:
                    userAccessQueryPart.setFieldName(QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADD);
                    group.append(userAccessQueryPart);
                    break;
                case REMOVE_FROM_COLLECTION:
                    userAccessQueryPart.setFieldName(QueryFieldNames.COLLECTION_USERS_WHO_CAN_REMOVE);
                    group.append(userAccessQueryPart);
                    break;
                case ADMINISTER_COLLECTION:
                    userAccessQueryPart.setFieldName(QueryFieldNames.COLLECTION_USERS_WHO_CAN_ADMINISTER);
                    group.append(userAccessQueryPart);
                    break;
                default:
                    QueryPartGroup rightsGroup = new QueryPartGroup(Operator.OR);
                    rightsGroup.append(new FieldQueryPart<Boolean>(QueryFieldNames.HIDDEN, Boolean.FALSE));
                    rightsGroup.append(userAccessQueryPart);
                    group.append(rightsGroup);
                    break;
            }
        }

        return group;

    }

    @Override
    public String generateQueryString() {
        return this.getQueryPart(user, permissions).generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        return "User: " + user.getProperName() + " " + permissions.getLabel();
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    @Override
    public boolean isDescriptionVisible() {
        return false;
    }

    @Override
    public void setDescriptionVisible(boolean visible) {
        // TODO Auto-generated method stub
    }

    @Override
    public Operator getOperator() {
        return Operator.AND;
    }
}
