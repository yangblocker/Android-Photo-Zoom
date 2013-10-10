package uk.co.senab.photoview;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class PhotoAnimation extends Animation {

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
	}
	
}
