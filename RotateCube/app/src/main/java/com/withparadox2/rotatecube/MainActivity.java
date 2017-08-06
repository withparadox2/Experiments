package com.withparadox2.rotatecube;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.*;
import static android.opengl.Matrix.*;


/**
 * Created by withparadox2 on 2017/8/6.
 */
public class MainActivity extends Activity {
  private GLSurfaceView mSurfaceView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSurfaceView = new GLSurfaceView(this);
    mSurfaceView.setEGLContextClientVersion(2);
    mSurfaceView.setRenderer(new CubeRender(this));
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

}
