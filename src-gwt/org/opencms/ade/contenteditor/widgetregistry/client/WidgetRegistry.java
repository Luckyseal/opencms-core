/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor.widgetregistry.client;

import com.alkacon.acacia.client.I_WidgetFactory;

import org.opencms.ade.contenteditor.shared.CmsExternalWidgetConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Command;

/**
 * The widget registry.<p>
 */
public final class WidgetRegistry {

    /** The register widget function name. */
    public static final String REGISTER_WIDGET_FACTORY_FUNCTION = "registerWidgetFactory";

    /** The widget registry instance. */
    private static WidgetRegistry INSTANCE;

    /** The widget registry. */
    private Map<String, I_WidgetFactory> m_widgetRegistry;

    /**
     * Constructor.<p>
     */
    private WidgetRegistry() {

        m_widgetRegistry = new HashMap<String, I_WidgetFactory>();
        exportWidgetRegistration();
    }

    /**
     * Returns the widget registry instance.<p>
     * 
     * @return the widget registry instance
     */
    public static WidgetRegistry getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new WidgetRegistry();
        }
        return INSTANCE;
    }

    /**
     * Returns the registered widget factories.<p>
     * 
     * @return the registered widget factories
     */
    public Map<String, I_WidgetFactory> getWidgetFactories() {

        return m_widgetRegistry;
    }

    /**
     * Registers external widgets.<p>
     * 
     * @param externalWidgetConfigurations the external widget configurations
     * @param callback the callback to execute when done
     */
    public void registerExternalWidgets(
        List<CmsExternalWidgetConfiguration> externalWidgetConfigurations,
        final Command callback) {

        final Set<String> initCalls = new HashSet<String>();
        for (CmsExternalWidgetConfiguration widgetConfiguration : externalWidgetConfigurations) {
            if (!m_widgetRegistry.containsKey(widgetConfiguration.getWidgetName())) {
                for (String cssResource : widgetConfiguration.getCssResourceLinks()) {
                    ensureStyleSheetIncluded(cssResource);
                }
                for (String javaScriptResource : widgetConfiguration.getJavaScriptResourceLinks()) {
                    ensureJavaScriptIncluded(javaScriptResource);
                }
                initCalls.add(widgetConfiguration.getInitCall());
            }
        }
        if (initCalls.isEmpty()) {
            callback.execute();
        } else {
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                /** The number of repeats. */
                private int m_repeats;

                /**
                 * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
                 */
                public boolean execute() {

                    m_repeats++;
                    Iterator<String> it = initCalls.iterator();
                    while (it.hasNext()) {
                        String initCall = it.next();
                        if (tryInitCall(initCall)) {
                            it.remove();
                        }
                    }
                    if (initCalls.isEmpty()) {
                        callback.execute();
                        return false;
                    } else {
                        return m_repeats < 100;
                    }

                }
            }, 50);
        }
    }

    /**
     * Registers a widget.<p>
     * 
     * @param widgetName the widget name 
     * @param widgetFactory the widget
     */
    public void registerWidgetFactory(String widgetName, I_WidgetFactory widgetFactory) {

        m_widgetRegistry.put(widgetName, widgetFactory);
    }

    /**
     * Tries to initializes a widget with the given initialization call. Returns false if the init method was not available within the window context yet.<p>
     * 
     * @param initCall the initialization function name
     * 
     * @return <code>true</code> if the initialization function was available and has been executed
     */
    protected native boolean tryInitCall(String initCall)/*-{
        try {
            if ($wnd[initCall]) {
                $wnd[initCall]();
                return true;
            }
        } catch (error) {
            throw "Failed excuting " + initCall
                    + " to initialize editing widget."
        }
        return false;
    }-*/;

    /**
     * Ensures a script tag is present within the window document context.<p>
     * 
     * @param javascriptLink the link to the java script resource
     */
    private native void ensureJavaScriptIncluded(String javascriptLink)/*-{
        var scripts = $wnd.document.scripts;
        for ( var i = 0; i < scripts.length; i++) {
            if (scripts[i].src != null
                    && scripts[i].src.indexOf(javascriptLink) >= 0) {
                // script resource is present
                return;
            }
        }
        // append the script tag to the head element
        var head = $wnd.document.getElementsByTagName("head")[0];
        var scriptNode = $wnd.document.createElement('link');
        scriptNode.type = 'text/javascript';
        scriptNode.src = javascriptLink;
        head.appendChild(scriptNode);
    }-*/;

    /**
     * Checks the window.document for given style-sheet and includes it if required.<p>
     * 
     * @param styleSheetLink the style-sheet link
     */
    private native void ensureStyleSheetIncluded(String styleSheetLink)/*-{
        var styles = $wnd.document.styleSheets;
        for ( var i = 0; i < styles.length; i++) {
            if (styles[i].href != null
                    && styles[i].href.indexOf(styleSheetLink) >= 0) {
                // style-sheet is present
                return;
            }
        }
        // include style-sheet into head
        var headID = $wnd.document.getElementsByTagName("head")[0];
        var cssNode = $wnd.document.createElement('link');
        cssNode.type = 'text/css';
        cssNode.rel = 'stylesheet';
        cssNode.href = styleSheetLink;
        headID.appendChild(cssNode);
    }-*/;

    /**
     * Exports the widget registration.<p>
     */
    private native void exportWidgetRegistration() /*-{
        var self = this;
        $wnd[@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::REGISTER_WIDGET_FACTORY_FUNCTION] = function(
                factory) {
            self.@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::registerWrapper(Lorg/opencms/ade/contenteditor/widgetregistry/client/WidgetFactoryWrapper;)(factory);
        }
    }-*/;

    /**
     * Registers a widget wrapper as widget.<p>
     * 
     * @param wrapper the wrapper object
     */
    private void registerWrapper(WidgetFactoryWrapper wrapper) {

        registerWidgetFactory(wrapper.getName(), wrapper);
    }
}
