/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.taskmanager.tasks;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.apache.commons.beanutils.PropertyUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.taskmanager.external.ExtTypes;
import org.geoserver.taskmanager.schedule.ParameterInfo;
import org.geoserver.taskmanager.schedule.TaskContext;
import org.geoserver.taskmanager.schedule.TaskException;
import org.geoserver.taskmanager.schedule.TaskResult;
import org.geoserver.taskmanager.schedule.TaskType;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A task that writes a timestamp for data and metadata to a layer's metadata.
 *
 * @author niels
 */
@Component("timeStampTaskType")
public class TimestampTaskTypeImpl implements TaskType {

    private static final Logger LOGGER = Logging.getLogger(TimestampTaskTypeImpl.class);

    public static final String NAME = "TimeStamp";

    private final Map<String, ParameterInfo> paramInfo = new LinkedHashMap<String, ParameterInfo>();

    public static final String PARAM_WORKSPACE = "workspace";

    public static final String PARAM_LAYER = "layer";

    @Autowired protected Catalog catalog;

    @Autowired protected ExtTypes extTypes;

    private String dataTimestampProperty = null;

    private String metadataTimestampProperty = null;

    @PostConstruct
    public void initParamInfo() {
        ParameterInfo paramWorkspace =
                new ParameterInfo(PARAM_WORKSPACE, extTypes.workspace, false);
        paramInfo.put(PARAM_WORKSPACE, paramWorkspace);
        paramInfo.put(
                PARAM_LAYER,
                new ParameterInfo(PARAM_LAYER, extTypes.internalLayer, true)
                        .dependsOn(false, paramWorkspace));
    }

    @Override
    public Map<String, ParameterInfo> getParameterInfo() {
        return paramInfo;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public TaskResult run(TaskContext ctx) throws TaskException {
        final LayerInfo layer = (LayerInfo) ctx.getParameterValues().get(PARAM_LAYER);
        final ResourceInfo rInfo = layer.getResource();

        Object oldTimestampValue;
        Object oldMetadataTimestampValue;

        Date currentTime = new Date();
        try {
            if (dataTimestampProperty != null) {
                oldTimestampValue =
                        PropertyUtils.getProperty(rInfo.getMetadata(), dataTimestampProperty);
                PropertyUtils.setProperty(rInfo.getMetadata(), dataTimestampProperty, currentTime);
                if (metadataTimestampProperty != null) {
                    oldMetadataTimestampValue =
                            PropertyUtils.getProperty(
                                    rInfo.getMetadata(), metadataTimestampProperty);
                    PropertyUtils.setProperty(
                            rInfo.getMetadata(), metadataTimestampProperty, currentTime);
                } else {
                    oldMetadataTimestampValue = null;
                }
                catalog.save(rInfo);
            } else {
                oldTimestampValue = null;
                oldMetadataTimestampValue = null;
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new TaskException(e);
        }

        return new TaskResult() {
            @Override
            public void commit() throws TaskException {
                // do nothing
            }

            @Override
            public void rollback() throws TaskException {
                // put old values back
                try {
                    if (dataTimestampProperty != null) {
                        PropertyUtils.setProperty(
                                rInfo.getMetadata(), dataTimestampProperty, oldTimestampValue);
                        if (metadataTimestampProperty != null) {
                            PropertyUtils.setProperty(
                                    rInfo.getMetadata(),
                                    metadataTimestampProperty,
                                    oldMetadataTimestampValue);
                        }
                        catalog.save(rInfo);
                    }
                } catch (IllegalAccessException
                        | InvocationTargetException
                        | NoSuchMethodException e) {
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                    throw new TaskException(e);
                }
            }
        };
    }

    @Override
    public void cleanup(TaskContext ctx) throws TaskException {
        // do nothing.
    }

    public String getDataTimestampProperty() {
        return dataTimestampProperty;
    }

    public void setDataTimestampProperty(String dataTimestampProperty) {
        this.dataTimestampProperty = dataTimestampProperty;
    }

    public String getMetadataTimestampProperty() {
        return metadataTimestampProperty;
    }

    public void setMetadataTimestampProperty(String metadataTimestampProperty) {
        this.metadataTimestampProperty = metadataTimestampProperty;
    }
}
