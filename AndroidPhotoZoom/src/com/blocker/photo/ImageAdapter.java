package com.blocker.photo;

import cn.yunlai.photo.R;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.Context;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private Animator mCurrentAnimator;

	private int mShortAnimationDuration = 300;

	private Context mContext;
	LayoutInflater inflater;

	public ImageAdapter(Context c) {
		mContext = c;
	}

	public int getCount() {
		return mThumbIds.length;
	}

	public Object getItem(int position) {
		return mThumbIds[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		final ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageResource(mThumbIds[position]);
		imageView.setTag(mThumbIds[position]);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				zoomImageFromThumb(view, position);
			}
		});

		return imageView;
	}

	// References to our images in res > drawable
	public int[] mThumbIds = { R.drawable.sample_0, R.drawable.sample_1,
			R.drawable.sample_2, R.drawable.sample_3, R.drawable.sample_4,
			R.drawable.sample_5, R.drawable.sample_6, R.drawable.sample_7,
			R.drawable.sample_8, R.drawable.sample_9, R.drawable.sample_10,
			R.drawable.sample_11, R.drawable.sample_12, R.drawable.sample_13,
			R.drawable.sample_14};
	float startScale;
	HackyViewPager viewPager;
	Rect startBounds;
	float startScaleFinal;
	private void zoomImageFromThumb(View thumbView, int position) {
		// If there's an animation in progress, cancel it immediately and
		// proceed with this one.
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}
		
		viewPager = (HackyViewPager)((Activity)mContext).findViewById(R.id.expanded_image);
		viewPager.setAdapter(new SamplePagerAdapter(mThumbIds, mContext));
		viewPager.setCurrentItem(position);
		
		// Calculate the starting and ending bounds for the zoomed-in image.
		// This step
		// involves lots of math. Yay, math.
		startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail,
		// and the
		// final bounds are the global visible rectangle of the container view.
		// Also
		// set the container view's offset as the origin for the bounds, since
		// that's
		// the origin for the positioning animation properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		
		((Activity)mContext).findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		// Adjust the start bounds to be the same aspect ratio as the final
		// bounds using the
		// "center crop" technique. This prevents undesirable stretching during
		// the animation.
		// Also calculate the start scaling factor (the end scaling factor is
		// always 1.0).
		
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
				.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		//  show the zoomed-in view. When the animation
		// begins,
		// it will position the zoomed-in view in the place of the thumbnail.
		viewPager.setVisibility(View.VISIBLE);
		// Set the pivot point for SCALE_X and SCALE_Y transformations to the
		// top-left corner of
		// the zoomed-in view (the default is the center of the view).
		
		
		AnimatorSet animSet = new AnimatorSet();
		animSet.setDuration(1);
		animSet.play(ObjectAnimator.ofFloat(viewPager, "pivotX", 0f))
		.with(ObjectAnimator.ofFloat(viewPager, "pivotY", 0f))
		.with(ObjectAnimator.ofFloat(viewPager, "alpha", 1.0f));
		animSet.start();
		
		
		// Construct and run the parallel animation of the four translation and
		// scale properties
		// (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(((MainActivity)mContext).gridView, "alpha", 1.0f, 0.f);
		ObjectAnimator animatorX = ObjectAnimator.ofFloat(viewPager, "x", startBounds.left, finalBounds.left);
		ObjectAnimator animatorY = ObjectAnimator.ofFloat(viewPager, "y", startBounds.top, finalBounds.top);
		ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(viewPager, "scaleX", startScale, 1f);
		ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(viewPager, "scaleY", startScale, 1f);

		set.play(alphaAnimator).with(animatorX).with(animatorY).with(animatorScaleX).with(animatorScaleY);
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;

		// Upon clicking the zoomed-in image, it should zoom back down to the
		// original bounds
		// and show the thumbnail instead of the expanded image.
		startScaleFinal = startScale;
		
	}
	
	
	public boolean getScaleFinalBounds(int position) {
		GridView gridView = ((MainActivity)mContext).gridView;
		View childView = gridView.getChildAt(position);
		
		startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();
		
		try {
			childView.getGlobalVisibleRect(startBounds);
		} catch (Exception e) {
			return false;
		}
		((Activity) mContext).findViewById(R.id.container)
				.getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
				.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}
		startScaleFinal = startScale;
		return true;
	}
	
    class SamplePagerAdapter extends PagerAdapter {

		private int[] sDrawables;
		private Context mContext;
		
		public SamplePagerAdapter(int[] imgIds, Context context) {
			this.sDrawables = imgIds;
			this.mContext = context;
		}
		@Override
		public int getCount() {
			return sDrawables.length;
		}
		
		@Override
		public View instantiateItem(ViewGroup container, final int position) {
			final PhotoView photoView = new PhotoView(container.getContext());
			photoView.setImageResource(sDrawables[position]);

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			photoView
					.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
						public void onPhotoTap(View paramAnonymousView,
								float paramAnonymousFloat1,
								float paramAnonymousFloat2) {
							if (mCurrentAnimator != null) {
								mCurrentAnimator.cancel();
							}
							
							photoView.clearZoom();
							
							boolean scaleResult = getScaleFinalBounds(position);
							// Animate the four positioning/sizing properties in parallel,
							// back to their
							// original values.
							AnimatorSet as = new AnimatorSet();
							ObjectAnimator containAlphaAnimator = ObjectAnimator.ofFloat(((MainActivity)mContext).gridView, "alpha", 0.f, 1.0f);
							if (scaleResult) {
								ObjectAnimator animatorX = ObjectAnimator.ofFloat(viewPager, "x", startBounds.left);
								ObjectAnimator animatorY = ObjectAnimator.ofFloat(viewPager, "y",  startBounds.top);
								ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(viewPager, "scaleX", startScaleFinal);
								ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(viewPager, "scaleY", startScaleFinal);
								
								as.play(containAlphaAnimator).with(animatorX).with(animatorY).with(animatorScaleX).with(animatorScaleY);
							}else { 
								//the selected photoview is beyond the mobile screen display
								//so it just fade out
								ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(viewPager, "alpha", 0.1f);
								as.play(alphaAnimator).with(containAlphaAnimator);
							}
							as.setDuration(mShortAnimationDuration);
							as.setInterpolator(new DecelerateInterpolator());
							as.addListener(new AnimatorListenerAdapter() {
								
								@Override
								public void onAnimationEnd(Animator animation) {
						            viewPager.clearAnimation();
									viewPager.setVisibility(View.GONE);
									mCurrentAnimator = null;
								}

								@Override
								public void onAnimationCancel(Animator animation) {
						            viewPager.clearAnimation();
									viewPager.setVisibility(View.GONE);
									mCurrentAnimator = null;
								}
							});
							as.start();
							mCurrentAnimator = as;
							
						}
					});
			
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

}