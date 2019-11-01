package com.andylai.glsurfacedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.andylai.glsurfacedemo.GLShape.Polygon;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.andylai.glsurfacedemo.GLShape.*;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
		MyGLSurfaceView glSurfaceView = new MyGLSurfaceView(this);
		setContentView(glSurfaceView);

		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

		Log.d("Andy", "" + Double.parseDouble(configurationInfo.getGlEsVersion()));
		Log.d("Andy", "" + (configurationInfo.reqGlEsVersion >= 0x20000));
		Log.d("Andy", "" + String.format("%X", configurationInfo.reqGlEsVersion));
	}

	class MyGLSurfaceView extends GLSurfaceView {
		private Context mContext;

		public MyGLSurfaceView(Context context) {
			super(context);
			mContext = context;
			// Create an OpenGL ES 2.0 context
			setEGLContextClientVersion(2);
			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(new MyGLRenderer());
//			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		}

		class MyGLRenderer implements GLSurfaceView.Renderer {
			private Triangle mTriangle;
			private Square mSquare;
			private Pizza mPizza;
			private Polygon mPolygon;

			private final float[] mMVPMatrix = new float[16];
			private final float[] mProjectionMatrix = new float[16];
			private final float[] mViewMatrix = new float[16];

			@Override
			public void onSurfaceCreated(GL10 unused, EGLConfig config) {
				// Set the background frame color
				GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
				mTriangle = new Triangle();
				mSquare = new Square();
				mPizza = new Pizza();
				mPolygon = new Polygon();
			}

			private float[] mRotationMatrix = new float[16];

			@Override
			public void onDrawFrame(GL10 unused) {
				float[] scratch = new float[16];

				// Set the camera position (View matrix)

				float eyeX = 0f;
				float eyeY = 0f;
				float eyeZ = -3f;
				float centerX = 0f;
				float centerY = 0f;
				float centerZ = 0f;
				float upX = 0f;
				float upY = 1f;
				float upZ = 0f;
				Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

				// Calculate the projection and view transformation
				Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

//				// Draw shape
//				mTriangle.draw(mMVPMatrix);
//				mSquare.draw(mMVPMatrix);
//				mPizza.draw(mMVPMatrix);

//				// 创建一个旋转矩阵
				long time = SystemClock.uptimeMillis() % 4000L;
				float angle = 0.090f * ((int) time);
				Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
//
//				// 将旋转矩阵与投影和相机视图组合在一起
				// Note that the mMVPMatrix factor *must be first* in order
				// for the matrix multiplication product to be correct.
				Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
//
//				// Draw triangle
				mSquare.draw(mMVPMatrix);
//				mPizza.draw(scratch);
//				mPolygon.draw(mMVPMatrix);
			}

			@Override
			public void onSurfaceChanged(GL10 unused, int width, int height) {
				GLES20.glViewport(0, 0, width, height);
				float ratio = (float) width / height;
				// 这个投影矩阵被应用于对象坐标在onDrawFrame（）方法中
				Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
			}
		}
	}
}
