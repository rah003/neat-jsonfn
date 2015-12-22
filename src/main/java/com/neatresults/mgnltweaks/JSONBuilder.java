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
package com.neatresults.mgnltweaks;

import static com.neatresults.Java8Util.asPropertyStream;
import static com.neatresults.Java8Util.getName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.neatresults.PredicateSplitterConsumer;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.PropertyUtil;

/**
 * Builder class for convering JCR nodes into json ... with few little extras :D .
 */
public class JSONBuilder implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(JSONBuilder.class);

    private ObjectMapper mapper = new ObjectMapper();
    private ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
    private SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
    private Node node;
    private List<String> regexExcludes = new LinkedList<String>();
    private boolean excludeAll = false;
    private boolean includeAll = false;
    private List<String> butInclude = new LinkedList<String>();
    private Map<String, String> expands = new HashMap<String, String>();

    private JSONBuilder() {
        mapper.registerModule(testModule);
    }

    public JSONBuilder expand(String propertyName, String repository) {
        this.expands.put(propertyName, repository);
        return this;
    }

    public JSONBuilder excludeAll() {
        this.excludeAll = true;
        return this;
    }

    public JSONBuilder excludeWithRegex(String... string) {
        this.regexExcludes.addAll(Arrays.asList(string));
        return this;
    }

    public JSONBuilder butInclude(String... string) {
        this.butInclude.addAll(Arrays.asList(string));
        return this;
    }

    public static JSONBuilder with(Node node) {
        JSONBuilder foo = new JSONBuilder();
        foo.node = node;
        return foo;
    }

    public String build() throws JsonProcessingException, PathNotFoundException, RepositoryException {
        String json = ow.writeValueAsString(new EntryableContentMap(this));
        return json;
    }

    @Override
    protected JSONBuilder clone() {
        try {
            return (JSONBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setNode(Node node) {
        this.node = node;
    }

    /**
     * Pimped up ContentMap that is Jackson friendly (supports entrySet() method).
     */
    public static class EntryableContentMap extends ContentMap {

        /**
         * Represents getters of the node itself.
         */
        private final Map<String, Method> specialProperties = new HashMap<String, Method>();

        private JSONBuilder config;

        public EntryableContentMap(JSONBuilder builder) {
            super(builder.node);
            this.config = builder;

            Class<? extends Node> clazz = builder.node.getClass();
            try {
                specialProperties.put("@name", clazz.getMethod("getName", (Class<?>[]) null));
                specialProperties.put("@id", clazz.getMethod("getIdentifier", (Class<?>[]) null));
                specialProperties.put("@path", clazz.getMethod("getPath", (Class<?>[]) null));
                specialProperties.put("@depth", clazz.getMethod("getDepth", (Class<?>[]) null));
                specialProperties.put("@nodeType", clazz.getMethod("getPrimaryNodeType", (Class<?>[]) null));
            } catch (SecurityException e) {
                log.debug("Failed to gain access to Node get***() method. Check VM security settings. {}", e.getLocalizedMessage(), e);
            } catch (NoSuchMethodException e) {
                log.debug("Failed to retrieve get***() method of Node class. Check the classpath for conflicting version of JCR classes. {}", e.getLocalizedMessage(), e);
            }
        }

        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            Map<String, Object> props = new HashMap<String, Object>();
            PropertyIterator properties;
            try {
                Node node = getJCRNode();
                properties = node.getProperties();
                Stream<String> stream;
                if (config.excludeAll) {
                    stream = asPropertyStream(properties)
                            .map(prop -> getName(prop))
                            .filter(name -> matchesRegex(name, config.butInclude));
                } else {
                    stream = asPropertyStream(properties)
                            .map(prop -> getName(prop))
                            .filter(name -> !matchesRegex(name, config.regexExcludes));
                }
                stream.forEach(new PredicateSplitterConsumer<String>(config.expands::containsKey,
                        expandableProperty -> props.put(expandableProperty, expand(expandableProperty, node)),
                        flatProperty -> props.put(flatProperty, PropertyUtil.getPropertyValueObject(node, flatProperty))));

                Stream<Entry<String, Method>> specialStream;
                if (config.excludeAll) {
                    specialStream = specialProperties.entrySet().stream()
                            .filter(entry -> matchesRegex(entry.getKey(), config.butInclude));
                } else {
                    specialStream = specialProperties.entrySet().stream()
                            .filter(entry -> !matchesRegex(entry.getKey(), config.regexExcludes));
                }
                specialStream.forEach(entry -> props.put(entry.getKey(), invoke(entry.getValue(), node)));

            } catch (RepositoryException e) {
                // ignore and return empty map
            }
            return props.entrySet();
        }

        private Object invoke(Method method, Node node) {
            try {
                Object result = method.invoke(node);
                if (result instanceof NodeType) {
                    return ((NodeType) result).getName();
                } else {
                    return result;
                }

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // ignore
            }
            return null;
        }

        private Object expand(String expandableProperty, Node node) {
            Map<String, Object> expanded = new HashMap<String, Object>();
            String workspace = config.expands.get(expandableProperty);
            try {
                Session session = MgnlContext.getJCRSession(workspace);
                Node expandedNode = session.getNodeByIdentifier(PropertyUtil.getValueString(node.getProperty(expandableProperty)));
                JSONBuilder builder = config.clone();
                builder.setNode(expandedNode);
                return new EntryableContentMap(builder);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }

            return expanded;
        }

        private boolean matchesRegex(String test, List<String> regexList) {
            return !regexList.stream().noneMatch(regex -> test.matches(regex));
        }
    }
}
