package com.wizzardo.tools.reflection;

import java.util.Collection;

/**
 * Created by wizzardo on 26/03/17.
 */
class Misc {

    static <E> StringBuilder join(StringBuilder sb, E[] arr, String separator) {
        int length = arr.length;
        for (int i = 0; i < length; i++) {
            if (i > 0)
                sb.append(separator);
            sb.append(arr[i]);
        }
        return sb;
    }

    static <E> StringBuilder join(StringBuilder sb, Collection<E> arr, String separator) {
        boolean comma = false;
        for (E anArr : arr) {
            if (!comma)
                comma = true;
            else
                sb.append(separator);

            sb.append(anArr);
        }
        return sb;
    }
}
