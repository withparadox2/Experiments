package com.withparadox2.rotatecube;

import android.view.MotionEvent;

import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;

/**
 * Created by withparadox2 on 2017/8/10.
 */

public class TouchEventHandler {
  private CubeRender mRender;

  public TouchEventHandler(CubeRender render) {
    this.mRender = render;
  }

  private float mLastX, mLastY;
  private float mDeltaX = 0f, mDeltaY = 0f;

  public boolean handleTouchEvent(MotionEvent event) {
    float ex = event.getX();
    float ey = event.getY();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        this.mLastX = ex;
        this.mLastY = ey;
        break;
      case MotionEvent.ACTION_MOVE:
        move(mDeltaX + ex - this.mLastX, mDeltaY + ey - this.mLastY);
        break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        mDeltaX = mDeltaX + ex - this.mLastX;
        mDeltaY = mDeltaY + ey - this.mLastY;

        break;
    }
    return true;
  }

  private void move(float deltaX, float deltaY) {

    float[] matrix = this.mRender.modelMatrix;
    setIdentityM(matrix, 0);

    rotateM(matrix, 0, CubeRender.PRE_ROTATE_X + deltaY / 5, 1f, 0f, 0f);
    rotateM(matrix, 0, CubeRender.PRE_ROTATE_Y + deltaX / 5, 0f, 1f, 0f);
    this.mRender.updateProjectMatrix(matrix);
  }
}
