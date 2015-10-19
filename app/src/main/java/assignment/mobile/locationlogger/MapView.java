package assignment.mobile.locationlogger;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Fragment Class used to display HTML Content
 * @author Joshua Coyle.
 * @author Robert Slavik
 */
public class MapView extends Fragment {
    //Html object holding the map
    private WebView mapView;

    /**
     * Part of Fragment lifecycle called when fragment is created
     * @param inflater - Inflates fragment0
     * @param container - Container holding fragment
     * @param savedInstanceState -
     * @return View holding fragment
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_view, container, false);
        return view;
    }

    /**
     * Part of Fragment lifecycle called when fragment is created
     * @param view - the inflated view
     * @param savedInstanceState - saved state
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupItems(view);


    }

    /**
     * Sets up items needed in fragment
     * @param view - the inflated view
     */
    private void setupItems(View view) {
        mapView = (WebView) view.findViewById(R.id.webView);
        mapView.getSettings().setJavaScriptEnabled(true);
        mapView.loadUrl("https://coyle5280.cartodb.com/viz/4e8468a2-704d-11e5-975e-0e674067d321/embed_map");
    }

}
