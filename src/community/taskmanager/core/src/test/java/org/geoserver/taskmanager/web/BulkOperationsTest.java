/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.taskmanager.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.taskmanager.AbstractWicketTaskManagerTest;
import org.geoserver.taskmanager.data.Batch;
import org.geoserver.taskmanager.data.TaskManagerDao;
import org.geoserver.taskmanager.data.TaskManagerFactory;
import org.geoserver.taskmanager.util.TaskManagerBeans;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BulkOperationsTest extends AbstractWicketTaskManagerTest {

    protected TaskManagerFactory fac;
    protected TaskManagerDao dao;

    @Before
    public void before() {
        fac = TaskManagerBeans.get().getFac();
        dao = TaskManagerBeans.get().getDao();

        login();
    }

    @After
    public void after() {
        logout();
    }

    @Test
    public void testBulkRunBatches() {

        Batch batch1 = fac.createBatch();
        batch1.setName("Z-BATCH");
        batch1 = dao.save(batch1);
        Batch batch2 = fac.createBatch();
        batch2.setName("Q-BATCH");
        batch2 = dao.save(batch2);

        tester.startPage(BulkOperationsPage.class);

        tester.assertComponent("form:bulkRunPanel:workspace", TextField.class);

        tester.assertComponent("form:bulkRunPanel:configuration", TextField.class);

        tester.assertComponent("form:bulkRunPanel:name", TextField.class);

        tester.assertComponent("form:bulkRunPanel:startDelay", NumberTextField.class);

        tester.assertComponent("form:bulkRunPanel:betweenDelay", NumberTextField.class);

        tester.assertComponent("form:bulkRunPanel:batchesFound", Label.class);

        tester.assertModelValue(
                "form:bulkRunPanel:batchesFound",
                "Found 0 batches that match the specified criteria");

        FormTester formTester = tester.newFormTester("form");

        formTester.setValue("bulkRunPanel:configuration", null);

        tester.executeAjaxEvent("form:bulkRunPanel:configuration", "change");

        tester.assertModelValue(
                "form:bulkRunPanel:batchesFound",
                "Found 2 batches that match the specified criteria");

        formTester.setValue("bulkRunPanel:name", "Q%");

        tester.executeAjaxEvent("form:bulkRunPanel:name", "change");

        tester.assertModelValue(
                "form:bulkRunPanel:batchesFound",
                "Found 1 batches that match the specified criteria");

        formTester.submit("bulkRunPanel:run");

        tester.assertModelValue(
                "form:bulkRunPanel:dialog:dialog:content:form:userPanel",
                "Are you sure you want to run 1 batches?");

        dao.delete(batch1);

        dao.delete(batch2);
    }
}
