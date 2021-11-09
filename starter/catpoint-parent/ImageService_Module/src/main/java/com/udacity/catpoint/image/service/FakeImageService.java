package com.udacity.catpoint.image.service;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Service that tries to guess if an image displays a cat.
 */
public class FakeImageService {
    private static final Random r = new Random();
    private boolean randomCat;

    public FakeImageService(){
        this.randomCat = r.nextBoolean();
    }

    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {

        this.randomCat = r.nextBoolean();
        return this.randomCat;
    }
}
