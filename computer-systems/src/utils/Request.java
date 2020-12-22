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

    private int clientID;

    private long processingTime;
    private long queuingTime;
    private long serviceTime;

    public Request(String requestValue, int clientID) {
        this.requestValue = requestValue;
        this.clientID = clientID;
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

    public int getClientID() {
        return clientID;
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

    public void computeIntervals() {
        this.serviceTime = this.receivedByClientTime.getTime() - this.sentByClientTime.getTime();
        this.queuingTime = this.finishedQueuingTime.getTime() - this.startingQueuingTime.getTime();
        this.processingTime = this.finishedTreatingRequestTime.getTime() - this.startingToTreatRequestTime.getTime();
    }

    public String createTimeString() {
        return serviceTime + " " + queuingTime + " " + processingTime + "\n";
    }

    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }

    public Date getStartingToTreatRequestTime() {
        return startingToTreatRequestTime;
    }

    public Date getStartingQueuingTime() {
        return startingQueuingTime;
    }

    public Date getFinishedQueuingTime() {
        return finishedQueuingTime;
    }

    public Date getFinishedTreatingRequestTime() {
        return finishedTreatingRequestTime;
    }

    public Date getSentByClientTime() {
        return sentByClientTime;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    public long getQueuingTime() {
        return queuingTime;
    }

    public void setQueuingTime(long queuingTime) {
        this.queuingTime = queuingTime;
    }

    public long getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(long serviceTime) {
        this.serviceTime = serviceTime;
    }
}
