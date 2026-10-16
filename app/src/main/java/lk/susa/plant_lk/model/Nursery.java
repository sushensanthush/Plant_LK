//package lk.susa.plant_lk.model;
//
//public class Nursery {
//
//    private String name;
//    private String address;
//    private String hours;
//    private String phone;
//    private double latitude;
//    private double longitude;
//
//    private String docId;
//
//    public Nursery() {}
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getAddress() { return address; }
//    public void setAddress(String address) { this.address = address; }
//
//    public String getHours() { return hours; }
//    public void setHours(String hours) { this.hours = hours; }
//
//    public String getPhone() { return phone; }
//    public void setPhone(String phone) { this.phone = phone; }
//
//    public double getLatitude() { return latitude; }
//    public void setLatitude(double latitude) { this.latitude = latitude; }
//
//    public double getLongitude() { return longitude; }
//    public void setLongitude(double longitude) { this.longitude = longitude; }
//
//    // Firestore docId
//    public String getDocId() { return docId; }
//    public void setDocId(String docId) { this.docId = docId; }
//}

package lk.susa.plant_lk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nursery {

    private String name;
    private String address;
    private String hours;
    private String phone;
    private double latitude;
    private double longitude;

    // Firestore document ID
    private String docId;
}