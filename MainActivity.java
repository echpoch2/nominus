package com.example.nexyi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Exchanger;

class buffer{
    public static MatOfRect points = new MatOfRect();
}
class Asc extends AsyncTask<Object, Void, Void>
{
    @Override
    protected Void doInBackground(Object...objects) {
        CascadeClassifier FD = (CascadeClassifier)objects[0];
        Mat rgba = (Mat) objects[1];
        FD.detectMultiScale(rgba, buffer.points);
        return null;
    }
}

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView javaCameraView;
    File cascFile;
    CascadeClassifier faceDetector;
    private Mat mRgba, mGrey;
    Exchanger<MatOfRect> ex = new Exchanger<MatOfRect>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaCameraView=(JavaCameraView)findViewById(R.id.javaCamView);
        if(!OpenCVLoader.initDebug())
        {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this,baseCallBack);
        }
        else
        {
            try {
                baseCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        javaCameraView.setCvCameraViewListener(this);
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
    mRgba= new Mat();
    mGrey = new Mat();
    }
    @Override
    public void onCameraViewStopped() {
    mRgba.release();
    mGrey.release();
    }
int count=0;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGrey = inputFrame.gray();
        new Asc().execute((Object) faceDetector, (Object) mRgba);
        count=0;
        for (Rect rect : buffer.points.toArray()) {
                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0));
        for (Rect rect : buffer.points.toArray()) {
            Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0));
        }
        return mRgba;
    }
    private BaseLoaderCallback baseCallBack= new BaseLoaderCallback(this ) {
        @Override
        public void onManagerConnected(int status) throws IOException {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
                    FileOutputStream fos = new FileOutputStream(cascFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer))!=-1)
                    {
                        fos.write(buffer, 0,bytesRead);

                    }
                    is.close();
                    fos.close();
                    faceDetector= new CascadeClassifier(cascFile.getAbsolutePath());
                    if (faceDetector.empty())
                    {
                        faceDetector = null;
                    }
                    else
                    {
                        cascadeDir.delete();
                    }
                    javaCameraView.enableView();

                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
            super.onManagerConnected(status);
        }
    };
}
