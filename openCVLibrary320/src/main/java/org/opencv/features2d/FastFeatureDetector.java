
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;

// C++: class FastFeatureDetector
//javadoc: FastFeatureDetector
public class FastFeatureDetector extends Feature2D {

    public static final int
            TYPE_5_8 = 0,
            TYPE_7_12 = 1,
            TYPE_9_16 = 2,
            THRESHOLD = 10000,
            NONMAX_SUPPRESSION = 10001,
            FAST_N = 10002;


    protected FastFeatureDetector(long addr) {
        super(addr);
    }


    //
    // C++: static Ptr_FastFeatureDetector create(int threshold = 10, bool nonmaxSuppression = true, int type = FastFeatureDetector::TYPE_9_16)
    //

    //javadoc: FastFeatureDetector::create(threshold, nonmaxSuppression, type)
    public static FastFeatureDetector create(int threshold, boolean nonmaxSuppression, int type) {

        FastFeatureDetector retVal = new FastFeatureDetector(create_0(threshold, nonmaxSuppression, type));

        return retVal;
    }

    //javadoc: FastFeatureDetector::create()
    public static FastFeatureDetector create() {

        FastFeatureDetector retVal = new FastFeatureDetector(create_1());

        return retVal;
    }


    //
    // C++:  bool getNonmaxSuppression()
    //

    // C++: static Ptr_FastFeatureDetector create(int threshold = 10, bool nonmaxSuppression = true, int type = FastFeatureDetector::TYPE_9_16)
    private static native long create_0(int threshold, boolean nonmaxSuppression, int type);


    //
    // C++:  int getThreshold()
    //

    private static native long create_1();


    //
    // C++:  int getType()
    //

    // C++:  bool getNonmaxSuppression()
    private static native boolean getNonmaxSuppression_0(long nativeObj);


    //
    // C++:  void setNonmaxSuppression(bool f)
    //

    // C++:  int getThreshold()
    private static native int getThreshold_0(long nativeObj);


    //
    // C++:  void setThreshold(int threshold)
    //

    // C++:  int getType()
    private static native int getType_0(long nativeObj);


    //
    // C++:  void setType(int type)
    //

    // C++:  void setNonmaxSuppression(bool f)
    private static native void setNonmaxSuppression_0(long nativeObj, boolean f);

    // C++:  void setThreshold(int threshold)
    private static native void setThreshold_0(long nativeObj, int threshold);

    // C++:  void setType(int type)
    private static native void setType_0(long nativeObj, int type);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: FastFeatureDetector::getNonmaxSuppression()
    public boolean getNonmaxSuppression() {

        boolean retVal = getNonmaxSuppression_0(nativeObj);

        return retVal;
    }

    //javadoc: FastFeatureDetector::setNonmaxSuppression(f)
    public void setNonmaxSuppression(boolean f) {

        setNonmaxSuppression_0(nativeObj, f);

        return;
    }

    //javadoc: FastFeatureDetector::getThreshold()
    public int getThreshold() {

        int retVal = getThreshold_0(nativeObj);

        return retVal;
    }

    //javadoc: FastFeatureDetector::setThreshold(threshold)
    public void setThreshold(int threshold) {

        setThreshold_0(nativeObj, threshold);

        return;
    }

    //javadoc: FastFeatureDetector::getType()
    public int getType() {

        int retVal = getType_0(nativeObj);

        return retVal;
    }

    //javadoc: FastFeatureDetector::setType(type)
    public void setType(int type) {

        setType_0(nativeObj, type);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
