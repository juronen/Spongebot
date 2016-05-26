package org.znu.core.structures.export;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CompactClass implements Serializable {

    private static final long serialVersionUID = 733300398773677701L;

    private String rsClass;
    private String botClass;

    private Map<String, CompactField> fields = new HashMap();

    public String getRsClass() {
        return rsClass;
    }

    public String getBotClass() {
        return botClass;
    }

    public Map<String, CompactField> getFields() {
        return fields;
    }

    public CompactClass(String rsClass, String botClass) {
        this.rsClass = rsClass;
        this.botClass = botClass;
    }

    public void addField(String name, CompactField field) {
        fields.put(name, field);
    }
}

