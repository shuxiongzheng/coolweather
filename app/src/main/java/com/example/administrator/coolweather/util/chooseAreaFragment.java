package com.example.administrator.coolweather.util;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.coolweather.R;
import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;

import java.util.ArrayList;
import java.util.List;

public class chooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String>dataList=new ArrayList<>();

    //省列表
    private List<Process>processList;
    //市列表
    private List<City>cityList;
    //县列表
    private List<County>countyList;
    //选中的省份
    private Process seletedProvince;
    //选中的城市
    private City seleteCity;
    //当前选中的级别
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view= inflater.inflate(R.layout.choose_area, container, false);
       titleText=(TextView)view.findViewById(R.id.title_View);
       backButton=(Button)view.findViewById(R.id.back_button);
       listView=(ListView)view.findViewById(R.id.list_view);
       adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
       listView.setAdapter(adapter);
        return view;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        backButton.setOnClickListener(new View.OnClickListener() {//返回键
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    //
                }else if (currentLevel==LEVEL_CITY){
                    //
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//点击事件
                if(currentLevel==LEVEL_PROVINCE){
                    seletedProvince=processList.get(position);
                    //

                }else if (currentLevel==LEVEL_CITY){
                    seleteCity=cityList.get(position);
                    //
                }
            }
        });
        //


    }
    




}
