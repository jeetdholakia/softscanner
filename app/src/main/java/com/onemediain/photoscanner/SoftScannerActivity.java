package com.onemediain.photoscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SoftScannerActivity extends Activity {

    private static final String TAG = "SoftScannerActivity";
    private static final int SELECT_PICTURE = 1;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    Mat sampledImage = null;
    Mat originalImage = null;
    ArrayList<org.opencv.core.Point> corners = new ArrayList<org.opencv.core.Point>();
    private String selectedImagePath;
    private double downSampleRatio = 0;

    private static double calculateSubSampleSize(Mat srcImage, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = srcImage.height();
        final int width = srcImage.width();
        double inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final double heightRatio = (double) reqHeight / (double) height;
            final double widthRatio = (double) reqWidth / (double) width;

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_scanner);

        final ImageView iv = findViewById(R.id.SSImageView);
        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.i(TAG, "event.getX(), event.getY(): " + event.getX() + " " + event.getY());
                int projectedX = (int) ((double) event.getX() * ((double) sampledImage.width() / (double) view.getWidth()));
                int projectedY = (int) ((double) event.getY() * ((double) sampledImage.height() / (double) view.getHeight()));
                org.opencv.core.Point corner = new org.opencv.core.Point(projectedX, projectedY);
                corners.add(corner);
                Imgproc.circle(sampledImage, corner, 5, new Scalar(0, 0, 255), 2);
                displayImage(sampledImage);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.soft_scanner, menu);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "selectedImagePath: " + selectedImagePath);
                loadImage(selectedImagePath);
                displayImage(sampledImage);
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    private String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private void displayImage(Mat image) {
        // convert to bitmap:
        Bitmap bitMap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(image, bitMap);

        // find the imageview and draw it!
        ImageView iv = findViewById(R.id.SSImageView);
        iv.setImageBitmap(bitMap);
    }

    private void loadImage(String path) {
        originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();
        sampledImage = new Mat();

        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        downSampleRatio = calculateSubSampleSize(rgbImage, width, height);

        Imgproc.resize(rgbImage, sampledImage, new Size(), downSampleRatio, downSampleRatio, Imgproc.INTER_AREA);

        try {
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    //get the mirrored image
                    sampledImage = sampledImage.t();
                    //flip on the y-axis
                    Core.flip(sampledImage, sampledImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    //get up side down image
                    sampledImage = sampledImage.t();
                    //Flip on the x-axis
                    Core.flip(sampledImage, sampledImage, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_openGallary) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), SELECT_PICTURE);
            return true;
        } else if (id == R.id.action_HTL) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat binaryImage = new Mat();
            Imgproc.cvtColor(sampledImage, binaryImage, Imgproc.COLOR_RGB2GRAY);
            Imgproc.Canny(binaryImage, binaryImage, 80, 100);

            Mat lines = new Mat();
            int threshold = 180;
            Imgproc.HoughLinesP(binaryImage, lines, 1, Math.PI / 180, threshold);

            Imgproc.cvtColor(binaryImage, binaryImage, Imgproc.COLOR_GRAY2RGB);
            for (int i = 0; i < lines.cols(); i++) {
                double[] line = lines.get(0, i);
                double xStart = line[0],
                        yStart = line[1],
                        xEnd = line[2],
                        yEnd = line[3];
                org.opencv.core.Point lineStart = new org.opencv.core.Point(xStart, yStart);
                org.opencv.core.Point lineEnd = new org.opencv.core.Point(xEnd, yEnd);

                Imgproc.line(binaryImage, lineStart, lineEnd, new Scalar(0, 0, 255), 3);
            }
            displayImage(binaryImage);

            return true;
        } else if (id == R.id.action_CHT) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat grayImage = new Mat();
            Imgproc.cvtColor(sampledImage, grayImage, Imgproc.COLOR_RGB2GRAY);

            double minDist = 50;
            int thickness = 5;
            double cannyHighThreshold = 200;
            double accumlatorThreshold = 100;
            Mat circles = new Mat();
            Imgproc.HoughCircles(grayImage, circles, Imgproc.CV_HOUGH_GRADIENT, 1, minDist, cannyHighThreshold, accumlatorThreshold, 0, 0);

            Imgproc.cvtColor(grayImage, grayImage, Imgproc.COLOR_GRAY2RGB);
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                double centerX = circle[0],
                        centerY = circle[1],
                        radius = circle[2];
                org.opencv.core.Point center = new org.opencv.core.Point(centerX, centerY);
                Imgproc.circle(grayImage, center, (int) radius, new Scalar(0, 0, 255), thickness);
            }
            displayImage(grayImage);
            return true;
        } else if (id == R.id.action_average) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat blurredImage = new Mat();
            Size size = new Size(7, 7);
            Imgproc.blur(sampledImage, blurredImage, size);

            displayImage(blurredImage);
            return true;
        } else if (id == R.id.action_gaussian) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat blurredImage = new Mat();
            Size size = new Size(35, 35);
            Imgproc.GaussianBlur(sampledImage, blurredImage, size, 0, 0);

            displayImage(blurredImage);
            return true;
        } else if (id == R.id.action_median) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat blurredImage = new Mat();
            int kernelDim = 5;
            Imgproc.medianBlur(sampledImage, blurredImage, kernelDim);

            displayImage(blurredImage);
            return true;
        } else if (id == R.id.action_bilateral) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat blurredImage = new Mat();
            int kernelDim = 41;
            Imgproc.bilateralFilter(sampledImage, blurredImage, kernelDim, 150, 450);

            displayImage(blurredImage);
            return true;
        } else if (id == R.id.action_sobel) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat blurredImage = new Mat();
            Size size = new Size(7, 7);
            Imgproc.GaussianBlur(sampledImage, blurredImage, size, 0, 0);

            Mat gray = new Mat();
            Imgproc.cvtColor(blurredImage, gray, Imgproc.COLOR_RGB2GRAY);

            Mat xD = new Mat(), yD = new Mat();
            Mat absXD = new Mat(), absYD = new Mat();
            int ddepth = CvType.CV_16S;
            Imgproc.Sobel(gray, xD, ddepth, 1, 0);
            Imgproc.Sobel(gray, yD, ddepth, 0, 1);

            Core.convertScaleAbs(xD, absXD);
            Core.convertScaleAbs(yD, absYD);

            Mat edgeImage = new Mat();
            Core.addWeighted(absXD, 0.5, absYD, 0.5, 0, edgeImage);

            displayImage(edgeImage);
            return true;
        } else if (id == R.id.action_canny) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat gray = new Mat();
            Imgproc.cvtColor(sampledImage, gray, Imgproc.COLOR_RGB2GRAY);

            Mat edgeImage = new Mat();
            Imgproc.Canny(gray, edgeImage, 100, 200);

            displayImage(edgeImage);
            return true;
        } else if (id == R.id.action_revert) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            displayImage(sampledImage);
            return true;
        } else if (id == R.id.action_rigidscan) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            Mat gray = new Mat();
            Imgproc.cvtColor(sampledImage, gray, Imgproc.COLOR_RGB2GRAY);
            Mat edgeImage = new Mat();
            Imgproc.Canny(gray, edgeImage, 100, 200);

            Mat lines = new Mat();
            int threshold = 180;
            Imgproc.HoughLinesP(edgeImage, lines, 1, Math.PI / 180, threshold, 60, 10);

            boolean[] include = new boolean[lines.cols()];
            double maxTop = edgeImage.rows();
            double maxBottom = 0;
            double maxRight = 0;
            double maxLeft = edgeImage.cols();
            int leftLine = 0;
            int rightLine = 0;
            int topLine = 0;
            int bottomLine = 0;
            ArrayList<org.opencv.core.Point> points = new ArrayList<org.opencv.core.Point>();

            for (int i = 0; i < lines.cols(); i++) {
                double[] line = lines.get(0, i);
                double xStart = line[0],
                        xEnd = line[2];
                if (xStart < maxLeft && !include[i]) {
                    maxLeft = xStart;
                    leftLine = i;

                }
                if (xEnd < maxLeft && !include[i]) {
                    maxLeft = xEnd;
                    leftLine = i;

                }
            }
            include[leftLine] = true;
            double[] line = lines.get(0, leftLine);
            double xStartleftLine = line[0],
                    yStartleftLine = line[1],
                    xEndleftLine = line[2],
                    yEndleftLine = line[3];
            org.opencv.core.Point lineStartleftLine = new org.opencv.core.Point(xStartleftLine, yStartleftLine);
            org.opencv.core.Point lineEndleftLine = new org.opencv.core.Point(xEndleftLine, yEndleftLine);
            points.add(lineStartleftLine);
            points.add(lineEndleftLine);

            for (int i = 0; i < lines.cols(); i++) {
                line = lines.get(0, i);
                double xStart = line[0],
                        xEnd = line[2];

                if (xStart > maxRight && !include[i]) {
                    maxRight = xStart;
                    rightLine = i;

                }
                if (xEnd > maxRight && !include[i]) {
                    maxRight = xEnd;
                    rightLine = i;

                }
            }
            include[rightLine] = true;

            line = lines.get(0, rightLine);
            double xStartRightLine = line[0],
                    yStartRightLine = line[1],
                    xEndRightLine = line[2],
                    yEndRightLine = line[3];
            org.opencv.core.Point lineStartRightLine = new org.opencv.core.Point(xStartRightLine, yStartRightLine);
            org.opencv.core.Point lineEndRightLine = new org.opencv.core.Point(xEndRightLine, yEndRightLine);
            points.add(lineStartRightLine);
            points.add(lineEndRightLine);

            for (int i = 0; i < lines.cols(); i++) {
                line = lines.get(0, i);
                double yStart = line[1],
                        yEnd = line[3];

                if (yStart < maxTop && !include[i]) {
                    maxTop = yStart;
                    topLine = i;

                }
                if (yEnd < maxTop && !include[i]) {
                    maxTop = yEnd;
                    topLine = i;

                }
            }
            include[topLine] = true;

            line = lines.get(0, topLine);
            double xStartTopLine = line[0],
                    yStartTopLine = line[1],
                    xEndTopLine = line[2],
                    yEndTopLine = line[3];
            org.opencv.core.Point lineStartTopLine = new org.opencv.core.Point(xStartTopLine, yStartTopLine);
            org.opencv.core.Point lineEndTopLine = new org.opencv.core.Point(xEndTopLine, yEndTopLine);
            points.add(lineStartTopLine);
            points.add(lineEndTopLine);

            for (int i = 0; i < lines.cols(); i++) {
                line = lines.get(0, i);
                double yStart = line[1],
                        yEnd = line[3];
                if (yStart > maxBottom && !include[i]) {
                    maxBottom = yStart;
                    bottomLine = i;

                }
                if (yEnd > maxBottom && !include[i]) {
                    maxBottom = yEnd;
                    bottomLine = i;

                }
            }
            include[bottomLine] = true;
            line = lines.get(0, bottomLine);
            double xStartBottomLine = line[0],
                    yStartBottomLine = line[1],
                    xEndBottomLine = line[2],
                    yEndBottomLine = line[3];
            org.opencv.core.Point lineStartBottomLine = new org.opencv.core.Point(xStartBottomLine, yStartBottomLine);
            org.opencv.core.Point lineEndBottomLine = new org.opencv.core.Point(xEndBottomLine, yEndBottomLine);
            points.add(lineStartBottomLine);
            points.add(lineEndBottomLine);

            MatOfPoint2f mat = new MatOfPoint2f();
            mat.fromList(points);

            RotatedRect rect = Imgproc.minAreaRect(mat);

            org.opencv.core.Point rect_points[] = new org.opencv.core.Point[4];
            rect.points(rect_points);
