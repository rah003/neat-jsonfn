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

import info.magnolia.jcr.util.ContentMap;

import javax.jcr.Node;

/**
 * Templating Functions to expose json builder.
 */
public class JsonTemplatingFunctions {

    public JsonTemplatingFunctions() {
    }

    /**
     * Will operate on passed in node.
     */
    public static JsonBuilder from(ContentMap content) {
        return from(content.getJCRNode());
    }

    /**
     * Will operate on passed in node.
     */
    public static JsonBuilder from(Node node) {
        JsonBuilder foo = new JsonBuilder();
        foo.setNode(node);
        return foo;
    }

    /**
     * Will skip current node, but iterate over all children of it instead.
     */
    public static JsonBuilder fromChildNodesOf(ContentMap content) {
        return fromChildNodesOf(content.getJCRNode());
    }

    /**
     * Will skip current node, but iterate over all children of it instead.
     */
    public static JsonBuilder fromChildNodesOf(Node node) {
        JsonBuilder foo = new JsonBuilder();
        foo.setNode(node);
        foo.setChildrenOnly(true);
        return foo;
    }

    /**
     * Will operate on passed in node and append output to provided json.
     */
    public static JsonBuilder appendFrom(String json, ContentMap content) {
        return appendFrom(json, content.getJCRNode());
    }

    /**
     * Will operate on passed in node.
     */
    public static JsonBuilder appendFrom(String json, Node node) {
        JsonBuilder foo = new JsonBuilder();
        foo.setNode(node);
        foo.setJson(json);
        return foo;
    }

}
