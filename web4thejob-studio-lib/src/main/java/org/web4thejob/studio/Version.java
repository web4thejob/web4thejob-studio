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

package org.web4thejob.studio;

import org.zkoss.util.resource.Locators;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Veniamin Isaias on 5/8/2014.
 */
public class Version {
    public static String getVersion() {

        try (InputStream input = Locators.getDefault().getResourceAsStream("org/web4thejob/studio/version.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("application.version");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }


}
