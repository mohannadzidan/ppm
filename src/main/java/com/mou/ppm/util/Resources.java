package com.mou.ppm.util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mou.ppm.Ppm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public final class Resources {
    private Resources() {
    }

    public static String getString(String resourceName) {
        try {
            var stream =getStream(resourceName);
            if(stream == null)return null;
            return new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getStream(String resourceName) {
        return Ppm.class.getClassLoader().getResourceAsStream(resourceName);
    }

}
