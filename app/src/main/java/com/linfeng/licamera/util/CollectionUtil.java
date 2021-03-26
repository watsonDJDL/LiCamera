package com.linfeng.licamera.util;

import java.util.Collection;

public class CollectionUtil {

    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }
}
