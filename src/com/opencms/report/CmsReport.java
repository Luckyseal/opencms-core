/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/report/Attic/CmsReport.java,v $
* Date   : $Date: 2002/12/04 18:32:38 $
* Version: $Revision: 1.4 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.report;

import java.util.*;
import com.opencms.linkmanagement.*;

/**
 * Title:        OpenCms
 * Description: The report object for the workplace. It stores all information till
 *              they are retrieved.
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsReport implements I_CmsReport{

    /**
     * the report objects i.e. String, CmsPageLink, ...
     */
    private Vector m_content;

    /**
     * this vector stores the type of the objects in the content vector.
     */
    private Vector m_type;

    /**
     * counter to remember what is already shown. Indicates the next index
     * of the content and type vector that has to be reported.
     */
    private int m_indexNext;

    /**
     * the seperator.
     */
    private String[] m_seperator = null;

    /**
     * the standard seperator
     */
    public static final String C_STANDARD_SEPERATOR = "\n\n\n";

    /**
     * the diffrent types supported by this class.
     */
    public static final Integer C_TYPE_STRING = new Integer(0);
    public static final Integer C_TYPE_PAGELINKS = new Integer(1);
    public static final Integer C_TYPE_SEPERATOR = new Integer(2);

    /**
     * constructor
     */
    public CmsReport() {
        m_content = new Vector();
        m_type = new Vector();
        m_indexNext = 0;
        m_seperator = new String[]{"\n", "  ", "\n", "\n"};
    }

    /**
     * constructor
     * @param seperator A String Array that is used to seperate entrys in the report
     */
    public CmsReport(String[] seperator){
        m_seperator = seperator;
        m_content = new Vector();
        m_type = new Vector();
        m_indexNext = 0;
    }

    /**
     * adds a predefined seperator.
     * @param int says which seperator should be used.
     */
    public void addSeperator(int sepNumber){
        m_type.add(C_TYPE_SEPERATOR);
        if((m_seperator != null)&&(m_seperator.length > sepNumber)){
            m_content.add(m_seperator[sepNumber]);
        }else{
            m_content.add(C_STANDARD_SEPERATOR);
        }
    }

    /**
     * adds the standard seperator.
     */
    public void addSeperator(){
        m_type.add(C_TYPE_SEPERATOR);
        m_content.add(C_STANDARD_SEPERATOR);
    }

    /**
     * adds a new object to the report: String
     * @param String the new reportable Object.
     */
    public void addString(String value){
        m_type.add(C_TYPE_STRING);
        // replace all " with ' (otherwise the generated JavaScript will not work)
        value = value.replace('\"', '\'');
        // now exchange all linefeeds with <br> (otherwise the generated JavaScript will not work)
        // this is esp. required for exception stack traces that might be added here
        StringTokenizer tok = new StringTokenizer(value, "\r\n");
        String brValue = "";
        while (tok.hasMoreTokens()) {
            brValue += tok.nextToken();
            if (tok.hasMoreTokens()) brValue += "<br>";
        }        
        m_content.add(brValue);
    }

    /**
     * adds a new object to the report: CmsPageLinks
     * @param CmsPageLinks.
     */
    public void addPageLinks(CmsPageLinks value){
        m_type.add(C_TYPE_PAGELINKS);
        m_content.add(value);
    }

    /**
     * says if this report contains any page link objects. Used for the linkcheck:
     * if any broken links in the project then the report contains pagelinks.
     */
    public boolean containsPageLinks(){
        return m_type.contains(C_TYPE_PAGELINKS);
    }

    /**
     * Generates the report from nextIndex to end and sets the nextIndex to the end.
     */
    public String getReportUpdate(){
        StringBuffer result = new StringBuffer();
        int indexEnd = m_content.size();
        for(int i=m_indexNext; i<indexEnd; i++){
            Integer curType = (Integer)m_type.elementAt(i);
            if(curType.equals(C_TYPE_PAGELINKS)){
                CmsPageLinks links = (CmsPageLinks)m_content.elementAt(i);
                result.append(m_seperator[2] + links.getResourceName() + m_seperator[3] + m_seperator[0]);
                for(int index=0; index<links.getLinkTargets().size(); index++){
                    result.append(m_seperator[1]+(String)links.getLinkTargets().elementAt(index)+ m_seperator[0]);
                }
                result.append(m_seperator[0]);
            }else if(curType.equals(C_TYPE_STRING)){
                result.append((String)m_content.elementAt(i));
            }else if(curType.equals(C_TYPE_SEPERATOR)){
                result.append((String)m_content.elementAt(i));
            }
        }
        m_indexNext = indexEnd;

        return result.toString();
    }

}