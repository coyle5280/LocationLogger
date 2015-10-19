package assignment.mobile.locationlogger;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * The settings menu fragment used by the Location Logger application,
 * has options to change settings
 * @author Josh Coyle
 * @author Robert Slavik
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    //variables
    private settingsListener mSettings;
    //set text color to white
    private Spinner timerSpinner;
    private Spinner distanceSpinner;
    //EditText objects
    private EditText nameEditText;
    private EditText descriptionEditText;
    //Button Object
    private Button saveSettings;


    /**
     * when the fragment view is created
     * @param inflater- to inflate the view
     * @param container- where to place the view
     * @param savedInstanceState- saved state
     * @return - the view
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.setting_frag, container, false);
        setupButtons(view);
        return view;

    }

    /**
     * after the fragment view is created
     * @param view- the view
     * @param savedInstanceState - the saved state
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saveSettings.setVisibility(View.INVISIBLE);

    }

    /**
     * When fragment is attached
     * @param context - Application Context
     */
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mSettings = (settingsListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "not implementing Interface");
        }

    }

    /**
     * setup the buttons in the settings fragment
     */
    private void setupButtons(View sView) {
        nameEditText = (EditText) sView.findViewById(R.id.editTextName);
        descriptionEditText = (EditText) sView.findViewById(R.id.editTextObservation);



        saveSettings = (Button) sView.findViewById(R.id.saveSettings);
        saveSettings.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                setOptions();
                                            }
                                        }
        );

        distanceSpinner = (Spinner) sView.findViewById(R.id.spinnerDistance);
        distanceSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterDistance = ArrayAdapter.createFromResource(getActivity(),
                R.array.distanceArray, android.R.layout.simple_spinner_item);

        adapterDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        distanceSpinner.setAdapter(adapterDistance);

        timerSpinner = (Spinner) sView.findViewById(R.id.spinnerFrequency);
        timerSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterFrequency = ArrayAdapter.createFromResource(getActivity(),
                R.array.frequencyArray, android.R.layout.simple_spinner_item);
        adapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timerSpinner.setAdapter(adapterFrequency);
    }

    /**
     * Create settings Object and call method in Activity
     */
    private void setOptions(){

        SettingsObject newSettingsObject = new SettingsObject(nameEditText.getText().toString(),
                descriptionEditText.getText().toString(),
                Integer.parseInt(timerSpinner.getSelectedItem().toString()),
                Integer.parseInt(distanceSpinner.getSelectedItem().toString())
             );
        Log.e("settings_object", newSettingsObject.toString());

        mSettings.updateSettings(newSettingsObject);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            saveSettings.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Not Used
    }
    @Override
    public void onPause(){
        super.onPause();
    }

    /**
     *Interface method for Activity
     */
    public interface settingsListener{
         void updateSettings(SettingsObject settings);
    }


}
