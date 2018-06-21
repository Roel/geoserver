/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.opengis.wfs20.StoredQueryDescriptionType;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.util.IOUtils;
import org.geoserver.wfs.StoredQuery;
import org.geoserver.wfs.StoredQueryProvider;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.Parser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Tests that namespaces are correctly handled by WFS and app-schema when features belonging to
 * different namespaces are chained together.
 */
public final class NamespacesWfsTest extends AbstractAppSchemaTestSupport {

    private static final File TEST_ROOT_DIRECTORY;

    private static final String TEST_STORED_QUERY_ID = "NamespacesTestStoredQuery";

    /* Should return the same result as a GetFeature request against the Station feature type */
    private static final String TEST_STORED_QUERY_DEFINITION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<wfs:StoredQueryDescription id='"
                    + TEST_STORED_QUERY_ID
                    + "'"
                    + " xmlns:xlink=\"http://www.w3.org/1999/xlink\""
                    + " xmlns:ows=\"http://www.opengis.net/ows/1.1\""
                    + " xmlns:gml=\"${GML_NAMESPACE}\""
                    + " xmlns:wfs=\"http://www.opengis.net/wfs/2.0\""
                    + " xmlns:fes=\"http://www.opengis.net/fes/2.0\">>\n"
                    + "  <wfs:QueryExpressionText\n"
                    + "   returnFeatureTypes='st_${GML_PREFIX}:Station_${GML_PREFIX}'\n"
                    + "   language='urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression'\n"
                    + "   isPrivate='false'>\n"
                    + "    <wfs:Query typeNames='st_${GML_PREFIX}:Station_${GML_PREFIX}'>\n"
                    + "      <fes:Filter>\n"
                    + "        <fes:PropertyIsEqualTo>\n"
                    + "          <fes:ValueReference>st_${GML_PREFIX}:measurements/ms_${GML_PREFIX}:Measurement/ms_${GML_PREFIX}:name</fes:ValueReference>\n"
                    + "          <fes:Literal>wind</fes:Literal>\n"
                    + "        </fes:PropertyIsEqualTo>\n"
                    + "      </fes:Filter>\n"
                    + "    </wfs:Query>\n"
                    + "  </wfs:QueryExpressionText>\n"
                    + "</wfs:StoredQueryDescription>";

