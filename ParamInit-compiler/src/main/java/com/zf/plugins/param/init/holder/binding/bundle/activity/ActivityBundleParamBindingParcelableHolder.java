package com.zf.plugins.param.init.holder.binding.bundle.activity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.zf.plugins.param.init.AnnotationEnv;
import com.zf.plugins.param.init.ClassNameConstant;
import com.zf.plugins.param.init.MethodSpecBuilderCallBack;
import com.zf.plugins.param.init.MethodSpecUtils;
import com.zf.plugins.param.init.holder.action.ActivityCreationHolder;
import com.zf.plugins.param.init.holder.binding.bundle.ActivityBundleParamBindingHolder;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public class ActivityBundleParamBindingParcelableHolder extends ActivityBundleParamBindingHolder {

    private ActivityBundleParamBindingParcelableHolder(AnnotationEnv annotationEnv, Element element, boolean isSupportV4, boolean isAndroidX) {
        super(annotationEnv, element, isSupportV4, isAndroidX);
    }

    @Override
    public boolean onSetValue(MethodSpec.Builder methodSpec) {
        methodSpec.beginControlFlow("if ( $N != null )", getOriginFiledName());
        MethodSpecUtils.codeBlock(methodSpec, new MethodSpecBuilderCallBack() {
            @Override
            public boolean innerBlock(MethodSpec.Builder builder) {
                builder.addStatement("bundle.putParcelable($N,$N)", getParamFiledName(), getOriginFiledName());
                return false;
            }
        });
        methodSpec.endControlFlow();
        return true;
    }

    @Override
    public boolean onGetValue(MethodSpec.Builder methodSpec) {
        TypeMirror typeMirror = getElement().asType();
        methodSpec.addAnnotation(ClassNameConstant.getNullableClassName(isAndroidX()));
        methodSpec.returns(ClassName.get(typeMirror));
        methodSpec.addStatement("return bundle.getParcelable($N)", getParamFiledName());
        return true;
    }

    public static class CreationHolder extends ActivityCreationHolder<ActivityBundleParamBindingParcelableHolder> {


        public CreationHolder(AnnotationEnv annotationEnv, Element element, boolean isSupportV4, boolean isAndroidX) {
            super(annotationEnv,element,isSupportV4,isAndroidX);
        }

        public ActivityBundleParamBindingParcelableHolder getHolder() {
            return new ActivityBundleParamBindingParcelableHolder(this.annotationEnv, this.element, isSupportV4, isAndroidX);
        }
    }
}
