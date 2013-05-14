package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;

/**
 * $Id$
 * 
 * Provides DAO access for Person entities, including a variety of methods for
 * looking up a Person in tDAR.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class AccountDao extends Dao.HibernateBase<Account> {

    public AccountDao() {
        super(Account.class);
    }

    @SuppressWarnings("unchecked")
    public Set<Account> findAccountsForUser(Person user, Status ... statuses) {
        if (ArrayUtils.isEmpty(statuses)) {
            statuses = new Status[1];
            statuses[0] = Status.ACTIVE;
        }
        Set<Account> accountGroups = new HashSet<Account>();
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNTS_FOR_PERSON);
        query.setParameter("personId", user.getId());
        query.setParameterList("statuses", statuses);
        accountGroups.addAll(query.list());
        for (AccountGroup group : findAccountGroupsForUser(user)) {
            accountGroups.addAll(group.getAccounts());
        }
        return accountGroups;
    }

    @SuppressWarnings("unchecked")
    public List<AccountGroup> findAccountGroupsForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_GROUPS_FOR_PERSON);
        query.setParameter("personId", user.getId());
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE));
        return query.list();

    }

    public AccountGroup getAccountGroup(Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_GROUP_FOR_ACCOUNT);
        query.setParameter("accountId", account.getId());
        return (AccountGroup) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findResourcesWithDifferentAccount(List<Resource> resourcesToEvaluate, Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.RESOURCES_WITH_NON_MATCHING_ACCOUNT_ID);
        query.setParameter("accountId", account.getId());
        query.setParameterList("ids", Persistable.Base.extractIds(resourcesToEvaluate));
        return (List<Long>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findResourcesWithNullAccount(List<Resource> resourcesToEvaluate) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.RESOURCES_WITH_NULL_ACCOUNT_ID);
        query.setParameterList("ids", Persistable.Base.extractIds(resourcesToEvaluate));
        return (List<Long>) query.list();
    }

    public void updateTransientAccountOnResources(Collection<Resource> resourcesToEvaluate) {
        Map<Long, Resource> resourceIdMap = Persistable.Base.createIdMap(resourcesToEvaluate);
        String sql = String.format(TdarNamedQueries.QUERY_ACCOUNTS_FOR_RESOURCES, StringUtils.join(resourceIdMap.keySet().toArray()));
        if (CollectionUtils.isEmpty(resourceIdMap.keySet()) || resourceIdMap.keySet().size() == 1 && resourceIdMap.keySet().contains(-1L)) {
            return;
        }
        Query query = getCurrentSession().createSQLQuery(sql);

        Map<Long, Account> accountIdMap = new HashMap<Long, Account>();
        for (Object objs : query.list()) {
            Object[] obj = (Object[]) objs;
            Long resourceId = ((BigInteger) obj[0]).longValue();
            Long accountId = null;
            if (obj[1] != null) {
                accountId = ((BigInteger) obj[1]).longValue();
            }
            Account account = accountIdMap.get(accountId);
            if (account == null) {
                account = find(accountId);
                accountIdMap.put(accountId, account);
            }
            Resource resource = resourceIdMap.get(resourceId);
            if (resource != null) {
                logger.trace("setting account {} for resource {}", accountId, resourceId);
                resource.setAccount(account);
            } else {
                logger.error("resource is null somehow for id: {}, account {}", resourceId, account);
            }
        }
    }

    public void updateAccountInfo(Account account) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ACCOUNT_QUOTA_INIT);
        query.setParameter("accountId", account.getId());
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT, Status.DUPLICATE, Status.FLAGGED_ACCOUNT_BALANCE));
        Long totalFiles = 0L;
        Long totalSpaceInBytes = 0L;
        for (Object objs : query.list()) {
            Object[] obj = (Object[]) objs;
            if (obj[0] != null) {
                totalFiles = ((Long) obj[0]).longValue();
            }
            if (obj[1] != null) {
                totalSpaceInBytes = ((Long) obj[1]).longValue();
            }
        }
        account.setFilesUsed(totalFiles);
        account.setSpaceUsedInBytes(totalSpaceInBytes);
    }

    public List findUnassignedInvoicesForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.UNASSIGNED_INVOICES_FOR_PERSON);
        query.setParameter("personId", user.getId());
        query.setParameterList("statuses", Arrays.asList(TransactionStatus.TRANSACTION_SUCCESSFUL));
        return query.list();
    }

    public List findInvoicesForUser(Person user) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.INVOICES_FOR_PERSON);
        query.setParameter("personId", user.getId());
        return query.list();
    }
}
