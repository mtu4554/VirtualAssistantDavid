package com.david.notify.davidnotifyme.edupage;

public abstract class EdupageSerializable {


    abstract public String serialize();

    abstract public EdupageSerializable init(String[] data);


    abstract public String getId();

    abstract public String getName();

    abstract public int getParameterCount();
}
