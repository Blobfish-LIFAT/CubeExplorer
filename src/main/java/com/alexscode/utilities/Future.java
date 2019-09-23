package com.alexscode.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Reimplementation of useful methods from next versions of java
 */
public final class Future {
    public static int transferTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read = 0, count = 0;
        while ((read = in.read(buffer, 0, 4096)) >= 0) {
            out.write(buffer, 0, read);
            count += read;
        }
        return count;
    }

    /**
     * Utility method to join a list of strings
     * @param strings the strings to join like a list of names
     * @param separator a separator (will not be added at the end) eg ", "
     * @return eg: JP, Alex, PAUL, Pierre
     */
    public static String join(List<String> strings, String separator){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i));
            if (i != strings.size() - 1)
                sb.append(separator);
        }
        return sb.toString();
    }
}
