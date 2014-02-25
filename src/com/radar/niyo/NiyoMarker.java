package com.radar.niyo;


import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NiyoMarker extends ImageView {

	public NiyoMarker(Context context, AttributeSet attrs) {
		super(context, attrs);
//		initMarker();
	}
	
	public NiyoMarker(Context context) {
		super(context);
//		initMarker();
	}
	
	public void initMarker(int drawable){
		setBackgroundDrawable(getContext().getResources().getDrawable(drawable));
		setPadding(7, 9, 7, 9);
//		LayoutParams params = new LayoutParams(63, 77);
//		setLayoutParams(params);
		setScaleType(ScaleType.FIT_START);
	}
}
