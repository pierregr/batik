/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.refimpl.bridge;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeMutationEvent;
import org.apache.batik.bridge.IllegalAttributeValueException;
import org.apache.batik.bridge.MissingAttributeException;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.refimpl.bridge.resources.Messages;
import org.apache.batik.util.UnitProcessor;

import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.svg.SVGElement;

/**
 * A factory for the &lt;rect> SVG element.
 *
 * @author <a href="mailto:Thierry.Kormann@sophia.inria.fr">Thierry Kormann</a>
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class SVGRectElementBridge extends SVGShapeElementBridge {

    /**
     * Returns a <tt>Rectangle2D.Float</tt> or a
     * <tt>RoundRectangle2D.Float</tt> depending on the 'x', 'y',
     * 'width', 'height', 'rx' and 'ry' attributes.
     */
    protected Shape createShape(BridgeContext ctx,
                                SVGElement svgElement,
                                CSSStyleDeclaration decl,
                                UnitProcessor.Context uctx) {

        // parse the x attribute, (default is 0)
        String s = svgElement.getAttributeNS(null, ATTR_X);
        float x = 0;
        if (s.length() != 0) {
            x = UnitProcessor.svgToUserSpace(s,
                                             svgElement,
                                             UnitProcessor.HORIZONTAL_LENGTH,
                                             uctx);
        }

        // parse the y attribute, (default is 0)
        s = svgElement.getAttributeNS(null, ATTR_Y);
        float y = 0;
        if (s.length() != 0) {
            y = UnitProcessor.svgToUserSpace(s,
                                             svgElement,
                                             UnitProcessor.VERTICAL_LENGTH,
                                             uctx);
        }

        // parse the width attribute (required and must be positive)
        s = svgElement.getAttributeNS(null, ATTR_WIDTH);
        float w;
        if (s.length() == 0) {
            throw new MissingAttributeException(
                Messages.formatMessage("rect.width.required", null));
        } else {
            w = UnitProcessor.svgToUserSpace(s,
                                             svgElement,
                                             UnitProcessor.HORIZONTAL_LENGTH,
                                             uctx);
            if (w < 0) {
                throw new IllegalAttributeValueException(
                    Messages.formatMessage("rect.width.negative", null));
            }
        }

        // parse the height attribute (required and must be positive)
        s = svgElement.getAttributeNS(null, ATTR_HEIGHT);
        float h;
        if (s.length() == 0) {
            throw new MissingAttributeException(
                Messages.formatMessage("rect.height.required", null));
        } else {
            h = UnitProcessor.svgToUserSpace(s,
                                             svgElement,
                                             UnitProcessor.VERTICAL_LENGTH,
                                             uctx);
            if (h < 0) {
                throw new IllegalAttributeValueException(
                    Messages.formatMessage("rect.height.negative", null));
            }
        }

        // parse the rx attribute (must be positive if any)
        s = svgElement.getAttributeNS(null, ATTR_RX);
        boolean rxs = s.length() != 0;
        float rx = 0;
        if (s.length() != 0) {
            rx = UnitProcessor.svgToUserSpace(s,
                                              svgElement,
                                              UnitProcessor.HORIZONTAL_LENGTH,
                                              uctx);
            if (rx < 0) {
                throw new IllegalAttributeValueException(
                    Messages.formatMessage("rect.rx.negative", null));
            }
        }
        rx = (rx > w / 2) ? w / 2 : rx;

        // parse the ry attribute (must be positive if any)
        s = svgElement.getAttributeNS(null, ATTR_RY);
        boolean rys = s.length() != 0;
        float ry = 0;
        if (s.length() != 0) {
            ry = UnitProcessor.svgToUserSpace(s,
                                              svgElement,
                                              UnitProcessor.VERTICAL_LENGTH,
                                              uctx);
            if (ry < 0) {
                throw new IllegalAttributeValueException(
                    Messages.formatMessage("rect.ry.negative", null));
            }
        }
        ry = (ry > h / 2) ? h / 2 : ry;

        if (rxs && rys) {
            if (rx == 0 || ry == 0) {
                return new Rectangle2D.Float(x, y, w, h);
            } else {
                return new RoundRectangle2D.Float(x, y, w, h, rx*2, ry*2);
            }
        } else if (rxs) {
            if (rx == 0) {
                return new Rectangle2D.Float(x, y, w, h);
            } else {
                return new RoundRectangle2D.Float(x, y, w, h, rx*2, rx*2);
            }
        } else if (rys) {
            if (ry == 0) {
                return new Rectangle2D.Float(x, y, w, h);
            } else {
                return new RoundRectangle2D.Float(x, y, w, h, ry*2, ry*2);
            }
        } else {
            return new Rectangle2D.Float(x, y, w, h);
        }
    }

    public void update(BridgeMutationEvent evt) {
        super.update(evt);
        BridgeContext ctx = evt.getBridgeContext();
        SVGElement svgElement = (SVGElement) evt.getElement();
        CSSStyleDeclaration cssDecl
            = ctx.getViewCSS().getComputedStyle(svgElement, null);
        UnitProcessor.Context uctx
            = new DefaultUnitProcessorContext(ctx, cssDecl);
        ShapeNode shapeNode = (ShapeNode) evt.getGraphicsNode();
        switch(evt.getType()) {
        case BridgeMutationEvent.PROPERTY_MUTATION_TYPE:
            String attrName = evt.getAttrName();
            if (attrName.equals(ATTR_X) ||
                    attrName.equals(ATTR_Y) ||
                    attrName.equals(ATTR_WIDTH) ||
                    attrName.equals(ATTR_HEIGHT)) {
                Shape shape = createShape(ctx, svgElement, cssDecl, uctx);
                shapeNode.setShape(shape);
            }
            break;
        case BridgeMutationEvent.STYLE_MUTATION_TYPE:
            throw new Error("Not yet implemented");
        }
    }

}
