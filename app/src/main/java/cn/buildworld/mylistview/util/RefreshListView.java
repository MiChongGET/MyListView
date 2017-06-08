package cn.buildworld.mylistview.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import cn.buildworld.mylistview.R;

/**
 * 作者：MiChong on 2017/6/8 0008 21:06
 * 邮箱：1564666023@qq.com
 *
 * 下拉刷新自定义设置
 *
 */
public class RefreshListView extends ListView {

    private View view;

    public RefreshListView(Context context) {
        super(context);
        init();
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initHeaderView();
    }

    /**
     * 初始化布局
     */
    private void initHeaderView() {

        view = View.inflate(getContext(), R.layout.layout_header_list,null);

        addHeaderView(view);

    }
}
