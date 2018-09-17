//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.geoserver.metadata.web;

import org.geoserver.metadata.web.panel.MetadataPanel;
import org.geoserver.web.ComponentAuthorizer;
import org.geoserver.web.GeoServerBasePage;
import org.geotools.util.logging.Logging;

import java.util.logging.Logger;

public class MetadataTemplatePage extends GeoServerBasePage {

    private static final Logger LOGGER = Logging.getLogger(MetadataTemplatePage.class);

    private static final long serialVersionUID = 2273966783474224452L;

    public MetadataTemplatePage() {
    }

    public void onInitialize() {
        super.onInitialize();
        this.add(new MetadataPanel("metadataTemplatePanel"));
    }

    protected ComponentAuthorizer getPageAuthorizer() {
        return ComponentAuthorizer.AUTHENTICATED;
    }
}
