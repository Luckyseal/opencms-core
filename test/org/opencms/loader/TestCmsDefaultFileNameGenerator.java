/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/loader/TestCmsDefaultFileNameGenerator.java,v $
 * Date   : $Date: 2011/05/13 13:30:14 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests the default file name generation.<p>
 */
public class TestCmsDefaultFileNameGenerator extends TestCase {

    /** List of names with 5 digits. */
    public static final List<String> NAMES_5 = Arrays.asList(new String[] {"/file_00001.xml", "/file_00002.xml"});

    /** List of names with 4 digits. */
    public static final List<String> NAMES_4 = Arrays.asList(new String[] {"/file_0001.xml", "/file_0002.xml"});

    /**
     * Tests the default file name generation.<p>
     * 
     * @throws Exception in case the test fails
     */
    public void testFileNumbering() throws Exception {

        CmsDefaultFileNameGenerator defaultGenerator = new CmsDefaultFileNameGenerator();

        String next5Name = defaultGenerator.getNewFileNameFromList(NAMES_5, "/file_%(number).xml", 5);
        assertEquals("/file_00003.xml", next5Name);

        String next4Name = defaultGenerator.getNewFileNameFromList(NAMES_4, "/file_%(number).xml", 4);
        assertEquals("/file_0003.xml", next4Name);

        String next45Name = defaultGenerator.getNewFileNameFromList(NAMES_4, "/file_%(number:4).xml", 5);
        assertEquals("/file_0003.xml", next45Name);

        String next54Name = defaultGenerator.getNewFileNameFromList(NAMES_5, "/file_%(number:5).xml", 4);
        assertEquals("/file_00003.xml", next54Name);

        String nextEmptyName = defaultGenerator.getNewFileNameFromList(
            Collections.<String> emptyList(),
            "/file_%(number:1).xml",
            4);
        assertEquals("/file_1.xml", nextEmptyName);

        String next9Name = defaultGenerator.getNewFileNameFromList(NAMES_5, "/file_%(number:9).xml", 4);
        assertEquals("/file_000000001.xml", next9Name);
    }
}