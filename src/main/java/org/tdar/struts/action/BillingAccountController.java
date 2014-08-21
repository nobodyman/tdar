package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.billing.AccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@HttpsOnly
public class BillingAccountController extends AbstractPersistableController<Account> {

    public static final String UPDATE_QUOTAS = "updateQuotas";
    public static final String FIX_FOR_DELETE_ISSUE = "fix";
    public static final String CHOOSE = "choose";
    public static final String VIEW_ID = "view?id=${id}";
    private static final long serialVersionUID = 2912533895769561917L;
    public static final String NEW_ACCOUNT = "new_account";
    private static final String LIST_INVOICES = "listInvoices";
    private Long invoiceId;
    private List<Account> accounts = new ArrayList<>();
    private List<Invoice> invoices = new ArrayList<>();
    private List<Resource> resources = new ArrayList<>();

    private AccountGroup accountGroup;
    private List<TdarUser> authorizedMembers = new ArrayList<>();
    private Long accountGroupId;
    private String name;
    private Integer quantity = 1;
    private String description;

    private Long numberOfFiles = 0L;
    private Long numberOfMb = 0L;
    private Date expires = new DateTime().plusYears(1).toDate();

    @Autowired
    private transient AccountService accountService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @SkipValidation
    @Action(value = CHOOSE, results = {
            @Result(name = SUCCESS, location = "select-account.ftl"),
            @Result(name = NEW_ACCOUNT, location = "add?invoiceId=${invoiceId}", type = REDIRECT)
    })
    public String selectAccount() throws TdarActionException {
        Invoice invoice = getInvoice();
        if (invoice == null) {
            throw new TdarRecoverableRuntimeException(getText("billingAccountController.invoice_is_requried"));
        }
        if (!authorizationService.canAssignInvoice(invoice, getAuthenticatedUser())) {
            throw new TdarRecoverableRuntimeException(getText("billingAccountController.rights_to_assign_this_invoice"));
        }
        setAccounts(accountService.listAvailableAccountsForUser(invoice.getOwner(), Status.ACTIVE, Status.FLAGGED_ACCOUNT_BALANCE));
        if (CollectionUtils.isNotEmpty(getAccounts())) {
            return SUCCESS;
        }
        return NEW_ACCOUNT;
    }

