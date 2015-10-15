package assignment.mobile.locationlogger;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by coyle on 10/11/2015.
 */
public class MapView extends Fragment {

    private WebView mapView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_view, container, false);
        return view;
    }


    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupItems(view);


    }

    private void setupItems(View view) {
        mapView = (WebView) view.findViewById(R.id.webView);
        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.loadUrl("https://coyle5280.cartodb.com/viz/4e8468a2-704d-11e5-975e-0e674067d321/embed_map");
        //String customHtml = "";
        //mapView.loadData(customHtml, "text/html", "UTF-8");

    }

}
