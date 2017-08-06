package com.withparadox2.rotatecube.util;

import android.util.Log;
import android.util.Pair;

import static android.opengl.GLES20.*;

/**
 * Created by withparadox2 on 2017/8/6.
 */

public class ShaderHelper {
  static final String TAG = ShaderHelper.class.getSimpleName();

  public static int compileVertexShader(String shaderCode) {
    return compileShader(GL_VERTEX_SHADER, shaderCode);
  }

  public static int compileFragmentShader(String shaderCode) {
    return compileShader(GL_FRAGMENT_SHADER, shaderCode);
  }

  private static int compileShader(int type, String shaderCode) {
    final int shaderObjectId = glCreateShader(type);
    if (shaderObjectId == 0) {
      Log.e(TAG, "Could not create new shader.");
      return 0;
    }
    glShaderSource(shaderObjectId, shaderCode);
    glCompileShader(shaderObjectId);

    final int[] compileStatus = new int[1];
    glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);



    if (compileStatus[0] == 0) {
      // If it failed, delete the shader object.
      glDeleteShader(shaderObjectId);
      Log.e(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(
          shaderObjectId));
      Log.w(TAG, "Compilation of shader failed.");
      return 0;
    }
    return shaderObjectId;
  }

  public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
    int programId = glCreateProgram();
    if (programId == 0) {
      Log.e(TAG, "Could not create new program.");
      return 0;
    }
    glAttachShader(programId, vertexShaderId);
    glAttachShader(programId, fragmentShaderId);
    glLinkProgram(programId);

    final int[] linkStatus = new int[1];
    glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
    if (linkStatus[0] == 0) {
      glDeleteProgram(programId);
      Log.w(TAG, "Link program failed.");
      return 0;
    }
    return programId;
  }

  public static boolean validateProgram(int programObjectId) {
    glValidateProgram(programObjectId);
    final int[] validateStatus = new int[1];
    glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
    Log.v(TAG,
        "Results of validating program: " + validateStatus[0] + "\nLog:" + glGetProgramInfoLog(
            programObjectId));
    return validateStatus[0] != 0;
  }
}
