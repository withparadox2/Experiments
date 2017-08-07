package com.withparadox2.rotatecube;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import com.withparadox2.rotatecube.util.ShaderHelper;
import com.withparadox2.rotatecube.util.Utils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by withparadox2 on 2017/8/6.
 */

public class CubeRender implements GLSurfaceView.Renderer {
  private int mProgram;
  private int uColorLocation;
  private int aPositionLocation;
  private int uMatrixLocation;

  private Context mContext;

  private final FloatBuffer vertexData;

  private static final int POSITION_COMPONENT_COUNT = 3;
  private static final int BYTES_PER_FLOAT = 4;

  private static final String U_MATRIX = "u_Matrix";
  private final float[] projectionMatrix = new float[16];
  private final float[] backMatrix = new float[16];
  private final float[] modelMatrix = new float[16];

  float[] outerPoints = {
      -1f, -1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f,
      1f, -1f, 1f,
  };

  float[] backOuterPoints = {
      -1f, -1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f,
      1f, -1f, 1f,
  };

  float[] innerPoints = {
      -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
      -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f,
  };

  float[] backInnerPoints = {
      -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
      -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f,
  };

  float[] lines = new float[144];

  public CubeRender(Context context) {
    this.mContext = context;

    //float[] tableVerticesWithTriangles = {
    //    //下
    //    -1f, -1f, -1f,
    //    -1f,  1f, -1f,
    //     1f, -1f, -1f,
    //     1f,  1f, -1f,
    //
    //    //上
    //    -1f, -1f,  1f,
    //    -1f,  1f,  1f,
    //     1f, -1f,  1f,
    //     1f,  1f,  1f,
    //
    //    //左
    //    -1f, -1f, -1f,
    //    -1f, -1f,  1f,
    //    -1f,  1f, -1f,
    //    -1f,  1f,  1f,
    //
    //    //右
    //     1f, -1f, -1f,
    //     1f, -1f,  1f,
    //     1f,  1f, -1f,
    //     1f,  1f,  1f,
    //
    //    //前
    //    -1f, -1f, -1f,
    //    -1f, -1f,  1f,
    //     1f, -1f, -1f,
    //     1f, -1f,  1f,
    //
    //    //后
    //    -1f,  1f, -1f,
    //    -1f,  1f,  1f,
    //     1f,  1f, -1f,
    //     1f,  1f,  1f,
    //};

    fillLines();
    vertexData = ByteBuffer.allocateDirect(lines.length * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();

    vertexData.put(lines);
  }

  private void fillLines() {
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 3; j++) {
        int index = 3 * i + j;
        //outer: left and right frame
        lines[index] = outerPoints[index];
        //inner: left and right frame
        lines[48 + index] = innerPoints[index];

        int target = 3 * (i / 2) + j;
        target = i % 2 == 0 ? target : target + 12;
        //outer: four connectors between left and right
        lines[24 + index] = outerPoints[target];
        //inner: four connectors between left and right
        lines[72 + index] = innerPoints[target];

        //eight connectors between outer and inner
        target = 6 * i + j;
        lines[96 + target] = outerPoints[index];
        lines[96 + target + 3] = innerPoints[index];
      }
    }
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0f, 0f, .0f, .5f);

    glEnable(GL_DEPTH_TEST);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    int vertexShader = ShaderHelper.compileVertexShader(
        Utils.readTextFileFromResource(mContext, R.raw.simple_vertex_shader));
    int fragmentShader = ShaderHelper.compileFragmentShader(
        Utils.readTextFileFromResource(mContext, R.raw.simple_fragment_shader));
    mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

    glUseProgram(mProgram);

    uColorLocation = glGetUniformLocation(mProgram, "u_Color");
    aPositionLocation = glGetAttribLocation(mProgram, "a_Position");
    uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);

    vertexData.position(0);
    glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0,
        vertexData);

    glEnableVertexAttribArray(aPositionLocation);
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    Matrix.perspectiveM(projectionMatrix, 0, 45, (float) width / (float) height, 0.1f, 100f);

    setIdentityM(modelMatrix, 0);

    translateM(modelMatrix, 0, 0f, 0f, -10f);
    rotateM(modelMatrix, 0, -30f, 0.5f, 1f, 0f);

    multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
    System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    System.arraycopy(temp, 0, backMatrix, 0, temp.length);
  }

  private final float[] temp = new float[16];
  private float angle = 0f;

  private void updateMatrix() {
    angle -= 0.0005f;
    setIdentityM(modelMatrix, 0);
    rotateM(modelMatrix, 0, angle, 1f, 0.3f, 0.8f);

    multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
    System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
  }

  private void updateVertexData(float percent) {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 3; j++) {
        int index = i * 3 + j;
        int index2 = index + 12;

        float base = 0.3f;

        if (j == 0) {
          outerPoints[index2] = backOuterPoints[index2] - (percent)  * 2;

          outerPoints[index] = backOuterPoints[index] + percent * 0.5f;

          innerPoints[index2] = backInnerPoints[index2] + percent * 0.5f;
          innerPoints[index] = backInnerPoints[index] + percent;
        } else {
          float scale = 1.2f;
          if (percent < base) {
            outerPoints[index2] = backOuterPoints[index2] * (1 + percent / base * (scale - 1));
          } else {
            outerPoints[index2] =
                backOuterPoints[index2] * (scale + (percent - base) / (1 - base) * (1 - scale));
          }

          innerPoints[index2] = backInnerPoints[index2] * (1f + percent);

          outerPoints[index] = backOuterPoints[index] * (1 - 0.5f * percent);
        }
      }
    }

    fillLines();
    vertexData.put(lines);
    vertexData.position(0);

    setIdentityM(modelMatrix, 0);
    rotateM(modelMatrix, 0, -percent * 90, 1f, 0.f, 0.f);

    multiplyMM(temp, 0, backMatrix, 0, modelMatrix, 0);
    System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
  }

  private float percent = 0f;
  private int loopCount = 0;

  @Override public void onDrawFrame(GL10 gl) {
    if (percent >= 1f) {
      percent = 0f;
      loopCount++;
    }
    updateVertexData(percent);
    percent += 0.009;

    //updateMatrix();

    glClear(GLES20.GL_COLOR_BUFFER_BIT);
    glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

    //glEnable(GL_BLEND);
    //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    //
    //glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 0.5f);
    //glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    //
    //glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 0.5f);
    //glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);
    //
    //glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 0.5f);
    //glDrawArrays(GL_TRIANGLE_STRIP, 8, 4);
    //
    //glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 0.5f);
    //glDrawArrays(GL_TRIANGLE_STRIP, 12, 4);
    //
    //glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 0.5f);
    //glDrawArrays(GL_TRIANGLE_STRIP, 16, 4);
    //
    //glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0.5f);
    //glDrawArrays(GL_TRIANGLE_STRIP, 20, 4);

    glLineWidth(9.0f);
    if (loopCount % 4 == 0) {
      glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 0, 4);

      glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 16, 4);

      glUniform4f(uColorLocation, .8f, 0.5f, .0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 20, 4);

      glUniform4f(uColorLocation, 0.6f, .2f, 0.7f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 4, 4);

      glUniform4f(uColorLocation, 0.7f, .6f, 0.3f, 1.0f);
      glDrawArrays(GL_LINES, 8, 8);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 32, 8);

      glUniform4f(uColorLocation, .2f, 0.9f, .3f, 1.0f);
      glDrawArrays(GL_LINES, 24, 8);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 40, 8);

    } else if (loopCount % 4 == 1) {
      glUniform4f(uColorLocation, 0.6f, .2f, 0.7f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 0, 4);

      glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 16, 4);

      glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 20, 4);

      glUniform4f(uColorLocation, .8f, 0.5f, .0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 4, 4);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 8, 8);

      glUniform4f(uColorLocation, 0.7f, .6f, 0.3f, 1.0f);
      glDrawArrays(GL_LINES, 32, 8);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 24, 8);

      glUniform4f(uColorLocation, .2f, 0.9f, .3f, 1.0f);
      glDrawArrays(GL_LINES, 40, 8);

    } else if (loopCount % 4 == 2) {
      glUniform4f(uColorLocation, .8f, 0.5f, .0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 0, 4);

      glUniform4f(uColorLocation, 0.6f, .2f, 0.7f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 16, 4);

      glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 20, 4);

      glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 4, 4);

      glUniform4f(uColorLocation, .2f, 0.9f, .3f, 1.0f);
      glDrawArrays(GL_LINES, 8, 8);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 32, 8);

      glUniform4f(uColorLocation, 0.7f, .6f, 0.3f, 1.0f);
      glDrawArrays(GL_LINES, 24, 8);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 40, 8);

    } else if (loopCount % 4 == 3) {
      glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 0, 4);

      glUniform4f(uColorLocation, .8f, 0.5f, .0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 16, 4);

      glUniform4f(uColorLocation, 0.6f, .2f, 0.7f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 20, 4);

      glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
      glDrawArrays(GL_LINE_LOOP, 4, 4);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 8, 8);

      glUniform4f(uColorLocation, .2f, 0.9f, .3f, 1.0f);
      glDrawArrays(GL_LINES, 32, 8);

      glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
      glDrawArrays(GL_LINES, 24, 8);

      glUniform4f(uColorLocation, 0.7f, .6f, 0.3f, 1.0f);
      glDrawArrays(GL_LINES, 40, 8);
    }

    //
    //// Draw the first mallet blue.
    //glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
    //glDrawArrays(GL_POINTS, 8, 1);
    //// Draw the second mallet red.
    //glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    //glDrawArrays(GL_POINTS, 9, 1);
  }
}
