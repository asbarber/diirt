/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.graphene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import org.diirt.util.array.ListNumber;
import org.diirt.util.array.SortedListView;

/**
 * Renderer for a line graph.
 *
 * @author carcassi
 */
public class LineTimeGraph2DRenderer extends TemporalGraph2DRenderer<LineTimeGraph2DRendererUpdate> {

    public static java.util.List<InterpolationScheme> supportedInterpolationScheme = Arrays.asList(
            InterpolationScheme.NEAREST_NEIGHBOR, 
            InterpolationScheme.PREVIOUS_VALUE,
            InterpolationScheme.LINEAR,
            InterpolationScheme.CUBIC);
    
    @Override
    public LineTimeGraph2DRendererUpdate newUpdate() {
        return new LineTimeGraph2DRendererUpdate();
    }

    private InterpolationScheme interpolation = InterpolationScheme.NEAREST_NEIGHBOR;

    /**
     * Creates a new line graph renderer.
     * 
     * @param imageWidth the graph width
     * @param imageHeight the graph height
     */
    public LineTimeGraph2DRenderer(int imageWidth, int imageHeight) {
        super(imageWidth, imageHeight);
    }

    /**
     * The current interpolation used for the line.
     * 
     * @return the current interpolation
     */
    public InterpolationScheme getInterpolation() {
        return interpolation;
    }
    
    @Override
    public void update(LineTimeGraph2DRendererUpdate update) {
        super.update(update);
        if (update.getInterpolation() != null) {
            interpolation = update.getInterpolation();
        }
    }

    /**
     * Draws the graph on the given graphics context.
     * 
     * @param g the graphics on which to display the data
     * @param data the data to display
     */
    public void draw(Graphics2D g, TimeSeriesDataset data) {
        this.g = g;
        
        calculateRanges(data.getStatistics().getRange(), data.getTimeInterval());
        calculateGraphArea();
        drawBackground();
        drawGraphArea();
        
        ListNumber xValues = data.getNormalizedTime();
        ListNumber yValues = data.getValues();

        setClip(g);
        g.setColor(Color.BLACK);
        drawValueLine(xValues, yValues, interpolation);
    }
}