//			for( int j = 0; j < 4; j++ )
//			Core.line(sampledImage, rect_points[j], rect_points[(j+1)%4], new Scalar(0,0,255), 3);
//			displayImage(sampledImage);


            Mat correctedImage = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type());
            Mat srcPoints = Converters.vector_Point2f_to_Mat(Arrays.asList(rect_points));
            Mat destPoints = Converters.vector_Point2f_to_Mat(Arrays.asList(new org.opencv.core.Point(0, correctedImage.rows()),
                    new org.opencv.core.Point(0, 0),
                    new org.opencv.core.Point(correctedImage.cols(), 0),
                    new org.opencv.core.Point(correctedImage.cols(), correctedImage.rows())));

            Mat transformation = Imgproc.getPerspectiveTransform(srcPoints, destPoints);
            Imgproc.warpPerspective(sampledImage, correctedImage, transformation, correctedImage.size());
            displayImage(correctedImage);
        } else if (id == R.id.action_flexscan) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat gray = new Mat();
            Imgproc.cvtColor(sampledImage, gray, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(15, 15), 0);

            Mat edgeImage = new Mat();
            Imgproc.Canny(gray, edgeImage, 150, 300);

            Mat lines = new Mat();
            int threshold = 200;
            Imgproc.HoughLinesP(edgeImage, lines, 1, Math.PI / 180, threshold, 100, 60);
            ArrayList<org.opencv.core.Point> flexCorners = new ArrayList<org.opencv.core.Point>();

            //Find the intersection of the four lines to get the four corners
            for (int i = 0; i < lines.cols(); i++) {
                for (int j = i + 1; j < lines.cols(); j++) {
                    org.opencv.core.Point intersectionPoint = getLinesIntersection(lines.get(0, i), lines.get(0, j));
                    if (intersectionPoint != null) {
                        Log.i(TAG, "intersectionPoint: " + intersectionPoint.x + " " + intersectionPoint.y);
                        flexCorners.add(intersectionPoint);
                    }
                }
            }

            MatOfPoint2f cornersMat = new MatOfPoint2f();
            cornersMat.fromList(flexCorners);
            Log.i(TAG, "cornersMat: " + cornersMat);
            MatOfPoint2f approxConrers = new MatOfPoint2f();
            Imgproc.approxPolyDP(cornersMat, approxConrers, Imgproc.arcLength(cornersMat, true) * 0.02, true);
            Log.i(TAG, "approxConrers: " + approxConrers);
            if (approxConrers.rows() < 4) {
                Context context = getApplicationContext();
                CharSequence text = "Couldn't detect an object with four corners!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            //find the centroid of the polygon to order the found corners
            flexCorners.clear();
            Converters.Mat_to_vector_Point2f(approxConrers, flexCorners);
            org.opencv.core.Point centroid = new org.opencv.core.Point(0, 0);

            for (org.opencv.core.Point point : flexCorners) {
                Log.i(TAG, "Point x: " + point.x + " Point y: " + point.y);
                centroid.x += point.x;
                centroid.y += point.y;
            }
            centroid.x /= ((double) flexCorners.size());
            centroid.y /= ((double) flexCorners.size());

            sortCorners(flexCorners, centroid);

            for (org.opencv.core.Point point : flexCorners) {
                Log.i(TAG, "PointAfterSort x: " + point.x + " PointAfterSort y: " + point.y);
                Imgproc.circle(sampledImage, point, 10, new Scalar(0, 0, 255), 2);
            }

            Mat correctedImage = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type());
            Mat srcPoints = Converters.vector_Point2f_to_Mat(flexCorners);

            Mat destPoints = Converters.vector_Point2f_to_Mat(Arrays.asList(new org.opencv.core.Point(30, 30),
                    new org.opencv.core.Point(correctedImage.cols() - 30, 30),
                    new org.opencv.core.Point(correctedImage.cols() - 30, correctedImage.rows() - 30),
                    new org.opencv.core.Point(30, correctedImage.rows() - 30)));

            Mat transformation = Imgproc.getPerspectiveTransform(srcPoints, destPoints);
            Imgproc.warpPerspective(sampledImage, correctedImage, transformation, correctedImage.size());
            displayImage(correctedImage);
        } else if (id == R.id.action_manScan) {
            if (sampledImage == null) {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            if (corners.size() != 4) {
                Context context = getApplicationContext();
                CharSequence text = "You need to select four corners!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                corners.clear();
                return true;
            }
            //find the centroid of the polygon to order the found corners
            org.opencv.core.Point centroid = new org.opencv.core.Point(0, 0);
            for (org.opencv.core.Point point : corners) {
                centroid.x += point.x;
                centroid.y += point.y;
            }
            centroid.x /= corners.size();
            centroid.y /= corners.size();

            sortCorners(corners, centroid);
            Mat correctedImage = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type());
            Mat srcPoints = Converters.vector_Point2f_to_Mat(corners);

            Mat destPoints = Converters.vector_Point2f_to_Mat(Arrays.asList(new org.opencv.core.Point(0, 0),
                    new org.opencv.core.Point(correctedImage.cols(), 0),
                    new org.opencv.core.Point(correctedImage.cols(), correctedImage.rows()),
                    new org.opencv.core.Point(0, correctedImage.rows())));

            Mat transformation = Imgproc.getPerspectiveTransform(srcPoints, destPoints);
            Imgproc.warpPerspective(sampledImage, correctedImage, transformation, correctedImage.size());

            displayImage(correctedImage);
        }

        return super.onOptionsItemSelected(item);
    }

    private org.opencv.core.Point getLinesIntersection(double[] firstLine, double[] secondLine) {
        double FX1 = firstLine[0], FY1 = firstLine[1], FX2 = firstLine[2], FY2 = firstLine[3];
        double SX1 = secondLine[0], SY1 = secondLine[1], SX2 = secondLine[2], SY2 = secondLine[3];
        org.opencv.core.Point intersectionPoint = null;
        //Make sure the we will not divide by zero
        double denominator = (FX1 - FX2) * (SY1 - SY2) - (FY1 - FY2) * (SX1 - SX2);
        if (denominator != 0) {
            intersectionPoint = new org.opencv.core.Point();
            intersectionPoint.x = ((FX1 * FY2 - FY1 * FX2) * (SX1 - SX2) - (FX1 - FX2) * (SX1 * SY2 - SY1 * SX2)) / denominator;
            intersectionPoint.y = ((FX1 * FY2 - FY1 * FX2) * (SY1 - SY2) - (FY1 - FY2) * (SX1 * SY2 - SY1 * SX2)) / denominator;
            if (intersectionPoint.x < 0 || intersectionPoint.y < 0)
                return null;
        }
        return intersectionPoint;
    }

    void sortCorners(ArrayList<org.opencv.core.Point> corners, org.opencv.core.Point center) {
        ArrayList<org.opencv.core.Point> top = new ArrayList<org.opencv.core.Point>();
        ArrayList<org.opencv.core.Point> bottom = new ArrayList<org.opencv.core.Point>();

        for (int i = 0; i < corners.size(); i++) {
            if (corners.get(i).y < center.y)
                top.add(corners.get(i));
            else
                bottom.add(corners.get(i));
        }

        double topLeft = top.get(0).x;
        int topLeftIndex = 0;
        for (int i = 1; i < top.size(); i++) {
            if (top.get(i).x < topLeft) {
                topLeft = top.get(i).x;
                topLeftIndex = i;
            }
        }

        double topRight = 0;
        int topRightIndex = 0;
        for (int i = 0; i < top.size(); i++) {
            if (top.get(i).x > topRight) {
                topRight = top.get(i).x;
                topRightIndex = i;
            }
        }

        double bottomLeft = bottom.get(0).x;
        int bottomLeftIndex = 0;
        for (int i = 1; i < bottom.size(); i++) {
            if (bottom.get(i).x < bottomLeft) {
                bottomLeft = bottom.get(i).x;
                bottomLeftIndex = i;
            }
        }

        double bottomRight = bottom.get(0).x;
        int bottomRightIndex = 0;
        for (int i = 1; i < bottom.size(); i++) {
            if (bottom.get(i).x > bottomRight) {
                bottomRight = bottom.get(i).x;
                bottomRightIndex = i;
            }
        }

        org.opencv.core.Point topLeftPoint = top.get(topLeftIndex);
        org.opencv.core.Point topRightPoint = top.get(topRightIndex);
        org.opencv.core.Point bottomLeftPoint = bottom.get(bottomLeftIndex);
        org.opencv.core.Point bottomRightPoint = bottom.get(bottomRightIndex);

        corners.clear();
        corners.add(topLeftPoint);
        corners.add(topRightPoint);
        corners.add(bottomRightPoint);
        corners.add(bottomLeftPoint);
    }
}
