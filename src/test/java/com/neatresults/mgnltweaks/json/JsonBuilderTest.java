/**
 *
 * Copyright 2015 by Jan Haderka <jan.haderka@neatresults.com>
 *
 * This file is part of neat-tweaks module.
 *
 * Neat-tweaks is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Neat-tweaks is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neat-tweaks.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0 <http://www.gnu.org/licenses/gpl.txt>
 *
 * Should you require distribution under alternative license in order to
 * use neat-tweaks commercially, please contact owner at the address above.
 *
 */
package com.neatresults.mgnltweaks.json;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypeTemplateUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.link.Link;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

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

/**
 * Just a test.
 */
public class JsonBuilderTest extends RepositoryTestCase {

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
     * jsonfn.from(content).add(".*").print()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testPrintAll() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).add(".*").print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"mgnl:created\" : "));
        assertThat(json, containsString("\"jcr:created\" : "));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).add(".*").print()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testPrintMultiple() throws Exception {
        // GIVEN
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("foo", new String[] { "baz", "bar", "boo" });
        // WHEN
        String json = JsonTemplatingFunctions.from(node).add("foo").print();
        // THEN
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"foo\" : [ \"baz\", \"bar\", \"boo\" ]"));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).add("@link").print()
     *
     * ==> { "@link" : "/some/path.html" }
     */
    @Test
    public void testPrintLink() throws Exception {
        // GIVEN
        Session session = MgnlContext.getInstance().getJCRSession("website");
        URI2RepositoryManager man = mock(URI2RepositoryManager.class);
        when(man.getURI(any(Link.class))).thenReturn("/Foo/Link/Test.html");
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, man);
        I18nContentSupport i18n = mock(I18nContentSupport.class);
        when(i18n.toI18NURI(any(String.class))).thenReturn("/Foo/Link/Test.html");
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18n);
        // WHEN
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).add("@link").print();
        // THEN
        assertThat(json, startsWith("{"));
        // starts w/ null, because we don't set any context path
        assertThat(json, containsString("\"@link\" : \"null/Foo/Link/Test.html\""));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").print()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpand() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        String json = JsonTemplatingFunctions.from(node).expand("baz", "category").add("@id").print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").print()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandMultivalue() throws Exception {
        // GIVEN
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        Session catSession = catNode.getSession();
        catSession.getWorkspace().copy(catNode.getPath(), "/othercat");
        catSession.save();
        Node catNode2 = catSession.getNode("/othercat");
        node.setProperty("baz", new String[] { catNode.getIdentifier(), catNode2.getIdentifier() });
        node.save();
        // WHEN
        String json = JsonTemplatingFunctions.from(node).expand("baz", "category").add("@id").print();
        // THEN
        assertThat(json, startsWith("{"));
        // [{ == array of props ;)
        assertThat(json, containsString("\"baz\" : [ {"));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, containsString("" + catNode2.getIdentifier()));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").print()
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
        String json = JsonTemplatingFunctions.from(node).expand("baz", "category").add("@id").print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").exclude(".*:.*").print()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandAndFilter() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        String json = JsonTemplatingFunctions.from(node).expand("baz", "category").add("name").print();
        assertThat(json, startsWith("{"));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"name\" : \"myCategory\""));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").exclude(".*:.*").print()
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
        String json = JsonTemplatingFunctions.fromChildNodesOf(node).expand("baz", "category").add("@name").add("name").print();
        assertThat(json, startsWith("["));
        assertThat(json, allOf(containsString("\"alias\""), containsString("\"alias2\""), containsString("\"alias3\""), containsString("\"alias4\""), containsString("\"alias5\""), containsString("\"alias6\"")));
        assertThat(json, not(containsString("\"" + node.getIdentifier() + "\"")));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"name\" : \"myCategory\""));
        assertThat(json, endsWith("]"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").down(4).print()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandAndFilterAndRepeatWithChildren() throws Exception {
        // GIVEN
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section/article/mgnl:apex");
        NodeIterator iter = node.getNodes();
        while (iter.hasNext()) {
            iter.nextNode().setProperty("baz", catNode.getIdentifier());
        }
        node.getNode("alias2").addNode("level3", "mgnl:contentNode").addNode("level4", "mgnl:contentNode").addNode("level5", "mgnl:contentNode");
        session.save();
        // WHEN
        String json = JsonTemplatingFunctions.fromChildNodesOf(node).expand("baz", "category").down(3).add("@name").print();

        // THEN
        assertThat(json, startsWith("["));
        assertThat(json, allOf(containsString("\"alias\""), containsString("\"alias2\""), containsString("\"alias3\""), containsString("\"alias4\""), containsString("\"alias5\""), containsString("\"alias6\"")));
        assertThat(json, not(containsString("\"" + node.getIdentifier() + "\"")));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"@name\" : \"foo\""));
        assertThat(json, containsString("\"@name\" : \"level4\""));
        assertThat(json, not(containsString("\"@name\" : \"level5\"")));
        assertThat(json, endsWith("]"));
    }

    /**
     * jsonfn.from(content).expand("baz", "category").exclude(".*:.*").print()
     *
     * ==> { "foo" : "hahaha", "baz" : {"identifier" : "1234-123456-1234", "name" : "cat1"}, b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExpandAndExcludeAndFilter() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("baz", catNode.getIdentifier());
        node.save();
        String json = JsonTemplatingFunctions.from(node).expand("baz", "category").add("@id", "name", "baz").print();
        assertThat(json, startsWith("{"));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, containsString("\"baz\" : {"));
        assertThat(json, containsString("\"name\" : \"myCategory\""));
        assertThat(json, containsString("" + catNode.getIdentifier()));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).exclude(".*foo").print()
     *
     * ==> { "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExcludes() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).add("mgnl:.*").print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"mgnl:created\" : "));
        assertThat(json, not(containsString("\"jcr:created\" : ")));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).exclude(".*foo", "bar.*").print()
     *
     * ==> { "a" :"x", b: 1234, ... }
     */
    @Test
    public void testExcludes2() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).add(".*").exclude("jcr:.*", "mgnl:.*", "@.*").print();
        assertEquals("{ }", json);
    }

    /**
     * jsonfn.from(content).exclude(".*").print()
     *
     * ==> { }
     */
    @Test
    public void testExcludes3() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).exclude(".*").print();
        assertEquals("{ }", json);
    }

    /**
     * jsonfn.from(content).add("someProperty").print()
     *
     * ==> { "someProperty" : "someValue" }
     */
    @Test
    public void testExcludes4() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).add("mgnl:createdBy").print();
        assertEquals("{\n" +
                "  \"mgnl:createdBy\" : \"anonymous\"\n" +
                "}", json);
    }

    /**
     * jsonfn.from(content).add(".*").print()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testPrintAllWithAppend() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).down(2).add("name").print();
        json = JsonTemplatingFunctions.appendFrom(json, session.getNode("/home/mgnl:apex")).down(2).add("name").print();
        assertThat(json, startsWith("["));
        assertThat(json, containsString("\"name\" : \"c\""));
        assertThat(json, containsString("\"name\" : \"a\""));
        assertThat(json, endsWith("]"));
    }

    /**
     * to output all in single line:
     *
     * jsonfn.from(content).add(".*").inline().print()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testPrintInline() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).down(2).add("name").inline().print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"name\":\"c\""));
        assertThat(json, endsWith("}"));
        assertThat(json, not(containsString("\n")));
    }

    /**
     * to output all stuff from root, jcr:xxx node types should be skipped:
     *
     * jsonfn.from(root).add(".*").print()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testExcludeSystemNodes() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.fromChildNodesOf(session.getNode("/")).down(2).add("@name").inline().print();
        assertThat(json, startsWith("["));
        assertThat(json, containsString("\"@name\":\"home\""));
        assertThat(json, endsWith("]"));
        assertThat(json, not(containsString("jcr:system")));
    }

    /**
     * jsonfn.from(content).add(".*").inline().print()
     *
     * ==> { "foo" : "that\\'s it", ... }
     */
    @Test
    public void testPrintDoubleQuoted() throws Exception {
        // GIVEN
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("escape", "that\"s it");
        String json = JsonTemplatingFunctions.from(node).add("escape").inline().print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"escape\":\"that\\\\\"s it\""));
        assertThat(json, endsWith("}"));
    }

    /**
     * jsonfn.from(content).add(".*").inline().print()
     *
     * ==> { "foo" : "that\\'s it", ... }
     */
    @Test
    public void testPrintQuoted() throws Exception {
        // GIVEN
        Session session = MgnlContext.getInstance().getJCRSession("website");
        Node node = session.getNode("/home/section2/article/mgnl:apex");
        node.setProperty("escape", "that\"s it");
        String json = JsonTemplatingFunctions.from(node).add("escape").inline().print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"escape\":\"that\\\\\"s it\""));
        assertThat(json, endsWith("}"));
    }

    /**
     * to output all stuff from root, jcr:xxx node types should be skipped:
     *
     * jsonfn.from(root).add(".*").print()
     *
     * ==> { "foo" : "hahaha", "a" :"x", b: 1234, "bar" : "meh", ... }
     */
    @Test
    public void testLimitByTypesOfNodes() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.fromChildNodesOf(session.getNode("/")).down(2).allowOnlyNodeTypes("rep:system").add("@name").inline().print();
        assertThat(json, startsWith("["));
        assertThat(json, not(containsString("\"@name\":\"home\"")));
        assertThat(json, endsWith("]"));
        assertThat(json, containsString("jcr:system"));
    }

    /**
     * Mask chars not friendly to your js framework in names of objects and properties jsonfn.from(content).add(".*").maskChars(":","_").print()
     *
     * ==> { "a" :"x", b: 1234, "b_r" : "property called b:r", ... }
     */
    @Test
    public void testMaskChars() throws Exception {
        Session session = MgnlContext.getInstance().getJCRSession("website");
        String json = JsonTemplatingFunctions.from(session.getNode("/home/section2/article/mgnl:apex")).add("mgnl:.*").add("@name").maskChar(':', '_').print();
        assertThat(json, startsWith("{"));
        assertThat(json, containsString("\"mgnl_created\" : "));
        assertThat(json, not(containsString("\"mgnl:created\" : ")));
        assertThat(json, endsWith("}"));
    }

}