    @Action(value = "create-code",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = VIEW_ID, type = "redirect"),
                    @Result(name = INPUT, location = VIEW_ID, type = "redirect")
            })
    @PostOnly
    @SkipValidation
    public String createCouponCode() throws TdarActionException {
        try {
            for (int i = 0; i < quantity; i++) {
                accountService.generateCouponCode(getAccount(), getNumberOfFiles(), getNumberOfMb(), getExipres());
            }
            accountService.updateQuota(getAccount());
        } catch (Throwable e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        return SUCCESS;
    }

    @Override
    public void loadListData() {
        if (authorizationService.isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER)) {
            getAccounts().addAll(accountService.findAll());
        }
    }

    @SkipValidation
    @RequiresTdarUserGroup(TdarGroup.TDAR_BILLING_MANAGER)
    @Action(value = LIST_INVOICES, results = { @Result(name = SUCCESS, location = "list-invoices.ftl") })
    public String listInvoices() {
        if (authorizationService.isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER)) {
            getInvoices().addAll(getGenericService().findAll(Invoice.class));
            Collections.sort(getInvoices(), new Comparator<Invoice>() {
                @Override
                public int compare(Invoice o1, Invoice o2) {
                    return ObjectUtils.compare(o2.getDateCreated(), o1.getDateCreated());
                }
            });
        }
        return SUCCESS;
    }

    public Invoice getInvoice() {
        return getGenericService().find(Invoice.class, invoiceId);
    }

    @Override
    protected String save(Account persistable) {
        getLogger().info("invoiceId {}", getInvoiceId());

        // if we're coming from "choose" and we want a "new account"
        if (Persistable.Base.isTransient(getAccount()) && StringUtils.isNotBlank(getName())) {
            getAccount().setName(getName());
            getAccount().setDescription(getDescription());
        } else {
            getAuthorizedMembers().addAll(getAccount().getAuthorizedMembers());
        }

        if (Persistable.Base.isNotNullOrTransient(invoiceId)) {
            Invoice invoice = getInvoice();
            getLogger().info("attaching invoice: {} ", invoice);
            // if we have rights
            if (Persistable.Base.isTransient(getAccount())) {
                getAccount().setOwner(invoice.getOwner());
            }
            accountService.checkThatInvoiceBeAssigned(invoice, getAccount()); // throw exception if you cannot
            // make sure you add back all of the valid account holders
            getAccount().getInvoices().add(invoice);
            getGenericService().saveOrUpdate(invoice);
            getGenericService().saveOrUpdate(getAccount());
            updateQuotas();
        }
        getAccount().getAuthorizedMembers().clear();
        getAccount().getAuthorizedMembers().addAll(getGenericService().loadFromSparseEntities(getAuthorizedMembers(), TdarUser.class));

        getLogger().info("authorized members: {}", getAccount().getAuthorizedMembers());
        return SUCCESS;
    }

    @Override
    protected void delete(Account persistable) {
        // TODO Auto-generated method stub
    }

    @SkipValidation
    @WriteableSession
    @Action(value = UPDATE_QUOTAS, results = {
            @Result(name = SUCCESS, location = "view?id=${id}", type = REDIRECT)
    })
    public String updateQuotas() {
        accountService.updateQuota(getAccount(), getAccount().getResources());
        return SUCCESS;
    }

    /**
     * Temporary controller to fix issue where deleted items were counted wrong/differently before
     * we're changing the values here automatically
     * 
     * @return stuff
     */
    @SkipValidation
    @Action(value = FIX_FOR_DELETE_ISSUE, results = {
            @Result(name = SUCCESS, location = "view?id=${id}", type = REDIRECT)
    })
    public String fix() {
        accountService.resetAccountTotalsToHaveOneFileLeft(getAccount());
        return SUCCESS;
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        getLogger().info("isViewable {} {}", getAuthenticatedUser(), getAccount().getId());
        if (Persistable.Base.isNullOrTransient(getAuthenticatedUser())) {
            return false;
        }

        if (authorizationService.can(InternalTdarRights.VIEW_BILLING_INFO, getAuthenticatedUser())) {
            return true;
        }

        if (getAuthenticatedUser().equals(getAccount().getOwner()) || getAccount().getAuthorizedMembers().contains(getAuthenticatedUser())) {
            return true;
        }

        return false;
    }

    @Override
    public Class<Account> getPersistableClass() {
        return Account.class;
    }

    @Override
    public String loadViewMetadata() {
        setAccountGroup(accountService.getAccountGroup(getAccount()));
        getAuthorizedMembers().addAll(getAccount().getAuthorizedMembers());
        getResources().addAll(getAccount().getResources());
        Persistable.Base.sortByUpdatedDate(getResources());
        return SUCCESS;
    }

    public Account getAccount() {
        if (getPersistable() == null) {
            setPersistable(createPersistable());
        }

        return getPersistable();
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return true;
    }

    public void setAccount(Account account) {
        setPersistable(account);
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public AccountGroup getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(AccountGroup accountGroup) {
        this.accountGroup = accountGroup;
    }

    public Long getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(Long accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    public Person getBlankPerson() {
        return new Person();
    }

    @DoNotObfuscate(reason = "needs access to Email Address on view page")
    public List<TdarUser> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(List<TdarUser> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    public boolean isBillingAdmin() {
        return authorizationService.isMember(getAuthenticatedUser(), TdarGroup.TDAR_BILLING_MANAGER);
    }

    public BillingActivityModel getBillingActivityModel() {
        return accountService.getLatestActivityModel();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Long getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Long numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public Long getNumberOfMb() {
        return numberOfMb;
    }

    public void setNumberOfMb(Long numberOfMb) {
        this.numberOfMb = numberOfMb;
    }

    public Date getExipres() {
        return expires;
    }

    public void setExipres(Date exipres) {
        this.expires = exipres;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

}