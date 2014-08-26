/*
 * Copyright 2014 Veniamin Isaias
 *
 * This file is part of Web4thejob Studio.
 *
 * Web4thejob Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Web4thejob Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Web4thejob Studio.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.web4thejob.studio.support;

import org.zkoss.io.Files;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class FileUtils {

    public static List<String> readAllLines(InputStream is) {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        List<String> list = new ArrayList<>();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(new Error());
        } finally {
            Files.close(is);
        }
        return list;
    }

    public static List<String> readAllLines(Reader reader) {
        BufferedReader r = new BufferedReader(reader);
        List<String> list = new ArrayList<>();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(new Error());
        } finally {
            Files.close(reader);
        }
        return list;
    }

    public static void writeStringToFile(File file, String data, String encoding) throws IOException {
        OutputStream output = null;
        try {
            output = openOutputStream(file);
            write(data, output, encoding);
        } finally {
            Files.close(output);
        }

    }

    private static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }

    private static void write(String data, OutputStream output, String encoding)
            throws IOException {
        if (data != null) {
            if (encoding == null) {
                write(data, output);
            } else {
                output.write(data.getBytes(encoding));
            }
        }
    }

    private static void write(String data, OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data.getBytes());
        }
    }

}
