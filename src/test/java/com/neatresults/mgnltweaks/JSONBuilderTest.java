package com.neatresults.mgnltweaks;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypeTemplateUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.RepositoryTestCase;

/**
 * Just a test.
 */
public class JSONBuilderTest extends RepositoryTestCase {

    private final String rootTree = "/home.@type=mgnl:page\n" +
            "/home/mgnl_apex.@type=mgnl:apex\n" +
            "/home/mgnl_apex/alias.@type=mgnl:contentNode\n" +
            "/home/mgnl_apex/alias.name=a\n" +
            "/home/mgnl_apex/alias2.@type=mgnl:contentNode\n" +
            "/home/mgnl_apex/alias2.name=das\n" +
            "/home/mgnl_apex/alias2.locale=de\n" +
            "/home/mgnl_apex/alias3.@type=mgnl:contentNode\n" +
            "/home/mgnl_apex/alias3.name=this\n" +
            "/home/mgnl_apex/alias3.locale=en\n" +
            "/home/section.@type=mgnl:page\n" +
            "/home/section/mgnl_apex.@type=mgnl:apex\n" +
            "/home/section/mgnl_apex/alias.@type=mgnl:contentNode\n" +
            "/home/section/mgnl_apex/alias.name=b\n" +
            "/home/section/mgnl_apex/alias2.@type=mgnl:contentNode\n" +
            "/home/section/mgnl_apex/alias2.name=bb\n" +
            "/home/section/mgnl_apex/alias3.@type=mgnl:contentNode\n" +
            "/home/section/mgnl_apex/alias3.name=duplicate_name\n" +
            "/home/section/mgnl_apex/alias4.@type=mgnl:contentNode\n" +
            "/home/section/mgnl_apex/alias4.name=ist\n" +
            "/home/section/mgnl_apex/alias4.locale=de\n" +
            "/home/section/mgnl_apex/alias5.@type=mgnl:contentNode\n" +
            "/home/section/mgnl_apex/alias5.name=is\n" +
            "/home/section/mgnl_apex/alias5.locale=en\n" +
            "/home/section2.@type=mgnl:page\n" +
            "/home/section2/mgnl_apex.@type=mgnl:apex\n" +
            "/home/section2/mgnl_apex/alias.@type=mgnl:contentNode\n" +
            "/home/section2/mgnl_apex/alias.name=x\n" +
            "/home/section/article.@type=mgnl:page\n" +
            "/home/section/article/mgnl_apex.@type=mgnl:apex\n" +
            "/home/section/article/mgnl_apex/alias.@type=mgnl:contentNode\n" +
            "/home/section/article/mgnl_apex/alias.name=c\n" +
            "/home/section/article/mgnl_apex/alias2.@type=mgnl:contentNode\n" +
            "/home/section/article/mgnl_apex/alias2.name=duplicate_name\n" +
            "/home/section/article/mgnl_apex/alias3.@type=mgnl:contentNode\n" +
            "/home/section/article/mgnl_apex/alias3.name=c\n" +
            "/home/section/article/mgnl_apex/alias3.locale=de\n" +
            "/home/section/article/mgnl_apex/alias4.@type=mgnl:contentNode\n" +
            "/home/section/article/mgnl_apex/alias4.locale=de\n" +
            "/home/section/article/mgnl_apex/alias4.name=dec\n" +
            "/home/section/article/mgnl_apex/alias5.@type=mgnl:contentNode\n" +
            "/home/section/article/mgnl_apex/alias5.locale=de\n" +
            "/home/section/article/mgnl_apex/alias5.name=deutsch\n" +
            "/home/section/article/mgnl_apex/alias6.@type=mgnl:contentNode\n" +
            "/home/section/article/mgnl_apex/alias6.locale=en\n" +
            "/home/section/article/mgnl_apex/alias6.name=english\n" +
            "/home/section2/article.@type=mgnl:page\n" +
            "/home/section2/article/mgnl_apex.@type=mgnl:apex\n" +
            "/home/section2/article/mgnl_apex/alias.@type=mgnl:contentNode\n" +
            "/home/section2/article/mgnl_apex/alias.name=c\n";
    private Node catNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Session session = MgnlContext.getInstance().getJCRSession("website");
        // register node type
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate type = NodeTypeTemplateUtil.createSimpleNodeType(nodeTypeManager, "mgnl:apex",
                Arrays.asList(NodeType.NT_HIERARCHY_NODE, NodeType.MIX_REFERENCEABLE, NodeTypes.Created.NAME, NodeTypes.Activatable.NAME, NodeTypes.LastModified.NAME, NodeTypes.Renderable.NAME));
        nodeTypeManager.registerNodeType(type, true);
        session.save();

        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(rootTree));
        session.save();

        // silly but can't use : in name in util
        session.move("/home/mgnl_apex", "/home/mgnl:apex");
        session.move("/home/section/mgnl_apex", "/home/section/mgnl:apex");
        session.move("/home/section2/mgnl_apex", "/home/section2/mgnl:apex");
        session.move("/home/section/article/mgnl_apex", "/home/section/article/mgnl:apex");
        session.move("/home/section2/article/mgnl_apex", "/home/section2/article/mgnl:apex");
        session.save();

        Components.getSingleton(RepositoryManager.class).createWorkspace("magnolia", "category");
        Session catSession = MgnlContext.getInstance().getJCRSession("category");
        // register node type
        NodeTypeManager catNodeTypeManager = catSession.getWorkspace().getNodeTypeManager();
        type = NodeTypeTemplateUtil.createSimpleNodeType(nodeTypeManager, "category", Arrays.asList(NodeType.NT_HIERARCHY_NODE, NodeType.MIX_REFERENCEABLE, NodeTypes.Created.NAME, NodeTypes.Activatable.NAME, NodeTypes.LastModified.NAME, NodeTypes.Renderable.NAME));
        nodeTypeManager.registerNodeType(type, true);

        catNode = catSession.getRootNode().addNode("foo", "category");
        catNode.setProperty("name", "myCategory");
        catSession.save();
        MgnlContext.setLocale(null);
    }

    /**
     * jsonfn.with(content).produce()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testPrintAll() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JSONBuilder.with(session.getNode("/home/section2/article/mgnl:apex")).build();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"mgnl:created\" : "));
        assertThat(json, containsString("\"jcr:created\" : "));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.with(content).expand("baz", "category").produce()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpand() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        String json = JSONBuilder.with(node).expand("baz", "category").build();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.with(content).expand("baz", "category").produce()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandWithPropHidingWrapper() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        node = new JCRMgnlPropertiesFilteringNodeWrapper(node);
        String json = JSONBuilder.with(node).expand("baz", "category").build();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, endsWith("}"));
        System.out.println(json);
    }

    /**
     * jsonfn.with(content).expand("baz", "category").excludeWithRegex(".*:.*").produce()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandAndFilter() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        String json = JSONBuilder.with(node).expand("baz", "category").excludeWithRegex(".*:.*").build();
        assertThat(json, startsWith("{"));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"name\" : \"myCategory\""));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.with(content).expand("baz", "category").excludeWithRegex(".*:.*").produce()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandAndFilterWithChildren() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section/article/mgnl:apex");
        NodeIterator iter = node.getNodes();
        while (iter.hasNext()) {
            iter.nextNode().setProperty("baz", catNode.getIdentifier());
        }
        session.save();
        String json = JSONBuilder.withChildNodesOf(node).expand("baz", "category").excludeWithRegex(".*:.*").build();
        assertThat(json, startsWith("{"));
        assertThat(json, allOf(containsString("\"alias\""), containsString("\"alias2\""), containsString("\"alias3\""), containsString("\"alias4\""), containsString("\"alias5\""), containsString("\"alias6\"")));
        assertThat(json, not(containsString("\"" + node.getIdentifier() + "\"")));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"name\" : \"myCategory\""));
        assertThat(json, endsWith("}"));
        System.out.println(json);
    }

    /**
     * jsonfn.with(content).expand("baz", "category").excludeWithRegex(".*:.*").produce()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandAndExcludeAndFilter() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        String json = JSONBuilder.with(node).expand("baz", "category").excludeAll().butInclude("@id", "name", "baz").build();
        assertThat(json, startsWith("{"));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"name\" : \"myCategory\""));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, endsWith("}"));
        System.out.println(json);
    }

    /**
     * jsonfn.with(content).excludeWithRegex(".*foo").produce()
     *
     * ==> { "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExcludes() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JSONBuilder.with(session.getNode("/home/section2/article/mgnl:apex")).excludeWithRegex("jcr:.*").build();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"mgnl:created\" : "));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.with(content).excludeWithRegex(".*foo", "bar.*").produce()
     *
     * ==> { "a" :"x", b: 1234, ... }
     */
    @Test
    public void testExcludes2() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JSONBuilder.with(session.getNode("/home/section2/article/mgnl:apex")).excludeWithRegex("jcr:.*", "mgnl:.*", "@.*").build();
        assertEquals("{ }", json);
    }

    /**
     * jsonfn.with(content).excludeWithRegex(".*").produce()
     *
     * ==> { }
     */
    @Test
    public void testExcludes3() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JSONBuilder.with(session.getNode("/home/section2/article/mgnl:apex")).excludeWithRegex(".*").build();
        assertEquals("{ }", json);
    }

    /**
     * jsonfn.with(content).excludeAll().butInclude("someProperty").produce()
     *
     * ==> { "someProperty" : "someValue" }
     */
    @Test
    public void testExcludes4() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JSONBuilder.with(session.getNode("/home/section2/article/mgnl:apex")).excludeAll().butInclude("mgnl:createdBy").build();
        assertEquals("{\n" +
                "  \"mgnl:createdBy\" : \"anonymous\"\n" +
                "}", json);
    }


}
