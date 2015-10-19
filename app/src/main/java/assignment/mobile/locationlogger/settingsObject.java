package assignment.mobile.locationlogger;

/**
 * Object used to hold settings
 * @author Joshua Coyle.
 * @author Robert Slavik
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

    /**
     *
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     *
     * @return
     */
    public String getObservation(){
        return observation;
    }

    /**
     *
     * @return
     */
    public int getTimerFrequencyVariable(){
        return timerFrequencyVariable;
    }

    /**
     *
     * @return
     */
    public int getDistanceUpdateVariable(){
        return distanceUpdateVariable;
    }

    /**
     *
     * @return
     */
    public String toString(){
        return "Name: " + name + " Observation: " + observation + " Timer: " +
                timerFrequencyVariable + " Distance: " + distanceUpdateVariable;
    }

}
