package org.znu.core.structures.export;

import java.io.Serializable;

public class CompactField implements Serializable {

    private static final long serialVersionUID = 4002027759000908502L;

    private String name;
    private String descriptor;
    private int multiplier;

    public String getDescriptor() {
        return descriptor;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public String getName() {
        return name;
    }

    public CompactField(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public CompactField(String name, String descriptor, int multiplier) {
        this.name = name;
        this.descriptor = descriptor;
        this.multiplier = multiplier;
    }
}
