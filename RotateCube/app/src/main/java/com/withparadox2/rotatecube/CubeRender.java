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
import static android.opengl.Matrix.multiplyMV;
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
  private final float[] modelMatrix = new float[16];

  private final float[] hyperMatrix = new float[16];

  private float[][] vertexes = {
      { -1, -1, -1, -1 }, { -1, -1, -1, 1 }, { -1, -1, 1, -1 }, { -1, -1, 1, 1 }, { -1, 1, -1, -1 },
      { -1, 1, -1, 1 }, { -1, 1, 1, -1 }, { -1, 1, 1, 1 }, { 1, -1, -1, -1 }, { 1, -1, -1, 1 },
      { 1, -1, 1, -1 }, { 1, -1, 1, 1 }, { 1, 1, -1, -1 }, { 1, 1, -1, 1 }, { 1, 1, 1, -1 },
      { 1, 1, 1, 1 }
  };

  private int[][] edges = {
      { 1, 3 }, { 1, 5 }, { 1, 9 }, { 3, 7 }, { 3, 11 }, { 5, 7 }, { 5, 13 }, { 7, 15 }, { 9, 11 },
      { 9, 13 }, { 11, 15 }, { 13, 15 }, { 2, 4 }, { 2, 6 }, { 2, 10 }, { 4, 8 }, { 4, 12 },
      { 6, 8 }, { 6, 14 }, { 8, 16 }, { 10, 12 }, { 10, 14 }, { 12, 16 }, { 14, 16 }, { 1, 2 },
      { 3, 4 }, { 5, 6 }, { 7, 8 }, { 9, 10 }, { 11, 12 }, { 13, 14 }, { 15, 16 }
  };

  private float[] newLines = new float[192];//32 * 2 * 3

  private float[] points = new float[16 * 3];

  public CubeRender(Context context) {
    this.mContext = context;
    setIdentityM(hyperMatrix, 0);

    projection(0f);
    vertexData = ByteBuffer.allocateDirect(newLines.length * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();

    vertexData.put(newLines);
  }

  private float[] tempArray = new float[4];

  private void projection(float angle) {
    hyperMatrix[0] = (float) Math.cos(angle);
    hyperMatrix[3] = (float) -Math.sin(angle);
    hyperMatrix[12] = (float) Math.sin(angle);
    hyperMatrix[15] = (float) Math.cos(angle);

    for (int i = 0; i < vertexes.length; i++) {
      multiplyMV(tempArray, 0, hyperMatrix, 0, vertexes[i], 0);
      for (int j = 0; j < 3; j++) {
        points[i * 3 + j] = tempArray[j] / (3 - tempArray[3]);
      }
    }
    fillNewLines();
  }

  private void fillNewLines() {
    for (int i = 0; i < edges.length; i++) {
      int[] e = edges[i];
      int p1 = (e[0] - 1) * 3;
      int p2 = (e[1] - 1) * 3;

      for (int j = 0; j < 3; j++) {
        newLines[i * 6 + j] = points[p1 + j];
        newLines[i * 6 + 3 + j] = points[p2 + j];
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

    Matrix.perspectiveM(projectionMatrix, 0, 45, (float) width / (float) height, 0.1f, 20f);

    setIdentityM(modelMatrix, 0);

    translateM(modelMatrix, 0, 0f, 0f, -5f);
    rotateM(modelMatrix, 0, 20f, 1f, 0f, 0f);
    rotateM(modelMatrix, 0, 130f, 0f, 1f, 0f);

    multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
    System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
  }

  private final float[] temp = new float[16];

  private void updateNew(float percent) {
    projection(percent);
    vertexData.put(newLines);
    vertexData.position(0);
  }

  private float percent = 0f;

  @Override public void onDrawFrame(GL10 gl) {
    percent += 0.015;
    updateNew(percent);

    glClear(GLES20.GL_COLOR_BUFFER_BIT);
    glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

    glLineWidth(5.0f);
    //12
    glUniform4f(uColorLocation, 1f, 0f, 0f, 1.0f);
    glDrawArrays(GL_LINES, 0, 24);
    //12
    glUniform4f(uColorLocation, 0f, 1f, 0f, 1.0f);
    glDrawArrays(GL_LINES, 24, 24);
    //8
    glLineWidth(2.0f);
    glUniform4f(uColorLocation, 1f, 0f, 1f, 1.0f);
    glDrawArrays(GL_LINES, 48, 16);
  }
}
