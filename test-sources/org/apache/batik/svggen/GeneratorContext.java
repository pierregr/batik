/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.svggen;

import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;

import java.awt.*;
import java.awt.geom.*;
import org.w3c.dom.*;
import java.util.*;
import java.net.URL;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.util.SVGConstants;

/**
 * Testing customization of the SVGGeneratorContext and generation of 
 * SVG Fonts.
 *
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @version $Id$
 */
public class GeneratorContext extends SVGAccuracyTest implements SVGConstants {
    public static class TestIDGenerator extends SVGIDGenerator {
        public String generateID(String prefix) {
            return "test"+super.generateID(prefix);
        }
    }

    public static class TestStyleHandler extends DefaultStyleHandler {
        private CDATASection styleSheet;
        public TestStyleHandler(CDATASection styleSheet) {
            this.styleSheet = styleSheet;
        }
        public void setStyle(Element element, Map styleMap,
                             SVGGeneratorContext generatorContext) {
            Iterator iter = styleMap.keySet().iterator();
            // create a new class id in the style sheet
            String id = generatorContext.getIDGenerator().generateID("C");
            styleSheet.appendData("."+id+" {");
            // append each key/value pairs
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)styleMap.get(key);
                styleSheet.appendData(key+":"+value+";");
            }
            styleSheet.appendData("}\n");
            // reference the class id of the style sheet on the element to be styled
            element.setAttribute("class", id);
        }
    }

    private Element topLevelGroup = null;

    public GeneratorContext(Painter painter,
                            URL refURL) {
        super(painter, refURL);
    }

    protected SVGGraphics2D buildSVGGraphics2D() {
        // Use Batik's DOM implementation to create a Document
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document domFactory = impl.createDocument(namespaceURI, SVG_SVG_TAG, null);

        // Create a default context from our Document instance
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(domFactory);
        
        // Set ID generator
        ctx.setIDGenerator(new TestIDGenerator());

        // Extension Handler to be done
        // Image Handler to be done

        // Set Style handler
        CDATASection styleSheet = domFactory.createCDATASection("");
        ctx.setStyleHandler(new TestStyleHandler(styleSheet));

        // Set the generator comment
        ctx.setComment("Generated by the Batik Test Framework. Test:\u00e9j");

        // Turn SVG Font embedding on.
        ctx.setEmbeddedFontsOn(true);

        // Set the default font to use
        GraphicContextDefaults defaults 
            = new GraphicContextDefaults();
        defaults.font = new Font("Lucida Sans", Font.PLAIN, 12);
        ctx.setGraphicContextDefaults(defaults);

        //
        // Build SVGGraphics2D with our customized context
        //
        SVGGraphics2D g2d = new SVGGraphics2D(ctx, false);

        // Append our stylesheet to the top level group.
        topLevelGroup = g2d.getTopLevelGroup();
        Element style = domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_STYLE_TAG);
        style.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, "text/css");
        style.appendChild(styleSheet);
        topLevelGroup.appendChild(style);

        return g2d;
    }

    protected void configureSVGGraphics2D(SVGGraphics2D g2d) {
        topLevelGroup.appendChild(g2d.getTopLevelGroup());
        g2d.setTopLevelGroup(topLevelGroup);
    }
}

