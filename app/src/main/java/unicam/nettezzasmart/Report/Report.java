package unicam.nettezzasmart.Report;

import android.graphics.Bitmap;

public class Report {
    private int code;
    private int status;
    private String when;
    private String lat;
    private String lon;
    private String trashType;
    private String comment;
    private Bitmap photoFile;

    public Report(int code, String when, String lat, String lon, String trashType, String comment, Bitmap photoFile) {
        this.when=when;
        this.lat=lat;
        this.lon=lon;
        this.code=code;
        this.status=0;
        this.trashType=trashType;
        this.comment=comment;
        this.photoFile=photoFile;
        ReportCollection.report_list.add(this);
    }

    public int getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

    public String getLat() { return lat; }

    public String getLon() { return lon; }

    public String getWhen() { return when; }

    public String getTrashType() {
        return trashType;
    }

    public String getComment() {
        return comment;
    }

    public Bitmap getPhotoFile() {return photoFile; }

    public void setCode(int code) { this.code = code; }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setWhen(String when) { this.when = when; }

    public void setLat(String lat) { this.lat = lat; }

    public void setLon(String lon) { this.lon = lon; }

    public void setTrashType(String trashType) {
        this.trashType = trashType;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setPhotoFile(Bitmap photoFile) { this.photoFile = photoFile; }

}
