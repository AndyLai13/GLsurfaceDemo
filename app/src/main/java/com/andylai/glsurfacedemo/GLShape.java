package com.andylai.glsurfacedemo;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class GLShape {

	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}

	public static final String FIELD_MATRIX_MVP = "uMVPMatrix";
	public static final String FIELD_POSITION = "vPosition";

	private static final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 " + FIELD_MATRIX_MVP + ";" +
			"attribute vec4 " + FIELD_POSITION + ";" +
			"void main() {" +
			// the matrix must be included as a modifier of gl_Position
			// Note that the uMVPMatrix factor *must be first* in order
			// for the matrix multiplication product to be correct.
			"  gl_Position = " + FIELD_MATRIX_MVP  + "*" + FIELD_POSITION  + ";" +
			//" gl_Position = vPosition;" +
			"}";

	private static final String fragmentShaderCode =
			"precision mediump float;" +
			"uniform vec4 vColor;" +
			"void main() {" +
			"  gl_FragColor = vColor;" +
			"}";

	private static float color[] = {255, 0, 1.0f, 1.0f};

	static class Polygon {
		private FloatBuffer vertexBuffer;
		private ShortBuffer drawListBuffer;

		// number of coordinates per vertex in this array
		static final int COORDS_PER_VERTEX = 3;
		static float squareCoords[] = {
				-0.5f, 0.5f, 0.0f,   // top left
				-0.5f, -0.5f, 0.0f,   // bottom left
				0.5f, -0.5f, 0.0f,   // bottom right
				0.5f, 0.5f, 0.0f,

				0.5f, -0.5f, 0.0f,
				0.5f, 0.5f, 0.0f,
				1.0f, -0.5f, 0.0f,

				}; // top right

		private short drawOrder[] = {
				0, 1, 2,
				0, 2, 3,
				4, 5, 6,
		}; // order to draw vertices

		private final int mProgram;

		private int mPositionHandle;
		private int mColorHandle;

		private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
		private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;

		private int mMVPMatrixHandle;

//		private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;

		public Polygon() {
			// 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
			ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(squareCoords);
			vertexBuffer.position(0);

			// 初始化ByteBuffer，长度为arr数组的长度*2，因为一个short占2个字节
			ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
			dlb.order(ByteOrder.nativeOrder());
			drawListBuffer = dlb.asShortBuffer();
			drawListBuffer.put(drawOrder);
			drawListBuffer.position(0);

			int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

			mProgram = GLES20.glCreateProgram();
			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragmentShader);
			GLES20.glLinkProgram(mProgram);
		}

		public void draw(float[] mvpMatrix) {
			GLES20.glUseProgram(mProgram);
			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
					GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);
			mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			// get handle to shape's transformation matrix
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			// Pass the projection and view transformation to the shader
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 4, 7);

			GLES20.glDisableVertexAttribArray(mPositionHandle);
		}
	}

	static class Triangle {

		// Use to access and set the view transformation
		private int mMVPMatrixHandle;

		private FloatBuffer vertexBuffer;

		// number of coordinates per vertex in this array
		static final int COORDS_PER_VERTEX = 3;
		static float triangleCoords[] = {   // in counterclockwise order:
				0.0f, 0.433f, 0.0f, // top
				-0.5f, -0.433f, 0.0f, // bottom left
				0.5f, -0.433f, 0.0f
		};


		private int mPositionHandle;
		private int mColorHandle;

		private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
		private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex


		private final int mProgram;

		public Triangle() {
			// 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
			ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(triangleCoords);
			vertexBuffer.position(0);

			int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

			// 创建空的OpenGL ES程序
			mProgram = GLES20.glCreateProgram();
			// 添加顶点着色器到程序中
			GLES20.glAttachShader(mProgram, vertexShader);
			// 添加片段着色器到程序中
			GLES20.glAttachShader(mProgram, fragmentShader);
			// 创建OpenGL ES程序可执行文件
			GLES20.glLinkProgram(mProgram);
		}

		public void draw(float[] mvpMatrix) {
			GLES20.glUseProgram(mProgram);

			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
					GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);

			mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);

			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

			GLES20.glDisableVertexAttribArray(mPositionHandle);
		}
	}

	static class Square {

		private FloatBuffer vertexBuffer;
		private ShortBuffer drawListBuffer;

		// number of coordinates per vertex in this array
		static final int COORDS_PER_VERTEX = 3;
		static float squareCoords[] = {
				-0.5f, 0.5f, 0.0f,   // top left
				-0.5f, -0.5f, 0.0f,   // bottom left
				0.5f, -0.5f, 0.0f,   // bottom right
				0.5f, 0.5f, 0.0f}; // top right

		private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

		private final int mProgram;

		private int mPositionHandle;
		private int mColorHandle;

		private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
		private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;

		private int mMVPMatrixHandle;

		public Square() {
			// 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
			ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(squareCoords);
			vertexBuffer.position(0);

			// 初始化ByteBuffer，长度为arr数组的长度*2，因为一个short占2个字节
			ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
			dlb.order(ByteOrder.nativeOrder());
			drawListBuffer = dlb.asShortBuffer();
			drawListBuffer.put(drawOrder);
			drawListBuffer.position(0);

			int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

			mProgram = GLES20.glCreateProgram();
			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragmentShader);
			GLES20.glLinkProgram(mProgram);
		}

		public void draw(float[] mvpMatrix) {
			GLES20.glUseProgram(mProgram);
			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
					GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);
			mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
			GLES20.glUniform4fv(mColorHandle, 1, color, 0);
			// get handle to shape's transformation matrix
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			// Pass the projection and view transformation to the shader
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);


			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
			GLES20.glDisableVertexAttribArray(mPositionHandle);
		}
	}

	static class Pizza {

		private FloatBuffer vertexBuffer;
		private ShortBuffer drawListBuffer;

		// Use to access and set the view transformation
		private int mMVPMatrixHandle;

		// number of coordinates per vertex in this array
		static final int COORDS_PER_VERTEX = 3;
		//		static float squareCoords[] = {
//				0f, 0f , 0f,
//				(float) Math.cos(Math.PI/2 * 20/20),  (float) Math.sin(Math.PI/2 * 20/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 19/20),  (float) Math.sin(Math.PI/2 * 19/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 18/20),  (float) Math.sin(Math.PI/2 * 18/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 17/20),  (float) Math.sin(Math.PI/2 * 17/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 16/20),  (float) Math.sin(Math.PI/2 * 16/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 15/20),  (float) Math.sin(Math.PI/2 * 15/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 14/20),  (float) Math.sin(Math.PI/2 * 14/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 13/20),  (float) Math.sin(Math.PI/2 * 13/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 12/20),  (float) Math.sin(Math.PI/2 * 12/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 11/20),  (float) Math.sin(Math.PI/2 * 11/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 10/20),  (float) Math.sin(Math.PI/2 * 10/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 9/20),  (float) Math.sin(Math.PI/2 * 9/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 8/20),  (float) Math.sin(Math.PI/2 * 8/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 7/20),  (float) Math.sin(Math.PI/2 * 7/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 6/20),  (float) Math.sin(Math.PI/2 * 6/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 5/20),  (float) Math.sin(Math.PI/2 * 5/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 4/20),  (float) Math.sin(Math.PI/2 * 4/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 3/20),  (float) Math.sin(Math.PI/2 * 3/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 2/20),  (float) Math.sin(Math.PI/2 * 2/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 1/20),  (float) Math.sin(Math.PI/2 * 1/20), 0.0f,
//				(float) Math.cos(Math.PI/2 * 0/20),  (float) Math.sin(Math.PI/2 * 0/20), 0.0f,}; // top right
		static float squareCoords[];
		private short[] drawOrder; // order to draw vertices

//		private short drawOrder[] = {
//				0, 1, 2,
//				0, 2, 3,
//				0, 3, 4,
//				0, 4, 5,
//				0, 5, 6,
//				0, 6, 7,
//				0, 7, 8,
//				0, 8, 9,
//				0, 9, 10,
//				0, 10, 11,
//				0, 11, 12,
//				0, 12, 13,
//				0, 13, 14,
//				0, 14, 15,
//				0, 15, 16,
//				0, 16, 17,
//				0, 17, 18,
//				0, 18, 19,
//				0, 19, 20,
//				0, 20, 21,
//		}; // order to draw vertices

		int count = 10000;
		float radius = 0.3f;

		private short[] getDrawOrder() {
			ArrayList<Short> temp1 = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				temp1.add((short) 0);
				temp1.add((short) (i + 1));
				temp1.add((short) (i + 2));
			}

			short[] temp2 = new short[temp1.size()];
			for (int i = 0; i < temp1.size(); i++) {
				temp2[i] = temp1.get(i);
			}
			return temp2;
		}

		private float[] getSquareCoords() {
			ArrayList<Float> temp1 = new ArrayList<>();
			temp1.add(0f);
			temp1.add(0f);
			temp1.add(0f);

			for (int i = 0; i < count; i++) {
				temp1.add((float) (radius * Math.cos(Math.PI / 2 * (count - i) / count)));
				temp1.add((float) (radius * Math.sin(Math.PI / 2 * (count - i) / count)));
				temp1.add(0f);
			}

			float[] temp2 = new float[temp1.size()];
			for (int i = 0; i < temp1.size(); i++) {
				temp2[i] = temp1.get(i);
			}
			return temp2;
		}

		private final int mProgram;

		private int mPositionHandle;
		private int mColorHandle;

		private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
		private int vertexCount;

		public Pizza() {
			drawOrder = getDrawOrder();
			squareCoords = getSquareCoords();
			vertexCount = squareCoords.length / COORDS_PER_VERTEX;
			// 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
			ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			vertexBuffer = bb.asFloatBuffer();
			vertexBuffer.put(squareCoords);
			vertexBuffer.position(0);

			// 初始化ByteBuffer，长度为arr数组的长度*2，因为一个short占2个字节
			ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
			dlb.order(ByteOrder.nativeOrder());
			drawListBuffer = dlb.asShortBuffer();
			drawListBuffer.put(drawOrder);
			drawListBuffer.position(0);

			int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

			mProgram = GLES20.glCreateProgram();
			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragmentShader);
			GLES20.glLinkProgram(mProgram);
		}

		public void draw(float[] mvpMatrix) {
			GLES20.glUseProgram(mProgram);
			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
					GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);
			mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

			GLES20.glUniform4fv(mColorHandle, 1, color, 0);

			mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			// Pass the projection and view transformation to the shader
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);


			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
			GLES20.glDisableVertexAttribArray(mPositionHandle);
		}

		private IntBuffer intBufferUtil(int[] arr) {
			IntBuffer mBuffer;
			// 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
			ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
			// 数组排列用nativeOrder
			qbb.order(ByteOrder.nativeOrder());
			mBuffer = qbb.asIntBuffer();
			mBuffer.put(arr);
			mBuffer.position(0);
			return mBuffer;
		}

		private FloatBuffer floatBufferUtil(float[] arr) {
			FloatBuffer mBuffer;
			// 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
			ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
			// 数组排列用nativeOrder
			qbb.order(ByteOrder.nativeOrder());
			mBuffer = qbb.asFloatBuffer();
			mBuffer.put(arr);
			mBuffer.position(0);
			return mBuffer;
		}

		private ShortBuffer shortBufferUtil(short[] arr) {
			ShortBuffer mBuffer;
			// 初始化ByteBuffer，长度为arr数组的长度*2，因为一个short占2个字节
			ByteBuffer dlb = ByteBuffer.allocateDirect(
					// (# of coordinate values * 2 bytes per short)
					arr.length * 2);
			dlb.order(ByteOrder.nativeOrder());
			mBuffer = dlb.asShortBuffer();
			mBuffer.put(arr);
			mBuffer.position(0);
			return mBuffer;
		}
	}
}
