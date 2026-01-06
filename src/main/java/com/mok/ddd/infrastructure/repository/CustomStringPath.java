package com.mok.ddd.infrastructure.repository;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.StringExpression;

import java.io.Serial;
import java.lang.reflect.AnnotatedElement;

public class CustomStringPath extends StringExpression implements Path<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final PathImpl<String> pathMixin;

    protected CustomStringPath(PathImpl<String> mixin) {
        super(mixin);
        this.pathMixin = mixin;
    }

    public CustomStringPath(Path<?> parent, String property) {
        this(PathMetadataFactory.forProperty(parent, property));
    }

    public CustomStringPath(PathMetadata metadata) {
        super(ExpressionUtils.path(String.class, metadata));
        this.pathMixin = (PathImpl<String>)this.mixin;
    }

    public CustomStringPath(String var) {
        this(PathMetadataFactory.forVariable(var));
    }

    public final <R, C> R accept(Visitor<R, C> v, C context) {
        return (R)v.visit(this.pathMixin, context);
    }

    public PathMetadata getMetadata() {
        return this.pathMixin.getMetadata();
    }

    public Path<?> getRoot() {
        return this.pathMixin.getRoot();
    }

    public AnnotatedElement getAnnotatedElement() {
        return this.pathMixin.getAnnotatedElement();
    }
}
