package cn.buildworld.mylistview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.buildworld.mylistview.util.RefreshListView;

public class MainActivity extends AppCompatActivity{

    private RefreshListView listView;
    private ArrayList<String> arrayList;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (RefreshListView) findViewById(R.id.list_item);

        listView.setRefreshListener(new RefreshListView.OnrefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        arrayList.add(0,"我是刷新的数据");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                listView.onRefreshComplete();
                            }
                        });
                    }
                }.start();
            }

            //加载更多
            @Override
            public void LoaderMore() {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        arrayList.add("我是加载的数据的数据1234");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                listView.onRefreshComplete();
                            }
                        });
                    }
                }.start();
            }
        });

        arrayList = new ArrayList<>();

        for (int i = 0; i <30 ; i++) {
            arrayList.add("我是第"+i);
        }

        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
    }


    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setTextSize(20f);

            textView.setText(arrayList.get(position));
            return textView;
        }
    }
}
