/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.data.service.impl;


import org.geoserver.metadata.data.dto.FieldTypeEnum;
import org.geoserver.metadata.data.dto.MetadataAttributeConfiguration;
import org.geoserver.metadata.data.dto.MetadataEditorConfiguration;
import org.geoserver.metadata.data.service.MetadataEditorConfigurationService;
import org.springframework.stereotype.Repository;

@Repository
public class MetadataEditorConfigurationServiceImpl implements MetadataEditorConfigurationService {

    @Override
    public MetadataEditorConfiguration readConfiguration() {
        MetadataEditorConfiguration configuration = new MetadataEditorConfiguration();

        configuration.getAttributes().add(new MetadataAttributeConfiguration("testveld", FieldTypeEnum.TEXT));
        configuration.getAttributes().add(new MetadataAttributeConfiguration("anderveld", FieldTypeEnum.NUMBER));
        MetadataAttributeConfiguration dropdown = new MetadataAttributeConfiguration("drop it", FieldTypeEnum.DROPDOWN);
        dropdown.getValues().add("MC Hammer");
        dropdown.getValues().add("Sledge Hammer");
        dropdown.getValues().add("War Hammer");
        configuration.getAttributes().add(dropdown);


        return configuration;
    }
}