    private static final Map<String, String> GML31_PARAMETERS =
            Collections.unmodifiableMap(
                    Stream.of(
                                    new SimpleEntry<>("GML_PREFIX", "gml31"),
                                    new SimpleEntry<>(
                                            "GML_NAMESPACE", "http://www.opengis.net/gml"),
                                    new SimpleEntry<>(
                                            "GML_LOCATION",
                                            "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd"))
                            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    private static final Map<String, String> GML32_PARAMETERS =
            Collections.unmodifiableMap(
                    Stream.of(
                                    new SimpleEntry<>("GML_PREFIX", "gml32"),
                                    new SimpleEntry<>(
                                            "GML_NAMESPACE", "http://www.opengis.net/gml/3.2"),
                                    new SimpleEntry<>(
                                            "GML_LOCATION",
                                            "http://schemas.opengis.net/gml/3.2.1/gml.xsd"))
                            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    static {
        try {
            // create the tests root directory
            TEST_ROOT_DIRECTORY = IOUtils.createTempDirectory("app-schema-stations");
        } catch (Exception exception) {
            throw new RuntimeException("Error creating temporary directory.", exception);
        }
    }

    @AfterClass
    public static void afterTests() {
        try {
            // remove tests root directory
            IOUtils.delete(TEST_ROOT_DIRECTORY);
        } catch (Exception exception) {
            throw new RuntimeException(
                    String.format(
                            "Error removing tests root directory '%s'.",
                            TEST_ROOT_DIRECTORY.getAbsolutePath()));
        }
    }

    /**
     * Helper class that will setup custom complex feature types using the stations data set.
     * Parameterization will be used to setup complex features types for GML31 and GML32 based on
     * the same mappings files and schemas.
     */
    private static final class MockData extends AbstractAppSchemaMockData {

        // stations GML 3.1 namespaces
        private static final String STATIONS_PREFIX_GML31 = "st_gml31";
        private static final String STATIONS_URI_GML31 = "http://www.stations_gml31.org/1.0";
        private static final String MEASUREMENTS_PREFIX_GML31 = "ms_gml31";
        private static final String MEASUREMENTS_URI_GML31 =
                "http://www.measurements_gml31.org/1.0";

        // stations GML 3.2 namespaces
        private static final String STATIONS_PREFIX_GML32 = "st_gml32";
        private static final String STATIONS_URI_GML32 = "http://www.stations_gml32.org/1.0";
        private static final String MEASUREMENTS_PREFIX_GML32 = "ms_gml32";
        private static final String MEASUREMENTS_URI_GML32 =
                "http://www.measurements_gml32.org/1.0";

        @Override
        public void addContent() {
            // add GML 3.1 namespaces
            putNamespace(STATIONS_PREFIX_GML31, STATIONS_URI_GML31);
            putNamespace(MEASUREMENTS_PREFIX_GML31, MEASUREMENTS_URI_GML31);
            // add GML 3.2 namespaces
            putNamespace(STATIONS_PREFIX_GML32, STATIONS_URI_GML32);
            putNamespace(MEASUREMENTS_PREFIX_GML32, MEASUREMENTS_URI_GML32);
            // add GML 3.1 feature type
            addFeatureType(STATIONS_PREFIX_GML31, "gml31", GML31_PARAMETERS);
            // add GML 3.2 feature type
            addFeatureType(STATIONS_PREFIX_GML32, "gml32", GML32_PARAMETERS);
        }

        /**
         * Helper method that will add the stations feature type customizing it for the desired GML
         * version.
         */
        private void addFeatureType(
                String namespacePrefix, String gmlPrefix, Map<String, String> parameters) {
            // create root directory
            File gmlDirectory = new File(TEST_ROOT_DIRECTORY, gmlPrefix);
            gmlDirectory.mkdirs();
            // add the necessary files
            File stationsMappings =
                    new File(gmlDirectory, String.format("stations_%s.xml", gmlPrefix));
            File stationsProperties =
                    new File(gmlDirectory, String.format("stations_%s.properties", gmlPrefix));
            File stationsSchema =
                    new File(gmlDirectory, String.format("stations_%s.xsd", gmlPrefix));
            File measurementsMappings =
                    new File(gmlDirectory, String.format("measurements_%s.xml", gmlPrefix));
            File measurementsProperties =
                    new File(gmlDirectory, String.format("measurements_%s.properties", gmlPrefix));
            File measurementsSchema =
                    new File(gmlDirectory, String.format("measurements_%s.xsd", gmlPrefix));
            // perform the parameterization
            substituteParameters(
                    "/test-data/stations/mappings/stations.xml", parameters, stationsMappings);
            substituteParameters(
                    "/test-data/stations/data/stations.properties", parameters, stationsProperties);
            substituteParameters(
                    "/test-data/stations/schemas/stations.xsd", parameters, stationsSchema);
            substituteParameters(
                    "/test-data/stations/mappings/measurements.xml",
                    parameters,
                    measurementsMappings);
            substituteParameters(
                    "/test-data/stations/data/measurements.properties",
                    parameters,
                    measurementsProperties);
            substituteParameters(
                    "/test-data/stations/schemas/measurements.xsd", parameters, measurementsSchema);
            // create the feature type
            addFeatureType(
                    namespacePrefix,
                    String.format("Station_%s", gmlPrefix),
                    stationsMappings.getAbsolutePath(),
                    stationsProperties.getAbsolutePath(),
                    stationsSchema.getAbsolutePath(),
                    measurementsMappings.getAbsolutePath(),
                    measurementsProperties.getAbsolutePath(),
                    measurementsSchema.getAbsolutePath());
        }

        /**
         * Helper method that reads a resource to a string performs the parameterization and writes
         * the result to the provided new file.
         */
        private static void substituteParameters(
                String resourceName, Map<String, String> parameters, File newFile) {
            // read the resource content
            String resourceContent = resourceToString(resourceName);
            resourceContent = substituteParametersInTemplateString(resourceContent, parameters);
            try {
                // write the final resource content to the provided location
                Files.write(newFile.toPath(), resourceContent.getBytes());
            } catch (Exception exception) {
                throw new RuntimeException(
                        String.format(
                                "Error writing content to file '%s'.", newFile.getAbsolutePath()),
                        exception);
            }
        }

        private static String substituteParametersInTemplateString(
                String templateString, Map<String, String> parameters) {
            String processedString = templateString;

            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                processedString =
                        processedString.replace(
                                String.format("${%s}", parameter.getKey()), parameter.getValue());
            }

            return processedString;
        }

        /** Helper method the reads a resource content to a string. */
        private static String resourceToString(String resourceName) {
            try (InputStream input = NamespacesWfsTest.class.getResourceAsStream(resourceName)) {
                return IOUtils.toString(input);
            } catch (Exception exception) {
                throw new RuntimeException(
                        String.format("Error reading resource '%s' content.", resourceName),
                        exception);
            }
        }
    }

    @Override
    protected MockData createTestData() {
        // instantiate our custom complex types
        return new MockData();
    }

    // xpath engines used to check WFS responses
    private XpathEngine WFS11_XPATH_ENGINE;
    private XpathEngine WFS20_XPATH_ENGINE;

    @Before
    public void beforeTest() {
        // instantiate WFS 1.1 xpath engine
        WFS11_XPATH_ENGINE =
                buildXpathEngine(
                        "wfs", "http://www.opengis.net/wfs",
                        "gml", "http://www.opengis.net/gml");
        // instantiate WFS 2.0 xpath engine
        WFS20_XPATH_ENGINE =
                buildXpathEngine(
                        "wfs", "http://www.opengis.net/wfs/2.0",
                        "gml", "http://www.opengis.net/gml/3.2");
    }

    /** * GetFeature tests ** */
    @Test
    public void globalServiceGetFeatureNamespacesWfs11() throws Exception {
        Document document =
                getAsDOM("wfs?request=GetFeature&version=1.1.0&typename=st_gml31:Station_gml31");
        checkWfs11StationsGetFeatureResult(document);
    }

    @Test
    public void virtualServiceGetFeatureNamespacesWfs11() throws Exception {
        Document document =
                getAsDOM(
                        "st_gml31/wfs?request=GetFeature&version=1.1.0&typename=st_gml31:Station_gml31");
        checkWfs11StationsGetFeatureResult(document);
    }

    @Test
    public void globalServiceGetFeatureNamespacesWfs20() throws Exception {
        Document document =
                getAsDOM("wfs?request=GetFeature&version=2.0&typename=st_gml32:Station_gml32");
        checkWfs20StationsGetFeatureResult(document);
    }

    @Test
    public void virtualServiceGetFeatureNamespacesWfs20() throws Exception {
        Document document =
                getAsDOM(
                        "st_gml32/wfs?request=GetFeature&version=2.0&typename=st_gml32:Station_gml32");
        checkWfs20StationsGetFeatureResult(document);
    }

    /** * GetPropertyValue tests ** */
    @Test
    public void globalServiceGetPropertyValueNamespacesGml32() throws Exception {
        Document document =
                getAsDOM(
                        "wfs?request=GetPropertyValue&version=2.0&typename=st_gml32:Station_gml32&valueReference=st_gml32:measurements");
        checkGml32StationsGetPropertyValueResult(document);
    }

    @Test
    public void virtualServiceGetPropertyValueNamespacesGml32() throws Exception {
        Document document =
                getAsDOM(
                        "st_gml32/wfs?request=GetPropertyValue&version=2.0&typename=st_gml32:Station_gml32&valueReference=st_gml32:measurements");
        checkGml32StationsGetPropertyValueResult(document);
    }

    /** * StoredQuery tests ** */
    @Test
    public void globalServiceStoredQueryNamespacesGml32() throws Exception {
        StoredQueryProvider storedQueryProvider = new StoredQueryProvider(getCatalog());
        try {
            createTestStoredQuery(storedQueryProvider, GML32_PARAMETERS);

            Document document =
                    getAsDOM(
                            "wfs?request=GetFeature&version=2.0&StoredQueryID="
                                    + TEST_STORED_QUERY_ID);
            checkWfs20StationsGetFeatureResult(document);
        } finally {
            storedQueryProvider.removeAll();
            assertTrue(storedQueryProvider.listStoredQueries().size() == 1);
        }
    }

    @Test
    public void virtualServiceStoredQueryNamespacesGml32() throws Exception {
        StoredQueryProvider storedQueryProvider = new StoredQueryProvider(getCatalog());
        try {
            createTestStoredQuery(storedQueryProvider, GML32_PARAMETERS);

            Document document =
                    getAsDOM(
                            "st_gml32/wfs?request=GetFeature&version=2.0&StoredQueryID="
                                    + TEST_STORED_QUERY_ID);
            checkWfs20StationsGetFeatureResult(document);
        } finally {
            storedQueryProvider.removeAll();
            assertTrue(storedQueryProvider.listStoredQueries().size() == 1);
        }
    }

    @Test
    public void globalServiceStoredQueryNamespacesGml31() throws Exception {
        StoredQueryProvider storedQueryProvider = new StoredQueryProvider(getCatalog());
        try {
            createTestStoredQuery(storedQueryProvider, GML31_PARAMETERS);

            Document document =
                    getAsDOM(
                            "wfs?request=GetFeature&version=2.0&outputFormat=gml3&StoredQueryID="
                                    + TEST_STORED_QUERY_ID);
            checkWfs11StationsGetFeatureResult(document);
        } finally {
            storedQueryProvider.removeAll();
            assertTrue(storedQueryProvider.listStoredQueries().size() == 1);
        }
    }

    @Test
    public void virtualServiceStoredQueryNamespacesGml31() throws Exception {
        StoredQueryProvider storedQueryProvider = new StoredQueryProvider(getCatalog());
        try {
            createTestStoredQuery(storedQueryProvider, GML31_PARAMETERS);

            Document document =
                    getAsDOM(
                            "st_gml31/wfs?request=GetFeature&version=2.0&outputFormat=gml3&StoredQueryID="
                                    + TEST_STORED_QUERY_ID);
            checkWfs11StationsGetFeatureResult(document);
        } finally {
            storedQueryProvider.removeAll();
            assertTrue(storedQueryProvider.listStoredQueries().size() == 1);
        }
    }

    private void createTestStoredQuery(
            StoredQueryProvider storedQueryProvider, Map<String, String> parameters)
            throws Exception {
        StoredQueryDescriptionType storedQueryDescriptionType =
                createTestStoredQueryDefinition(parameters);
        StoredQuery result = storedQueryProvider.createStoredQuery(storedQueryDescriptionType);

        assertTrue(storedQueryProvider.listStoredQueries().size() == 2);
        assertThat(result.getName(), is(TEST_STORED_QUERY_ID));
        assertThat(
                storedQueryProvider.getStoredQuery(TEST_STORED_QUERY_ID).getName(),
                is(TEST_STORED_QUERY_ID));
    }

    private StoredQueryDescriptionType createTestStoredQueryDefinition(
            Map<String, String> parameters) throws Exception {
        Parser p = new Parser(new WFSConfiguration());
        p.setRootElementType(WFS.StoredQueryDescriptionType);

        String queryDefinition =
                MockData.substituteParametersInTemplateString(
                        TEST_STORED_QUERY_DEFINITION, parameters);
        StringReader reader = new StringReader(queryDefinition);
        try {
            return (StoredQueryDescriptionType) p.parse(reader);
        } finally {
            reader.close();
        }
    }

    /** Check the result of a WFS 1.1 (GML 3.1) get feature request targeting stations data set. */
    private void checkWfs11StationsGetFeatureResult(Document document) {
        checkCount(
                WFS11_XPATH_ENGINE,
                document,
                1,
                "/wfs:FeatureCollection/gml:featureMember/"
                        + "st_gml31:Station_gml31[@gml:id='st.1']/st_gml31:measurements/ms_gml31:Measurement[ms_gml31:name='temperature']");
        checkCount(
                WFS11_XPATH_ENGINE,
                document,
                1,
                "/wfs:FeatureCollection/gml:featureMember/"
                        + "st_gml31:Station_gml31[@gml:id='st.1']/st_gml31:location/gml:Point[gml:pos='1.0 -1.0']");
    }

    /** Check the result of a WFS 2.0 (GML 3.2) get feature request targeting stations data set. */
    private void checkWfs20StationsGetFeatureResult(Document document) {
        checkCount(
                WFS20_XPATH_ENGINE,
                document,
                1,
                "/wfs:FeatureCollection/wfs:member/"
                        + "st_gml32:Station_gml32[@gml:id='st.1']/st_gml32:measurements/ms_gml32:Measurement[ms_gml32:name='temperature']");
        checkCount(
                WFS20_XPATH_ENGINE,
                document,
                1,
                "/wfs:FeatureCollection/wfs:member/"
                        + "st_gml32:Station_gml32[@gml:id='st.1']/st_gml32:location/gml:Point[gml:pos='1.0 -1.0']");
    }

    /**
     * Check the result of a WFS 2.0 (GML 3.2) get property value request targeting the Station
     * feature type.
     */
    private void checkGml32StationsGetPropertyValueResult(Document document) {
        checkCount(
                WFS20_XPATH_ENGINE,
                document,
                1,
                "/wfs:ValueCollection/wfs:member/"
                        + "st_gml32:measurements/ms_gml32:Measurement[ms_gml32:name='temperature']");
        checkCount(
                WFS20_XPATH_ENGINE,
                document,
                1,
                "/wfs:ValueCollection/wfs:member/"
                        + "st_gml32:measurements/ms_gml32:Measurement[ms_gml32:name='wind']");
    }

    /**
     * Helper method that evaluates a xpath and checks if the number of nodes found correspond to
     * the expected number,
     */
    private void checkCount(
            XpathEngine xpathEngine, Document document, int expectedCount, String xpath) {
        try {
            // evaluate the xpath and compare the number of nodes found
            assertThat(
                    xpathEngine.getMatchingNodes(xpath, document).getLength(), is(expectedCount));
        } catch (Exception exception) {
            throw new RuntimeException("Error evaluating xpath.", exception);
        }
    }

    /** Helper method that builds a xpath engine that will use the provided GML namespaces. */
    private XpathEngine buildXpathEngine(String... namespaces) {
        // build xpath engine
        XpathEngine xpathEngine = XMLUnit.newXpathEngine();
        Map<String, String> finalNamespaces = new HashMap<>();
        // add common namespaces
        finalNamespaces.put("ows", "http://www.opengis.net/ows");
        finalNamespaces.put("ogc", "http://www.opengis.net/ogc");
        finalNamespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
        finalNamespaces.put("xsd", "http://www.w3.org/2001/XMLSchema");
        finalNamespaces.put("xlink", "http://www.w3.org/1999/xlink");
        finalNamespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        // add al catalog namespaces
        finalNamespaces.putAll(getTestData().getNamespaces());
        // add provided namespaces
        if (namespaces.length % 2 != 0) {
            throw new RuntimeException("Invalid number of namespaces provided.");
        }
        for (int i = 0; i < namespaces.length; i += 2) {
            finalNamespaces.put(namespaces[i], namespaces[i + 1]);
        }
        // add namespaces to the xpath engine
        xpathEngine.setNamespaceContext(new SimpleNamespaceContext(finalNamespaces));
        return xpathEngine;
    }
}
