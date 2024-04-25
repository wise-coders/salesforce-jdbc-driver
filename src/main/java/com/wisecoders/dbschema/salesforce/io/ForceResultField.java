package com.wisecoders.dbschema.salesforce.io;

/**
 * Licensed under <a href="https://creativecommons.org/licenses/by-nd/4.0/">CC BY-ND 4.0 DEED</a>, copyright <a href="https://wisecoders.com">Wise Coders GmbH</a>, used by <a href="https://dbschema.com">DbSchema Database Designer</a>.
 * Code modifications allowed only as pull requests to the <a href="https://github.com/wise-coders/salesforce-jdbc-driver">public GIT repository</a>.
 */
public class ForceResultField {

    private final String entityType;
    private final String name;
    private Object value;
    private final String fieldType;

    public ForceResultField(String entityType, String fieldType, String name, Object value) {

        super();
        this.entityType = entityType;
        this.name = name;
        this.value = value;
        this.fieldType = fieldType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getFullName() {
        return entityType != null ? entityType + "." + name : name;
    }

    @Override
    public String toString() {
        return name + "=" + value + ( entityType != null ? " [entityType=" + entityType : "");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ForceResultField other = (ForceResultField) obj;
        if (entityType == null) {
            if (other.entityType != null)
                return false;
        } else if (!entityType.equals(other.entityType))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            return other.value == null;
        } else return value.equals(other.value);
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
