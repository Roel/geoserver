package org.geoserver.taskmanager.tasks;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.taskmanager.AbstractTaskManagerTest;
import org.geoserver.taskmanager.data.Batch;
import org.geoserver.taskmanager.data.Configuration;
import org.geoserver.taskmanager.data.Task;
import org.geoserver.taskmanager.data.TaskManagerDao;
import org.geoserver.taskmanager.data.TaskManagerFactory;
import org.geoserver.taskmanager.schedule.BatchJobService;
import org.geoserver.taskmanager.util.TaskManagerDataUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class TimestampTaskTest extends AbstractTaskManagerTest {

    private static final String ATT_LAYER = "layer";

    @Autowired private TaskManagerDao dao;

    @Autowired private TaskManagerFactory fac;

    @Autowired private TaskManagerDataUtil dataUtil;

    @Autowired private BatchJobService bjService;

    @Autowired private Scheduler scheduler;

    private Configuration config;

    private Batch batch;

    @Override
    public boolean setupDataDirectory() throws Exception {
        DATA_DIRECTORY.addWcs11Coverages();
        return true;
    }

    @Before
    public void setupBatch() throws Exception {
        config = fac.createConfiguration();
        config.setName("my_config");
        config.setWorkspace("some_ws");

        Task task = fac.createTask();
        task.setName("task");
        task.setType(TimestampTaskTypeImpl.NAME);
        dataUtil.setTaskParameterToAttribute(task, MetadataSyncTaskTypeImpl.PARAM_LAYER, ATT_LAYER);
        dataUtil.addTaskToConfiguration(config, task);

        config = dao.save(config);
        task = config.getTasks().get("task");

        batch = fac.createBatch();
        batch.setName("batch");
        dataUtil.addBatchElement(batch, task);

        batch = bjService.saveAndSchedule(batch);

        config = dao.init(config);
    }

    @After
    public void clearDataFromDatabase() {
        if (batch != null) {
            dao.delete(batch);
        }
        if (config != null) {
            dao.delete(config);
        }
    }

    @Test
    public void test() throws SchedulerException, SQLException, IOException {
        // create metadata map
        CoverageInfo ci = geoServer.getCatalog().getCoverageByName("DEM");
        ci.getMetadata().put("custom", new HashMap<>());
        geoServer.getCatalog().save(ci);

        dataUtil.setConfigurationAttribute(config, ATT_LAYER, "DEM");
        config = dao.save(config);

        Trigger trigger =
                TriggerBuilder.newTrigger().forJob(batch.getId().toString()).startNow().build();
        scheduler.scheduleJob(trigger);

        while (scheduler.getTriggerState(trigger.getKey()) != TriggerState.NONE) {}

        ci = geoServer.getCatalog().getCoverageByName("DEM");
        assertNotNull(ci.getMetadata().get("revisionDate"));
        assertNotNull(((Map<?, ?>) ci.getMetadata().get("custom")).get("_timestamp"));
        geoServer.getCatalog().save(ci);
    }
}
