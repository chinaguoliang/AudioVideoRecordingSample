package com.serenegiant.glutils;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: GLDrawer2D.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Helper class to draw to whole view using specific texture and texture matrix
 */
public class GLDrawer2D extends GPUImageFilter{
	private static final boolean DEBUG = false; // TODO set false on release
	private static final String TAG = "GLDrawer2D";

//	private static final String vss
//		= "uniform mat4 uMVPMatrix;\n"
//		+ "uniform mat4 uTexMatrix;\n"
//		+ "attribute highp vec4 aPosition;\n"
//		+ "attribute highp vec4 aTextureCoord;\n"
//		+ "varying highp vec2 vTextureCoord;\n"
//		+ "\n"
//		+ "void main() {\n"
//		+ "	gl_Position = uMVPMatrix * aPosition;\n"
//		+ "	vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
//		+ "}\n";
//
//
//	private static final String fss
//		= "#extension GL_OES_EGL_image_external : require\n"
//		+ "precision mediump float;\n"
//		+ "uniform samplerExternalOES sTexture;\n"
//		+ "varying highp vec2 vTextureCoord;\n"
//		+ "void main() {\n"
//		+ "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
//		+ "}";

	//==========================================================
	public static final String NO_FILTER_VERTEX_SHADER = "" +
			"attribute vec4 position;\n" +
			"attribute vec4 inputTextureCoordinate;\n" +
			" \n" +
			"varying vec2 textureCoordinate;\n" +
			" \n" +
			"void main()\n" +
			"{\n" +
			"    gl_Position = position;\n" +
			"    textureCoordinate = inputTextureCoordinate.xy;\n" +
			"}";

	public static final String BILATERAL_FRAGMENT_SHADER = "" +
			"   varying highp vec2 textureCoordinate;\n" +
			"\n" +
			"    uniform sampler2D inputImageTexture;\n" +
			"\n" +
			"    uniform highp vec2 singleStepOffset;\n" +
			"    uniform highp vec4 params;\n" +
			"    uniform highp float brightness;\n" +
			"\n" +
			"    const highp vec3 W = vec3(0.299, 0.587, 0.114);\n" +
			"    const highp mat3 saturateMatrix = mat3(\n" +
			"        1.1102, -0.0598, -0.061,\n" +
			"        -0.0774, 1.0826, -0.1186,\n" +
			"        -0.0228, -0.0228, 1.1772);\n" +
			"    highp vec2 blurCoordinates[24];\n" +
			"\n" +
			"    highp float hardLight(highp float color) {\n" +
			"    if (color <= 0.5)\n" +
			"        color = color * color * 2.0;\n" +
			"    else\n" +
			"        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
			"    return color;\n" +
			"}\n" +
			"\n" +
			"    void main(){\n" +
			"    highp vec3 centralColor = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
			"    blurCoordinates[0] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
			"    blurCoordinates[1] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
			"    blurCoordinates[2] = textureCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
			"    blurCoordinates[3] = textureCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
			"    blurCoordinates[4] = textureCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
			"    blurCoordinates[5] = textureCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
			"    blurCoordinates[6] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
			"    blurCoordinates[7] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
			"    blurCoordinates[8] = textureCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
			"    blurCoordinates[9] = textureCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
			"    blurCoordinates[10] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
			"    blurCoordinates[11] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
			"    blurCoordinates[12] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
			"    blurCoordinates[13] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
			"    blurCoordinates[14] = textureCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
			"    blurCoordinates[15] = textureCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
			"    blurCoordinates[16] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
			"    blurCoordinates[17] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
			"    blurCoordinates[18] = textureCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
			"    blurCoordinates[19] = textureCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
			"    blurCoordinates[20] = textureCoordinate.xy + singleStepOffset * vec2(-2.0, -2.0);\n" +
			"    blurCoordinates[21] = textureCoordinate.xy + singleStepOffset * vec2(-2.0, 2.0);\n" +
			"    blurCoordinates[22] = textureCoordinate.xy + singleStepOffset * vec2(2.0, -2.0);\n" +
			"    blurCoordinates[23] = textureCoordinate.xy + singleStepOffset * vec2(2.0, 2.0);\n" +
			"\n" +
			"    highp float sampleColor = centralColor.g * 22.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[0]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[1]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[2]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[3]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[4]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[5]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[6]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[7]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[8]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[9]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[10]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[11]).g;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[12]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[13]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[14]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[15]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[16]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[17]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[18]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[19]).g * 2.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[20]).g * 3.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[21]).g * 3.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[22]).g * 3.0;\n" +
			"    sampleColor += texture2D(inputImageTexture, blurCoordinates[23]).g * 3.0;\n" +
			"\n" +
			"    sampleColor = sampleColor / 62.0;\n" +
			"\n" +
			"    highp float highPass = centralColor.g - sampleColor + 0.5;\n" +
			"\n" +
			"    for (int i = 0; i < 5; i++) {\n" +
			"        highPass = hardLight(highPass);\n" +
			"    }\n" +
			"    highp float lumance = dot(centralColor, W);\n" +
			"\n" +
			"    highp float alpha = pow(lumance, params.r);\n" +
			"\n" +
			"    highp vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;\n" +
			"\n" +
			"    smoothColor.r = clamp(pow(smoothColor.r, params.g), 0.0, 1.0);\n" +
			"    smoothColor.g = clamp(pow(smoothColor.g, params.g), 0.0, 1.0);\n" +
			"    smoothColor.b = clamp(pow(smoothColor.b, params.g), 0.0, 1.0);\n" +
			"\n" +
			"    highp vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);\n" +
			"    highp vec3 bianliang = max(smoothColor, centralColor);\n" +
			"    highp vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;\n" +
			"\n" +
			"    gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);\n" +
			"    gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);\n" +
			"    gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);\n" +
			"\n" +
			"    highp vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n" +
			"    gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);\n" +
			"    gl_FragColor.rgb = vec3(gl_FragColor.rgb + vec3(brightness));\n" +
			"}";


