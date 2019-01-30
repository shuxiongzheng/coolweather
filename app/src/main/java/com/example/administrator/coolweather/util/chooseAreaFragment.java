package com.example.administrator.coolweather.util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather.MainActivity;
import com.example.administrator.coolweather.R;
import com.example.administrator.coolweather.WeatherActivity;
import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;
import com.example.administrator.coolweather.db.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
    private List<Province>provinceList;
    //市列表
    private List<City>cityList;
    //县列表
    private List<County>countyList;
    //选中的省份
    private Province seletedProvince;
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//点击事件
                if(currentLevel==LEVEL_PROVINCE){
                    seletedProvince=provinceList.get(position);

                    queryCities();

                }else if (currentLevel==LEVEL_CITY){
                    seleteCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {//如果当前碎片在MainActivity中
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof  WeatherActivity){//如果当前碎片在WeatherActivity中
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();//关闭滑动菜单
                        activity.swipeRefresh.setRefreshing(true);//打开刷新天气
                        activity.requestWeather(weatherId);//更新天气
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {//返回键
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();

                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        Log.d("queryProvinces","++++++++++++++---------------++++++++++++++++");
        queryProvinces();




    }
        //查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
        private void queryProvinces(){
            titleText.setText("中国");
            backButton.setVisibility(View.GONE);
            provinceList=DataSupport.findAll(Province.class);
            if (provinceList.size()>0){
                dataList.clear();
                for (Province province:provinceList){
                    dataList.add(province.getProvinceName());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel=LEVEL_PROVINCE;

            }else {
                String address="http://guolin.tech/api/china";
                Log.d("queryProvinces","-------------------------------+++++++++++++++++");
                queryFromServer(address,"province");
                Log.d("queryProvinces","-------------------------------+++++++++++++++++");
            }
        }
    //查询选中省所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryCities(){
            titleText.setText(seletedProvince.getProvinceName());//显示选择省份的名字
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid = ?",String.valueOf(seletedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
                Log.d("queryProvinces","++++++++++++88888+++++++++++++++++++"+city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{

            int provinceCode=seletedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            Log.d("queryProvinces","++++++++++++88888+++++++++++++++++++"+provinceCode);
            queryFromServer(address,"city");
        }
    }
    //查询全国所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryCounties(){
            titleText.setText(seleteCity.getCityName());
            backButton.setVisibility(View.VISIBLE);
            countyList=DataSupport.where("cityid = ?",String.valueOf(seleteCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int provinceCode=seletedProvince.getProvinceCode();
            int cityCode=seleteCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据传入的地址和类型从服务器上查询省市县数据
        private void queryFromServer(String address,final String type){
            showProgressDialog();
            HttpUtil.sendOkHttpRequest(address, new Callback() {

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();

                    boolean result=false;
                    if ("province".equals(type)){
                        result=Utility.handleProvinceResponse(responseText);
                    }else if("city".equals(type)){
                        result=Utility.handleCityResponse(responseText,seletedProvince.getId());
                    }else if("county".equals(type)){
                        result=Utility.handleCountyResponse(responseText,seleteCity.getId());
                    }
                    if (result){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                if ("province".equals(type)){
                                    queryProvinces();
                                }else if ("city".equals(type)){
                                    queryCities();
                                }else if ("county".equals(type)){
                                    queryCounties();
                                }
                            }
                        });
                    }

                }
                @Override
                public void onFailure(Call call, IOException e) {
                    //通过runOnUiThread方法回到主线程处理逻辑
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            });
        }

        //显示进度对话框
        private void showProgressDialog(){
            if (progressDialog==null){
                progressDialog=new ProgressDialog(getActivity());
                progressDialog.setMessage("正在加载...你妹");
                progressDialog.setCanceledOnTouchOutside(false);
            }
            progressDialog.show();
        }
        //关闭对话框
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }



}
