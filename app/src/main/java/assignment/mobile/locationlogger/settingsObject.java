package assignment.mobile.locationlogger;

/**
 * Created by coyle on 10/14/2015.
 */
public class SettingsObject {
    String name;
    String observation;
    int timerFrequencyVariable;
    int distanceUpdateVariable;

    public SettingsObject(String theName, String theObservation, int timerFreq, int distance){
        name = theName;
        observation = theObservation;
        timerFrequencyVariable = timerFreq;
        distanceUpdateVariable = distance;
    }
    public String getName(){
        return name;
    }

    public String getObservation(){
        return observation;
    }

    public int getTimerFrequencyVariable(){
        return timerFrequencyVariable;
    }

    public int getDistanceUpdateVariable(){
        return distanceUpdateVariable;
    }

}
