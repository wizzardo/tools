package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowNoop<T> extends Flow<T, Object> {
    @Override
    protected void onEnd() {
    }
}
