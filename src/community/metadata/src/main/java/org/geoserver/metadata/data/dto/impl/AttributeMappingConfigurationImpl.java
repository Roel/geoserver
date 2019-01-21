/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.data.dto.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import org.geoserver.metadata.data.dto.AttributeComplexTypeMapping;
import org.geoserver.metadata.data.dto.AttributeMapping;
import org.geoserver.metadata.data.dto.AttributeMappingConfiguration;

/**
 * Toplevel Object that matches yaml structure.
 *
 * <p>This part or the yaml contains the configuration that matches fields in the xml (Xpath
 * expressions) to the field configuration of the geoserver metadata GUI.
 *
 * <p>example of the yaml file: metadata-mapping.yaml
 *
 * @author Timothy De Bock - timothy.debock.github@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeMappingConfigurationImpl implements AttributeMappingConfiguration {

    List<AttributeMapping> geonetworkmapping = new ArrayList<>();

    List<AttributeComplexTypeMapping> objectmapping = new ArrayList<>();

    @Override
    public List<AttributeMapping> getGeonetworkmapping() {
        return geonetworkmapping;
    }

    @Override
    public void setGeonetworkmapping(List<AttributeMapping> geonetworkmapping) {
        this.geonetworkmapping = geonetworkmapping;
    }

    @Override
    public List<AttributeComplexTypeMapping> getObjectmapping() {
        return objectmapping;
    }

    @Override
    public void setObjectmapping(List<AttributeComplexTypeMapping> objectmapping) {
        this.objectmapping = objectmapping;
    }

    @Override
    public AttributeComplexTypeMapping findType(String typename) {
        for (AttributeComplexTypeMapping type : objectmapping) {
            if (typename.equals(type.getTypename())) {
                return type;
            }
        }
        return null;
    }
}
