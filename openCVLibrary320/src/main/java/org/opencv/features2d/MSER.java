
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.utils.Converters;

import java.util.List;

// C++: class MSER
//javadoc: MSER
public class MSER extends Feature2D {

    protected MSER(long addr) {
        super(addr);
    }


    //
    // C++: static Ptr_MSER create(int _delta = 5, int _min_area = 60, int _max_area = 14400, double _max_variation = 0.25, double _min_diversity = .2, int _max_evolution = 200, double _area_threshold = 1.01, double _min_margin = 0.003, int _edge_blur_size = 5)
    //

    //javadoc: MSER::create(_delta, _min_area, _max_area, _max_variation, _min_diversity, _max_evolution, _area_threshold, _min_margin, _edge_blur_size)
    public static MSER create(int _delta, int _min_area, int _max_area, double _max_variation, double _min_diversity, int _max_evolution, double _area_threshold, double _min_margin, int _edge_blur_size) {

        MSER retVal = new MSER(create_0(_delta, _min_area, _max_area, _max_variation, _min_diversity, _max_evolution, _area_threshold, _min_margin, _edge_blur_size));

        return retVal;
    }

    //javadoc: MSER::create()
    public static MSER create() {

        MSER retVal = new MSER(create_1());

        return retVal;
    }


    //
    // C++:  bool getPass2Only()
    //

    // C++: static Ptr_MSER create(int _delta = 5, int _min_area = 60, int _max_area = 14400, double _max_variation = 0.25, double _min_diversity = .2, int _max_evolution = 200, double _area_threshold = 1.01, double _min_margin = 0.003, int _edge_blur_size = 5)
    private static native long create_0(int _delta, int _min_area, int _max_area, double _max_variation, double _min_diversity, int _max_evolution, double _area_threshold, double _min_margin, int _edge_blur_size);


    //
    // C++:  int getDelta()
    //

    private static native long create_1();


    //
    // C++:  int getMaxArea()
    //

    // C++:  bool getPass2Only()
    private static native boolean getPass2Only_0(long nativeObj);


    //
    // C++:  int getMinArea()
    //

    // C++:  int getDelta()
    private static native int getDelta_0(long nativeObj);


    //
    // C++:  void detectRegions(Mat image, vector_vector_Point& msers, vector_Rect& bboxes)
    //

    // C++:  int getMaxArea()
    private static native int getMaxArea_0(long nativeObj);


    //
    // C++:  void setDelta(int delta)
    //

    // C++:  int getMinArea()
    private static native int getMinArea_0(long nativeObj);


    //
    // C++:  void setMaxArea(int maxArea)
    //

    // C++:  void detectRegions(Mat image, vector_vector_Point& msers, vector_Rect& bboxes)
    private static native void detectRegions_0(long nativeObj, long image_nativeObj, long msers_mat_nativeObj, long bboxes_mat_nativeObj);


    //
    // C++:  void setMinArea(int minArea)
    //

    // C++:  void setDelta(int delta)
    private static native void setDelta_0(long nativeObj, int delta);


    //
    // C++:  void setPass2Only(bool f)
    //

    // C++:  void setMaxArea(int maxArea)
    private static native void setMaxArea_0(long nativeObj, int maxArea);

    // C++:  void setMinArea(int minArea)
    private static native void setMinArea_0(long nativeObj, int minArea);

    // C++:  void setPass2Only(bool f)
    private static native void setPass2Only_0(long nativeObj, boolean f);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: MSER::getPass2Only()
    public boolean getPass2Only() {

        boolean retVal = getPass2Only_0(nativeObj);

        return retVal;
    }

    //javadoc: MSER::setPass2Only(f)
    public void setPass2Only(boolean f) {

        setPass2Only_0(nativeObj, f);

        return;
    }

    //javadoc: MSER::getDelta()
    public int getDelta() {

        int retVal = getDelta_0(nativeObj);

        return retVal;
    }

    //javadoc: MSER::setDelta(delta)
    public void setDelta(int delta) {

        setDelta_0(nativeObj, delta);

        return;
    }

    //javadoc: MSER::getMaxArea()
    public int getMaxArea() {

        int retVal = getMaxArea_0(nativeObj);

        return retVal;
    }

    //javadoc: MSER::setMaxArea(maxArea)
    public void setMaxArea(int maxArea) {

        setMaxArea_0(nativeObj, maxArea);

        return;
    }

    //javadoc: MSER::getMinArea()
    public int getMinArea() {

        int retVal = getMinArea_0(nativeObj);

        return retVal;
    }

    //javadoc: MSER::setMinArea(minArea)
    public void setMinArea(int minArea) {

        setMinArea_0(nativeObj, minArea);

        return;
    }

    //javadoc: MSER::detectRegions(image, msers, bboxes)
    public void detectRegions(Mat image, List<MatOfPoint> msers, MatOfRect bboxes) {
        Mat msers_mat = new Mat();
        Mat bboxes_mat = bboxes;
        detectRegions_0(nativeObj, image.nativeObj, msers_mat.nativeObj, bboxes_mat.nativeObj);
        Converters.Mat_to_vector_vector_Point(msers_mat, msers);
        msers_mat.release();
        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
