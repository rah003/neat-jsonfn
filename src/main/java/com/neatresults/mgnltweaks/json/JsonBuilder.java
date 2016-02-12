/**
 *
 * Copyright 2015-2016 by Jan Haderka <jan.haderka@neatresults.com>
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

import static com.neatresults.Java8Util.asNodeStream;
import static com.neatresults.Java8Util.asPropertyStream;
import static com.neatresults.Java8Util.getName;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.neatresults.PredicateSplitterConsumer;

/**
 * Builder class for converting JCR nodes into json ... with few little extras :D .
 */
public class JsonBuilder implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(JsonBuilder.class);
    private static final Pattern ESCAPES = Pattern.compile("\\\\");

    private ObjectMapper mapper = new ObjectMapper();
    private Node node;
    private List<String> regexExcludes = new LinkedList<String>();
    private List<String> butInclude = new LinkedList<String>();
    private Map<String, String> expands = new HashMap<String, String>();

    private boolean childrenOnly;

    private int level;

    private LinkedList<String> renditions = new LinkedList<String>();

    private String preexisingJson;

    private boolean inline;

    private boolean wrapForI18n;

    private String allowedNodeTypes = "^(?!rep:).*$";


    protected JsonBuilder() {
    }

    protected void setNode(Node node) {
        this.node = node;
    }

    protected void setChildrenOnly(boolean childrenOnly) {
        this.childrenOnly = childrenOnly;
    }

    /**
     * Will expand id into sub array.
     *
     * @param propertyName
     *            property to expand.
     * @param repository
     *            repository in which to look for node matching the id specified in the property.
     */
    public JsonBuilder expand(String propertyName, String repository) {
        this.expands.put(propertyName, repository);
        this.butInclude.add(propertyName);
        return this;
    }

    /**
     * Will attempt to retrieve rendition specific link for all generated links. Links will be available as "@rendition_name" : "link-or-null"
     *
     * @param variation
     *            rendition to create link for.
     */
    public JsonBuilder binaryLinkRendition(String... variation) {
        this.renditions.addAll(Arrays.asList(variation));
        return this;
    }

    /**
     * Excludes properties matching provided regex.
     */
    public JsonBuilder exclude(String... string) {
        this.regexExcludes.addAll(Arrays.asList(string));
        return this;
    }

    /**
     * will expand children of current node number of levels down.
     */
    public JsonBuilder down(int level) {
        // one for zero based index, and one for not including the number passed in
        this.level = level - 1;
        return this;
    }

    public JsonBuilder wrapForI18n() {
        this.wrapForI18n = true;
        return this;
    }

    public JsonBuilder inline() {
        this.inline = true;
        return this;
    }

    /**
     * Includes only specified properties. Use together with excludeAll().
     */
    public JsonBuilder add(String... string) {
        this.butInclude.addAll(Arrays.asList(string));
        return this;
    }

    /**
     * Includes only specified properties. Use together with excludeAll().
     */
    public JsonBuilder addAll() {
        this.butInclude.add(".*");
        return this;
    }

    /**
     * Executes configured chain of operations and produces the json output.
     */
    public String print() {
        ObjectWriter ow = mapper.writer();
        if (!inline) {
            ow = ow.withDefaultPrettyPrinter();
        }

        if (wrapForI18n) {
            node = new I18nNodeWrapper(node);
        }
        try {
            String json;
            if (childrenOnly) {
                Collection<EntryableContentMap> childNodes = new LinkedList<EntryableContentMap>();
                NodeIterator nodes = this.node.getNodes();
                asNodeStream(nodes).filter(this::isAllowedNodeType).map(this::cloneWith).forEach(builder -> childNodes.add(new EntryableContentMap(builder)));
                json = ow.writeValueAsString(childNodes);
            } else {
                json = ow.writeValueAsString(new EntryableContentMap(this));
            }

            if (StringUtils.isNotEmpty(preexisingJson)) {
                if (preexisingJson.trim().endsWith("}")) {
                    json = "[" + preexisingJson + "," + json + "]";
                } else if (preexisingJson.trim().endsWith("]")) {
                    json = StringUtils.substringBeforeLast(preexisingJson, "]") + "," + json + "]";
                }
            }
            json = ESCAPES.matcher(json).replaceAll("\\\\\\\\");
            return json;
        } catch (JsonProcessingException | RepositoryException e) {
            // ignore
        }

        return "{ }";
    }

    private boolean isAllowedNodeType(Node n) {
        try {
            return n != null && n.getPrimaryNodeType().getName().matches(allowedNodeTypes);
        } catch (RepositoryException e) {
            // when failing to check because of the repo issue, assume node is fine.
            log.error(e.getMessage(), e);
            return true;
        } catch (PatternSyntaxException e) {
            // when failing due to broken pattern, leave result empty to alert dev to broken pattern.
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private JsonBuilder cloneWith(Node n) {
        JsonBuilder clone = clone();
        clone.node = n;
        return clone;
    }

    private JsonBuilder cloneLevelDown(Node n) {
        JsonBuilder clone = clone();
        clone.node = n;
        clone.level--;
        return clone;
    }

    @Override
    protected JsonBuilder clone() {
        try {
            return (JsonBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pimped up ContentMap that is Jackson friendly (supports entrySet() method).
     */
    public static class EntryableContentMap extends ContentMap {

        /**
         * Represents getters of the node itself.
         */
        private final Map<String, Method> specialProperties = new HashMap<String, Method>();

        private JsonBuilder config;

        public EntryableContentMap(JsonBuilder builder) {
            super(builder.node);
            this.config = builder;

            Class<? extends Node> clazz = builder.node.getClass();
            try {
                specialProperties.put("@name", clazz.getMethod("getName", (Class<?>[]) null));
                specialProperties.put("@id", clazz.getMethod("getIdentifier", (Class<?>[]) null));
                specialProperties.put("@path", clazz.getMethod("getPath", (Class<?>[]) null));
                specialProperties.put("@depth", clazz.getMethod("getDepth", (Class<?>[]) null));
                specialProperties.put("@nodeType", clazz.getMethod("getPrimaryNodeType", (Class<?>[]) null));
                specialProperties.put("@link", LinkUtil.class.getMethod("createAbsoluteLink", Node.class));
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
                stream = asPropertyStream(properties)
                        .map(prop -> getName(prop))
                        .filter(name -> matchesRegex(name, config.butInclude))
                        .filter(name -> !matchesRegex(name, config.regexExcludes));

                // do not try to include binary data since we don't try to encode them either and jackson just blows w/o that
                stream.filter(name -> PropertyUtil.getJCRPropertyType(PropertyUtil.getPropertyValueObject(node, name)) != PropertyType.BINARY)
                .forEach(new PredicateSplitterConsumer<String>(config.expands::containsKey,
                        expandableProperty -> props.put(expandableProperty, expand(expandableProperty, node)),
                        flatProperty -> props.put(flatProperty, PropertyUtil.getPropertyValueObject(node, flatProperty))));

                Stream<Entry<String, Method>> specialStream;
                specialStream = specialProperties.entrySet().stream()
                        .filter(entry -> matchesRegex(entry.getKey(), config.butInclude))
                        .filter(entry -> !matchesRegex(entry.getKey(), config.regexExcludes));
                specialStream.forEach(entry -> props.put(entry.getKey(), invoke(entry.getValue(), node)));
                if (config.level > 0) {
                    asNodeStream(node.getNodes())
                    .map(config::cloneLevelDown)
                    .forEach(builder -> props.put(getName(builder.node), new EntryableContentMap(builder)));
                }

                if (node.getPrimaryNodeType().getName().equals("mgnl:asset")) {
                    config.renditions.stream().forEach(rendition -> props.put("@rendition_" + rendition, generateRenditionLink(rendition, node)));
                }
            } catch (RepositoryException e) {
                // ignore and return empty map
                e.printStackTrace();
            }
            return props.entrySet();
        }

        private Object generateRenditionLink(String rendition, Node node) {
            try {
                Class<?> clazz = Class.forName("info.magnolia.dam.templating.functions.DamTemplatingFunctions");
                Method dammethod = clazz.getMethod("getAssetLink", String.class, String.class);
                Object damfn = Components.newInstance(clazz);
                return dammethod.invoke(damfn, "jcr:" + node.getIdentifier(), rendition);
            } catch (RepositoryException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // bad luck we handle it the usual way
                e.printStackTrace();
            }
            return null;
        }

        private Object invoke(Method method, Node node) {
            try {
                try {
                    if (method.getName().equals("createAbsoluteLink") && node.getPrimaryNodeType().getName().equals("mgnl:asset")) {
                        Class<?> clazz = this.getClass().forName("info.magnolia.dam.templating.functions.DamTemplatingFunctions");
                        Method dammethod = clazz.getMethod("getAssetLink", String.class);
                        Object damfn = clazz.newInstance();
                        return dammethod.invoke(damfn, "jcr:" + node.getIdentifier());

                    }
                } catch (RepositoryException | ClassNotFoundException | InstantiationException | NoSuchMethodException | SecurityException e) {
                    // bad luck we handle it the usual way
                }
                if (method.getParameterCount() > 0) {
                    return method.invoke(null, node);
                }
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
            final String workspace = config.expands.get(expandableProperty);
            try {
                Property property = node.getProperty(expandableProperty);
                if (property.isMultiple()) {
                    List<String> expandables = PropertyUtil.getValuesStringList(property.getValues());
                    return expandables.stream()
                            .map(expandable -> expandSingle(expandable, workspace))
                            .collect(Collectors.toList());
                } else {
                    String expandable = PropertyUtil.getValueString(property);
                    return expandSingle(expandable, workspace);
                }

            } catch (RepositoryException e) {
                log.debug(e.getMessage(), e);
            }

            return expanded;
        }

        private EntryableContentMap expandSingle(String expandable, String workspace) {
            if (expandable == null) {
                return null;
            }
            if (expandable.startsWith("jcr:")) {
                expandable = StringUtils.removeStart(expandable, "jcr:");
            }
            Node expandedNode;
            try {
                Session session = MgnlContext.getJCRSession(workspace);
                if (expandable.startsWith("/")) {
                    expandedNode = session.getNode(expandable);
                } else {
                    expandedNode = session.getNodeByIdentifier(expandable);
                }
                JsonBuilder builder = config.clone();
                if (builder.wrapForI18n) {
                    expandedNode = new I18nNodeWrapper(expandedNode);
                }
                builder.setNode(expandedNode);
                return new EntryableContentMap(builder);
            } catch (RepositoryException e) {
                log.debug(e.getMessage(), e);
                return null;
            }
        }

        private boolean matchesRegex(String test, List<String> regexList) {
            return !regexList.stream().noneMatch(regex -> test.matches(regex));
        }
    }

    public void setJson(String json) {
        this.preexisingJson = json;
    }
}
