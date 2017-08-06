package com.withparadox2.rotatecube;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.withparadox2.rotatecube.util.ShaderHelper;
import com.withparadox2.rotatecube.util.Utils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
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
        // Triangle 1
        -0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f, -0.5f, 0.5f, 0f,

        -0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f,

        -0.5f, 0.5f, 0f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,

        -0.5f, 0.5f, 0f, 0.5f, 0.5f, 0f, 0.5f, 0.5f, 0.5f,

        -0.5f, -0.5f, 0f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f,

        -0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f, 0.5f, -0.5f, 0.5f,

        -0.5f, -0.5f, 0f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f,

        -0.5f, -0.5f, 0f, -0.5f, 0.5f, 0f, -0.5f, 0.5f, 0.5f,

        0.5f, -0.5f, 0f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f,

        0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f, 0.5f, 0.5f, 0.5f,

        -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,

        -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,

        // Line 1
        //-0.5f, 0f, 0f, 1f, 0.5f, 0f, 0f, 1f,
        //
        //// Mallets
        //0f, -0.25f, 0f, 1f, 0f, 0.25f, 0f, 1f
    };

    for (int i = 0; i < tableVerticesWithTriangles.length; i++) {
      if ((i + 1) % 3 != 0) {
        tableVerticesWithTriangles[i] = tableVerticesWithTriangles[i] * 0.5f;
      }
    }

    vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer();

    vertexData.put(tableVerticesWithTriangles);
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0f, 0f, .0f, .5f);

    //glEnable(GLES20.GL_DEPTH_TEST);
    //glDepthFunc(GLES20.GL_LEQUAL);
    //glDepthMask(true);

    glEnable(GL_DEPTH_TEST);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    //glEnable(GL_CULL_FACE);

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

    //final float aspectRatio =
    //    width > height ? (float) width / (float) height : (float) height / (float) width;
    //if (width > height) {
    //  // Landscape
    //  orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
    //} else {
    //  // Portrait or square
    //  orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
    //}

    perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);

    setIdentityM(modelMatrix, 0);

    translateM(modelMatrix, 0, 0f, 0f, -2.5f);
    rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

    multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
    System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
  }

  final float[] temp = new float[16];
  float angle = 0f;

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
    glDrawArrays(GL_TRIANGLES, 0, 6);

    glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 0f);
    glDrawArrays(GL_TRIANGLES, 6, 6);

    glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 0f);
    glDrawArrays(GL_TRIANGLES, 12, 6);

    glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 0f);
    glDrawArrays(GL_TRIANGLES, 18, 6);

    glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 0f);
    glDrawArrays(GL_TRIANGLES, 24, 6);

    glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0f);
    glDrawArrays(GL_TRIANGLES, 30, 6);

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

  public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float n, float f) {
    final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);

    final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));
    m[0] = a / aspect;
    m[1] = 0f;
    m[2] = 0f;
    m[3] = 0f;

    m[4] = 0f;
    m[5] = a;
    m[6] = 0f;
    m[7] = 0f;

    m[8] = 0f;
    m[9] = 0f;
    m[10] = -((f + n) / (f - n));
    m[11] = -1f;

    m[12] = 0f;
    m[13] = 0f;
    m[14] = -((2f * f * n) / (f - n));
    m[15] = 0f;
  }
}
