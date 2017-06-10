package cn.buildworld.mylistview.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import cn.buildworld.mylistview.R;

/**
 * 作者：MiChong on 2017/6/8 0008 21:06
 * 邮箱：1564666023@qq.com
 *
 * 下拉刷新自定义设置
 *
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

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
    private TextView last_refresh;
    private View footerView;
    private int footerViewMeasuredHeight;
    private String TAG = "滑动监听";
    private boolean isLoadingMore ;


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
        initHeaderView();//头布局
        initAnimation();

        initFooterView();//脚布局
        setOnScrollListener(this);//滑动监听
    }


    /**
     * 初始化脚布局
     *
     */
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.layout_footer_list,null);
        footerView.measure(0,0);
        footerViewMeasuredHeight = footerView.getMeasuredHeight();

        footerView.setPadding(0,-footerViewMeasuredHeight,0,0);

        addFooterView(footerView);

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
        last_refresh = (TextView) view.findViewById(R.id.tv_desc_last_refresh);



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

                if (onrefreshListener != null){
                    //通知调用者，让其加载网络数据
                    onrefreshListener.onRefresh();
                }

                break;

            default:
                break;
        }
    }


    //监听回调
    private OnrefreshListener onrefreshListener;//刷新监听

    public interface OnrefreshListener{
        void onRefresh();
        void LoaderMore();
    }

    public void setRefreshListener(OnrefreshListener onrefreshListener){
        this.onrefreshListener = onrefreshListener;
    }


    //刷新结束，恢复效果
    public void onRefreshComplete() {
        if (isLoadingMore){//加载更多

            footerView.setPadding(0,-footerViewMeasuredHeight,0,0);
            isLoadingMore = false;

        }else {
            //下拉刷新
            currentState = PULL_TO_REFRESH;
            mTitleText.setText("下拉刷新");
            view.setPadding(0,-mHeaderViewHeight,0,0);//隐藏头布局
            bar.setVisibility(INVISIBLE);
            mArrowView.setVisibility(VISIBLE);

            String time = getTime();
//        System.out.println("刷新时间："+time);
            last_refresh.setText("最后刷新时间："+time);
        }


    }


    //获取时间
    public String getTime() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }


    //滑动监听
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        Log.i(TAG, "onScrollStateChanged: "+scrollState);
        if(isLoadingMore){
            return; // 已经在加载更多.返回
        }

        // 最新状态是空闲状态, 并且当前界面显示了所有数据的最后一条. 加载更多
        if (scrollState == SCROLL_STATE_IDLE && getLastVisiblePosition() >= (getCount()-1)){
            isLoadingMore = true;

            footerView.setPadding(0, 0, 0, 0);
            setSelection(getCount()); // 跳转到最后一条, 使其显示出加载更多.

            if (onrefreshListener != null){
                onrefreshListener.LoaderMore();
            }
        }
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
