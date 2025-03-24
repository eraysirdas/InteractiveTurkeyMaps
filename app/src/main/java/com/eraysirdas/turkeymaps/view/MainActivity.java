package com.eraysirdas.turkeymaps.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.eraysirdas.turkeymaps.R;
import com.eraysirdas.turkeymaps.SharedPrefManager;
import com.eraysirdas.turkeymaps.model.SearchCityResponse;
import com.eraysirdas.turkeymaps.model.SearchDataByTypeResponse;
import com.eraysirdas.turkeymaps.model.MapsModel;
import com.eraysirdas.turkeymaps.model.SearchDataByTypeRequest;
import com.eraysirdas.turkeymaps.service.api.MapsAPI;
import com.eraysirdas.turkeymaps.service.datalayer.RetrofitClient;
import com.eraysirdas.turkeymaps.service.repository.MapsRepository;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    final String svgUrl = "file:///android_asset/tr.svg";
    private CompositeDisposable compositeDisposable;
    private MapsRepository mapsRepository;
    ArrayList<MapsModel> mapsModels;
    private final String BASE_URL = "https://flora.biocoder.com.tr/api/homeapi/";
    WebView webView;

    private LinearLayout filterPanel;
    private ConstraintLayout mainLayout;

    private boolean isFilterPanelOpen = false;

    Spinner spinnerPlantOrHoney;
    Spinner spinnerSearchType;
    ProgressDialog progressDialog;
    boolean dataLoadedSuccessful=false;



    private final List<String> cities = Arrays.asList(
            "adana", "adıyaman", "afyonkarahisar", "ağrı", "amasya", "ankara", "ANTALYA", "artvin", "aydın",
            "balıkesir", "bilecik", "bingöl", "bitlis", "bolu", "burdur", "bursa", "çanakkale", "çankırı",
            "çorum", "denizli", "diyarbakır", "edirne", "elazığ", "erzincan", "erzurum", "eskişehir",
            "gaziantep", "giresun", "gümüşhane", "hakkari", "hatay", "ısparta", "mersin", "istanbul",
            "izmir", "kars", "kastamonu", "kayseri", "kırklareli", "kırşehir", "kocaeli", "konya",
            "kütahya", "malatya", "manisa", "kahramanmaraş", "mardin", "MUĞLA", "muş", "nevşehir",
            "niğde", "ordu", "rize", "sakarya", "samsun", "siirt", "sinop", "sivas", "tekirdağ",
            "tokat", "trabzon", "tunceli", "şanlıurfa", "uşak", "van", "yozgat", "zonguldak",
            "aksaray", "bayburt", "karaman", "kırıkkale", "batman", "şırnak", "bartın", "ardahan",
            "iğdır", "yalova", "karabük", "kilis", "osmaniye", "düzce", "kuzey-kıbrıs"
    );





    List<String> allHoneyNames;
    Map<String, Set<String>> honeyCityMap;

    List<String> allPlantNames;
    Map<String, Set<String>> plantCityMap;

    SharedPrefManager sharedPrefManager;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        webView = findViewById(R.id.webView);
        sharedPrefManager = new SharedPrefManager(this);


        filterPanel = findViewById(R.id.filterPanel);
        mainLayout = findViewById(R.id.main);
        spinnerPlantOrHoney = findViewById(R.id.spinnerPlantOrHoney);
        spinnerSearchType = findViewById(R.id.spinnerSearchType);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // WebView ayarları
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // JavaScript'i etkinleştir
        webSettings.setBuiltInZoomControls(true); // Yerleşik zoom kontrollerini etkinleştir
        webSettings.setDisplayZoomControls(false); // Zoom kontrollerini gizle (isteğe bağlı)
        webSettings.setSupportZoom(true); // Zoom'u destekle
        webSettings.setUseWideViewPort(true); // Geniş görünüm portunu etkinleştir

        webView.setInitialScale(100);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // SVG yüklendikten sonra JavaScript'i enjekte et
                injectJavaScript(view);

                applyRegionColors(view);
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.loadUrl(svgUrl);


        loadData();


        setupSpinners();



    }

    private void searchCity(String isBalType,String searchQuery,String searchType) {
        MapsAPI mapsAPI = RetrofitClient.getClient().create(MapsAPI.class);

        SearchDataByTypeRequest request = new SearchDataByTypeRequest(isBalType,searchQuery,searchType);

        Call<List<SearchCityResponse>> call = mapsAPI.searchCity(request);

        call.enqueue(new Callback<List<SearchCityResponse>>() {
            @Override
            public void onResponse(Call<List<SearchCityResponse>> call, Response<List<SearchCityResponse>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    List<SearchCityResponse> data = response.body();

                    Set<String> cities = new HashSet<>();
                    for(SearchCityResponse city : data){
                        cities.add(city.name.toLowerCase());
                    }

                    System.out.println("API Request: isBalType=" + isBalType + ", searchQuery=" + searchQuery + ", searchType=" + searchType);
                    System.out.println("API Response: " + response.body());
                    System.out.println("***********************"+cities);

                    String color = isBalType.equalsIgnoreCase("Bitki") ? "rgb(18, 121, 46)" : "rgb(151, 91, 0)";
                    highlightCitiesOnMap(cities, color);

                } else {
                    Log.e("MainActivity", "API isteği başarısız.");
                }
            }

            @Override
            public void onFailure(Call<List<SearchCityResponse>> call, Throwable t) {
                Log.e("MainActivity", "API isteği başarısız: " + t.getMessage());
            }
        });
    }

    

    private void setupSearchSpinner(List<SearchDataByTypeResponse> list) {

        List<String> plantOrHoneyList = new ArrayList<>();
        plantOrHoneyList.add(0, "Tür Seçin veya Arayın");

        for (SearchDataByTypeResponse item : list) {
            plantOrHoneyList.add(item.name); // query alanını spinnera ekle
        }
        Collections.sort(plantOrHoneyList.subList(1, list.size()));

        System.out.println("*************************************"+plantOrHoneyList);

       /* for(String i : allHoneyNames){
            System.out.println("TÜM BALLAR : "+i);
        }*/

        ArrayAdapter<String> adapterSearchType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plantOrHoneyList);
        adapterSearchType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if(spinnerSearchType!=null){
            spinnerSearchType.setAdapter(adapterSearchType);
            setupSpinnerSearchListener();
        }
        

            /*spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (("Tür Seçin veya Arayın").equalsIgnoreCase(parent.getItemAtPosition(position).toString())) {
                        changeAllPathsColor("rgb(211, 111, 0)");
                    } else {
                        changeAllPathsColor("rgb(211, 111, 0)");
                        String selectedHoney = allHoneyNames.get(position);
                        highlightCitiesOnMap(honeyCityMap.get(selectedHoney), "rgb(151, 91, 0)");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Hiçbir şey seçilmediğinde yapılacak işlemler
                }
            });*/

    }

    private void setupSpinnerSearchListener() {
        if(spinnerSearchType!=null){
            spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (!"Tür Seçin veya Arayın".equalsIgnoreCase(selectedItem)) {
                        String isBalType = spinnerPlantOrHoney.getSelectedItem().toString().equalsIgnoreCase("Bitki") ? "bitki" : "bal";
                        String color = isBalType.equalsIgnoreCase("Bitki") ? "rgb(29, 186, 71)" : "rgb(211, 111, 0)";
                        changeAllPathsColor(color);
                        searchCity(isBalType, selectedItem, "");
                        System.out.println("***************************"+selectedItem);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    private void highlightCitiesOnMap(Set<String> cities, String color) {

        // JavaScript kodu: Belirli şehirlerin path'lerini bul ve boya
        StringBuilder jsCode = new StringBuilder();
        jsCode.append("var paths = document.querySelectorAll('path');");
        jsCode.append("paths.forEach(function(path) {");
        jsCode.append("    var parentG = path.closest('g');");
        jsCode.append("    if (parentG) {");
        jsCode.append("        var cityName = parentG.getAttribute('id');");

        // Şehir adlarını JavaScript'e aktar
        jsCode.append("        var targetCities = [");
        for (String city : cities) {
            jsCode.append("'").append(city).append("',");
        }
        jsCode.append("];");

        jsCode.append("        if (targetCities.includes(cityName)) {");
        jsCode.append("            path.setAttribute('fill', '").append(color).append("');"); //rgb(18, 121, 46)
        jsCode.append("        }");
        jsCode.append("    }");
        jsCode.append("});");

        // JavaScript'i WebView'de çalıştır
        webView.evaluateJavascript(jsCode.toString(), null);
    }

    private void setupSpinners() {
        String[] type = {"Seçiniz", "Bitki", "Bal"};
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, type);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerPlantOrHoney != null) {
            spinnerPlantOrHoney.setAdapter(adapterType);
            setupSpinnerListener();
        }
    }

    private void setupSpinnerListener() {
        spinnerPlantOrHoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Seçilen öğe üzerinde işlem yap
                if ("Bitki".equalsIgnoreCase(selectedItem)) {
                    changeAllPathsColor("rgb(29, 186, 71)");
                    searchDataByType("bitki","","");

                } else if ("Bal".equalsIgnoreCase(selectedItem)) {
                    changeAllPathsColor("rgb(211, 111, 0)");
                    searchDataByType("bal","","");
                   // onAllCitiesDataLoaded(allHoneyNames);
                } else {
                    spinnerSearchType.setAdapter(null);
                    applyRegionColors(webView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void searchDataByType(String isBalType, String searchQuery, String searchType) {
        MapsAPI mapsAPI = RetrofitClient.getClient().create(MapsAPI.class);

        SearchDataByTypeRequest request = new SearchDataByTypeRequest(isBalType,searchQuery,searchType);

        Call<List<SearchDataByTypeResponse>> call = mapsAPI.searchDataByType(request);

        call.enqueue(new Callback<List<SearchDataByTypeResponse>>() {
            @Override
            public void onResponse(Call<List<SearchDataByTypeResponse>> call, Response<List<SearchDataByTypeResponse>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    List<SearchDataByTypeResponse> data = response.body();
                    setupSearchSpinner(data);
                } else {
                    Log.e("MainActivity", "API isteği başarısız.");
                }
            }

            @Override
            public void onFailure(Call<List<SearchDataByTypeResponse>> call, Throwable t) {
            Log.e("MainActivity", "API isteği başarısız: " + t.getMessage());
            }
        });
    }
    private void loadData() {

        MapsAPI mapsAPI = RetrofitClient.getClient().create(MapsAPI.class);

        mapsRepository = new MapsRepository(mapsAPI);
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(mapsRepository.getMapsData().subscribe(this::handleLoadDataResponse));

        /*Set<String> processedHoneyTypes = new HashSet<>();
        Set<String> processedPlantTypes = new HashSet<>();
        AtomicInteger remainingRequests = new AtomicInteger(cities.size());
        allHoneyNames = new ArrayList<>();
        honeyCityMap = new HashMap<>();
        allPlantNames = new ArrayList<>();
        plantCityMap = new HashMap<>();

        for (String city : cities) {

            Call<MapsModel> call = mapsAPI.getDetailData(city);

            call.enqueue(new Callback<MapsModel>() {
                @Override
                public void onResponse(@NonNull Call<MapsModel> call, @NonNull Response<MapsModel> response) {
                    if (response.isSuccessful()) {
                        MapsModel responseList = response.body();
                        if (responseList != null) {


                            List<CityHoneyModel> cityHoneyModels = responseList.getCityHoney();
                            if (cityHoneyModels != null && !cityHoneyModels.isEmpty()) {
                                for (CityHoneyModel honeyModel : cityHoneyModels) {
                                    if (!processedHoneyTypes.contains(honeyModel.honeyVerietyName)) { // Bal türü işlenmemişse
                                        allHoneyNames.add(honeyModel.honeyVerietyName);
                                        processedHoneyTypes.add(honeyModel.honeyVerietyName); // Bal türünü işlenmiş olarak işaretle
                                    }

                                    if (honeyCityMap.containsKey(honeyModel.honeyVerietyName)) {
                                        honeyCityMap.get(honeyModel.honeyVerietyName).add(city);
                                    } else {
                                        Set<String> citySet = new HashSet<>();
                                        citySet.add(city);
                                        honeyCityMap.put(honeyModel.honeyVerietyName, citySet);
                                    }
                                }
                            }

                            List<CityPlantModel> cityPlantModels = responseList.getCityPlants();
                            if (cityPlantModels != null && !cityPlantModels.isEmpty()) {
                                for (CityPlantModel plantModel : cityPlantModels) {

                                    if (!processedPlantTypes.contains(plantModel.plantName)) { // Bal türü işlenmemişse
                                        allPlantNames.add(plantModel.plantName);
                                        processedPlantTypes.add(plantModel.plantName); // Bal türünü işlenmiş olarak işaretle
                                    }

                                    if (plantCityMap.containsKey(plantModel.plantName)) {
                                        plantCityMap.get(plantModel.plantName).add(city);
                                    } else {
                                        Set<String> citySet = new HashSet<>();
                                        citySet.add(city);
                                        plantCityMap.put(plantModel.plantName, citySet);
                                    }
                                }
                            }


                        }
                    }
                    if (remainingRequests.decrementAndGet() == 0) {
                        // Tüm istekler tamamlandığında yapılacak işlemler
                        //onAllCitiesDataLoaded2(allPlantNames, plantCityMap);
                        //onAllCitiesDataLoaded(allHoneyNames, honeyCityMap);
                        dataLoadedSuccessful = true;
                        progressDialog.dismiss();
                        saveDataToSharedPreferences();
                        setupSpinnerListener();
                    }

                }

                @Override
                public void onFailure(Call<MapsModel> call, Throwable t) {
                    t.printStackTrace();
                    if (t instanceof SocketTimeoutException) {
                        System.out.println("Zaman aşımı hatası: Sunucu yanıt vermiyor.");
                    } else if (t instanceof ConnectException) {
                        System.out.println("Bağlantı hatası: Sunucuya ulaşılamıyor.");
                    } else if (t instanceof HttpException) {
                        HttpException httpException = (HttpException) t;
                        System.out.println("HTTP hatası: " + httpException.code() + " - " + httpException.message());
                    } else {
                        System.out.println("Bilinmeyen hata: " + t.getMessage());
                    }
                }
            });

        }*/
    }

    private void changeAllPathsColor(String color) {
        String jsCode = "var paths = document.querySelectorAll('path');" +
                "paths.forEach(function(path) {" +
                "    path.setAttribute('fill', '" + color + "');" +
                "});";
        webView.evaluateJavascript(jsCode, null);
    }

    public void filterDeleteBtnClicked(View v) {
        spinnerPlantOrHoney.setSelection(0);
        spinnerSearchType.setSelection(0);
        spinnerSearchType.setAdapter(null);
        applyRegionColors(webView);
    }

    public void sideBarBtnClicked(View view) {

        if (isFilterPanelOpen) {
            filterPanel.animate()
                    .translationX(filterPanel.getWidth())
                    .withEndAction(()->{

                        // Panel kapatılıyor
                        filterPanel.setVisibility(View.GONE);
                        spinnerPlantOrHoney.setVisibility(View.INVISIBLE);
                        spinnerSearchType.setVisibility(View.INVISIBLE);
            }).start();

        } else {
            // Panel açılıyor
            filterPanel.animate()
                    .translationX(0)
                    .withStartAction(()->{
                        filterPanel.setVisibility(View.VISIBLE);
                        spinnerPlantOrHoney.setVisibility(View.VISIBLE);
                        spinnerSearchType.setVisibility(View.VISIBLE);
                    }).start();
        }

        isFilterPanelOpen = !isFilterPanelOpen;

    }

    private void handleLoadDataResponse(List<MapsModel> mapsModelList) {
        mapsModels = new ArrayList<>(mapsModelList);
    }

    private void injectJavaScript(WebView webView) {
        String jsCode = "document.addEventListener('click', function(event) {" +
                "    var target = event.target;" +
                "    if (target.tagName.toLowerCase() === 'path') {" +
                "        var oldHighlight = document.querySelectorAll('.highlighted');" +
                "        oldHighlight.forEach(function(item) {" +
                "            item.classList.remove('highlighted');" +
                "            item.style.stroke = 'none';" +
                "        });" +
                "" +
                "        target.classList.add('highlighted');" +
                "        target.style.stroke = '#FF0000';" +
                "        target.style.strokeWidth = '3px';" +
                "        target.style.strokeDasharray = '5,5';" +
                "        target.style.pointerEvents = 'auto';" +
                "" +
                "        event.stopPropagation();" +
                "        event.preventDefault();" +
                "        var parentG = target.closest('g');" +
                "        if (parentG) {" +
                "            var id = parentG.getAttribute('id');" +
                "            Android.onPathClicked(id);" +
                "        }" +
                "    }" +
                "});";
        webView.evaluateJavascript(jsCode, null);


        String jsCode2 = "document.addEventListener('contextmenu', function(event) {" +
                "    event.preventDefault();" +
                "    var target = event.target;" +
                "    if (target.tagName.toLowerCase() === 'path') {" +
                "        var oldHighlight = document.querySelectorAll('.highlighted');" +
                "        oldHighlight.forEach(function(item) {" +
                "            item.classList.remove('highlighted');" +
                "            item.style.stroke = 'none';" +
                "        });" +
                "" +
                "        target.classList.add('highlighted');" +
                "        target.style.stroke = '#FF0000';" +
                "        target.style.strokeWidth = '3px';" +
                "        target.style.strokeDasharray = '5,5';" +
                "        target.style.pointerEvents = 'auto';" +
                "" +
                "        var parentG = target.closest('g');" +
                "        if (parentG) {" +
                "            var id = parentG.getAttribute('id');" +
                "            var x = event.clientX;" +
                "            var y = event.clientY;" +
                "            Android.onLongClick(id, x, y);" +
                "        }" +
                "    }" +
                "});";

        webView.evaluateJavascript(jsCode2, null);

    }

    private void applyRegionColors(WebView webView) {
        String colorJsCode = "var regions = document.querySelectorAll('g[class]');" + // class özelliği olan tüm <g> elementlerini seç
                "regions.forEach(function(region) {" +
                "    var className = region.getAttribute('class');" + // class değerini al
                "    var paths = region.querySelectorAll('path');" + // <g> içindeki TÜM <path> elementlerini seç
                "    paths.forEach(function(path) {" + // Her bir <path> üzerinde döngü yap
                "        switch (className) {" +
                "            case 'akdeniz':" +
                "                path.setAttribute('fill', '#71b1b1');" +
                "                break;" +
                "            case 'marmara':" +
                "                path.setAttribute('fill', '#d01b1b');" +
                "                break;" +
                "            case 'ic-anadolu':" +
                "                path.setAttribute('fill', '#8d8484');" +
                "                break;" +
                "            case 'ege':" +
                "                path.setAttribute('fill', '#3498DB');" +
                "                break;" +
                "            case 'karadeniz':" +
                "                path.setAttribute('fill', '#E79C2A');" +
                "                break;" +
                "            case 'guney-dogu-anadolu':" +
                "                path.setAttribute('fill', '#9b3e00');" +
                "                break;" +
                "            case 'dogu-anadolu':" +
                "                path.setAttribute('fill', '#706385');" +
                "                break;" +
                "            case 'kuzey-kibris':" +
                "                path.setAttribute('fill', '#d01b1b');" +
                "                break;" +
                "        }" +
                "    });" +
                "});";
        webView.evaluateJavascript(colorJsCode, null);
    }

    // JavaScript ile iletişim için arayüz
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void onPathClicked(String id) {

            // Tıklanan bölgenin ID ve adını Toast mesajı olarak göster
            //String message = "Tıklanan Bölge: " + name + " (ID: " + id + ")";
            //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            // API'den gelen veri listesi içinde eşleşen ID'yi ara
            if (mapsModels != null) {
                for (MapsModel model : mapsModels) {
                    if (model.getName().equalsIgnoreCase(id)) { // id ve name eşleşmesi
                        goDetailsActivity(model);
                        return;
                    }
                }
            }
        }


        @JavascriptInterface
        public void onLongClick(String id, int x, int y) {
            // Uzun tıklanan bölgenin ID'sini ve koordinatlarını işle
            if (mapsModels != null) {
                for (MapsModel model : mapsModels) {
                    if (model.getName().equalsIgnoreCase(id)) {

                        String info = model.getName() + "\n" +
                                model.districtCount + " ilçe\n" +
                                model.produceCount + " üretici\n" +
                                model.produceCount + " kovan";

                        //Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();

                        showPopup(info,x,y);
                        return;
                    }
                }
            }
        }
    }


    private void goDetailsActivity(MapsModel model) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("model",model);
        startActivity(intent);
    }

     private void showPopup(String info, int x, int y) {

        LayoutInflater inflater =  (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_menu,null);

        TextView textView = popupView.findViewById(R.id.popup_text);
        textView.setText(info);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

         int[] wbViewLocation = new int[2];
         webView.getLocationOnScreen(wbViewLocation);
         int adjustedX = wbViewLocation[0] + x;
         int adjustedY = wbViewLocation[1] + y;


        popupWindow.showAtLocation(webView, Gravity.NO_GRAVITY, adjustedX, adjustedY);
        // popupWindow.showAsDropDown(webView, x, y, Gravity.NO_GRAVITY);
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}