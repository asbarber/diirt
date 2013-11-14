/**
 * Copyright (C) 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.graphene.profile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.epics.graphene.*;

/**
 *
 * @author asbarber
 */
public class ProfileScatterGraph2D extends ProfileGraph2D<ScatterGraph2DRenderer, Point2DDataset>{

    @Override
    protected Point2DDataset getDataset() {
        return ProfileGraph2D.makePoint2DGaussianRandomData(getNumDataPoints());
    }

    @Override
    protected ScatterGraph2DRenderer getRenderer(int imageWidth, int imageHeight) {
        return new ScatterGraph2DRenderer(imageWidth, imageHeight);
    }

    @Override
    protected void render(Graphics2D graphics, ScatterGraph2DRenderer renderer, Point2DDataset data) {
        renderer.draw(graphics, data);
    }
    
    @Override
    public String getGraphTitle() {
        return "ScatterGraph2D";
    }
    
    
    public static void main(String[] args){
        ProfileScatterGraph2D profiler = new ProfileScatterGraph2D();
        profiler.profile();
        profiler.printStatistics();
        profiler.saveStatistics();        
    }    
}
