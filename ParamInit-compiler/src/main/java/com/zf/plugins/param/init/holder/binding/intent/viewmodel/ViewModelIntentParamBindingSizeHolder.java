package com.zf.plugins.param.init.holder.binding.intent.viewmodel;

import com.squareup.javapoet.MethodSpec;
import com.zf.plugins.param.init.AnnotationEnv;
import com.zf.plugins.param.init.holder.action.ViewModelCreationHolder;
import com.zf.plugins.param.init.holder.binding.intent.ViewModelIntentParamBindingHolder;

import javax.lang.model.element.Element;

public class ViewModelIntentParamBindingSizeHolder extends ViewModelIntentParamBindingHolder {

    private ViewModelIntentParamBindingSizeHolder(AnnotationEnv annotationEnv, Element element, boolean isSupportV4, boolean isAndroidX) {
        super(annotationEnv, element, isSupportV4, isAndroidX);
    }


    @Override
    public boolean onSetValue(MethodSpec.Builder methodSpec) {
        return false;
    }

    @Override
    public boolean onGetValue(MethodSpec.Builder methodSpec) {
        return false;

    }

    public static class CreationHolder extends ViewModelCreationHolder<ViewModelIntentParamBindingSizeHolder> {


        public CreationHolder(AnnotationEnv annotationEnv, Element element, boolean isSupportV4, boolean isAndroidX) {
            super(annotationEnv, element, isSupportV4, isAndroidX);
        }

        public ViewModelIntentParamBindingSizeHolder getHolder() {
            return new ViewModelIntentParamBindingSizeHolder(this.annotationEnv, this.element, isSupportV4, isAndroidX);
        }
    }
}
