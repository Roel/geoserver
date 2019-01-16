/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.data.service;

import java.io.IOException;
import java.util.List;
import org.geoserver.metadata.AbstractMetadataTest;
import org.geoserver.metadata.data.dto.AttributeMappingConfiguration;
import org.geoserver.metadata.data.dto.MetadataAttributeConfiguration;
import org.geoserver.metadata.data.dto.MetadataEditorConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test yaml parsing.
 *
 * @author Timothy De Bock - timothy.debock.github@gmail.com
 */
public class YamlServiceTest extends AbstractMetadataTest {

    @Autowired private YamlService yamlService;

    @Test
    public void testFileRegistry() throws IOException {
        MetadataEditorConfiguration configuration = yamlService.readConfiguration();
        Assert.assertNotNull(configuration);
        Assert.assertEquals(8, configuration.getAttributes().size());
        Assert.assertEquals(3, configuration.getGeonetworks().size());
        Assert.assertEquals(4, configuration.getTypes().size());

        Assert.assertEquals(
                "indentifier-single",
                findAttribute(configuration.getAttributes(), "indentifier-single").getLabel());
        Assert.assertEquals(
                "indentifier-single",
                findAttribute(configuration.getAttributes(), "indentifier-single").getLabel());
        Assert.assertEquals(
                "dropdown-field",
                findAttribute(configuration.getAttributes(), "dropdown-field").getLabel());
        Assert.assertEquals(
                "refsystem as list",
                findAttribute(configuration.getAttributes(), "refsystem-as-list").getLabel());

        List<MetadataAttributeConfiguration> complexAttributes =
                configuration.findType("referencesystem").getAttributes();
        Assert.assertEquals("Code", findAttribute(complexAttributes, "code").getLabel());
    }

    @Test
    public void testGeonetworkMappingRegistry() throws IOException {
        AttributeMappingConfiguration configuration = yamlService.readMapping();
        Assert.assertNotNull(configuration);
        Assert.assertEquals(5, configuration.getGeonetworkmapping().size());
        Assert.assertEquals(1, configuration.getObjectmapping().size());
    }

    private MetadataAttributeConfiguration findAttribute(
            List<MetadataAttributeConfiguration> configurations, String key) {
        for (MetadataAttributeConfiguration attribute : configurations) {
            if (attribute.getKey().equals(key)) {
                return attribute;
            }
        }
        return null;
    }
}
