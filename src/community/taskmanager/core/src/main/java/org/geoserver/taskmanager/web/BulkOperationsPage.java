/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.taskmanager.web;

import org.apache.wicket.markup.html.form.Form;
import org.geoserver.taskmanager.web.panel.bulk.BulkRunPanel;
import org.geoserver.web.ComponentAuthorizer;
import org.geoserver.web.GeoServerSecuredPage;

public class BulkOperationsPage extends GeoServerSecuredPage {

    private static final long serialVersionUID = -3476820703264158330L;

    @Override
    public void onInitialize() {
        super.onInitialize();

        Form<Object> form = new Form<Object>("form");
        form.add(new BulkRunPanel("bulkRunPanel"));

        add(form);
    }

    protected ComponentAuthorizer getPageAuthorizer() {
        return ComponentAuthorizer.ADMIN;
    }
}
