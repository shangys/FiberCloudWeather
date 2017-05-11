package com.fibercloudweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fibercloudweather.android.db.City;
import com.fibercloudweather.android.db.County;
import com.fibercloudweather.android.db.Province;
import com.fibercloudweather.android.util.HttpUtil;
import com.fibercloudweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by kevin on 2017/5/10.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;//进度对话框
    private TextView titleText;//标题
    private Button backButton;//返回按钮
    private ListView listView;//数据列表
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();//存放数据
    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表

    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的市

    private int currentLevel;//当前选中的级别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);//动态加载布局
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();//获取当前点击位置的县的天气id
                    if (getActivity() instanceof MainActivity){//省市县数据的碎片如果在主页面中 当处于县级列表水平时  点击列表会跳转到天气内容界面
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);//进入到天气内容显示界面
                        intent.putExtra("weather_id",weatherId);//把天气id以键值对的形式保存到了intent里并传递到了WeatherActivity活动中
                        startActivity(intent);
                        getActivity().finish();//关闭当前界面
                    }else if(getActivity() instanceof WeatherActivity){//如果省市县数据的碎片在天气内容显示界面 那就关闭滑动菜单 显示下拉刷新进度条 然后请求新城市（即用户点击列表时对应位置的城市）的天气信息
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){//如果当前是县级列表 那么就返回到市级列表
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){//如果当前是市级列表 那么就返回到省级列表
                    queryProvinces();
                }
            }
        });
        queryProvinces();//从这里开始加载省级数据
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
       titleText.setText("中国");//省列表的头标题是中国
        backButton.setVisibility(View.GONE);//省级列表已经不能再返回了 所以将返回按钮隐藏起来
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province:provinceList){//遍历provinceList 取出其中的省对象，并将其对应的省名称添加到dataList中
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//通知适配器数据变化
            listView.setSelection(0);//将省份列表移动到最开始处
            currentLevel = LEVEL_PROVINCE;//设置当前选中的级别
        }else {
            String address = "http://guolin.tech/api/china";//该服务器用于提供全国所有省市县的数据信息，访问该服务器后，则该服务器会返回一段JSON格式的数据，包括中国所有省份的名称及省份id
            queryFromServer(address,"province");//根据请求的地址 从服务器上查询数据
        }
    }

    /**
     * 查询选中的省的内部所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());//选中的省的名字作为市列表的头标题
        backButton.setVisibility(View.VISIBLE);//设置返回按钮可见
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中的市的所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());//选中的市为县列表的头标题
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县的数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address,final String type){
        showProgressDialog();//显示进度对话框
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {//响应的数据会回调到该方法中
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭进度对话框
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("country".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
