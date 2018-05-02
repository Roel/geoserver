/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.taskmanager.external.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.geoserver.taskmanager.external.DbSource;
import org.geoserver.taskmanager.external.DbTable;
import org.geoserver.taskmanager.external.Dialect;
import org.geoserver.taskmanager.external.ExternalGS;
import org.geoserver.taskmanager.util.NamedImpl;
import org.geoserver.taskmanager.util.SqlUtil;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.factory.GeoTools;

import it.geosolutions.geoserver.rest.encoder.GSAbstractStoreEncoder;
import it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder;

/**
 * DbSource for Jndi.
 * 
 * @author Niels Charlier
 *
 */
public class PostgisJndiDbSourceImpl extends NamedImpl implements DbSource  {
    
    private String jndiName;
    
    private String schema;
    
    private Map<String, String> targetJndiNames = new HashMap<String, String>();       

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public Map<String, String> getTargetJndiNames() {
        return targetJndiNames;
    }

    public void setTargetJndiNames(Map<String, String> targetJndiNames) {
        this.targetJndiNames = targetJndiNames;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        Context ctx = null;
        DataSource ds = null;        
        
        try {
            ctx = GeoTools.getInitialContext(GeoTools.getDefaultHints());
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
            
        try {
            ds = (DataSource) ctx.lookup(jndiName);
        } catch (NamingException e) {
            throw new SQLException(e);
        }
        
        return ds;            
    }

    @Override
    public GSAbstractStoreEncoder getStoreEncoder(String name, ExternalGS extGs) {
        String targetJndiName = targetJndiNames.get(extGs.getName());
        GSPostGISDatastoreEncoder encoder = new GSPostGISDatastoreEncoder(name);
        encoder.setJndiReferenceName(targetJndiName == null ? jndiName : targetJndiName);
        encoder.setSchema(schema);
        return encoder;
    }

    @Override
    public Map<String, Serializable> getParameters() {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(PostgisNGJNDIDataStoreFactory.DBTYPE.key, "postgis");
        params.put(PostgisNGJNDIDataStoreFactory.JNDI_REFNAME.key, jndiName);
        params.put(PostgisNGJNDIDataStoreFactory.SCHEMA.key, schema);
        return params;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public GSAbstractStoreEncoder postProcess(GSAbstractStoreEncoder encoder, DbTable table) {
        if (table != null) {
            String schema = SqlUtil.schema(table.getTableName());
            if (schema != null) {
                ((GSPostGISDatastoreEncoder) encoder).setSchema(schema);
            }
        }
        return encoder;
    }

    @Override
    public Dialect getDialect() {
        return new PostgisDialectImpl();
    }

}