	private static final float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
	private static final float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

	private final FloatBuffer pVertex = null;
	private final FloatBuffer pTexCoord = null;
	private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
	private final float[] mMvpMatrix = new float[16];

	private static final int FLOAT_SZ = Float.SIZE / 8;
	private static final int VERTEX_NUM = 4;
	private static final int VERTEX_SZ = VERTEX_NUM * 2;

	//============================
	private float toneLevel;
	private float beautyLevel;
	private float brightLevel;

	private int paramsLocation;
	private int brightnessLocation;
	private int singleStepOffsetLocation;


	private final FloatBuffer mGLCubeBuffer;
	private final FloatBuffer mGLTextureBuffer;

	static final float CUBE[] = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f,
	};

	public static final float TEXTURE_NO_ROTATION[] = {
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,
	};

	/**
	 * Constructor
	 * this should be called in GL context
	 */
	public GLDrawer2D() {
//		pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
//				.order(ByteOrder.nativeOrder()).asFloatBuffer();
//		pVertex.put(VERTICES);
//		pVertex.flip();
//		pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
//				.order(ByteOrder.nativeOrder()).asFloatBuffer();
//		pTexCoord.put(TEXCOORD);
//		pTexCoord.flip();
//
//		hProgram = loadShader(NO_FILTER_VERTEX_SHADER, BILATERAL_FRAGMENT_SHADER);
//		GLES20.glUseProgram(hProgram);
//        maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
//        maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
//        muMVPMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uMVPMatrix");
//        muTexMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uTexMatrix");
//
//		Matrix.setIdentityM(mMvpMatrix, 0);
//        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
//        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mMvpMatrix, 0);
//		GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pVertex);
//		GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
//		GLES20.glEnableVertexAttribArray(maPositionLoc);
//		GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
		super.onInit();
		paramsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
		brightnessLocation = GLES20.glGetUniformLocation(getProgram(), "brightness");
		singleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");

		toneLevel = 0.47f;
		beautyLevel = 0.42f;
		brightLevel = 0.34f;

		setParams(beautyLevel, toneLevel);
		setBrightLevel(brightLevel);

		mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		mGLCubeBuffer.put(CUBE).position(0);

		mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);

	}

	/**
	 * terminatinng, this should be called in GL context
	 */
	public void release() {
		if (hProgram >= 0)
			GLES20.glDeleteProgram(hProgram);
		hProgram = -1;
	}

	/**
	 * draw specific texture with specific texture matrix
	 * @param tex_id texture ID
	 * @param tex_matrix texture matrix、if this is null, the last one use(we don't check size of this array and needs at least 16 of float)
	 */
	public void draw(final int tex_id, final float[] tex_matrix) {
//		GLES20.glUseProgram(hProgram);
//		if (tex_matrix != null)
//			GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, 0);
//        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex_id);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
//		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//        GLES20.glUseProgram(0);
		onDraw(tex_id, mGLCubeBuffer, mGLTextureBuffer);
	}

	/**
	 * Set model/view/projection transform matrix
	 * @param matrix
	 * @param offset
	 */
	public void setMatrix(final float[] matrix, final int offset) {
//		if ((matrix != null) && (matrix.length >= offset + 16)) {
//			System.arraycopy(matrix, offset, mMvpMatrix, 0, 16);
//		} else {
//			Matrix.setIdentityM(mMvpMatrix, 0);
//		}
	}
	/**
	 * create external texture
	 * @return texture ID
	 */
	public static int initTex() {
		if (DEBUG) Log.v(TAG, "initTex:");
		final int[] tex = new int[1];
		GLES20.glGenTextures(1, tex, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);

//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		return tex[0];
	}

	/**
	 * delete specific texture
	 */
	public static void deleteTex(final int hTex) {
		if (DEBUG) Log.v(TAG, "deleteTex:");
		final int[] tex = new int[] {hTex};
		GLES20.glDeleteTextures(1, tex, 0);
	}

	/**
	 * load, compile and link shader
	 * @param vss source of vertex shader
	 * @param fss source of fragment shader
	 * @return
	 */
	public static int loadShader(final String vss, final String fss) {
		if (DEBUG) Log.v(TAG, "loadShader:");
		int vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vs, vss);
		GLES20.glCompileShader(vs);
		final int[] compiled = new int[1];
		GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			if (DEBUG) Log.e(TAG, "Failed to compile vertex shader:"
					+ GLES20.glGetShaderInfoLog(vs));
			GLES20.glDeleteShader(vs);
			vs = 0;
		}

		int fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fs, fss);
		GLES20.glCompileShader(fs);
		GLES20.glGetShaderiv(fs, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			if (DEBUG) Log.w(TAG, "Failed to compile fragment shader:"
				+ GLES20.glGetShaderInfoLog(fs));
			GLES20.glDeleteShader(fs);
			fs = 0;
		}

		final int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vs);
		GLES20.glAttachShader(program, fs);
		GLES20.glLinkProgram(program);

		return program;
	}


	//===========================================
	public void setBeautyLevel(float beautyLevel) {
		this.beautyLevel = beautyLevel;
		setParams(beautyLevel, toneLevel);
	}

	public void setBrightLevel(float brightLevel) {
		this.brightLevel = brightLevel;
		setFloat(brightnessLocation, 0.6f * (-0.5f + brightLevel));
	}

	public void setParams(float beauty, float tone) {
		float[] vector = new float[4];
		vector[0] = 1.0f - 0.6f * beauty;
		vector[1] = 1.0f - 0.3f * beauty;
		vector[2] = 0.1f + 0.3f * tone;
		vector[3] = 0.1f + 0.3f * tone;
		setFloatVec4(paramsLocation, vector);
	}

	private void setTexelSize(final float w, final float h) {
		setFloatVec2(singleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
	}

	@Override
	public void onOutputSizeChanged(final int width, final int height) {
		super.onOutputSizeChanged(width, height);
		setTexelSize(width, height);
	}

}
