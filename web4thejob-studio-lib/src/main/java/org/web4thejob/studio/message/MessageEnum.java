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

package org.web4thejob.studio.message;

/**
 * Created by e36132 on 14/5/2014.
 */
public enum MessageEnum {
    COMPONENT_SELECTED, COMPONENT_ADDED, COMPONENT_DETACHED,
    EVALUATE_XML, XML_EVAL_FAILED, XML_EVAL_SUCCEEDED,
    EVALUATE_ZUL, ZUL_EVAL_FAILED, ZUL_EVAL_SUCCEEDED,
    CODE_CHANGED, ATTRIBUTE_CHANGED,
    SET_BOOKMARK, RESET, RESTORE_BOOKMARK, ZK_PAGE_VISITED, NON_ZK_PAGE_VISITED,
    DESIGNER_ACTIVATED, OUTLINE_ACTIVATED, CODE_ACTIVATED, ZSCRIPT_ADDED,
    DOWNLOAD_REQUESTED

}
