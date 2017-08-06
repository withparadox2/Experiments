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

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.perspectiveM;
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

  public CubeRender(Context context) {
    this.mContext = context;

    float[] tableVerticesWithTriangles = {
        //下
        -1f, -1f, -1f,
        -1f,  1f, -1f,
         1f, -1f, -1f,
         1f,  1f, -1f,

        //上
        -1f, -1f,  1f,
        -1f,  1f,  1f,
         1f, -1f,  1f,
         1f,  1f,  1f,

        //左
        -1f, -1f, -1f,
        -1f, -1f,  1f,
        -1f,  1f, -1f,
        -1f,  1f,  1f,

        //右
         1f, -1f, -1f,
         1f, -1f,  1f,
         1f,  1f, -1f,
         1f,  1f,  1f,

        //前
        -1f, -1f, -1f,
        -1f, -1f,  1f,
         1f, -1f, -1f,
         1f, -1f,  1f,

        //后
        -1f,  1f, -1f,
        -1f,  1f,  1f,
         1f,  1f, -1f,
         1f,  1f,  1f,
    };

    vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();

    vertexData.put(tableVerticesWithTriangles);
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
    rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);

    multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
    System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
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

  @Override public void onDrawFrame(GL10 gl) {
    updateMatrix();
    glClear(GLES20.GL_COLOR_BUFFER_BIT);
    glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

    glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 0f);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 0f);
    glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);

    glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 0f);
    glDrawArrays(GL_TRIANGLE_STRIP, 8, 4);

    glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 0f);
    glDrawArrays(GL_TRIANGLE_STRIP, 12, 4);

    glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 0f);
    glDrawArrays(GL_TRIANGLE_STRIP, 16, 4);

    glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0f);
    glDrawArrays(GL_TRIANGLE_STRIP, 20, 4);

    //glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    //glDrawArrays(GL_LINES, 6, 2);
    //
    //// Draw the first mallet blue.
    //glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
    //glDrawArrays(GL_POINTS, 8, 1);
    //// Draw the second mallet red.
    //glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    //glDrawArrays(GL_POINTS, 9, 1);
  }

}
