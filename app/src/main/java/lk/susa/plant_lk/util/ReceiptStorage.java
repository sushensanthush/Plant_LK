package lk.susa.plant_lk.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import lk.susa.plant_lk.model.Order;
import lk.susa.plant_lk.model.Product;

public class ReceiptStorage {

    private static final String TAG = "ReceiptStorage";
    private static final String PUBLIC_FOLDER = "Plant.lk Receipts";
    private static final int W = 40; // receipt width in characters

    private static String fileName(String orderId) {
        return "receipt_" + orderId + ".txt";
    }


    public static String buildReceipt(Order order, Map<String, Product> products) {
        StringBuilder sb = new StringBuilder();
        String line = repeat('-', W);
        String bold = repeat('=', W);

        Date date = (order.getOrderDate() != null) ? order.getOrderDate().toDate() : new Date();
        String when = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(date);

        sb.append(bold).append('\n');
        sb.append(center("PLANT.LK")).append('\n');
        sb.append(center("Sri Lanka's Plant Marketplace")).append('\n');
        sb.append(bold).append('\n');
        sb.append(center("ORDER RECEIPT")).append("\n\n");
        sb.append(field("Order No", order.getOrderId())).append('\n');
        sb.append(field("Date", when)).append('\n');
        sb.append(field("Status", order.getStatus() != null ? order.getStatus() : "PAID")).append('\n');
        sb.append(line).append('\n');
        sb.append(padRight("ITEM", 22)).append(padLeft("QTY", 4)).append(padLeft("AMOUNT", 14)).append('\n');
        sb.append(line).append('\n');

        double subtotal = 0;
        if (order.getOrderItems() != null) {
            for (Order.OrderItem item : order.getOrderItems()) {
                Product p = products.get(item.getProductId());
                String title = (p != null && p.getTitle() != null) ? p.getTitle() : item.getProductId();
                double amount = item.getUnitPrice() * item.getQuantity();
                subtotal += amount;
                sb.append(padRight(trim(title, 21), 22))
                        .append(padLeft(String.valueOf(item.getQuantity()), 4))
                        .append(padLeft(money(amount), 14)).append('\n');
                sb.append("  @ LKR ").append(money(item.getUnitPrice())).append(" each\n");
            }
        }

        double shipping = order.getTotalAmount() - subtotal;
        sb.append(line).append('\n');
        sb.append(row("Subtotal", "LKR " + money(subtotal))).append('\n');
        if (shipping > 0.005) {
            sb.append(row("Shipping", "LKR " + money(shipping))).append('\n');
        }
        sb.append(bold).append('\n');
        sb.append(row("TOTAL", "LKR " + money(order.getTotalAmount()))).append('\n');
        sb.append(bold).append('\n');

        Order.Address a = order.getShippingAddress();
        if (a != null) {
            sb.append("\nSHIP TO\n");
            sb.append(line).append('\n');
            appendIfPresent(sb, a.getName());
            appendIfPresent(sb, a.getAddress1());
            appendIfPresent(sb, a.getAddress2());
            String cityLine = (nz(a.getCity()) + " " + nz(a.getPostcode())).trim();
            appendIfPresent(sb, cityLine);
            if (!nz(a.getContact()).isEmpty()) sb.append("Tel: ").append(a.getContact()).append('\n');
            if (!nz(a.getEmail()).isEmpty()) sb.append(a.getEmail()).append('\n');
        }

        sb.append('\n').append(line).append('\n');
        sb.append(center("Thank you for shopping with us!")).append('\n');
        sb.append(center("Grow something beautiful.")).append('\n');
        sb.append(center("www.plant.lk")).append('\n');
        sb.append(bold).append('\n');
        return sb.toString();
    }




    public static String buildSmsMessage(Order order) {
        int items = 0;
        if (order.getOrderItems() != null) {
            for (Order.OrderItem i : order.getOrderItems()) items += i.getQuantity();
        }
        String city = order.getShippingAddress() != null ? nz(order.getShippingAddress().getCity()) : "";
        return "Plant.lk: Order #" + order.getOrderId() + " confirmed.\n"
                + "Items: " + items + " | Total: LKR " + money(order.getTotalAmount()) + "\n"
                + (city.isEmpty() ? "" : "Delivering to: " + city + "\n")
                + "Thank you for shopping with Plant.lk!";
    }




    public static boolean save(Context context, String orderId, String content) {
        try (FileOutputStream fos = context.openFileOutput(fileName(orderId), Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            Log.d(TAG, "Saved internal: " + getPath(context, orderId));
        } catch (IOException e) {
            Log.e(TAG, "Internal save failed", e);
            return false;
        }
        exportToDocuments(context, orderId, content);
        return true;
    }



    private static void exportToDocuments(Context context, String orderId, String content) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName(orderId));
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.Files.FileColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/" + PUBLIC_FOLDER);
            Uri uri = context.getContentResolver()
                    .insert(MediaStore.Files.getContentUri("external"), values);
            if (uri == null) return;
            try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                if (os != null) os.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Log.e(TAG, "Documents export failed", e);
        }
    }

    public static String getPath(Context context, String orderId) {
        return new File(context.getFilesDir(), fileName(orderId)).getAbsolutePath();
    }

    public static String read(Context context, String orderId) {
        try (FileInputStream fis = context.openFileInput(fileName(orderId));
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }



    private static String money(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    private static void appendIfPresent(StringBuilder sb, String s) {
        if (!nz(s).isEmpty()) sb.append(nz(s)).append('\n');
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private static String padRight(String s, int n) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }

    private static String padLeft(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < n; i++) sb.append(' ');
        return sb.append(s).toString();
    }

    private static String center(String s) {
        int pad = Math.max(0, (W - s.length()) / 2);
        return repeat(' ', pad) + s;
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }

    private static String field(String label, String value) {
        return padRight(label, 9) + ": " + nz(value);
    }

    private static String row(String left, String right) {
        int gap = Math.max(1, W - left.length() - right.length());
        return left + repeat(' ', gap) + right;
    }
}