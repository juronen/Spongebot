package org.spongebot.loader.updater.imported;

import org.znu.core.structures.export.CompactClass;
import org.znu.core.structures.export.CompactField;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Reader {

    private File directory;

    public Reader(File directory) {
        this.directory = directory;
    }

    private CompactClass readSingle(InputStream stream) {
        try {
            ObjectInputStream ois = new ObjectInputStream(stream);
            CompactClass rsClass = (CompactClass) ois.readObject();
            ois.close();
            stream.close();
            System.out.println("Read: " + rsClass.getBotClass() + " = " + rsClass.getRsClass());
            for (Map.Entry<String, CompactField> e : rsClass.getFields().entrySet()) {
                CompactField field = e.getValue();
                System.out.println(">> " + e.getKey() + " " + field.getName() + " " + field.getDescriptor() + " " + field.getMultiplier());
            }
            return rsClass;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LinkedHashMap<String, CompactClass> readDir() {
        LinkedHashMap<String, CompactClass> classes = new LinkedHashMap();
        File file = directory;
        for (File f : file.listFiles()) {
            try {
                classes.put(f.getName().split("\\.")[0], readSingle(new FileInputStream(f)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }


}
