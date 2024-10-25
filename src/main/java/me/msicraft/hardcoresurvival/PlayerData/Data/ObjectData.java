package me.msicraft.hardcoresurvival.PlayerData.Data;

public class ObjectData {

    private final ObjectType objectType;
    private Object object;

    public ObjectData(ObjectType objectType, Object object) {
        this.objectType = objectType;
        this.object = object;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public enum ObjectType {
        STRING, INTEGER, BOOLEAN, DOUBLE, LIST;
    }

}
