package com.yogaub.giorgio.parkado.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yogaub.giorgio.parkado.R;


/**
 * Created by Giorgio on yogaub.
 *
 * This class is a service responsible for the creation and management of the Chat Head.
 */

public class FloatingViewService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private ImageView cancelView;
    private WindowManager.LayoutParams floatingViewParams;
    private WindowManager.LayoutParams cancelParams;
    private int windowHeight;
    private int windowWidth;
    private int centerOfScreenByX;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialization
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //The root element of the collapsed view layout
        final View collapsedView = floatingView.findViewById(R.id.collapse_view);
        //The root element of the expanded view layout
        final View expandedView = floatingView.findViewById(R.id.expanded_container);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        windowHeight = displaymetrics.heightPixels;
        windowWidth = displaymetrics.widthPixels;

        floatingViewParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );

        floatingViewParams.gravity = Gravity.TOP | Gravity.LEFT;
        floatingViewParams.x = 0;
        floatingViewParams.y = 100;

        floatingView.setOnTouchListener(new View.OnTouchListener(){
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = floatingViewParams.x;
                        initialY = floatingViewParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        addCancelBinView();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        centerOfScreenByX = windowWidth / 2;

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }else {
                            // remove collapse view when it is in the cancel area
                            if (insideCancelArea(v)) {
                                stopSelf();
                            }
                        }
                        // always remove recycle bin ImageView when paper is dropped
                        windowManager.removeView(cancelView);
                        cancelView = null;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        floatingViewParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        floatingViewParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, floatingViewParams);
                        if (insideCancelArea(v)){
                            cancelView.setImageResource(R.drawable.close);
                        }
                        else {
                            cancelView.setImageResource(R.drawable.cancel);
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatingView, floatingViewParams);


        //Set the close button
        ImageView closeButton = (ImageView) floatingView.findViewById(R.id.expanded_image_view);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        });


    }

    private boolean isViewCollapsed() {
        return floatingView == null || floatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    private boolean insideCancelArea(View v){
        return ((floatingViewParams.y > windowHeight - cancelView.getHeight() - v.getHeight()) &&
                ((floatingViewParams.x > centerOfScreenByX - cancelView.getWidth() - v.getWidth() / 2) &&
                        (floatingViewParams.x < centerOfScreenByX + cancelView.getWidth() / 2)));
    }


    private void addCancelBinView() {
        cancelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        cancelParams.gravity = Gravity.BOTTOM | Gravity.CENTER;

        cancelView = new ImageView(this);
        cancelView.setPadding(20, 20, 20, 50);
        cancelView.setImageResource(R.drawable.cancel);

        cancelParams.x = 0;
        cancelParams.y = 0;

        windowManager.addView(cancelView, cancelParams);
    }


}
