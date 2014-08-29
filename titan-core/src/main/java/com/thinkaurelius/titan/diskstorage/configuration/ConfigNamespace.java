package com.thinkaurelius.titan.diskstorage.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.thinkaurelius.titan.core.util.ReflectiveConfigOptionLoader;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class ConfigNamespace extends ConfigElement {

    private final boolean isUmbrella;
    private final Map<String,ConfigElement> children = Maps.newHashMap();

    public ConfigNamespace(ConfigNamespace parent, String name, String description, boolean isUmbrella) {
        super(parent,name,description);
        this.isUmbrella=isUmbrella;
    }

    public ConfigNamespace(ConfigNamespace parent, String name, String description) {
        this(parent,name,description,false);
    }

    /**
     * Wether this namespace is an umbrella namespace, that is, is expects immediate sub-namespaces which are user defined.
     * @return
     */
    public boolean isUmbrella() {
        return isUmbrella;
    }

    /**
     * Whether this namespace or any parent namespace is an umbrella namespace.
     * @return
     */
    public boolean hasUmbrella() {
        if (isUmbrella()) return true;
        if (isRoot()) return false;
        return getNamespace().hasUmbrella();
    }

    @Override
    public boolean isOption() {
        return false;
    }

    void registerChild(ConfigElement element) {
        Preconditions.checkNotNull(element);
        Preconditions.checkArgument(element.getNamespace()==this,"Configuration element registered with wrong namespace");
        Preconditions.checkArgument(!children.containsKey(element.getName()),
                "A configuration element with the same name has already been added to this namespace: %s",element.getName());
        children.put(element.getName(),element);
    }

    public Iterable<ConfigElement> getChildren() {
        return children.values();
    }

    public ConfigElement getChild(String name) {

        ConfigElement child = children.get(name);

        if (null == child) {
            // Attempt to load
            ReflectiveConfigOptionLoader.INSTANCE.loadStandard(this.getClass());
            child = children.get(name);
            if (null == child) {
                ReflectiveConfigOptionLoader.INSTANCE.loadAll();
                child = children.get(name);
            }
        }

        return child;
    }

}