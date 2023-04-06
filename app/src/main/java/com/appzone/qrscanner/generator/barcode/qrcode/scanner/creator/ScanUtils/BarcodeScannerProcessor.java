package com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.Utils.BackgroundMusic;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

    private static final String TAG = "BarcodeProcessor";

    private final BarcodeScanner barcodeScanner;
    //    Activity activity;
    Context context;
    BackgroundMusic backgroundMusic;
    Activity activity;



    public BarcodeScannerProcessor(Context context, Activity activity, BackgroundMusic backgroundMusic) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.backgroundMusic = backgroundMusic;
//        this.activity = activity;
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        // new BarcodeScannerOptions.Builder()
        //     .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        //     .build();
        barcodeScanner = BarcodeScanning.getClient();
//        loadInterFirst();

    }



    @Override
    public void stop() {
        super.stop();
        barcodeScanner.close();
    }

    @Override
    protected Task<List<Barcode>> detectInImage(InputImage image) {
        return barcodeScanner.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay, boolean scan) {
        Log.e("1212212121", "onSuccess: " +barcodes.size());
        if (barcodes.isEmpty()) {
            if (scan){
                Toast.makeText(activity, "No QR/Barcode detected", Toast.LENGTH_SHORT).show();
            }
        } else if (barcodes.size() == 1) {
            Barcode barcode = barcodes.get(0);
            graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
            logExtrasForTesting(barcode);

            backgroundMusic.PlayVibrate500(activity, 1000);
            backgroundMusic.Playsound(activity, "beep.mp3");



        }
//        for (int i = 0; i < barcodes.size(); ++i) {
//            Barcode barcode = barcodes.get(i);
//            graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
//            logExtrasForTesting(barcode);
//        }
    }

    private void logExtrasForTesting(Barcode barcode) {
        if (barcode != null) {
            Log.v(
                    MANUAL_TESTING_LOG,
                    String.format(
                            "Detected barcode's bounding box: %s", barcode.getBoundingBox().flattenToString()));
            Log.v(
                    MANUAL_TESTING_LOG,
                    String.format(
                            "Expected corner point size is 4, get %d", barcode.getCornerPoints().length));
            for (Point point : barcode.getCornerPoints()) {
                Log.v(
                        MANUAL_TESTING_LOG,
                        String.format("Corner point is located at: x = %d, y = %d", point.x, point.y));
            }
            Log.v(MANUAL_TESTING_LOG, "barcode display value: " + barcode.getDisplayValue());
            Log.v(MANUAL_TESTING_LOG, "barcode raw value: " + barcode.getRawValue());
            Barcode.DriverLicense dl = barcode.getDriverLicense();
            Barcode.Sms sms = barcode.getSms();
            Barcode.Email email = barcode.getEmail();
            Barcode.GeoPoint geoPoint = barcode.getGeoPoint();
            Barcode.Phone phone = barcode.getPhone();
            Barcode.WiFi wifi = barcode.getWifi();
            Barcode.UrlBookmark url = barcode.getUrl();
            Barcode.ContactInfo contactInfo = barcode.getContactInfo();
            Barcode.CalendarEvent calendarEvent = barcode.getCalendarEvent();
            String string = barcode.getDisplayValue();


            String data = "";
            String type = "Text";
            String format = "";

            if (barcode.getFormat() == Barcode.FORMAT_QR_CODE) {
                format = "QR Code";
            } else {
                format = "BarCode";
            }


            if (dl != null) {
                type = "dl";
                data = "driver license city: " + dl.getAddressCity() + "\n" +
                        "driver license state: " + dl.getAddressState() + "\n" +
                        "driver license street: " + dl.getAddressStreet() + "\n" +
                        "driver license zip code: " + dl.getAddressZip() + "\n" +
                        "driver license birthday: " + dl.getBirthDate() + "\n" +
                        "driver license document type: " + dl.getDocumentType() + "\n" +
                        "driver license first name: " + dl.getFirstName() + "\n" +
                        "driver license middle name: " + dl.getMiddleName() + "\n" +
                        "driver license last name: " + dl.getLastName() + "\n" +
                        "driver license gender: " + dl.getGender() + "\n" +
                        "driver license issue date: " + dl.getIssueDate() + "\n" +
                        "driver license issue country: " + dl.getIssuingCountry() + "\n" +
                        "driver license number: " + dl.getLicenseNumber();
            }
            if (sms != null) {
                type = "SMS";
                data = "Message: " + sms.getMessage() + "\n" +
                        "Phone: " + sms.getPhoneNumber();

            }
            if (email != null) {
                type = "Email";
                data = "Address: " + email.getAddress() + "\n" +
                        "Subject: " + email.getSubject() + "\n" +
                        "Body: " + email.getBody();
            }
            if (geoPoint != null) {
                type = "Geo point";
                data = "Lat: " + geoPoint.getLat() + "\n" +
                        "Lng: " + geoPoint.getLng();
            }
            if (contactInfo != null) {
                type = "Contact";

                String name = "";
                if (contactInfo.getName() != null) {

                    name = contactInfo.getName().getFormattedName();
                }

                String address = "";
                if (!contactInfo.getAddresses().isEmpty()) {

                    for (int i = 0; i < contactInfo.getAddresses().size(); i++) {
                        address = contactInfo.getAddresses().get(0).getAddressLines() + "\n";
                    }
                }

                String email_contact = "";
                if (!contactInfo.getEmails().isEmpty()) {

                    for (int i = 0; i < contactInfo.getEmails().size(); i++) {
                        email_contact = contactInfo.getEmails().get(i).getAddress() + "\n";
                    }
                }

                String phone_contact = "";
                if (!contactInfo.getPhones().isEmpty()) {

                    for (int i = 0; i < contactInfo.getPhones().size(); i++) {
                        phone_contact = contactInfo.getPhones().get(i).getNumber() + "\n";
                    }
                }

                String url_contact = "";
                if (!contactInfo.getUrls().isEmpty()) {

                    for (int i = 0; i < contactInfo.getUrls().size(); i++) {
                        url_contact = contactInfo.getUrls().get(i) + "\n";
                    }
                }

                data = contactInfo.getTitle() + "\n" +
                        name + "\n" +
                        address + "\n" +
                        contactInfo.getOrganization() + "\n" +
                        email_contact + "\n" +
                        phone_contact + "\n" +
                        url_contact;
            }
            if (phone != null) {
                type = "Phone";
                data = "Phone: " + phone.getNumber();
            }
            if (url != null) {
                type = "Web URL";
                data = "URL: " + url.getUrl();
            }
            if (wifi != null) {
                type = "Wifi";
                String wifi_type = "";
                if (wifi.getEncryptionType() == 2) {
                    wifi_type = "WPA";
                } else if (wifi.getEncryptionType() == 3) {
                    wifi_type = "WAP";
                } else {
                    wifi_type = "Open";
                }

                data = "Name: " + wifi.getSsid() + "\n" +
                        "Password: " + wifi.getPassword() + "\n" +
                        "Network type: " + wifi_type;
            }
            if (data.equalsIgnoreCase("")) {

                if (string != null) {
                    type = "Text";
                    data = string;
                }
            }
            if (calendarEvent!=null){
                type = "Calender Event";

                Calendar beginCal = Calendar.getInstance();
                beginCal.set(barcode.getCalendarEvent().getStart().getYear(), barcode.getCalendarEvent().getStart().getMonth(), barcode.getCalendarEvent().getStart().getDay(), barcode.getCalendarEvent().getStart().getHours(), barcode.getCalendarEvent().getStart().getMinutes());
                Date date = new Date();
                date.setTime(beginCal.getTimeInMillis());
                String formattedDate=new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(date);

                Calendar endCal = Calendar.getInstance();
                endCal.set(barcode.getCalendarEvent().getEnd().getYear(), barcode.getCalendarEvent().getEnd().getMonth(), barcode.getCalendarEvent().getEnd().getDay(), barcode.getCalendarEvent().getEnd().getHours(), barcode.getCalendarEvent().getEnd().getMinutes());
                Date date_end = new Date();
                date_end.setTime(endCal.getTimeInMillis());
                String formattedDate_end=new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(date_end);



                data = "Description: " + calendarEvent.getDescription() + "\n" +
//                        "Start: " + calendarEvent.getStart().getMonth()+" "+calendarEvent.getStart().getDay()+", "+calendarEvent.getStart().getYear()+" "+calendarEvent.getStart().getHours()+":"+calendarEvent.getStart().getMinutes() + "\n" +
                        "Start: " + formattedDate + "\n" +
                        "End: " + formattedDate_end + "\n" +
                        "Summery: " + calendarEvent.getSummary() + "\n" +
                        "Location: " + calendarEvent.getLocation();
            }


            Paper.init(context);
//            Toast.makeText(context, type, Toast.LENGTH_SHORT).show();
            Paper.book().write("type", type);
            Paper.book().write("data", data);
            Paper.book().write("barcode", barcode);
            Paper.book().write("format", format);


//            scan_counter=scan_counter+1;
            Intent intent = new Intent(context, QRResultView.class);
//            context.startActivity(intent);

            context.startActivity(intent);


        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}


