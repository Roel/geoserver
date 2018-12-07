/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.web.panel.attribute;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.metadata.data.dto.AttributeConfiguration;
import org.geoserver.metadata.data.dto.FieldTypeEnum;
import org.geoserver.metadata.data.dto.OccurrenceEnum;
import org.geoserver.metadata.data.model.ComplexMetadataMap;
import org.geoserver.metadata.data.service.GeneratorService;
import org.geoserver.metadata.data.service.impl.MetadataConstants;
import org.geoserver.metadata.web.resource.WicketFileResourceLoader;
import org.geoserver.platform.resource.Resource;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerTablePanel;

/**
 * Entry point for the gui generation. This parses the configuration and adds simple fields, complex
 * fields (composition of multiple simple fields) and lists of simple or complex fields.
 *
 * @author Timothy De Bock - timothy.debock.github@gmail.com
 */
public class AttributesTablePanel extends Panel {
    private static final long serialVersionUID = 1297739738862860160L;

    public AttributesTablePanel(
            String id,
            GeoServerDataProvider<AttributeConfiguration> dataProvider,
            IModel<ComplexMetadataMap> metadataModel,
            Map<String, List<Integer>> derivedAtts) {
        super(id, metadataModel);

        List<IStringResourceLoader> loaders =
                getApplication().getResourceSettings().getStringResourceLoaders();
        boolean loaded = false;
        for (IStringResourceLoader loader : loaders) {
            if (loader instanceof WicketFileResourceLoader) {
                if (((WicketFileResourceLoader) loader)
                        .getResourceBundleName()
                        .equals("metadata")) {
                    loaded = true;
                    break;
                }
            }
        }
        if (!loaded) {
            GeoServerDataDirectory data =
                    GeoServerApplication.get()
                            .getApplicationContext()
                            .getBean(GeoServerDataDirectory.class);
            Resource metadataFolder = data.get(MetadataConstants.DIRECTORY);
            WicketFileResourceLoader loader =
                    new WicketFileResourceLoader(metadataFolder.toString(), "metadata");
            loader.setShouldThrowException(false);
            loaders.add(loader);
        }

        GeoServerTablePanel<AttributeConfiguration> tablePanel =
                createAttributesTablePanel(dataProvider, derivedAtts);
        tablePanel.setFilterVisible(false);
        tablePanel.setFilterable(false);
        tablePanel.getTopPager().setVisible(false);
        tablePanel.getBottomPager().setVisible(false);
        tablePanel.setOutputMarkupId(false);
        tablePanel.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
        tablePanel.setSelectable(false);
        tablePanel.setSortable(false);
        add(tablePanel);
    }

    private GeoServerTablePanel<AttributeConfiguration> createAttributesTablePanel(
            GeoServerDataProvider<AttributeConfiguration> dataProvider,
            Map<String, List<Integer>> derivedAtts) {

        return new GeoServerTablePanel<AttributeConfiguration>(
                "attributesTablePanel", dataProvider) {
            private static final long serialVersionUID = 5267842353156378075L;

            @Override
            protected Component getComponentForProperty(
                    String id,
                    IModel<AttributeConfiguration> itemModel,
                    GeoServerDataProvider.Property<AttributeConfiguration> property) {
                if (property.equals(AttributeDataProvider.NAME)) {
                    String labelValue = resolveLabelValue(itemModel.getObject());
                    return new Label(id, labelValue);
                }
                if (property.equals(AttributeDataProvider.VALUE)) {
                    AttributeConfiguration attributeConfiguration = itemModel.getObject();
                    if (OccurrenceEnum.SINGLE.equals(attributeConfiguration.getOccurrence())) {
                        Component component =
                                EditorFactory.getInstance()
                                        .create(
                                                attributeConfiguration,
                                                id,
                                                getMetadataModel().getObject());
                        // disable components with values from the templates
                        if (component != null
                                && derivedAtts != null
                                && derivedAtts.containsKey(attributeConfiguration.getKey())) {
                            boolean disableInput =
                                    derivedAtts.get(attributeConfiguration.getKey()).size() > 0;
                            component.setEnabled(!disableInput);
                        }
                        return component;
                    } else if (attributeConfiguration.getFieldType() == FieldTypeEnum.COMPLEX) {
                        RepeatableComplexAttributeDataProvider repeatableDataProvider =
                                new RepeatableComplexAttributeDataProvider(
                                        attributeConfiguration, getMetadataModel());

                        return new RepeatableComplexAttributesTablePanel(
                                id,
                                repeatableDataProvider,
                                getMetadataModel(),
                                attributeConfiguration,
                                GeoServerApplication.get()
                                        .getBeanOfType(GeneratorService.class)
                                        .findGeneratorByType(attributeConfiguration.getTypename()),
                                derivedAtts);
                    } else {
                        RepeatableAttributeDataProvider<String> repeatableDataProvider =
                                new RepeatableAttributeDataProvider<String>(
                                        EditorFactory.getInstance()
                                                .getItemClass(attributeConfiguration),
                                        attributeConfiguration,
                                        getMetadataModel());
                        return new RepeatableAttributesTablePanel(
                                id, repeatableDataProvider, getMetadataModel(), derivedAtts);
                    }
                }
                return null;
            }
        };
    }

    /**
     * Try to find the label from the resource bundle
     *
     * @param attribute
     * @return
     */
    private String resolveLabelValue(AttributeConfiguration attribute) {

        try {
            return getString(AttributeConfiguration.PREFIX + attribute.getKey());
        } catch (MissingResourceException ignored) {
        }
        return attribute.getLabel();
    }

    @SuppressWarnings("unchecked")
    public IModel<ComplexMetadataMap> getMetadataModel() {
        return (IModel<ComplexMetadataMap>) getDefaultModel();
    }
}