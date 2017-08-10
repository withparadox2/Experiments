package com.withparadox2.rotatecube;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;


/**
 * Created by withparadox2 on 2017/8/6.
 */
public class MainActivity extends Activity {
  private GLSurfaceView mSurfaceView;
  private CubeRender mRender;
  private TouchEventHandler mTouchEventHandler;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSurfaceView = new GLSurfaceView(this);
    mSurfaceView.setEGLContextClientVersion(2);
    mRender = new CubeRender(this);
    mTouchEventHandler = new TouchEventHandler(mRender);
    mSurfaceView.setRenderer(mRender);
    setContentView(mSurfaceView);
  }

  @Override protected void onResume() {
    super.onResume();
    mSurfaceView.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
    mSurfaceView.onPause();
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    mTouchEventHandler.handleTouchEvent(event);
    return super.onTouchEvent(event);
  }
}
