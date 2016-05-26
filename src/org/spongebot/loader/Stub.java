package org.spongebot.loader;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Credits to Dx2
public class Stub implements AppletStub {

    private Map<String, String> parameters = new HashMap();

    private URL documentBase;

    private URL codeBase;

    private static final Pattern PARAMETER_PATTERN = Pattern.compile("<param name=\"([^\\s]+)\"\\s+value=\"([^>]*)\">");
    private static final Pattern ARCHIVE_PATTERN = Pattern.compile("archive=(.*?)\\.jar");

    public static Map<String, String> getParameters(URL url) throws IOException {
        String source;
        try (InputStream in = url.openStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            source = new String(out.toByteArray());
        }

        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(source);
        while (matcher.find()) {
            parameters.put(matcher.group(1), matcher.group(2));
        }
        matcher = ARCHIVE_PATTERN.matcher(source);
        if (!matcher.find()) {
            // do something about it
        }
        parameters.put("_archive", matcher.group(1));
        return parameters;
    }


    public Stub() {
        try {
            parameters = getParameters(new URL("http://oldschool45.runescape.com"));
            documentBase = new URL("http://oldschool45.runescape.com");
            codeBase = new URL("http://oldschool45.runescape.com");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        return documentBase;
    }

    @Override
    public URL getCodeBase() {
        return codeBase;
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public AppletContext getAppletContext() {
        return null;
    }

    @Override
    public void appletResize(int width, int height) {

    }
}
