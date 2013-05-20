package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.service.AccountService;
import org.tdar.struts.action.resource.AbstractResourceControllerITCase;

public class BillingAccountControllerITCase extends AbstractResourceControllerITCase {

    @Override
    protected TdarActionSupport getController() {
        return null;
    }

    @Autowired
    AccountService accountService;
    
    @Test
    @Rollback
    public void testAccountControllerChoicesNoAccount() throws TdarActionException {
        // test fence for Invoice
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.prepare();
        String msg = null;
        try {
            assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(BillingAccountController.INVOICE_IS_REQURIED, msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesOkNewAccount() throws TdarActionException {
        Person user = createAndSaveNewPerson();
        Invoice invoice = new Invoice(user, PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.saveOrUpdate(invoice);

        BillingAccountController controller = generateNewController(BillingAccountController.class);
        init(controller, user);
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());

    }

    @Test
    @Rollback
    public void testAccountControllerChoicesNoRightsToAssign() throws TdarActionException {
        BillingAccountController controller = generateNewController(BillingAccountController.class);
        Invoice invoice = createTrivialInvoice();
        String msg = null;
        init(controller, createAndSaveNewPerson());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        try {
            assertEquals(BillingAccountController.NEW_ACCOUNT, controller.selectAccount());
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertEquals(BillingAccountController.RIGHTS_TO_ASSIGN_THIS_INVOICE, msg);
    }

    @Test
    @Rollback
    public void testAccountControllerChoicesSelectAccounts() throws TdarActionException {
        Invoice invoice = createTrivialInvoice();
        invoice.setOwner(getAdminUser());
        Account account = createAccount(getAdminUser());
        BillingAccountController controller = generateNewController(BillingAccountController.class);
        init(controller, getAdminUser());
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        assertEquals(BillingAccountController.SUCCESS, controller.selectAccount());
        assertTrue(controller.getAccounts().contains(account));

    }

    private Invoice createTrivialInvoice() {
        Invoice invoice = new Invoice(getUser(), PaymentMethod.INVOICE, 10L, 0L, null);
        genericService.save(invoice);
        return invoice;
    }

    @Test
    @Rollback
    public void testAddingInvoiceToExistingAccount() throws TdarActionException {
        Long accountId = createAccount(getUser()).getId();
        Invoice invoice = createTrivialInvoice();
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setInvoiceId(invoice.getId());
        controller.setId(accountId);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        assertEquals(BillingAccountController.SUCCESS, save);
        assertTrue(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testReEvaluationAppropriateWithUncountedThings() throws TdarActionException, InstantiationException, IllegalAccessException {
        Person person = createAndSaveNewPerson();
        Account invoice = setupAccountWithInvoiceFiveResourcesAndSpace( accountService.getLatestActivityModel(), person);
        Project project = createAndSaveNewProject("PROJECT TEST TITLE");
        Document doc = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc, new File(TestConstants.TEST_DOCUMENT));
        doc.setStatus(Status.DELETED);
        Document doc2 = createAndSaveNewInformationResource(Document.class, person);
        addFileToResource(doc2, new File(TestConstants.TEST_DOCUMENT));
        invoice.getResources().add(doc);
        invoice.getResources().add(doc2);
        invoice.getResources().add(project);
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setId(invoice.getId());
        controller.prepare();
        controller.updateQuotas();
        assertEquals(1, invoice.getFilesUsed().intValue());
        assertEquals(4, invoice.getAvailableNumberOfFiles().intValue());
        assertEquals(5843584, invoice.getSpaceUsedInBytes().longValue());
//        controller.setServletRequest(getServletPostRequest());
//        String save = controller.save();
//        assertEquals(BillingAccountController.SUCCESS, save);
//        assertTrue(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testAddingInvoiceToNewAccount() throws TdarActionException {
        Long accountId = createAccount(getUser()).getId();
        Invoice invoice = createTrivialInvoice();
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.setInvoiceId(invoice.getId());
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        Long id = controller.getAccount().getId();
        assertEquals(BillingAccountController.SUCCESS, save);
        assertFalse(genericService.find(Account.class, accountId).getInvoices().contains(invoice));
        assertTrue(genericService.find(Account.class, id).getInvoices().contains(invoice));
    }

    @Test
    @Rollback
    public void testAddingUsersToAccount() throws TdarActionException {
        BillingAccountController controller = generateNewInitializedController(BillingAccountController.class);
        controller.prepare();
        controller.getAuthorizedMembers().add(getAdminUser());
        controller.setServletRequest(getServletPostRequest());
        String save = controller.save();
        Long id = controller.getAccount().getId();
        assertEquals(BillingAccountController.SUCCESS, save);

        Account account = genericService.find(Account.class, id);
        assertEquals(1, account.getAuthorizedMembers().size());
        assertTrue(account.getAuthorizedMembers().contains(getAdminUser()));
    }

}
