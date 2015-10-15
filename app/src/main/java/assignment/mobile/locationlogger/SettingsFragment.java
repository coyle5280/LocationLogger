package assignment.mobile.locationlogger;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.okhttp.internal.DiskLruCache;

/**
 * The settings menu fragment used by the color blender application,
 * has options to change settings in the color blender
 * @author Josh Coyle
 * @author Robert Slavik
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    //variables
    settingsListener mSettings;
    //set text color to white
    Spinner timerSpinner;
    Spinner distanceSpinner;
    EditText nameEditText;
    EditText descriptionEditText;


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
        setupButtons(view);


    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        try{
            mSettings = (settingsListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException(context.toString() + "not implementing Interface");
        }
    }

    /**
     * setup the buttons in the settings fragment
     */
    private void setupButtons(View sView) {
        nameEditText = (EditText) sView.findViewById(R.id.editTextName);
        descriptionEditText = (EditText) sView.findViewById(R.id.editTextObservation);

        distanceSpinner = (Spinner) sView.findViewById(R.id.spinnerDistance);
        ArrayAdapter<CharSequence> adapterDistance = ArrayAdapter.createFromResource(getActivity(),
                R.array.distanceArray, android.R.layout.simple_spinner_item);
        adapterDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(adapterDistance);

        timerSpinner = (Spinner) sView.findViewById(R.id.spinnerFrequency);
        ArrayAdapter<CharSequence> adapterFrequency = ArrayAdapter.createFromResource(getActivity(),
                R.array.frequencyArray, android.R.layout.simple_spinner_item);
        adapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timerSpinner.setAdapter(adapterFrequency);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        SettingsObject newSettings = new SettingsObject(nameEditText.getText().toString(),
                descriptionEditText.getText().toString(),
                (int)timerSpinner.getSelectedItem(),
                (int)distanceSpinner.getSelectedItem()
             );

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * The result of the color finder activity (Getting Text Color)
     * @param request_Code- code requested
     * @param result_Code- the result of the activity
     * @param colorsData- the color choosen
     */



    /**
     *
     */
    public interface settingsListener{
        public void updateSettings(SettingsObject settings);
    }


    }
