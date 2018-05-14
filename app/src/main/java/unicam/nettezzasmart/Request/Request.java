package unicam.nettezzasmart.Request;

public class Request {
        private int code;
        private int status;
        private String when;
        private String where;

     public Request(int code, String when, String where) {
            this.when=when;
            this.where=where;
            this.code=code;
            this.status=0;
            RequestCollection.request_list.add(this);
        }
     public int getCode() {
         return code;
     }

     public int getStatus() {
         return status;
     }

     public String getWhen() {
         return when;
     }

     public String getWhere() {
         return where;
     }

     public void setCode(int code) {
         this.code = code;
     }

     public void setStatus(int status) {
         this.status = status;
     }

     public void setWhen(String when) {
         this.when = when;
     }

     public void setWhere(String where) {
         this.where = where;
     }

    }
