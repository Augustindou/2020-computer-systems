package utils;

import java.io.Serializable;
import java.util.Date;
public class Request implements Serializable {

    private String requestValue;
    private String responseValue;

    private Date startingToTreatRequestTime;
    private Date startingQueuingTime;
    private Date finishedQueuingTime;
    private Date finishedTreatingRequestTime;
    private Date sentByClientTime;
    private Date receivedByClientTime;


    private long processingTime;
    private long queuingTime;
    private long serviceTime;

    public Request(String requestValue) {
        this.requestValue = requestValue;
    }

    public String getRequestValue() {
        return requestValue;
    }

    public void setRequestValue(String requestValue) {
        this.requestValue = requestValue;
    }

    public String getResponseValue() {
        return responseValue;
    }

    public void setStartingQueuingTime(Date startingQueuingTime) {
        this.startingQueuingTime = startingQueuingTime;
    }


    public void setFinishedQueuingTime(Date finishedQueuingTime) {
        this.finishedQueuingTime = finishedQueuingTime;
    }

    public void setStartingToTreatRequestTime(Date startingToTreatRequestTime) {
        this.startingToTreatRequestTime = startingToTreatRequestTime;
    }

    public void setFinishedTreatingRequestTime(Date finishedTreatingRequestTime) {
        this.finishedTreatingRequestTime = finishedTreatingRequestTime;
    }

    public void setSentByClientTime(Date sentByClientTime) {
        this.sentByClientTime = sentByClientTime;
    }

    public Date getReceivedByClientTime() {
        return receivedByClientTime;
    }

    public void setReceivedByClientTime(Date receivedByClientTime) {
        this.receivedByClientTime = receivedByClientTime;
    }


    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }

    public long computeQueuingTime() {
        this.queuingTime = this.finishedQueuingTime.getTime() - this.startingQueuingTime.getTime();
        return this.queuingTime;
    }

    public long computeServiceTime() {
        this.serviceTime = this.finishedTreatingRequestTime.getTime() - this.startingToTreatRequestTime.getTime();
        return this.serviceTime;
    }

    public Date getSentByClientTime() {
        return sentByClientTime;
    }


}
