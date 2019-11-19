/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.web.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.metadata.data.model.MetadataTemplate;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.data.resource.ResourceConfigurationPage;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.SimpleAjaxLink;

public class LinkedLayersPanel extends Panel {

    private static final long serialVersionUID = 4556549618384659724L;

    public LinkedLayersPanel(String id, IModel<MetadataTemplate> metadataTemplateModel) {
        super(id);

        add(
                new GeoServerTablePanel<ResourceInfo>(
                        "layersTable", new LinkedLayersDataProvider(metadataTemplateModel)) {

                    private static final long serialVersionUID = -6805672124565219769L;

                    @SuppressWarnings("unchecked")
                    @Override
                    protected Component getComponentForProperty(
                            String id,
                            IModel<ResourceInfo> itemModel,
                            Property<ResourceInfo> property) {
                        if (property.equals(LinkedLayersDataProvider.NAME)) {
                            if (GeoServerApplication.get()
                                            .getCatalog()
                                            .getResource(
                                                    itemModel.getObject().getId(),
                                                    ResourceInfo.class)
                                    != null) {
                                return new SimpleAjaxLink<String>(
                                        id, (IModel<String>) property.getModel(itemModel)) {
                                    private static final long serialVersionUID =
                                            -4516384813157392818L;

                                    @Override
                                    protected void onClick(AjaxRequestTarget target) {
                                        setResponsePage(
                                                new ResourceConfigurationPage(
                                                                itemModel.getObject(), false)
                                                        .setReturnPage(getPage()));
                                    }
                                };
                                /* id,
                                ResourceConfigurationPage.class,
                                property.getModel(itemModel),
                                ResourceConfigurationPage.NAME,
                                itemModel.getObject().getName(),
                                ResourceConfigurationPage.WORKSPACE,
                                itemModel.getObject().getStore().getWorkspace().getName());*/
                            }
                        }
                        return null;
                    }
                });
    }
}
