package com.neatresults.mgnltweaks;

import javax.jcr.Node;

import info.magnolia.jcr.util.ContentMap;

public class JsonTemplatingFunctions {

    public JsonTemplatingFunctions() {
    }

    /**
     * Will operate on passed in node.
     */
    public static JsonBuilder with(ContentMap content) {
        return with(content.getJCRNode());
    }

    /**
     * Will operate on passed in node.
     */
    public static JsonBuilder with(Node node) {
        JsonBuilder foo = new JsonBuilder();
        foo.setNode(node);
        return foo;
    }

    /**
     * Will skip current node, but iterate over all children of it instead.
     */
    public static JsonBuilder withChildNodesOf(ContentMap content) {
        return withChildNodesOf(content.getJCRNode());
    }

    /**
     * Will skip current node, but iterate over all children of it instead.
     */
    public static JsonBuilder withChildNodesOf(Node node) {
        JsonBuilder foo = new JsonBuilder();
        foo.setNode(node);
        foo.setChildrenOnly(true);
        return foo;
    }

}
