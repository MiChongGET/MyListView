package cn.buildworld.mylistview.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private static final int PULL_TO_REFRESH = 0;//下拉刷新
    private static final int RELEASE_REFRESH = 1;//释放刷新
    private static final int REFRESHING = 2;//正在刷新
    private int currentState ;//当前的刷新状态

    private RotateAnimation rotateUpAnim;
    private RotateAnimation rotateDownAnim;

    private ImageView mArrowView;
    private TextView mTitleText;
    private ProgressBar bar;
    private int paddingTop;


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


    /**
     * 初始化头布局，脚布局
     */


    private void init() {
        initHeaderView();
        initAnimation();
    }

    /**
     * 初始化头布局的动画
     */
    private void initAnimation() {
        //向上转，围绕着自己的中心，逆时针180
        rotateUpAnim = new RotateAnimation
                (0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
        rotateUpAnim.setDuration(300);
        rotateUpAnim.setFillAfter(true);//动画停留在最后的位置

        //向下转，围绕着自己的中心，逆时针180
        rotateDownAnim = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateDownAnim.setDuration(300);
        rotateDownAnim.setFillAfter(true);//动画停留在最后的位置

    }


    /**
     * 初始化布局
     */
    private int mHeaderViewHeight;
    private void initHeaderView() {

        view = View.inflate(getContext(), R.layout.layout_header_list,null);
        mArrowView = (ImageView) view.findViewById(R.id.img_arrow);
        mTitleText = (TextView) view.findViewById(R.id.tv_title);
        bar = (ProgressBar) view.findViewById(R.id.pb);




        //提前手动测量宽高
        view.measure(0,0);//按照设置的规则测量
        int height = view.getHeight();
        mHeaderViewHeight= view.getMeasuredHeight();


        //设置内边距。可以隐藏当前控件，-自身高度--隐藏头布局
        view.setPadding(0,-mHeaderViewHeight,0,0);
        addHeaderView(view);

    }


    private float downY ;
    private float moveY ;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        //判断滑动距离，给Header设置paddingTop
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = ev.getY();

                if (currentState == REFRESHING){
                    return super.onTouchEvent(ev);
                }

                float offset = moveY - downY;
                //只有偏移量>0m并且当前的第一个可见条目索引是0，才放大头部
                if (offset >0 && getFirstVisiblePosition() == 0){
                    paddingTop = (int) (-mHeaderViewHeight + offset);
                    view.setPadding(0, paddingTop,0,0);

                    if (paddingTop >= 0 && currentState != RELEASE_REFRESH){//完全显示
                        //变成释放刷新模式
                        currentState = RELEASE_REFRESH;
                        updateHeader();//根据最新的状态值更新头布局内容

                    }else if (paddingTop <0 && currentState != PULL_TO_REFRESH){
                        //切换下拉刷新模式
                        currentState = PULL_TO_REFRESH;
                        updateHeader();//根据最新的状态值更新头布局内容

                    }

                        return true;//当前事件被我们处理并消费
                }



                break;
            case MotionEvent.ACTION_UP:

                if (currentState == PULL_TO_REFRESH){//不完全显示，恢复
                    view.setPadding(0,-mHeaderViewHeight,0,0);

                }else if (currentState == RELEASE_REFRESH){//完全显示，执行正在刷新
                    view.setPadding(0,0,0,0);
                    currentState = REFRESHING;
                    updateHeader();
                }

                break;

            default:
                break;

        }

        return super.onTouchEvent(ev);

    }

    /**
     * 根据状态更新头布局内容
     */
    private void updateHeader() {
        switch (currentState){
            case PULL_TO_REFRESH://切换回下拉刷新
                mArrowView.startAnimation(rotateDownAnim);
                bar.setVisibility(INVISIBLE);
                mTitleText.setText("下拉刷新");
                break;
            case RELEASE_REFRESH://切换成释放刷新
                //做动画，改标题
                mArrowView.startAnimation(rotateUpAnim);
                mTitleText.setText("释放刷新");
                bar.setVisibility(INVISIBLE);
                break;
            case REFRESHING://刷新中
                mArrowView.clearAnimation();
                mArrowView.setVisibility(INVISIBLE);
                bar.setVisibility(VISIBLE);
                mTitleText.setText("正在刷新中...");

                break;

            default:
                break;
        }
    }
}
