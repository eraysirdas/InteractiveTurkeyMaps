package com.eraysirdas.turkeymaps.view;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;

import com.eraysirdas.turkeymaps.R;
import com.eraysirdas.turkeymaps.model.CityPlantModel;
import com.eraysirdas.turkeymaps.model.MapsModel;
import com.eraysirdas.turkeymaps.service.MapsAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
     final String svgUrl = "file:///android_asset/tr.svg";
     ArrayList<MapsModel> mapsModels;
     private String BASE_URL ="https://flora.biocoder.com.tr/api/homeapi/";
     Retrofit retrofit;

    private LinearLayout filterPanel;
    private ConstraintLayout mainLayout;

    private boolean isFilterPanelOpen = false;

    Spinner spinnerPlantOrHoney;
    Spinner spinnerSearchType;


    private List<String> cities = Arrays.asList(
            "adana", "ADIYAMAN", "AFYONKARAHİSAR", "AĞRI", "AMASYA", "ANKARA", "antalya", "ARTVİN", "aydın",
            "BALIKESİR", "BİLECİK", "BİNGÖL", "BİTLİS", "BOLU", "BURDUR", "BURSA", "çanakkale", "ÇANKIRI",
            "ÇORUM", "DENİZLİ", "DİYARBAKIR", "EDİRNE", "ELAZIĞ", "ERZİNCAN", "ERZURUM", "ESKİŞEHİR",
            "GAZİANTEP", "GİRESUN", "GÜMÜŞHANE", "HAKKARİ", "hatay", "ISPARTA", "mersin", "istanbul",
            "izmir", "KARS", "KASTAMONU", "KAYSERİ", "KIRKLARELİ", "KIRŞEHİR", "KOCAELİ", "KONYA",
            "KÜTAHYA", "MALATYA", "MANİSA", "KAHRAMANMARAŞ", "MARDİN", "muğla", "MUŞ", "NEVŞEHİR",
            "NİĞDE", "ORDU", "RİZE", "SAKARYA", "samsun", "SİİRT", "SİNOP", "SİVAS", "tekirdağ",
            "TOKAT", "trabzon", "TUNCELİ", "ŞANLIURFA", "UŞAK", "VAN", "YOZGAT", "ZONGULDAK",
            "AKSARAY", "BAYBURT", "KARAMAN", "KIRIKKALE", "BATMAN", "ŞIRNAK", "BARTIN", "ARDAHAN",
            "IĞDIR", "YALOVA", "KARABÜK", "KİLİS", "OSMANİYE", "DÜZCE"
    );

    private final Set<String> allPlantNames = new HashSet<>();

    Set<String> citiesWithPlant;
    String targetPlantName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        filterPanel = findViewById(R.id.filterPanel);
        mainLayout = findViewById(R.id.main);
        spinnerPlantOrHoney = findViewById(R.id.spinnerPlantOrHoney);
        spinnerSearchType = findViewById(R.id.spinnerSearchType);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        WebView webView = findViewById(R.id.webView);

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

        // JavaScript ile iletişim için arayüz ekle
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // SVG dosyasını yükle (assets klasöründen)
        webView.loadUrl(svgUrl);

        Gson gson = new GsonBuilder().setLenient().create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        loadData();
        plantLoadData();

        setupSpinners();
    }


    private void plantLoadData() {
        MapsAPI mapsAPI = retrofit.create(MapsAPI.class);
         citiesWithPlant = new HashSet<>();
          targetPlantName = "DİKEN OTU (PALLENİS SPİNOSA L.)";
        for (String city : cities) {
            Call<MapsModel> call = mapsAPI.getDetailData(city);

            call.enqueue(new Callback<MapsModel>() {
                @Override
                public void onResponse(Call<MapsModel> call, Response<MapsModel> response) {
                    if (response.isSuccessful()) {
                        MapsModel responseList = response.body();
                        if (responseList != null) {
                            List<CityPlantModel> cityPlantModels = responseList.getCityPlants();
                            if (cityPlantModels != null && !cityPlantModels.isEmpty()) {
                                for (CityPlantModel plantModel : cityPlantModels) {
                                    allPlantNames.add(plantModel.plantName);// PlantName'leri kümeye ekle

                                    if (targetPlantName.equalsIgnoreCase(plantModel.plantName)) {
                                        citiesWithPlant.add(city); // Bitkinin bulunduğu şehri ekle
                                    }
                                }
                            }
                        }
                    }
                    if (citiesWithPlant.size() > 0) {
                        System.out.println("Bitkinin bulunduğu şehirler: " + citiesWithPlant);
                        //highlightCitiesOnMap(citiesWithPlant);
                    }

                    // Tüm iller tamamlandığında işlem yap
                    /*if (!allPlantNames.isEmpty()) {
                        for(String i : allPlantNames){
                            System.out.println(i);
                        }
                    }*/
                }

                @Override
                public void onFailure(Call<MapsModel> call, Throwable t) {
                    System.out.println("API çağrısı başarısız. Hata: " + t.getMessage());
                    t.printStackTrace();
                }
            });
        }
    }

    private void highlightCitiesOnMap(Set<String> cities) {
        WebView webView = findViewById(R.id.webView);

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
        jsCode.append("            path.setAttribute('fill', 'rgb(18, 121, 46)');"); // Şehri kırmızı yap
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
        if(spinnerPlantOrHoney != null){
            spinnerPlantOrHoney.setAdapter(adapterType);
        }

        String[] searchType = {"Tür Seçin veya Arayın", "Gül", "Meşe", "Kaktüs","DİKEN OTU (PALLENİS SPİNOSA L."};
        //List<String> plantList = new ArrayList<>(allPlantNames);
        ArrayAdapter<String> adapterSearchType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, searchType);
        adapterSearchType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if(spinnerSearchType != null){
            spinnerSearchType.setAdapter(adapterSearchType);

        }

       if(spinnerSearchType!=null || spinnerPlantOrHoney!=null){
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
                    changeAllPathsColorToGreen("rgb(29, 186, 71)");
                }
                else if("Bal".equalsIgnoreCase(selectedItem)){
                    changeAllPathsColorToGreen("rgb(211, 111, 0)");
                }
                else{
                    WebView webView = findViewById(R.id.webView);
                    applyRegionColors(webView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if ("DİKEN OTU (PALLENİS SPİNOSA L.".equalsIgnoreCase(selectedItem)) {
                    highlightCitiesOnMap(citiesWithPlant);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void changeAllPathsColorToGreen(String color) {
        String jsCode = "var paths = document.querySelectorAll('path');" +
                "paths.forEach(function(path) {" +
                "    path.setAttribute('fill', '"+color+"');" +
                "});";
        WebView webView = findViewById(R.id.webView);
        webView.evaluateJavascript(jsCode, null);
    }

    public void sideBarBtnClicked(View v){
        spinnerPlantOrHoney.setSelection(0);
        spinnerSearchType.setSelection(0);
    }

    public void btnFilterClicked(View view){

        if (isFilterPanelOpen) {
            // Panel kapatılıyor
            filterPanel.setVisibility(View.INVISIBLE);
            spinnerPlantOrHoney.setVisibility(View.INVISIBLE);
           spinnerSearchType.setVisibility(View.INVISIBLE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mainLayout);
            constraintSet.connect(R.id.webView, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.applyTo(mainLayout);
        } else {
            // Panel açılıyor
            filterPanel.setVisibility(View.VISIBLE);
            spinnerPlantOrHoney.setVisibility(View.VISIBLE);
            spinnerSearchType.setVisibility(View.VISIBLE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mainLayout);
            constraintSet.connect(R.id.webView, ConstraintSet.END, R.id.filterPanel, ConstraintSet.START);
            constraintSet.applyTo(mainLayout);
        }

        isFilterPanelOpen = !isFilterPanelOpen;

    }


    private void loadData(){
        MapsAPI mapsAPI = retrofit.create(MapsAPI.class);

        Call<List<MapsModel>> call = mapsAPI.getData();

        call.enqueue(new Callback<List<MapsModel>>() {
            @Override
            public void onResponse(Call<List<MapsModel>> call, Response<List<MapsModel>> response) {
                if(response.isSuccessful()){
                    List<MapsModel> responseList = response.body();
                    mapsModels=new ArrayList<>(responseList);

                }
            }

            @Override
            public void onFailure(Call<List<MapsModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void injectJavaScript(WebView webView) {
        String jsCode = "document.addEventListener('click', function(event) {" +
                "    var target = event.target;" +
                "    if (target.tagName.toLowerCase() === 'path') {" +
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
    }

    private void goDetailsActivity(MapsModel model) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("model",model);
        startActivity(intent);
    }
}