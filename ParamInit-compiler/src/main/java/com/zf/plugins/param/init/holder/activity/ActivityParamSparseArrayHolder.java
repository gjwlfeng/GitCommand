package com.zf.plugins.param.init.holder.activity;

import com.zf.plugins.param.init.AnnotationEnv;

import javax.lang.model.element.Element;

public abstract class ActivityParamSparseArrayHolder extends ActivityParamHolder {

    public ActivityParamSparseArrayHolder(AnnotationEnv annotationEnv, Element element, boolean isSupportV4, boolean isAndroidX) {
        super(annotationEnv, element, isSupportV4, isAndroidX);
    }


}