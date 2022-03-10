package com.refinedmods.refinedstorage.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Needs to be implemented on a public static {@link IRSAPI} field.
 */
@Target(ElementType.FIELD)
public @interface RSAPIInject {
}
