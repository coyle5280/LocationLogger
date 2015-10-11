package assignment.mobile.locationlogger;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * The settings menu fragment used by the color blender application,
 * has options to change settings in the color blender
 * @author Josh Coyle
 * @author Robert Slavik
 */
public class SettingsFragment extends Fragment {
    //variables
    CheckBox checkboxBackground;
    CheckBox checkboxText;
    TextView fragColorText;
    Button fragBackGroundButton;
    Button saveButton;
    settingsListener  callBack;
    //set text color to white
    int colorText = -1;

    /**
     * when the fragment view is created
     * @param inflater- to inflate the view
     * @param container- where to place the view
     * @param savedInstanceState- saved state
     * @return - the view
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.setting_frag, container, false);
        return view;

    }

    /**
     * after the fragment view is created
     * @param view- the view
     * @param savedInstanceState - the saved state
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }

    /**
     * setup the buttons in the settings fragment
     */
    private void setupButtons() {



    }

    /**
     * The result of the color finder activity (Getting Text Color)
     * @param request_Code- code requested
     * @param result_Code- the result of the activity
     * @param colorsData- the color choosen
     */
    public void onActivityResult(int request_Code, int result_Code, Intent colorsData) {

    }

    /**
     *
     */
    public interface settingsListener{

    }


    }
