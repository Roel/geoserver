/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.metadata.web.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.metadata.data.dto.AttributeInput;
import org.geoserver.metadata.data.dto.MetadataAttributeConfiguration;
import org.geoserver.metadata.data.service.MetadataEditorConfigurationService;
import org.geoserver.metadata.web.panel.attribute.AttributeDataProvider;
import org.geoserver.metadata.web.panel.attribute.DropDownPanel;
import org.geoserver.metadata.web.panel.attribute.TextFieldPanel;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MetadataPanel extends Panel {
    private static final long serialVersionUID = 1297739738862860160L;

    private static final Logger LOGGER = Logging.getLogger(MetadataPanel.class);


    public MetadataPanel(String id) {
        super(id);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();


        //the attributes panel
        GeoServerTablePanel attributesPanel;
        add(attributesPanel = createAttributesPanel());
        attributesPanel.setFilterVisible(false);
        attributesPanel.setSelectable(false);
        attributesPanel.setPageable(false);
        attributesPanel.setSortable(false);
        attributesPanel.setOutputMarkupId(true);

    }


    private GeoServerTablePanel createAttributesPanel() {

        return new GeoServerTablePanel<AttributeInput>("attributesPanel", new AttributeDataProvider(), true) {

            private static final long serialVersionUID = -8943273843044917552L;

            @SuppressWarnings("unchecked")
            @Override
            protected Component getComponentForProperty(String id, IModel<AttributeInput> itemModel,
                                                        GeoServerDataProvider.Property<AttributeInput> property) {
                final GeoServerTablePanel<AttributeInput> tablePanel = this;
                if (property.equals(AttributeDataProvider.VALUE)) {
                    System.out.println(itemModel.getObject().getAttributeConfiguration().getLabel());
                    switch (itemModel.getObject().getAttributeConfiguration().getFieldType()) {
                        case TEXT:
                            return new TextFieldPanel(id, (IModel<String>) property.getModel(itemModel));
                        case NUMBER:
                            return new TextFieldPanel(id, (IModel<String>) property.getModel(itemModel));
                        case DROPDOWN:
                            final DropDownPanel ddp =
                                    new DropDownPanel(id, (IModel<String>) property.getModel(itemModel),
                                            itemModel.getObject().getAttributeConfiguration().getValues());

                            return ddp;

                    }
                }
                return null;
            }
        };
    }
}
